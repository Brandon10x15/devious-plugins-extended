package net.randosrs.miner;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class randOsrsMinerOverlay extends Overlay
{
	private final Client client;
	private final randOsrsMinerPlugin plugin;
	private final randOsrsMinerConfig config;

	@Inject
	private randOsrsMinerOverlay(Client client, randOsrsMinerPlugin plugin, randOsrsMinerConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
        /*
		WorldPoint center = plugin.getCenter();
		if (graphics == null || center == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return null;
		}*/

		if (config.drawCenter())
		{
			//center.outline(client, graphics, Color.ORANGE, String.format("Center: %s", config.centerTile()));
		}

		if (config.drawRadius())
		{
			/*List<Tile> tiles = Tiles.getSurrounding(center, config.attackRange());
			for (Tile tile : tiles)
			{
				if (tile == null)
				{
					continue;
				}

				if (tile.distanceTo(center) >= config.attackRange())
				{
					tile.getWorldLocation().outline(client, graphics, Color.WHITE);
				}
			}*/
		}
		return null;
	}
}
