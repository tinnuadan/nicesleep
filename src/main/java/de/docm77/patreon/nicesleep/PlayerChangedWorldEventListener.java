package de.docm77.patreon.nicesleep;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;

class PlayerChangedWorldEventListener implements Listener {

  private final JavaPlugin plugin;
  private final PlayerWorldChangeEventHandler worldChangeHandler;

  public PlayerChangedWorldEventListener(JavaPlugin plugin, PlayerWorldChangeEventHandler handler) {
    this.plugin = plugin;
    this.worldChangeHandler = handler;

    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  @EventHandler
  void onPlayerWorldChanged(PlayerChangedWorldEvent event) {
    this.plugin.getLogger().info("Player world changed");
    worldChangeHandler.worldChanged(event.getPlayer(), event.getFrom(), event.getPlayer().getWorld());
  }

}
