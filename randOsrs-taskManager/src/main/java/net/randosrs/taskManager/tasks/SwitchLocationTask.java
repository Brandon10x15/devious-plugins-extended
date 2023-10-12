package net.randosrs.taskManager.tasks;

import net.randosrs.taskManager.pluginInfo;
import net.randosrs.taskManager.randOsrsTaskManagerConfig;
import net.randosrs.taskManager.randOsrsTaskManagerPlugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.movement.pathfinder.model.MiningLocation;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.client.Static;

import static net.randosrs.taskManager.randOsrsTaskManagerPlugin.log;
import static net.runelite.api.util.Numbers.getRandomNumber;
import static net.unethicalite.api.commons.Time.getMilliseconds;

public class SwitchLocationTask extends TimedTask {

    private final randOsrsTaskManagerPlugin plugin;

    private MiningLocation currentMiningLocation;

    public SwitchLocationTask(randOsrsTaskManagerPlugin plugin) {
        super(plugin, "SwitchLocationTask");
        this.plugin = plugin;
    }

    public void init() {
        this.setMethod(this::doTask);
        this.setUpdateLengthMethod(this::updateLengthFunc);
        doTask();
    }

    public void doTask() {
        randOsrsTaskManagerConfig config = plugin.getConfig();

        if (config.miningTaskEnabled() && config.miningTaskSwitchLocations()) {
            LoopedPlugin minerPlugin = (LoopedPlugin) Static.getPluginManager().getPlugin(pluginInfo.MINER.getName());
            if (minerPlugin != null) {
                    if (this.currentMiningLocation == null) {
                        if(Game.isPlaying()) {
                            this.setCurrentMiningLocation(MiningLocation.getNearest(x -> x == config.miningTaskLocation1()
                                    || x == config.miningTaskLocation2()));
                        } else {
                            this.setCurrentMiningLocation(config.miningTaskLocation1());
                        }
                    } else {
                        this.setCurrentMiningLocation(this.currentMiningLocation.name().contains(config.miningTaskLocation1().name())
                                ? config.miningTaskLocation2() : config.miningTaskLocation1());
                    }
                }
        }
    }

    public void updateLengthFunc() {
        int locations = getLocationCount();
        if(locations < 2) {
            this.setMillis(-1);
            log(this.getName() + " stopping because there aren't enough locations.");
            return;
        }
        long evenLength = plugin.getPluginTasks().getSwitchPluginTask().isActive()
                ? plugin.getPluginTasks().getSwitchPluginTask().getMillis() / locations
                : plugin.getPluginTasks().getSessionTask().getMillis() / locations ;
        long min = evenLength - (evenLength / 5);
        long max = evenLength + (evenLength / 5);
        setMillis(getRandomNumber(min, max));
        /*if (plugin.getPluginTasks().getSessionTask() != null) {
            if (plugin.getPluginTasks().getSessionTask().getMillisLeft() - getMillis() < getMilliseconds(120, Time.TimeMultiplier.MINUTES)) {
                this.setMillis(-1);
                log(this.getName() + " stopping because session is too short.");
                return;
            }
        }*/
        log(this.getName() + " min: " + min + ", max: " + max + ", chosen: " + this.getMillis());
    }

    public int getLocationCount() {
        int tasks = 0;
        if (plugin.getConfig().miningTaskEnabled() && plugin.getConfig().miningTaskSwitchLocations() && plugin.getPluginTasks().getSwitchPluginTask().getCurrentPlugin() == pluginInfo.MINER) {
            tasks += pluginInfo.MINER.getLocationCount();
        }
        return tasks;
    }

    public MiningLocation getCurrentMiningLocation() {
        return currentMiningLocation;
    }

    public void setCurrentMiningLocation(MiningLocation currentMiningLocation) {
        if(this.currentMiningLocation == currentMiningLocation) { return; }
        this.currentMiningLocation = currentMiningLocation;
        Static.getConfigManager().setConfiguration(pluginInfo.MINER.getConfigGroup(), "mineLocations", currentMiningLocation);
        log(this.getName() + ": Current mining location changed to: " + this.currentMiningLocation.name());
    }
}
