package net.randosrs.taskManager.tasks;

import net.randosrs.taskManager.randOsrsTaskManagerPlugin;
import net.unethicalite.api.commons.Time;

import javax.inject.Inject;

import static net.randosrs.taskManager.randOsrsTaskManagerPlugin.log;
import static net.runelite.api.util.Numbers.getRandomNumber;
import static net.unethicalite.api.commons.Time.getMilliseconds;

public class BreakTask extends TimedTask {

    private final randOsrsTaskManagerPlugin plugin;

    public BreakTask(randOsrsTaskManagerPlugin plugin) {
        super(plugin, "BreakTask");
        this.plugin = plugin;
    }

    public void init() {
        setMethod(this::doTask);
        setUpdateLengthMethod(this::updateLengthFunc);
    }

    public void doTask() {
        randOsrsTaskManagerPlugin.login();
        plugin.getPluginTasks().getSessionTask().resetTask();
    }

    public void updateLengthFunc() {
        long min = getMilliseconds(plugin.getConfig().breakLength() - plugin.getConfig().breakVariance(), Time.TimeMultiplier.MINUTES);
        long max = getMilliseconds(plugin.getConfig().breakLength() + plugin.getConfig().breakVariance(), Time.TimeMultiplier.MINUTES);
        setMillis(getRandomNumber(min, max));
        log(this.getName() + " min: " + min + ", max: " + max + ", chosen: " + this.getMillis());
    }
}
