package net.randosrs.miner;

import static net.runelite.api.util.Numbers.*;
import static net.runelite.client.externalplugins.ExternalPluginManager.pluginManager;
import static net.unethicalite.api.commons.Time.sleepUntil;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.plugins.SubscribedPlugin;
import net.unethicalite.api.script.blocking_events.WelcomeScreenEvent;
import net.unethicalite.api.utils.MessageUtils;
import com.google.inject.Provides;
import net.runelite.api.util.Text;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.WildcardMatcher;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.LoopedPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import net.unethicalite.api.movement.pathfinder.model.MiningLocation;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;

import static net.unethicalite.api.commons.Time.sleep;

@PluginDescriptor(
        name = "-randOsrs Miner",
        description = "Sweet mining script that will switch pickaxes and ores/mining areas as you level.",
        enabledByDefault = false
)
@Slf4j
@Extension
public class randOsrsMinerPlugin extends LoopedPlugin
{
    private static final Pattern WORLD_POINT_PATTERN = Pattern.compile("^\\d{4,5} \\d{4,5} \\d$");

    private ScheduledExecutorService executor;

    @Inject
    private randOsrsMinerConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private randOsrsMinerOverlay minerOverlay;

    private boolean isWalkingBack = false;
    private int cycles = 0;
    private int switches = 0;
    private boolean loggedOut = false;
    private boolean loggingIn = false;
    private int skips = 0;
    private int getNewCycle() { return 25 * getRandomNumber(180, 300); }//return getRandomNumber(2, 5); } //
    private int switchAtCycle = getNewCycle();

    @Override
    public void startUp() throws Exception
    {
        super.startUp();
        overlayManager.add(minerOverlay);
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(() ->
        {
            try
            {
                if (!Game.isPlaying())
                {
                    return;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

    }

    @Provides
    public randOsrsMinerConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(randOsrsMinerConfig.class);
    }

    @Override
    public void shutDown()
    {
        overlayManager.remove(minerOverlay);
        if (executor != null)
        {
            executor.shutdown();
        }
    }

    public void setMiningLocation(MiningLocation miningLocation) {
        Static.getConfigManager().setConfiguration("randosrs-miner", "mineLocations", miningLocation);
    }

    @Override
    protected int loop()
    {
        if(!Plugins.isEnabled(this)) {
            System.out.println("Stopping plugin.");
            SwingUtilities.invokeLater(() -> Plugins.stopPlugin(this));
            return -4;
        }
        if(Game.isOnBreak()) {
            sleepRandTick(5);
            return -4;
        }

        MiningLocation miningArea = config.mineLocation();
        /*cycles++;
        if(cycles >= switchAtCycle) {
            switches++;
            switchAtCycle = getNewCycle();
            cycles = 0;
            SubscribedPlugin autoLogin = null;
            for (Plugin plug : pluginManager.getPlugins()) {
                if (plug.getName().contains("Auto Login")) {
                    autoLogin = (SubscribedPlugin) plug;
                    break;
                }
            }
            if (autoLogin != null) {
                boolean takeBreak = Boolean.parseBoolean(Static.getConfigManager().getConfiguration("randosrs-autologin", "takeBreaks"));
                if (takeBreak) {
                    System.out.println("Checking for break.");
                    if (Game.isOnBreak()) {
                        System.out.println("Logging back in..");
                        Game.setOnBreak(false);
                        Static.getEventBus().register(autoLogin);
                        autoLogin.refresh();
                        sleepUntil(Game::isPlaying, 30000);
                    } else if (switches == 2) {
                        System.out.println("Logging out..");
                        Game.setOnBreak(true);
                        Game.logout();
                        switches = 0;
                        return -4;
                    }
                }
            }

            MessageUtils.warning("Switching tasks.");

            if(miningArea == MiningLocation.VARROCK_EAST_MINE) {
                Static.getConfigManager().setConfiguration("randosrs-miner", "mineLocations", MiningLocation.VARROCK_WEST_MINE);
            }
            else if(miningArea == MiningLocation.VARROCK_WEST_MINE) {
                Static.getConfigManager().setConfiguration("randosrs-miner", "mineLocations", MiningLocation.VARROCK_EAST_MINE);
            }

            sleepRandTick(3);
            return -4;
        }*/

        if (!Game.isPlaying()) {
            MessageUtils.warning("Currently logged out.");
            sleepRandom(2000, 2500);
            return -4;
        }

        Actor interacting = Static.getClient().getLocalPlayer().getInteracting();
        if(interacting != null) {
            String name = interacting.getName();
            if (name != null && !name.isBlank()) {
                if (name.endsWith("rocks")) {
                    MessageUtils.warning("Already mining " + name + "..");
                } else {
                    MessageUtils.warning("Already interacting with " + name + "..");
                }
            } else {
                MessageUtils.warning("Already interacting..");
            }
            sleepRand();
            return -4;
        }

        if (config.eat())
        {
            if(Combat.getHealthPercent() <= config.healthPercent()) {
                MessageUtils.success("Eating food.");
                return eatFood();
            }
        }

        if (!Static.getClient().getLocalPlayer().isIdle() && !goingToBank && !isWalkingBack)
        {

            MessageUtils.warning("Already performing an action.");
            sleepRand();
            return -4;
        }

        if (Inventory.isFull() || !hasUseablePickaxe())
        {
            if(!goingToBank) {
                MessageUtils.success("Going to bank.");
            }
            return gotoOpenBank();
        }
        Players.getAll();
        List<String> rocks = Text.fromCSV(config.oreTypes());
        TileObject rock = TileObjects.getNearest(x -> x.getName() != null
                        && textToRocksMatches(rocks, x.getName())
                        && x.getWorldLocation().isInArea(miningArea.getArea()));

        if (rock == null) {
            if (Static.getClient().getLocalPlayer().getWorldLocation().isInArea(miningArea.getArea())) {
                TileObject emptyRock = TileObjects.getFirstSurrounding(Static.getClient().getLocalPlayer().getWorldLocation(),
                        2,
                        x -> x != null && x.getName().equals("Rocks"));
                if (emptyRock != null) {
                    MessageUtils.error("No rocks to mine in the area.");
                }
            } else {
                if (!isWalkingBack) {
                    MessageUtils.success("Walking back to area.");
                }
                isWalkingBack = true;
                Movement.walkTo(miningArea.getArea().getPointNearCenter());
            }
            sleepRand();
            return -4;
        }

        else
        {
            int distance = rock.getWorldLocation().distanceTo(Static.getClient().getLocalPlayer());
            if (distance > 10 || !Reachable.isInteractable(rock))
            {
                if(!isWalkingBack) {
                    MessageUtils.success("Walking back to area.");
                }
                isWalkingBack = true;
                Movement.walkTo(miningArea.getArea().getPointNearCenter());
                sleepRand();
                return -4;
            }
            if(skips < 16 && !rocks.contains("Coal") && !rocks.contains("Mithril") && !rocks.contains("Adamantite") && !rocks.contains("Runite")) {
                List<TileObject> emptyRocks = TileObjects.getSurrounding(
                        Static.getClient().getLocalPlayer().getWorldLocation(),
                        1,
                        x -> x != null && x.getName() != null
                                && x.getName().equals("Rocks")
                                && !isDullRock(x)
                );
                if (emptyRocks.size() > 1) {
                    if (distance > 2 || getDiagnals().contains(rock.getWorldLocation())) {
                        if(skips == 0) {
                            MessageUtils.warning("Waiting for rocks to respawn..");
                        }
                        skips++;
                        sleepRandom(100, 125);
                        return -4;
                    }
                }
            }
            skips = 0;
            isWalkingBack = false;
            MessageUtils.success("Mining " + rock.getName() + ".");
            rock.interact("Mine");
            sleepRandom(250, 450);
        }
        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > getRandomNumber(5,55))
        {
            Movement.toggleRun();
        }

        sleepRand();
        return -3;
    }

    private List<WorldPoint> getDiagnals() {
        WorldPoint playerLoc = Static.getClient().getLocalPlayer().getWorldLocation();
        return new ArrayList<>() {
            {
                add(new WorldPoint(playerLoc.getX() - 1, playerLoc.getY() - 1, playerLoc.getPlane()));
                add(new WorldPoint(playerLoc.getX() + 1, playerLoc.getY() - 1, playerLoc.getPlane()));
                add(new WorldPoint(playerLoc.getX() - 1, playerLoc.getY() + 1, playerLoc.getPlane()));
                add(new WorldPoint(playerLoc.getX() + 1, playerLoc.getY() + 1, playerLoc.getPlane()));
            }
        };
    }

    private boolean isDullRock(TileObject rock) {
        List<WorldPoint> dullRocks = new ArrayList<>();
        dullRocks.add(new WorldPoint(3182, 3373, 0));
        dullRocks.add(new WorldPoint(3181, 3370, 0));

        return dullRocks.contains(rock.getWorldLocation());
    }

    private boolean textToRocksMatches(List<String> itemNames, String itemName)
    {
        return itemNames.stream().anyMatch(name -> WildcardMatcher.matches(name + " rocks", itemName));
    }

    private int getBestAvailablePickaxeInventory() {
        return ((Static.getClient().getRealSkillLevel(Skill.MINING) >= 41 && Inventory.contains(ItemID.GILDED_PICKAXE)) ? ItemID.GILDED_PICKAXE :
                (Static.getClient().getRealSkillLevel(Skill.MINING) >= 41 && Inventory.contains(ItemID.RUNE_PICKAXE)) ? ItemID.RUNE_PICKAXE :
                        (Static.getClient().getRealSkillLevel(Skill.MINING) >= 31 && Inventory.contains(ItemID.ADAMANT_PICKAXE)) ? ItemID.ADAMANT_PICKAXE :
                                (Static.getClient().getRealSkillLevel(Skill.MINING) >= 21 && Inventory.contains(ItemID.MITHRIL_PICKAXE)) ? ItemID.MITHRIL_PICKAXE :
                                        (Static.getClient().getRealSkillLevel(Skill.MINING) >= 11 && Inventory.contains(ItemID.BLACK_PICKAXE)) ? ItemID.BLACK_PICKAXE :
                                                (Static.getClient().getRealSkillLevel(Skill.MINING) >= 6 && Inventory.contains(ItemID.STEEL_PICKAXE)) ? ItemID.STEEL_PICKAXE :
                                                        Inventory.contains(ItemID.IRON_PICKAXE) ? ItemID.IRON_PICKAXE :
                                                                Inventory.contains(ItemID.BRONZE_PICKAXE) ? ItemID.BRONZE_PICKAXE : -1);
    }
    private int getBestAvailablePickaxeBank() {
        return (Static.getClient().getRealSkillLevel(Skill.MINING) >= 41 && Bank.contains(ItemID.GILDED_PICKAXE)) ? ItemID.GILDED_PICKAXE :
                (Static.getClient().getRealSkillLevel(Skill.MINING) >= 41 && Bank.contains(ItemID.RUNE_PICKAXE)) ? ItemID.RUNE_PICKAXE :
                        (Static.getClient().getRealSkillLevel(Skill.MINING) >= 31 && Bank.contains(ItemID.ADAMANT_PICKAXE)) ? ItemID.ADAMANT_PICKAXE :
                                (Static.getClient().getRealSkillLevel(Skill.MINING) >= 21 && Bank.contains(ItemID.MITHRIL_PICKAXE)) ? ItemID.MITHRIL_PICKAXE :
                                        (Static.getClient().getRealSkillLevel(Skill.MINING) >= 11 && Bank.contains(ItemID.BLACK_PICKAXE)) ? ItemID.BLACK_PICKAXE :
                                                (Static.getClient().getRealSkillLevel(Skill.MINING) >= 6 && Bank.contains(ItemID.STEEL_PICKAXE)) ? ItemID.STEEL_PICKAXE :
                                                        Bank.contains(ItemID.IRON_PICKAXE) ? ItemID.IRON_PICKAXE :
                                                                Bank.contains(ItemID.BRONZE_PICKAXE) ? ItemID.BRONZE_PICKAXE :
                                                                        -1;
    }
    private boolean hasUseablePick = false;
    private boolean checkingPick = false;
    private boolean hasUseablePickaxe() {
        hasUseablePick = false;
        checkingPick = false;
        Static.getClientThread().invoke(() -> {
            Item weapon = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
            if (weapon != null) {
                weapon.getName().endsWith("pickaxe");
            }
            hasUseablePick = getBestAvailablePickaxeInventory() != -1 || (weapon != null && weapon.getName().endsWith("pickaxe"));
            checkingPick = true;
        });
        while(!checkingPick) {
            sleep(50);
        }
        return hasUseablePick;
    }
    private int getBestAvailablePickaxe() {
        int invPick = getBestAvailablePickaxeInventory();
        int bankPick = getBestAvailablePickaxeBank();
        int handPick = -1;
        Item weapon = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
        if(weapon != null) {
            handPick = weapon.getId();
        }
        return (invPick == ItemID.GILDED_PICKAXE || bankPick == ItemID.GILDED_PICKAXE|| handPick == ItemID.GILDED_PICKAXE) ? ItemID.GILDED_PICKAXE :
                (invPick == ItemID.RUNE_PICKAXE || bankPick == ItemID.RUNE_PICKAXE || handPick == ItemID.RUNE_PICKAXE) ? ItemID.RUNE_PICKAXE :
                        (invPick == ItemID.ADAMANT_PICKAXE || bankPick == ItemID.ADAMANT_PICKAXE || handPick == ItemID.ADAMANT_PICKAXE) ? ItemID.ADAMANT_PICKAXE :
                                (invPick == ItemID.MITHRIL_PICKAXE || bankPick == ItemID.MITHRIL_PICKAXE || handPick == ItemID.MITHRIL_PICKAXE) ? ItemID.MITHRIL_PICKAXE :
                                        (invPick == ItemID.BLACK_PICKAXE || bankPick == ItemID.BLACK_PICKAXE || handPick == ItemID.BLACK_PICKAXE) ? ItemID.BLACK_PICKAXE :
                                                (invPick == ItemID.STEEL_PICKAXE || bankPick == ItemID.STEEL_PICKAXE || handPick == ItemID.STEEL_PICKAXE) ? ItemID.STEEL_PICKAXE :
                                                        (invPick == ItemID.IRON_PICKAXE || bankPick == ItemID.IRON_PICKAXE || handPick == ItemID.IRON_PICKAXE) ? ItemID.IRON_PICKAXE :
                                                                (invPick == ItemID.BRONZE_PICKAXE || bankPick == ItemID.BRONZE_PICKAXE || handPick == ItemID.BRONZE_PICKAXE) ? ItemID.BRONZE_PICKAXE :
                                                                        -1;
    }
    private boolean goingToBank = false;
    private boolean bankingPickaxes = false;
    private boolean bankingItems = false;
    private int gotoOpenBank() {
        if (Bank.isOpen() && !bankingPickaxes && !bankingItems) {
            goingToBank = false;
            bankingItems = true;
            bankItems();
        } else {
            List<TileObject> banks = TileObjects.getSurrounding(Static.getClient().getLocalPlayer().getWorldLocation(), 7,
                    obj -> obj != null && obj.getName() != null
                            && (obj.hasAction("Bank") || obj.getName().startsWith("Bank chest") || obj.hasAction("Collect")));
            if (banks.size() > 0) {
                int rand = getRandomNumber(0, banks.size());
                sleepRandTick();
                if(!Bank.isOpen()) {
                    var banker = banks.get(rand);
                    banks.get(rand).interact(banker.hasAction("Bank") ? "Bank" : "Use");
                    sleepUntil(Bank::isOpen, 3000);
                    return -1;
                }
            } else {
                goingToBank = true;
                BankLocation bankLoc = BankLocation.getNearest();
                if(bankLoc != null) {
                    Movement.walkTo(bankLoc.getArea().getPointNearCenter());
                }
            }
        }
        return -4;
    }

    private int bankItems() {
        if(Bank.isOpen()) {
            sleepRandTick();
            while(Inventory.getFirst(item -> item != null && !item.getName().endsWith("pickaxe")) != null) {
                Bank.depositAll(item -> item != null && !item.getName().endsWith("pickaxe")); // Deposit all items except for pickaxes
                sleepRandTick(1);
            }
            withdrawFood();
            MessageUtils.success("Closing bank.");
            Bank.close();
            sleepRandTick();
            bankingItems = false;
            eatFood();
            return switchPickaxe();
        }
        return -4;
    }

    private int foodAmountInventory() {
        List<String> foods = Text.fromCSV(config.foods());
        return Inventory.getCount(x -> (x.getName() != null && foods.stream().anyMatch(a -> x.getName().contains(a)))
                || (foods.contains("Any") && x.hasAction("Eat")));
    }
    private int foodAmountBank() {
        List<String> foods = Text.fromCSV(config.foods());
        return Bank.getCount(x -> (x.getName() != null && foods.stream().anyMatch(a -> x.getName().contains(a)))
                || (foods.contains("Any") && x.hasAction("Eat")));
    }

    private int eatFood() {
        if(!config.eat()){
            return -4;
        }
        List<String> foods = Text.fromCSV(config.foods());
        if (foodAmountInventory() > 0) {
            if(Combat.getHealthPercent() <= config.healthPercent()) {
                Item food = Inventory.getFirst(x -> (x.getName() != null && foods.stream().anyMatch(a -> x.getName().contains(a)))
                        || (foods.contains("Any") && x.hasAction("Eat")));
                if (food != null) {
                    food.interact("Eat");
                    sleepRandTick();
                }
            }
        } else {
            if (!goingToBank) {
                MessageUtils.addMessage("You have no more food left to eat.");
            }
            if (Bank.isOpen()) {
                withdrawFood();
                Bank.close();
                sleepRandTick();
                goingToBank = false;
            } else {
                return gotoOpenBank();
            }
        }
        sleepRand();
        return -4;
    }

    private void withdrawFood() {
        if(!config.eat()){
            return;
        }
        MessageUtils.warning("Withdrawing food..");
        List<String> foods = Text.fromCSV(config.foods());
        sleepRandTick();
        Item bankItem = Bank.getFirst(x -> (x.getName() != null && foods.stream().anyMatch(a -> x.getName().contains(a))
                || (foods.contains("Any") && x.hasAction("Eat"))));
        if (bankItem != null) {
            Bank.withdraw(bankItem.getId(), 5, Bank.WithdrawMode.ITEM);
            sleepRandTick();
        } else {
            MessageUtils.error("No food found in bank.");
        }
    }

    private int switchPickaxe() {
        if(bankingItems || bankingPickaxes) {
            return -1;
        }
        int bestID = getBestAvailablePickaxe();
        if(bestID == -1) {
            return -7; // Has no useable pickaxe
        }
        Item weapon = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
        if(weapon != null) {
            if(weapon.getId() == bestID) {
                return -1; // Player already has best pickaxe equipped
            } else if(weapon.getName().endsWith("pickaxe")) {
                weapon.interact("Remove"); // Unequipped old pickaxe.
                sleepRandTick(3);
            }
        }
        Item pick = Inventory.getFirst(bestID);
        if(pick != null) {
            pick.interact("Wield"); // Trying to equip best pickaxe already in inventory.
            sleepRandTick(3);
            return -1;
        }
        if(gotoOpenBank() == -1) {
            bankingPickaxes = true;
            sleepRandTick();
            if (Bank.isOpen()) {
                while(Inventory.getFirst(item -> item != null && item.getName().endsWith("pickaxe")) != null) {
                    Bank.depositAll(i -> i != null && i.getName().endsWith("pickaxe")); // Deposit old pickaxes
                    sleepRandTick();
                }
                Bank.withdraw(bestID, 1, Bank.WithdrawMode.ITEM); // Withdraw best pickaxe
                sleepRandTick();
                sleepUntil(() -> Inventory.getFirst(i -> i != null && i.getId() == bestID) != null, 3000);
                Bank.close();
                sleepRandTick();
                bankingPickaxes = false;
            }
            Item invPickaxe = Inventory.getFirst(bestID);
            if(invPickaxe != null) {
                invPickaxe.interact("Wield"); // Equipped best pickaxe
                sleepRandTick(3);
            }
        }
        return -1;
    }

}