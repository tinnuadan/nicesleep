package de.docm77.patreon.nicesleep;

import org.bukkit.entity.Player;
import org.bukkit.*;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.HashSet;

public class PlayerBedEventListener implements Listener {
  JavaPlugin plugin;

  public PlayerBedEventListener(JavaPlugin plugin)
  {
    this.plugin = plugin;

    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.plugin.getLogger().info("PlayerBedEventListener created");
  }

  @Override
  public void finalize() {
    System.out.println("PlayerBedEventListener instance is getting destroyed");
  }

  @EventHandler
  void onPlayerBedEnter(PlayerBedEnterEvent event)
  {
    if(event.getBedEnterResult() == BedEnterResult.OK)
    {
      Server srv = this.plugin.getServer();
      CustomCommandSender sender = new CustomCommandSender(srv.getConsoleSender());
      Player player = event.getPlayer();
      World world = player.getWorld();
      Set<Player> playersInWorld = new HashSet<Player>();
      for(Player p : srv.getOnlinePlayers())
      {
        if(p.getWorld() == world)
        {
          boolean afk = false;
          try {
            boolean ret = srv.dispatchCommand(sender, "afkcheck " + p.getDisplayName());
            this.plugin.getLogger().info("afkcheck for " + p.getDisplayName() + ": " + sender.lastMessage + " / " + ret); 
            afk = !sender.lastMessage.toLowerCase().contains("playing");
          }
          catch(CommandException e) {
          }
          playersInWorld.add(p);
        }
      }
      this.plugin.getLogger().info(player.getDisplayName() + " went to bed");
    }
  }

  @EventHandler
  void onPlayerBedLeave(PlayerBedLeaveEvent event)
  {
    Player player = event.getPlayer();
    this.plugin.getLogger().info(player.getDisplayName() + " left the bed");
  }


}