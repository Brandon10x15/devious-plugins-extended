package net.randosrs.taskManager.tasks;

import net.randosrs.taskManager.pluginInfo;
import net.randosrs.taskManager.randOsrsTaskManagerConfig;
import net.randosrs.taskManager.randOsrsTaskManagerPlugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.client.Static;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.randosrs.taskManager.randOsrsTaskManagerPlugin.log;
import static net.runelite.api.util.Numbers.getRandomNumber;
import static net.runelite.client.externalplugins.ExternalPluginManager.pluginManager;
import static net.unethicalite.api.commons.Time.getMilliseconds;

public class SwitchPluginTask extends TimedTask {

    private final randOsrsTaskManagerPlugin plugin;
    private List<pluginInfo> availablePlugins;
    private pluginInfo currentPlugin;

    public SwitchPluginTask(randOsrsTaskManagerPlugin plugin) {
        super(plugin, "SwitchPluginTask");
        this.plugin = plugin;
    }

    public void init() {
        this.setMethod(this::doTask);
        this.setUpdateLengthMethod(this::updateLengthFunc);
        currentPlugin = pluginInfo.getFirstActive(plugin);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        plugin.getPluginTasks().getSwitchLocationTask().resetTask();
    }

    public void doTask() {
        int pluginCount = getPluginCount();
        if (pluginCount == 2) {
            setCurrentPlugin(availablePlugins.get(0));
        } else if (pluginCount > 2) {
            var plugins = availablePlugins.stream().filter(info -> info != currentPlugin).collect(Collectors.toList());
            int rand = getRandomNumber(0, plugins.size()-1);
            setCurrentPlugin(plugins.get(rand));
        }
        resetTask();
    }

    public void updateLengthFunc() {
        int tasksEnabled = getPluginCount();
        if(tasksEnabled < 2) {
            this.setMillis(-1);
            log(this.getName() + " stopping because there aren't enough plugins.");
            return;
        }
        long evenLength = plugin.getPluginTasks().getSessionTask().getMillis()/tasksEnabled;
        long min = evenLength - (evenLength/5);
        long max = evenLength + (evenLength/5);
        setMillis(getRandomNumber(min, max));
        if(plugin.getPluginTasks().getSessionTask() != null) {
            if (plugin.getPluginTasks().getSessionTask().getMillisLeft() - getMillis() < getMilliseconds(90, Time.TimeMultiplier.MINUTES)) {
                setMillis(-1);
                log(this.getName() + " stopping because session is too short.");
                return;
            }
        }
        log(this.getName() + " min: " + min + ", max: " + max + ", chosen: " + this.getMillis());
    }

    public pluginInfo getCurrentPlugin() {
        return currentPlugin;
    }

    public void setCurrentPlugin(pluginInfo currentPlugin) {
        pluginManager.setPluginEnabled(pluginManager.getPlugin(this.currentPlugin.getName()), false);
        this.currentPlugin = currentPlugin;
        log("Current plugin set to: " + this.currentPlugin.getName());
        pluginManager.setPluginEnabled(pluginManager.getPlugin(this.currentPlugin.getName()), true);
    }

    public int getPluginCount() {
        int tasks = 0;
        availablePlugins = new ArrayList<>();
        if(plugin.getConfig().miningTaskEnabled()) {
            tasks++;
            if(currentPlugin != pluginInfo.MINER) {
                availablePlugins.add(pluginInfo.MINER);
            }
        }
        return tasks;
    }
}
