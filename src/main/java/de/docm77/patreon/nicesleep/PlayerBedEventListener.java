package de.docm77.patreon.nicesleep;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerBedEventListener implements Listener {

  JavaPlugin plugin;
  PlayerSleepEvent sleepEvent;

  public PlayerBedEventListener(JavaPlugin plugin, PlayerSleepEvent sleepEvent) {
    this.plugin = plugin;
    this.sleepEvent = sleepEvent;

    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.plugin.getLogger().info("PlayerBedEventListener created");
  }

  @Override
  public void finalize() {
    System.out.println("PlayerBedEventListener instance is getting destroyed");
  }

  @EventHandler
  void onPlayerBedEnter(PlayerBedEnterEvent event) {
    if (event.getBedEnterResult() == BedEnterResult.OK) {
      Player player = event.getPlayer();
      this.plugin.getLogger().info(player.getDisplayName() + " went to bed");
      sleepEvent.playerEnteredBed(player);
    }
  }

  @EventHandler
  void onPlayerBedLeave(PlayerBedLeaveEvent event) {
    Player player = event.getPlayer();
    this.plugin.getLogger().info(player.getDisplayName() + " left the bed");
    sleepEvent.playerLeftBed(player);
  }

}