package net.randosrs.taskManager;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.unethicalite.api.movement.pathfinder.model.MiningLocation;

@ConfigGroup("randosrs-taskmanager")
public interface randOsrsTaskManagerConfig extends Config
{
	@ConfigSection(
			name = "General",
			description = "General settings",
			position = 991
	)
	String general = "General";

    @ConfigSection(
            name = "Tasks",
            description = "Task settings",
            position = 992,
            closedByDefault = true
    )
    String tasks = "Tasks";

    @ConfigSection(
            name = "Miner Task",
            description = "Miner task settings.",
            position = 992,
            closedByDefault = true
    )
    String minerTask = "Miner Task";

    @ConfigSection(
            name = "Debug",
            description = "Debug settings.",
            position = 993,
            closedByDefault = true
    )
    String debugName = "Debug";

    @ConfigItem(
            keyName = "takeBreaks",
            name = "Take Breaks",
            description = "Should the player logout and take a break?",
            position = 0,
            section = general
    )
    default boolean takeBreaks()
    {
        return true;
    }

    @ConfigItem(
            keyName = "sessionLength",
            name = "Session Length",
            description = "Amount of Minutes to be played before taking a break. (Will be randomized within Session Variance minutes)",
            position = 1,
            hidden = true,
            unhide = "takeBreaks",
            section = general
    )
    default int sessionLength()
    {
        return 360;
    }

    @ConfigItem(
            keyName = "sessionVariance",
            name = "Session Variance",
            description = "Amount of Minutes to be randomize the session length within. (Session Min=Length-Variance, Max=Length+Variance)",
            position = 2,
            hidden = true,
            unhide = "takeBreaks",
            section = general
    )
    default int sessionVariance()
    {
        return 120;
    }

    @ConfigItem(
            keyName = "breakLength",
            name = "Break Length",
            description = "Amount of Minutes to take a break. (Will be randomized within Break Variance minutes)",
            position = 3,
            hidden = true,
            unhide = "takeBreaks",
            section = general
    )
    default int breakLength()
    {
        return 180;
    }

    @ConfigItem(
            keyName = "breakVariance",
            name = "Break Variance",
            description = "Amount of Minutes to be randomize the session length within. (Session Min=Length-Variance, Max=Length+Variance)",
            position = 4,
            hidden = true,
            unhide = "takeBreaks",
            section = general
    )
    default int breakVariance()
    {
        return 120;
    }

    @ConfigItem(
            keyName = "takeSmallBreaks",
            name = "Take Small Breaks",
            description = "Take random 5-20 minute breaks.",
            position = 5,
            section = general
    )
    default boolean takeSmallBreaks()
    {
        return false;
    }

    @ConfigItem(
            keyName = "switchTasksEnabled",
            name = "Switch Tasks",
            description = "Switch between enabled tasks periodically.",
            position = 2,
            hidden = true,
            unhide = "miningTask",
            section = tasks
    )
    default boolean switchTasksEnabled()
    {
        return true;
    }

    @ConfigItem(
            keyName = "autoLoginTask",
            name = "Auto Login",
            description = "Enable the auto login task.",
            position = 0,
            section = tasks
    )
    default boolean autoLoginTask()
    {
        return true;
    }

    @ConfigItem(
            keyName = "miningTaskEnabled",
            name = "Enable Task",
            description = "Enable the mining task.",
            position = 0,
            section = minerTask
    )
    default boolean miningTaskEnabled()
    {
        return false;
    }

    @ConfigItem(
            keyName = "miningTaskSwitchLocations",
            name = "Switch Locations",
            description = "Switch mining locations about half way through task.",
            position = 1,
            hidden = true,
            unhide = "miningTaskEnabled",
            section = minerTask
    )
    default boolean miningTaskSwitchLocations()
    {
        return true;
    }

    @ConfigItem(
            keyName = "miningTaskLocation1",
            name = "First Location",
            description = "Choose the first mining location.",
            position = 2,
            hidden = true,
            unhide = "miningTaskEnabled",
            enabledBy = "miningTaskSwitchLocations",
            section = minerTask
    )
    default MiningLocation miningTaskLocation1()
    {
        return MiningLocation.VARROCK_EAST_MINE;
    }

    @ConfigItem(
            keyName = "miningTaskLocation2",
            name = "Second Location",
            description = "Choose the second mining location.",
            position = 3,
            hidden = true,
            unhide = "miningTaskEnabled",
            enabledBy = "miningTaskSwitchLocations",
            section = minerTask
    )
    default MiningLocation miningTaskLocation2()
    {
        return MiningLocation.VARROCK_WEST_MINE;
    }

    @ConfigItem(
            keyName = "showInfo",
            name = "Show Info",
            description = "Shows info text on screen.",
            position = 0,
            section = debugName
    )
    default boolean showInfo()
    {
        return true;
    }

}
