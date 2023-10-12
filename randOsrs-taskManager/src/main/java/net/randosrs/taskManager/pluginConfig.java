package net.randosrs.taskManager;

import net.unethicalite.api.movement.pathfinder.model.MiningLocation;
import net.unethicalite.api.plugins.Plugins;

import static net.randosrs.taskManager.randOsrsTaskManagerPlugin.log;
import static net.runelite.client.externalplugins.ExternalPluginManager.pluginManager;

public class pluginConfig {

    private final randOsrsTaskManagerPlugin plugin;
    private final pluginTasks plugTasks;
    private final randOsrsTaskManagerConfig config;
    // Config
    private final String group;
    private boolean takeBreaksEnabled;
    private int sessionLengthMinutes;
    private int sessionVarianceMinutes;
    private int breakLengthMinutes;
    private int breakVarianceMinutes;
    private boolean takeSmallBreaksEnabled;
    private boolean switchTasksEnabled;
    private boolean autoLoginTaskEnabled;
    private boolean miningTaskEnabled;
    private boolean miningTaskSwitchLocationsEnabled;
    private MiningLocation miningTaskLocation1;
    private MiningLocation miningTaskLocation2;
    public enum pluginChange {
        TAKEBREAKS("Take Breaks"),
        SESSIONLENGTH("Session Length"),
        SESSIONVARIANCE("Session Variance"),
        BREAKLENGTH("Break Length"),
        BREAKVARIANCE("Break Variance"),
        TAKESMALLBREAKS("Take Small Breaks"),
        SWITCHTASKS("Switch Tasks"),
        AUTOLOGINTASK("Auto Login Task"),
        MININGTASK("Mining Task"),
        MININGTASKSWITCHLOCATIONS("Mining Task Switch Locations"),
        MININGLOCATION1("Mining Task Location 1"),
        MININGLOCATION2("Mining Task Location 2"),
        ;
        private final String name;
        pluginChange(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    pluginConfig(randOsrsTaskManagerPlugin plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getConfig();
        this.plugTasks = this.plugin.getPluginTasks();
        this.group = "randosrs-taskmanager";
        initConfig();
    }
    private void initConfig() {
        takeBreaksEnabled = config.takeBreaks();
        sessionLengthMinutes = config.sessionLength();
        sessionVarianceMinutes = config.sessionVariance();
        breakLengthMinutes = config.breakLength();
        breakVarianceMinutes = config.breakVariance();
        takeSmallBreaksEnabled = config.takeSmallBreaks();
        switchTasksEnabled = config.switchTasksEnabled();
        autoLoginTaskEnabled = config.autoLoginTask();
        if(autoLoginTaskEnabled) {
            Plugins.startPlugin(pluginManager.getPlugin(pluginInfo.AUTOLOGIN.getName()));
        } else {
            Plugins.stopPlugin(pluginManager.getPlugin(pluginInfo.AUTOLOGIN.getName()));
        }
        miningTaskEnabled = config.miningTaskEnabled();
        if(miningTaskEnabled) {
            Plugins.startPlugin(pluginManager.getPlugin(pluginInfo.MINER.getName()));
        } else {
            Plugins.startPlugin(pluginManager.getPlugin(pluginInfo.MINER.getName()));
        }
        miningTaskSwitchLocationsEnabled = config.miningTaskSwitchLocations();
        miningTaskLocation1 = config.miningTaskLocation1();
        miningTaskLocation2 = config.miningTaskLocation2();
        log("Config initialized.");
    }

    public pluginChange updateConfig(pluginTasks plugTasks) {
        log("Config updated..");
        if(takeBreaksEnabled != config.takeBreaks()) {
            takeBreaksEnabled = config.takeBreaks();
            if(plugTasks.getBreakTask().isActive()) {
                plugTasks.getBreakTask().resetTask();
            }
            return pluginChange.TAKEBREAKS;
        }
        if(sessionLengthMinutes != config.sessionLength()) {
            sessionLengthMinutes = config.sessionLength();
            if(plugTasks.getSessionTask().isActive()) {
                plugTasks.getSessionTask().resetTask();
            }
            return pluginChange.SESSIONLENGTH;
        }
        if(sessionVarianceMinutes != config.sessionVariance()) {
            sessionVarianceMinutes = config.sessionVariance();
            if(plugTasks.getSessionTask().isActive()) {
                plugTasks.getSessionTask().resetTask();
            }
            return pluginChange.SESSIONVARIANCE;
        }
        if(breakLengthMinutes != config.breakLength()) {
            breakLengthMinutes = config.breakLength();
            if(plugTasks.getBreakTask().isActive()) {
                plugTasks.getBreakTask().resetTask();
            }
            return pluginChange.BREAKLENGTH;
        }
        if(breakVarianceMinutes != config.breakVariance()) {
            breakVarianceMinutes = config.breakVariance();
            if(plugTasks.getBreakTask().isActive()) {
                plugTasks.getBreakTask().resetTask();
            }
            return pluginChange.BREAKVARIANCE;
        }
        if(takeSmallBreaksEnabled != config.takeSmallBreaks()) {
            takeSmallBreaksEnabled = config.takeSmallBreaks();
            if(plugTasks.getSmallBreakTask().isActive()) {
                plugTasks.getSmallBreakTask().resetTask();
            }
            return pluginChange.TAKESMALLBREAKS;
        }
        if(switchTasksEnabled != config.switchTasksEnabled()) {
            switchTasksEnabled = config.switchTasksEnabled();
            if(plugTasks.getSwitchPluginTask().isActive()) {
                plugTasks.getSwitchPluginTask().resetTask();
            }
            return pluginChange.SWITCHTASKS;
        }
        if(autoLoginTaskEnabled != config.autoLoginTask()) {
            autoLoginTaskEnabled = config.autoLoginTask();
            if(autoLoginTaskEnabled) {
                Plugins.startPlugin(pluginManager.getPlugin(pluginInfo.AUTOLOGIN.getName()));
            } else {
                Plugins.stopPlugin(pluginManager.getPlugin(pluginInfo.AUTOLOGIN.getName()));
            }
            return pluginChange.AUTOLOGINTASK;
        }
        if(miningTaskEnabled != config.miningTaskEnabled()) {
            miningTaskEnabled = config.miningTaskEnabled();
            if(plugTasks.getSwitchPluginTask().isActive()) {
                plugTasks.getSwitchPluginTask().resetTask();
            }
            if(plugTasks.getSwitchLocationTask().isActive()) {
                plugTasks.getSwitchLocationTask().resetTask();
            }
            if(miningTaskEnabled) {
                pluginManager.setPluginEnabled(pluginManager.getPlugin(pluginInfo.MINER.getName()), true);
            } else {
                pluginManager.setPluginEnabled(pluginManager.getPlugin(pluginInfo.MINER.getName()), false);
            }
            return pluginChange.MININGTASK;
        }
        if(miningTaskSwitchLocationsEnabled != config.miningTaskSwitchLocations()) {
            miningTaskSwitchLocationsEnabled = config.miningTaskSwitchLocations();
            if(plugTasks.getSwitchLocationTask().isActive()) {
                plugTasks.getSwitchLocationTask().resetTask();
            }
            return pluginChange.MININGTASKSWITCHLOCATIONS;
        }
        if(miningTaskLocation1 != config.miningTaskLocation1()) {
            miningTaskLocation1 = config.miningTaskLocation1();
            if(plugTasks.getSwitchLocationTask().isActive()) {
                plugTasks.getSwitchLocationTask().resetTask();
            }
            return pluginChange.MININGLOCATION1;
        }
        if(miningTaskLocation2 != config.miningTaskLocation2()) {
            miningTaskLocation2 = config.miningTaskLocation2();
            if(plugTasks.getSwitchLocationTask().isActive()) {
                plugTasks.getSwitchLocationTask().resetTask();
            }
            return pluginChange.MININGLOCATION2;
        }
        return null;
    }

    public String getGroup() {
        return group;
    }

    public boolean isTakeBreaksEnabled() {
        return takeBreaksEnabled;
    }

    public int getSessionLengthMinutes() {
        return sessionLengthMinutes;
    }

    public int getSessionVarianceMinutes() {
        return sessionVarianceMinutes;
    }

    public int getBreakLengthMinutes() {
        return breakLengthMinutes;
    }

    public int getBreakVarianceMinutes() {
        return breakVarianceMinutes;
    }

    public boolean isTakeSmallBreaksEnabled() {
        return takeSmallBreaksEnabled;
    }

    public boolean isSwitchTasksEnabled() {
        return switchTasksEnabled;
    }

    public boolean isAutoLoginTaskEnabled() {
        return autoLoginTaskEnabled;
    }

    public boolean isMiningTaskEnabled() {
        return miningTaskEnabled;
    }

    public boolean isMiningTaskSwitchLocationsEnabled() {
        return miningTaskSwitchLocationsEnabled;
    }

    public MiningLocation getMiningTaskLocation1() {
        return miningTaskLocation1;
    }

    public MiningLocation getMiningTaskLocation2() {
        return miningTaskLocation2;
    }
}
