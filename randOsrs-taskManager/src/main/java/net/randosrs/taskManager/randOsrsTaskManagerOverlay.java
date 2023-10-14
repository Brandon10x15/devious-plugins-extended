package net.randosrs.taskManager;

import com.google.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

public class randOsrsTaskManagerOverlay extends OverlayPanel {
    private final randOsrsTaskManagerPlugin plugin;

    @Inject
    private randOsrsTaskManagerOverlay(randOsrsTaskManagerPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_CENTER);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(OverlayPriority.HIGHEST);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (graphics == null || !plugin.getConfig().showInfo() || plugin.getPluginTasks() == null)
        {
            return null;
        }

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Task Manager")
                .build());

        if(plugin.getPluginTasks().getSessionTask() != null && plugin.getPluginTasks().getSessionTask().isActive()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Session")
                    .right(printMillis(plugin.getPluginTasks().getSessionTask().getMillisLeft()))
                    .build());
        }
        if(plugin.getPluginTasks().getBreakTask() != null && plugin.getPluginTasks().getBreakTask().isActive()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Break")
                    .right(printMillis(plugin.getPluginTasks().getBreakTask().getMillisLeft()))
                    .build());
        }
        if(plugin.getPluginTasks().getSmallBreakTask() != null && plugin.getPluginTasks().getSmallBreakTask().isActive()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Sm Break")
                    .right(printMillis(plugin.getPluginTasks().getSmallBreakTask().getMillisLeft()))
                    .build());
        }
        if(plugin.getPluginTasks().getSmallBreakReturnTask() != null && plugin.getPluginTasks().getSmallBreakReturnTask().isActive()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Sm Break Return")
                    .right(printMillis(plugin.getPluginTasks().getSmallBreakReturnTask().getMillisLeft()))
                    .build());
        }
        if(plugin.getPluginTasks().getSwitchPluginTask() != null && plugin.getPluginTasks().getSwitchPluginTask().isActive()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Switch Plugin")
                    .right(printMillis(plugin.getPluginTasks().getSwitchPluginTask().getMillisLeft()))
                    .build());
        }
        if(plugin.getPluginTasks().getSwitchLocationTask() != null && plugin.getPluginTasks().getSwitchLocationTask().isActive()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Switch Location")
                    .right(printMillis(plugin.getPluginTasks().getSwitchLocationTask().getMillisLeft()))
                    .build());
        }

        return super.render(graphics);
    }

    private String printMillis(long millis) {
        long hrs = 0;
        long mins = 0;
        long secs = 0;
        while(millis > 3600000) {
            hrs++;
            millis -= 3600000;
        }
        while(millis > 60000) {
            mins++;
            millis -= 60000;
        }
        if(mins < 1) {
            secs = millis/1000;
        }

        return hrs + "h " + mins + "m" + (mins < 1 ? " " + secs + "s" : "");
    }
}
