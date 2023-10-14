package net.unethicalite.fighter;

import com.google.inject.Provides;
import com.openosrs.client.game.WorldLocation;
import net.runelite.api.*;
import net.runelite.api.util.Text;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.WildcardMatcher;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.plugins.SubscribedPlugin;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Prayers;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.runelite.api.util.Numbers.*;
import static net.unethicalite.api.commons.Time.sleep;
import static net.unethicalite.api.commons.Time.sleepUntil;

@PluginDescriptor(
		name = "-randOsrs Fighter",
		description = "A simple auto fighter",
		enabledByDefault = false
)
@Slf4j
@Extension
public class randOsrsFighterPlugin extends SubscribedPlugin
{
	private static final Pattern WORLD_POINT_PATTERN = Pattern.compile("^\\d{4,5} \\d{4,5} \\d$");

	private ScheduledExecutorService executor;

	@Inject
	private randOsrsFighterConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private randOsrsFighterOverlay randOsrsFighterOverlay;

	private final List<TileItem> notOurItems = new ArrayList<>();
    private boolean isTaskRunning = false;
    private int staminaLevel = getRandomNumber(5, 55);

	@Override
	public void startUp() throws Exception
	{
		super.startUp();
		overlayManager.add(randOsrsFighterOverlay);
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(() -> {
            if (!Game.isPlaying() || isTaskRunning) {
                return;
            }
            isTaskRunning = true;

            if (config.quickPrayer() && !Prayers.isQuickPrayerEnabled()) {
                Prayers.toggleQuickPrayer(true);
            }
            loop();
            isTaskRunning = false;
        }, 0, 100, TimeUnit.MILLISECONDS);

		if (Game.isPlaying() && getCenter() == null)
		{
			setCenter(Players.getLocal().getWorldLocation());
		}
	}

	@Provides
	public randOsrsFighterConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(randOsrsFighterConfig.class);
	}

	@Override
	public void shutDown()
	{
        System.out.println("Shutdown");
		overlayManager.remove(randOsrsFighterOverlay);
		if (executor != null)
		{
			executor.shutdown();
		}
	}

	protected boolean loop()
	{
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
		if (getCenter() == null) { setCenter(Static.getClient().getLocalPlayer().getWorldLocation()); }
        if (getCenter() == null) { return false; }

        if(eatFoodAction()) { return true; }
        if(drinkAntiPoisonAction()) { return true; }
        if(drinkAntiFireAction()) { return true; }
        if(disableWalkAction()) { return true; }

        checkRunning();
        if(isInteracting()) { return false; }

		if(prayerPotionAction()) { return true; }
        if(quickPrayerAction()) { return true; }
        if(buryBonesAction()) { return true; }
        if(isIdle()) { return false; }

        if(lootItemsAction()) { return true; }
        if(alchItemsAction()) { return true; }

        return attackMob();
    }

    private boolean attackMob() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if(config.switchStyles()) {
            String attackStyle = switchAttackStyle();
            if (attackStyle != null) {
                MessageUtils.success("Changed attack style to: " + attackStyle);
            }
        }
        NPC mob = Combat.getAttackableNPC(x -> x.getName() != null
                && textMatches(getMobs(), x.getName())
                && !x.isDead() && !x.isInteracting() && x.getCombatLevel() < Static.getClient().getLocalPlayer().getCombatLevel() * 0.6
                && x.getWorldLocation().distanceTo(getCenter()) < config.attackRange()
        );
        if (mob == null) {
            if (Static.getClient().getLocalPlayer().getWorldLocation().distanceTo(getCenter()) < 3) {
                MessageUtils.addMessage("No attackable monsters in area.");
                sleepRand();
                return false;
            }
            Movement.walkTo(getCenter());
            sleepRand();
            return true;
        }

        if (!Reachable.isInteractable(mob) && !config.disableWalk()) {
            Movement.walkTo(mob.getWorldLocation());
            sleepRand();
            return true;
        }

        mob.interact("Attack");
        sleepUntil(mob::isInteracting, getRandomNumber(6000, 9000));
        return true;
    }

    private void checkRunning() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return; }
        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > staminaLevel) { Movement.toggleRun(); staminaLevel = getRandomNumber(5, 55); }
    }

    private boolean isIdle() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if (!Static.getClient().getLocalPlayer().isIdle() && !Dialog.canContinue()) {
            sleepRandom(250, 2500);
            return true;
        }
        return false;
    }

    private boolean disableWalkAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if(config.disableWalk() && Movement.calculateDistance(getCenter()) > 1) {
            Movement.walkTo(getCenter());
            sleepRand();
            return true;
        }
        return false;
    }

    private boolean mobDropsBones(Actor mob) {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        String[] mobs = {"Barbarian", "Chicken", "Cow", "Giant rat", "Goblin" };
        for (var name : mobs) {
            if(mob.getName().contains(name)) { return true; }
        }
        return false;
    }

    private boolean isInteracting() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        Actor interacting = Static.getClient().getLocalPlayer().getInteracting();
        if(interacting != null) {
            String name = interacting.getName();
            if (name != null && !name.isBlank()) {
                if (getMobs().contains(name)) {
                    if(interacting.getInteracting() != null && interacting.getInteracting().getName() == Static.getClient().getLocalPlayer().getName()) {
                        MessageUtils.warning("Already fighting " + name + "..");
                        sleepUntil(interacting::isDead, getRandomNumber(20000, 30000));
                        if (interacting.isDead() && config.buryBones() && mobDropsBones(interacting)) {
                            boolean lootedBones = lootBonesAction(interacting.getWorldLocation());
                            while (!lootedBones) {
                                lootedBones = lootBonesAction(interacting.getWorldLocation());
                            }
                        }
                    }
                } else {
                    MessageUtils.warning("Already interacting with " + name + "..");
                }
            } else {
                MessageUtils.warning("Already interacting..");
            }
            sleepRand();
            return true;
        }
        return false;
    }

    private boolean quickPrayerAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if (config.flick() && Prayers.isQuickPrayerEnabled())
        {
            Prayers.toggleQuickPrayer(false);
            sleepRand();
            return true;
        }
        return false;
    }

    private boolean drinkAntiFireAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if (config.antifire() && (!Combat.isAntifired() && !Combat.isSuperAntifired())) {
            Item antifire = Inventory.getFirst(
                    config.antifireType().getDose1(),
                    config.antifireType().getDose2(),
                    config.antifireType().getDose3(),
                    config.antifireType().getDose4()
            );
            if (antifire != null) {
                antifire.interact("Drink");
                sleepRandTick(3);
                return true;
            }
        }
        return false;
    }

    private boolean alchItemsAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if (config.alching()) {
            AlchSpell alchSpell = config.alchSpell();
            if (alchSpell.canCast()) {
                List<String> alchItems = Text.fromCSV(config.alchItems());
                Item alchItem = Inventory.getFirst(x -> x.getName() != null && textMatches(alchItems, x.getName()));
                if (alchItem != null) {
                    Magic.cast(alchSpell.getSpell(), alchItem);
                    sleepRandTick(3);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean lootItemsAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if(Inventory.isFull() || (config.buryBones() && Inventory.getFreeSlots() < 2)) { return false; }
        TileItem loot = TileItems.getFirstSurrounding(getCenter(), config.attackRange(), x ->
                !notOurItems.contains(x)
                        && !shouldNotLoot(x) && (shouldLootByName(x) || shouldLootUntradable(x) || shouldLootByValue(x)
                        || (config.buryBones() && x.getName() != null && x.getName().toLowerCase().contains("bones")))
        );
        return pickupLoot(loot);
    }

    private boolean lootBonesAction(WorldPoint tile) {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if(Inventory.isFull()) { return false; }
        TileItem loot = TileItems.getFirstAt(tile, item ->
            !notOurItems.contains(item)
                    && (config.buryBones() && item.getName() != null && item.getName().toLowerCase().contains("bones"))
        );
        return pickupLoot(loot);
    }

    private boolean pickupLoot(TileItem loot) {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if(Inventory.isFull()) { return false; }
        if (loot != null && canPick(loot)) {
            int tries = 0;
            while(!Reachable.isInteractable(loot.getTile()) && !config.disableWalk() && tries < 5) {
                Movement.walkTo(loot.getTile().getWorldLocation());
                sleepRandTick(3);
                tries++;
            }
            int count = Inventory.getCount(loot.getName());
            loot.pickup();
            sleepUntil(() -> count != Inventory.getCount(loot.getName()), 5000);
            return true;
        }
        return false;
    }


    private boolean drinkAntiPoisonAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if (config.antipoison() && Combat.isPoisoned()) {
            Item antipoison = Inventory.getFirst(
                    config.antipoisonType().getDose1(),
                    config.antipoisonType().getDose2(),
                    config.antipoisonType().getDose3(),
                    config.antipoisonType().getDose4()
            );
            if (antipoison != null) {
                antipoison.interact("Drink");
                sleepRandTick(3);
                return true;
            }
        }
        return false;
    }

    private boolean prayerPotionAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if (config.restore() && Prayers.getPoints() < 5) {
            Item restorePotion = Inventory.getFirst(x -> x.hasAction("Drink")
                    && (x.getName().contains("Prayer potion") || x.getName().contains("Super restore")));
            if (restorePotion != null) {
                restorePotion.interact("Drink");
                sleepRandTick(3);
                return true;
            }
        }
        return false;
    }

    private boolean eatFoodAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if (config.eat() && Combat.getHealthPercent() <= config.healthPercent())
        {
            List<String> foods = Text.fromCSV(config.foods());
            Item food = Inventory.getFirst(x -> (x.getName() != null && foods.stream().anyMatch(a -> x.getName().contains(a)))
                    || (foods.contains("Any") && x.hasAction("Eat")));
            if (food != null)
            {
                food.interact("Eat");
                sleepRandTick(3);
                return true;
            }
        }
        return false;
    }

    private boolean buryBonesAction() {
        if (!Game.isPlaying() || Game.isOnBreak() || Static.getClient().getLocalPlayer() == null) { return false; }
        if (config.buryBones()) {
            Item bones = Inventory.getFirst(x -> x.hasAction("Bury") || x.hasAction("Scatter"));
            if (bones != null) {
                int count = Inventory.getCount(bones.getName());
                bones.interact(bones.hasAction("Bury") ? "Bury" : "Scatter");
                sleepUntil(() -> count != Inventory.getCount(bones.getName()), 3000);
                return true;
            }
        }
        return false;
    }

    private String switchAttackStyle() {
        for (int i = 10; i <= 100; i += 5) {
            if(i == 100) { i--; }
            if(Static.getClient().getRealSkillLevel(Skill.STRENGTH) < i) {
                if(Combat.getAttackStyle() != Combat.AttackStyle.SECOND) {
                    Combat.setAttackStyle(Combat.AttackStyle.SECOND); // Strength
                    return "Strength";
                }
                break;
            }
            else {
                if(i == 99) { i += 5; }
                if(Static.getClient().getRealSkillLevel(Skill.ATTACK) < i-5) {
                    if(Combat.getAttackStyle() != Combat.AttackStyle.FIRST) {
                        Combat.setAttackStyle(Combat.AttackStyle.FIRST); // Attack
                        return "Attack";
                    }
                    break;
                } else if(Static.getClient().getRealSkillLevel(Skill.DEFENCE) < i-5) {
                    if(Combat.getAttackStyle() != Combat.AttackStyle.FOURTH) {
                        Combat.setAttackStyle(Combat.AttackStyle.FOURTH); // Defence
                        return "Defence";
                    }
                    break;
                }
            }
        }
        return null;
    }

    @Subscribe
	public void onChatMessage(ChatMessage e)
	{
		String message = e.getMessage();
		if (message.contains("other players have dropped"))
		{
			var notOurs = TileItems.getAt(Players.getLocal().getWorldLocation(), x -> true);
			log.debug("{} are not our items", notOurs.stream().map(TileItem::getName).collect(Collectors.toList()));
			notOurItems.addAll(notOurs);
		}
		else if (config.disableAfterSlayerTask() && message.contains("You have completed your task!"))
		{
			SwingUtilities.invokeLater(() -> Plugins.stopPlugin(this));
		}
	}

	private boolean shouldNotLoot(TileItem item)
	{
		return textMatches(Text.fromCSV(config.dontLoot()), item.getName());
	}

	private boolean shouldLootUntradable(TileItem item)
	{
		return config.untradables()
				&& (!item.isTradable() || item.hasInventoryAction("Destroy"))
				&& item.getId() != ItemID.COINS_995;
	}

	private boolean shouldLootByValue(TileItem item)
	{
		return config.lootByValue()
				&& config.lootValue() > 0
				&& itemManager.getItemPrice(item.getId()) * item.getQuantity() > config.lootValue();
	}

	private boolean shouldLootByName(TileItem item)
	{
		return textMatches(Text.fromCSV(config.loots()), item.getName());
	}

	private boolean textMatches(List<String> itemNames, String itemName)
	{
		return itemNames.stream().anyMatch(name -> WildcardMatcher.matches(name, itemName));
	}

	private void setCenter(WorldPoint worldPoint)
	{
		configManager.setConfiguration(
				"randosrs-fighter",
				"centerTile",
				String.format("%s %s %s", worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane())
		);
	}

	protected WorldPoint getCenter()
	{
		String textValue = config.centerTile();
		if (textValue.isBlank() || !WORLD_POINT_PATTERN.matcher(textValue).matches())
		{
			return null;
		}

		List<Integer> split = Arrays.stream(textValue.split(" "))
				.map(Integer::parseInt)
				.collect(Collectors.toList());

		return new WorldPoint(split.get(0), split.get(1), split.get(2));
	}

	protected boolean canPick(TileItem tileItem)
	{
		return tileItem != null && tileItem.distanceTo(client.getLocalPlayer().getWorldLocation()) <= 5 && !Inventory.isFull();
	}

    private List<String> getMobs() { return Text.fromCSV(config.monster()); }

    @Override
    public void refresh() {

    }
}
