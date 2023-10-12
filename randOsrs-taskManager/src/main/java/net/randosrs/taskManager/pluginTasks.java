package net.randosrs.taskManager;

import net.randosrs.taskManager.tasks.*;
public class pluginTasks {

    private final randOsrsTaskManagerPlugin plugin;
    // TASKS
    private SessionTask sessionTask;
    private BreakTask breakTask;
    private SmallBreakReturnTask smallBreakReturnTask;
    private SmallBreakTask smallBreakTask;
    private SwitchLocationTask switchLocationTask;
    private SwitchPluginTask switchPluginTask;

    public pluginTasks(randOsrsTaskManagerPlugin plugin) {
        this.plugin = plugin;
        init();
    }

    public void init() {
        sessionTask = new SessionTask(plugin);
        breakTask = new BreakTask(plugin);
        smallBreakTask = new SmallBreakTask(plugin);
        smallBreakReturnTask = new SmallBreakReturnTask(plugin);
        switchLocationTask = new SwitchLocationTask(plugin);
        switchPluginTask = new SwitchPluginTask(plugin);
    }

    public void initTasks() {
        sessionTask.init();
        breakTask.init();
        smallBreakTask.init();
        smallBreakReturnTask.init();
        switchPluginTask.init();
        switchLocationTask.init();
        /*
        breakTask.resetTask();
        sessionTask.resetTask();
        smallBreakTask.resetTask();
        smallBreakReturnTask.resetTask();
        if(plugin.getPluginConfig().isSwitchTasksEnabled()) {
            switchPluginTask.resetTask();
        }
        if(plugin.getPluginConfig().isMiningTaskSwitchLocationsEnabled()) {
            switchLocationTask.resetTask();
        }*/
    }

    public SessionTask getSessionTask() {
        return sessionTask;
    }

    public BreakTask getBreakTask() {
        return breakTask;
    }

    public SmallBreakReturnTask getSmallBreakReturnTask() {
        return smallBreakReturnTask;
    }

    public SmallBreakTask getSmallBreakTask() {
        return smallBreakTask;
    }

    public SwitchLocationTask getSwitchLocationTask() {
        return switchLocationTask;
    }

    public SwitchPluginTask getSwitchPluginTask() {
        return switchPluginTask;
    }
}
