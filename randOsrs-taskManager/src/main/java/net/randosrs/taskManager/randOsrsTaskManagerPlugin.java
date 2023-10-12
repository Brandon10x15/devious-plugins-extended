package net.randosrs.taskManager;

import static net.runelite.api.util.Numbers.getRandomNumber;
import static net.unethicalite.api.commons.Time.sleepUntil;

import net.randosrs.taskManager.tasks.*;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.music.MusicConfig;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.events.LoginStateChanged;
import net.unethicalite.api.movement.pathfinder.model.MiningLocation;
import net.unethicalite.api.plugins.SubscribedPlugin;
import com.google.inject.Provides;
import net.unethicalite.api.game.Game;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@PluginDescriptor(
        name = "-randOsrs Task Manager",
        description = "Sweet script that will switch tasks and take breaks periodically.",
        enabledByDefault = false
)
@Slf4j
@Extension
public class randOsrsTaskManagerPlugin extends SubscribedPlugin {

    private ScheduledExecutorService executor;
    private pluginConfig pluginConfig;
    private pluginTasks pluginTasks;

    @Inject
    private randOsrsTaskManagerConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private randOsrsTaskManagerOverlay randOsrsTaskManagerOverlay;

    @Provides
    public randOsrsTaskManagerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(randOsrsTaskManagerConfig.class);
    }

    @Override
    public void startUp() throws Exception {
        super.startUp();
        overlayManager.add(randOsrsTaskManagerOverlay);
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(() ->
        {
            //log("startUp()");
            //if(this.pluginConfig == null) { this.pluginConfig = new pluginConfig(this); }
            //if(this.pluginTasks == null) { this.pluginTasks = new pluginTasks(this); this.pluginTasks.startTasks();}
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutDown() {
        stopPlugins();
        overlayManager.remove(randOsrsTaskManagerOverlay);
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Override
    public void refresh() {
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        log(gameStateChanged.getGameState());
        new Thread(() -> {
            if (gameStateChanged.getGameState() == GameState.LOGGED_IN && !Game.hasClickedPlay()) {
                if(getPluginTasks() == null) { return; }
                if(getPluginTasks().getSwitchPluginTask() == null) { return; }
                if(getPluginTasks().getSwitchPluginTask().getCurrentPlugin() == pluginInfo.MINER) {
                    if (config.miningTaskEnabled() && config.miningTaskSwitchLocations()) {
                        MiningLocation nearest = MiningLocation.getNearest(x -> x == config.miningTaskLocation1() || x == config.miningTaskLocation2());
                        if(nearest != getPluginTasks().getSwitchLocationTask().getCurrentMiningLocation()) {
                            getPluginTasks().getSwitchLocationTask().setCurrentMiningLocation(nearest);
                        }

                    }
                }
            }
            if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
                overlayManager.add(randOsrsTaskManagerOverlay);
            }
        }).start();
    }
    @Subscribe
    private void onPluginChanged(PluginChanged e) {
        if (e.getPlugin() != this) {
            return;
        }
        if (!e.isLoaded()) {
            return;
        }
        log("Starting plugin: " + this.getName());
        this.pluginConfig = new pluginConfig(this);
        this.pluginTasks = new pluginTasks(this);
        this.pluginTasks.initTasks();
        this.pluginTasks.getBreakTask().doTask();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if(this.getPluginConfig() == null) { return; }
        if (configChanged.getGroup().equals(this.getPluginConfig().getGroup())) {
            net.randosrs.taskManager.pluginConfig.pluginChange updated = pluginConfig.updateConfig(getPluginTasks());
            log("Task Manager config updated: " + (updated != null ? updated.getName() : "Enabled State"));
        }
    }
    // STATIC
    public static void log(Object object) {
        Static.getClient().getLogger().info(object.toString());
    }

    public static void logout() {
        Game.setOnBreak(true);
        int rand = getRandomNumber(0,2);
        if(rand == 0) {
            Game.logout();
        }
    }

    public static void login() {
        Game.setOnBreak(false);
        if (Game.getState() == GameState.LOGIN_SCREEN) {
            Static.getClient().getCallbacks().post(new LoginStateChanged(24));
        }
    }

    public void stopPlugins() {
        if(getPluginTasks() != null) {
            getPluginTasks().getBreakTask().stopTask();
            getPluginTasks().getSessionTask().stopTask();
            getPluginTasks().getSmallBreakTask().stopTask();
            getPluginTasks().getSmallBreakReturnTask().stopTask();
            getPluginTasks().getSwitchPluginTask().stopTask();
            getPluginTasks().getSwitchLocationTask().stopTask();
        }
    }

    public randOsrsTaskManagerConfig getConfig() {
        return config;
    }

    public pluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public pluginTasks getPluginTasks() {
        return pluginTasks;
    }
}