package net.randosrs.taskManager.tasks;

import net.randosrs.taskManager.randOsrsTaskManagerPlugin;
import net.runelite.api.GameState;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.events.LoginStateChanged;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;

import static net.randosrs.taskManager.randOsrsTaskManagerPlugin.log;
import static net.runelite.api.util.Numbers.getRandomNumber;
import static net.unethicalite.api.commons.Time.getMilliseconds;

public class SmallBreakReturnTask extends TimedTask {

    private final randOsrsTaskManagerPlugin plugin;

    public SmallBreakReturnTask(randOsrsTaskManagerPlugin plugin) {
        super(plugin, "SmallBreakReturnTask");
        this.plugin = plugin;
    }

    public void init() {
        this.setMethod(this::doTask);
        this.setUpdateLengthMethod(this::updateLengthFunc);
    }

    public void doTask() {
        Game.setOnBreak(false);
        if (Game.getState() == GameState.LOGIN_SCREEN) {
            Static.getClient().getCallbacks().post(new LoginStateChanged(24));
        }
        plugin.getPluginTasks().getSmallBreakTask().resetTask();
    }

    public void updateLengthFunc() {
        long min = getMilliseconds(5, Time.TimeMultiplier.MINUTES);
        long max = getMilliseconds(20, Time.TimeMultiplier.MINUTES);
        setMillis(getRandomNumber(min, max));
        log(this.getName() + " min: " + min + ", max: " + max + ", chosen: " + this.getMillis());
    }
}
