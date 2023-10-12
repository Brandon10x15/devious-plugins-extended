package net.randosrs.miner;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.unethicalite.api.movement.pathfinder.model.MiningLocation;

@ConfigGroup("randosrs-miner")
public interface randOsrsMinerConfig extends Config
{
	@ConfigSection(
			name = "General",
			description = "General settings",
			position = 991,
			closedByDefault = true
	)
	String general = "General";

	@ConfigSection(
			name = "Health",
			description = "General settings",
			position = 992,
			closedByDefault = true
	)
	String health = "Health";

	@ConfigSection(
			name = "Debug",
			description = "Debugging settings",
			position = 999,
			closedByDefault = true
	)
	String debug = "Debug";

    @ConfigItem(
            keyName = "mineLocations",
            name = "Mining Areas",
            description = "Walk to the specified mining area",
            position = 0,
            section = general
    )
    default MiningLocation mineLocation()
    {
        return MiningLocation.VARROCK_WEST_MINE;
    }

	@ConfigItem(
			keyName = "ores",
			name = "Ore Types",
			description = "Enter the types of ores. (Iron, Tin, Clay, etc..)",
			position = 0,
			section = general
	)
	default String oreTypes()
	{
		return "Tin";
	}

	@ConfigItem(
			keyName = "eat",
			name = "Eat food",
			description = "Eat food to heal",
			position = 0,
			section = health
	)
	default boolean eat()
	{
		return true;
	}

	@Range(max = 100)
	@ConfigItem(
			keyName = "eatHealthPercent",
			name = "Health %",
			description = "Health % to eat at",
			position = 1,
			section = health
	)
	default int healthPercent()
	{
		return 65;
	}

	@ConfigItem(
			keyName = "foods",
			name = "Food",
			description = "Food to eat, separated by comma. ex: Bones,Coins",
			position = 0,
			section = health
	)
	default String foods()
	{
		return "Any";
	}

	@ConfigItem(
			keyName = "drawRadius",
			name = "Draw attack area",
			description = "",
			position = 0,
			section = debug
	)
	default boolean drawRadius()
	{
		return false;
	}

	@ConfigItem(
			keyName = "drawCenter",
			name = "Draw center tile",
			description = "",
			position = 1,
			section = debug
	)
	default boolean drawCenter()
	{
		return false;
	}
}
