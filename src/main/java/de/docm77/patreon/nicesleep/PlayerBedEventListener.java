package de.docm77.patreon.nicesleep;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerBedEventListener implements Listener {

  BasePlugin plugin;
  PlayerSleepEventHandler sleepEventHandler;

  public PlayerBedEventListener(BasePlugin plugin, PlayerSleepEventHandler handler) {
    this.plugin = plugin;
    this.sleepEventHandler = handler;

    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  @EventHandler
  void onPlayerBedEnter(PlayerBedEnterEvent event) {
    if (event.getBedEnterResult() == BedEnterResult.OK) {
      Player player = event.getPlayer();
      this.plugin.logger().fine(player.getDisplayName() + " went to bed");
      sleepEventHandler.playerEnteredBed(player);
    }
  }

  @EventHandler
  void onPlayerBedLeave(PlayerBedLeaveEvent event) {
    Player player = event.getPlayer();
    this.plugin.logger().fine(player.getDisplayName() + " left the bed");
    sleepEventHandler.playerLeftBed(player);
  }

}
