package net.randosrs.taskManager;

public enum pluginInfo {
    AUTOLOGIN("-randOsrs Auto Login","randosrs-autologin",0),
    MINER("-randOsrs Miner","randosrs-miner",2),
    ;

    private final String name;
    private final String configGroup;
    private final int locationCount;
    pluginInfo(String name, String configGroup, int locationCount) {
        this.name = name;
        this.configGroup = configGroup;
        this.locationCount = locationCount;
    }

    public static pluginInfo getFirstActive(randOsrsTaskManagerPlugin plugin) {
        if(plugin.getPluginConfig().isMiningTaskEnabled()) {
            return pluginInfo.MINER;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public int getLocationCount() {
        return locationCount;
    }
}
