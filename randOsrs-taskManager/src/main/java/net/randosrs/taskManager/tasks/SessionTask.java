package net.randosrs.taskManager.tasks;

import net.randosrs.taskManager.randOsrsTaskManagerPlugin;
import net.unethicalite.api.commons.Time;

import static net.randosrs.taskManager.randOsrsTaskManagerPlugin.log;
import static net.runelite.api.util.Numbers.getRandomNumber;
import static net.unethicalite.api.commons.Time.getMilliseconds;

public class SessionTask extends TimedTask {

    private final randOsrsTaskManagerPlugin plugin;

    public SessionTask(randOsrsTaskManagerPlugin plugin) {
        super(plugin, "SessionTask");
        log("Starting breakTask..");
        this.plugin = plugin;
    }

    public void init() {
        setMethod(this::doTask);
        setUpdateLengthMethod(this::updateLengthFunc);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        plugin.getPluginTasks().getSmallBreakTask().resetTask();
        plugin.getPluginTasks().getSwitchPluginTask().resetTask();
    }

    public void doTask() {
        log("Running sessionTask.doTask()..");
        randOsrsTaskManagerPlugin.logout();
        plugin.getPluginTasks().getSmallBreakTask().stopTask();
        plugin.getPluginTasks().getSmallBreakReturnTask().stopTask();

        plugin.getPluginTasks().getSwitchLocationTask().stopTask();
        plugin.getPluginTasks().getSwitchLocationTask().doTask();
        plugin.getPluginTasks().getSwitchPluginTask().stopTask();
        plugin.getPluginTasks().getSwitchPluginTask().doTask();

        plugin.getPluginTasks().getBreakTask().resetTask();
    }

    public void updateLengthFunc() {
        long min = getMilliseconds(plugin.getConfig().sessionLength() - plugin.getConfig().sessionVariance(), Time.TimeMultiplier.MINUTES);
        long max = getMilliseconds(plugin.getConfig().sessionLength() + plugin.getConfig().sessionVariance(), Time.TimeMultiplier.MINUTES);
        setMillis(getRandomNumber(min, max));
        log(this.getName() + " min: " + min + ", max: " + max + ", chosen: " + this.getMillis());
    }
}
