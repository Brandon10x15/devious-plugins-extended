package net.randosrs.taskManager.tasks;

import net.randosrs.taskManager.randOsrsTaskManagerPlugin;
import net.unethicalite.api.commons.Time;

import static net.randosrs.taskManager.randOsrsTaskManagerPlugin.log;
import static net.runelite.api.util.Numbers.getRandomNumber;
import static net.unethicalite.api.commons.Time.getMilliseconds;

public class SmallBreakTask extends TimedTask {

    private final randOsrsTaskManagerPlugin plugin;

    public SmallBreakTask(randOsrsTaskManagerPlugin plugin) {
        super(plugin, "SmallBreakTask");
        this.plugin = plugin;
    }

    public void init() {
        this.setMethod(() -> {
            randOsrsTaskManagerPlugin.logout();
            plugin.getPluginTasks().getSmallBreakReturnTask().resetTask();
        });
        this.setUpdateLengthMethod(this::updateLengthFunc);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        plugin.getPluginTasks().getSmallBreakReturnTask().stopTask();
    }

    public void updateLengthFunc() {
        long min = plugin.getPluginTasks().getSessionTask().getMillis() / getRandomNumber(3, 5);
        long max = plugin.getPluginTasks().getSessionTask().getMillis() / getRandomNumber(2, 3);
        setMillis(getRandomNumber(min, max));
        if (plugin.getPluginTasks().getSessionTask() != null) {
            if (plugin.getPluginTasks().getSessionTask().getMillisLeft() - getMillis() < getMilliseconds(90, Time.TimeMultiplier.MINUTES)) {
                this.setMillis(-1);
                log(this.getName() + " stopping because session is too short.");
                return;
            }
        }
        log(this.getName() + " min: " + min + ", max: " + max + ", chosen: " + this.getMillis());
    }
}
