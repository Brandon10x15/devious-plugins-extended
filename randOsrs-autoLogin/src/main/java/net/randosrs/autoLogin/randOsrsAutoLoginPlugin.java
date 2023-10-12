package net.randosrs.autoLogin;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.World;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.events.LobbyWorldSelectToggled;
import net.unethicalite.api.events.LoginStateChanged;
import net.unethicalite.api.events.WorldHopped;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.plugins.SubscribedPlugin;
import net.unethicalite.api.script.blocking_events.WelcomeScreenEvent;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import org.jboss.aerogear.security.otp.Totp;
import org.pf4j.Extension;

import static net.runelite.api.util.Numbers.*;
import static net.unethicalite.api.commons.Time.sleepUntil;

@PluginDescriptor(name = "-randOsrs Auto Login", enabledByDefault = false)
@Extension
@Slf4j
public class randOsrsAutoLoginPlugin extends SubscribedPlugin {
    @Inject
    private randOsrsAutoLoginConfig config;

    @Inject
    private Client client;

    private boolean switchingWorlds = false;
    private boolean loggingIn = false;
    private boolean enteringAuth = false;

    @Inject
    private ConfigManager configManager;

    @Provides
    public randOsrsAutoLoginConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(randOsrsAutoLoginConfig.class);
    }

    @Subscribe
    public void onConfigButtonPressed(ConfigButtonClicked event) {
        System.out.println(event);
        if (!event.getGroup().equals("randosrs-autologin")) {
            return;
        }
        if (event.getKey().equals("selectSpecificWorld")) {
            if (config.selectSpecificWorld() && config.selectRandomWorld()) {
                configManager.setConfiguration("randosrs-autologin", "selectRandomWorld", false);
            }
        } else if (event.getKey().equals("selectRandomWorld")) {
            if (config.selectSpecificWorld() && config.selectRandomWorld()) {
                configManager.setConfiguration("randosrs-autologin", "selectSpecificWorld", false);
            }
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged e) {
        if (e.getGameState() == GameState.LOGIN_SCREEN && client.getLoginIndex() == 0) {
            prepareLogin();
        }
    }

    @Subscribe
    private void onLoginStateChanged(LoginStateChanged e) {
        switch (e.getIndex()) {
            case 2:
                login();
                break;
            case 4:
                enterAuth();
                break;
            case 24:
                prepareLogin();
                client.getCallbacks().post(new LoginStateChanged(2));
                break;
        }
    }

    @Subscribe
    private void onWorldHopped(WorldHopped e) {
        if (config.selectLastWorld()) {
            configManager.setConfiguration("randosrs-autologin", "specificWorld", e.getWorldId());
        }
    }

    @Subscribe
    private void onWidgetHiddenChanged(WidgetLoaded e) {
        if (config.pressPlay() && !Game.hasClickedPlay()) {
            Game.clickPlay();
        }
    }

    @Subscribe
    private void onLobbyWorldSelectToggled(LobbyWorldSelectToggled e) {
        if (e.isOpened()) {
            client.setWorldSelectOpen(false);
        }

        client.promptCredentials(false);
    }

    private void checkWorld() {
        new Thread(() -> {
            if (config.useWorldSelector()) {
                switchingWorlds = true;
                World selectedWorld = null;
                int oldWorld = Static.getClient().getWorld();
                if (config.selectSpecificWorld()) {
                    selectedWorld = Worlds.getFirst(config.specificWorld());
                } else if (config.selectRandomWorld()) {
                    selectedWorld = getBestWorld();
                }
                if (selectedWorld != null) {
                    client.changeWorld(selectedWorld);
                    sleepUntil(() -> Static.getClient().getWorld() == oldWorld, 15000);
                }
                switchingWorlds = false;
            }
        }).start();
    }


    private World getBestWorld() {
        World selectedWorld = null;
        int playerCount = 50;
        while (selectedWorld == null) {
            int finalPlayerCount = playerCount;
            selectedWorld = Worlds.getRandom(x -> Static.getClient().getWorld() != x.getId()
                    && x.isMembers() == config.isMember()
                    && x.isNormal()
                    && x.getPlayerCount() < finalPlayerCount
            );
            playerCount += 50;
        }
        return selectedWorld;
    }

    @Subscribe
    private void onPluginChanged(PluginChanged e) {
        if (e.getPlugin() != this) {
            return;
        }

        if (e.isLoaded() && Game.getState() == GameState.LOGIN_SCREEN) {
            prepareLogin();
            client.getCallbacks().post(new LoginStateChanged(2));
        }
    }

    private void prepareLogin() {
        switchingWorlds = false;
        loggingIn = false;
        enteringAuth = false;
        if (config.useWorldSelector() && ((config.selectSpecificWorld() && client.getWorld() != config.specificWorld()) || config.selectRandomWorld())) {
            client.loadWorlds();
        } else {
            client.promptCredentials(false);
        }
    }

    private void login() {
        new Thread(() -> {
            if (Game.isOnBreak()) {
                return;
            }
            loggingIn = true;
            checkWorld();
            while (switchingWorlds) {
                sleepRand();
            }
            while (client.getGameState() == GameState.LOGIN_SCREEN) {
                String name = Static.getClient().getUsername();
                if (name == null || !name.equalsIgnoreCase(config.username())) {
                    client.setUsername(config.username());
                    sleepRand();
                }
                String pass = Static.getClient().getPassword();
                if (pass == null || !pass.equalsIgnoreCase(config.password())) {
                    client.setPassword(config.password());
                    sleepRand();
                }
                Keyboard.sendEnter();
                sleepRand();
                Keyboard.sendEnter();
                sleepRand();
                Keyboard.sendEnter();
                sleepUntil(() -> client.getGameState() == GameState.LOGIN_SCREEN, 15000);
            }
            loggingIn = false;
        }).start();
    }

    private void enterAuth() {
        new Thread(() -> {
            enteringAuth = true;
            while (client.getGameState() == GameState.LOGIN_SCREEN_AUTHENTICATOR) {
                client.setOtp(new Totp(config.auth()).now());
                sleepRand();
                Keyboard.sendEnter();
                sleepRand();
                Keyboard.sendEnter();
                sleepRand();
                Keyboard.sendEnter();
                sleepUntil(() -> client.getGameState() != GameState.LOGIN_SCREEN_AUTHENTICATOR, 15000);
            }
        }).start();
    }

    @Override
    public void refresh() {
        if (Game.getState() == GameState.LOGIN_SCREEN) {
            prepareLogin();
            client.getCallbacks().post(new LoginStateChanged(2));
        }
    }
}
