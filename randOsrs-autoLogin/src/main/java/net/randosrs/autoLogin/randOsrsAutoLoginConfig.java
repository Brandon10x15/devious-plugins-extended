package net.randosrs.autoLogin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("randosrs-autologin")
public interface randOsrsAutoLoginConfig extends Config {
    @ConfigSection(
            name = "Account",
            description = "Account settings",
            position = 991,
            closedByDefault = true
    )
    String general = "General";

    @ConfigSection(
            name = "WorldSettings",
            description = "World selection settings",
            position = 992,
            closedByDefault = true
    )
    String worldSettings = "WorldSettings";

    @ConfigItem(
            keyName = "username",
            name = "Username",
            description = "Enter your username.",
            position = 0,
            section = general
    )
    default String username() {
        return "Username";
    }

    @ConfigItem(
            keyName = "password",
            name = "Password",
            description = "Enter your password.",
            secret = true,
            position = 1,
            section = general
    )
    default String password() {
        return "Password";
    }

    @ConfigItem(
            keyName = "auth",
            name = "Authenticator",
            description = "Enter your authentication code, if you have one.",
            secret = true,
            position = 2,
            section = general
    )
    default String auth() {
        return "Authenticator";
    }

    // Is member
    @ConfigItem(
            keyName = "isMember",
            name = "Membership",
            description = "Does this account have membership?",
            position = 3,
            section = general
    )
    default boolean isMember() {
        return false;
    }

    // Press play
    @ConfigItem(
            keyName = "pressPlay",
            name = "Press Play Button",
            description = "Press 'Click here to Play' button after login?",
            position = 6,
            section = general
    )
    default boolean pressPlay() {
        return false;
    }

    // Take breaks
    @ConfigItem(
            keyName = "takeBreaks",
            name = "Take Breaks",
            description = "Should this account take breaks?",
            position = 4,
            section = general
    )
    default boolean takeBreaks() {
        return false;
    }

    // World selector
    @ConfigItem(
            keyName = "useWorldSelector",
            name = "Use World Selector",
            description = "Use the world selector?",
            position = 0,
            section = worldSettings
    )
    default boolean useWorldSelector() {
        return false;
    }

    // Specific world
    @ConfigItem(
            keyName = "selectSpecificWorld",
            name = "Specific World",
            description = "Choose a specific world.",
            position = 1,
            hidden = true,
            unhide = "useWorldSelector",
            section = worldSettings
    )
    default boolean selectSpecificWorld() {
        return false;
    }

    // Last world
    @ConfigItem(
            keyName = "selectLastWorld",
            name = "Save Last World",
            description = "Save and join last world?",
            position = 2,
            hidden = true,
            unhide = "selectSpecificWorld",
            section = worldSettings
    )
    default boolean selectLastWorld() { return false; }

    // World number
    @ConfigItem(
            keyName = "world",
            name = "World",
            description = "Choose a world number",
            position = 3,
            hidden = true,
            unhide = "selectSpecificWorld",
            section = worldSettings
    )
    default int specificWorld() {
        return 542;
    }

    // Random world
    @ConfigItem(
            keyName = "selectRandomWorld",
            name = "Random World",
            description = "Select a random world.",
            position = 4,
            hidden = true,
            unhide = "useWorldSelector",
            section = worldSettings
    )
    default boolean selectRandomWorld() {
        return true;
    }

}
