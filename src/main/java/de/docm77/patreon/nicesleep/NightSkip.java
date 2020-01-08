package de.docm77.patreon.nicesleep;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Set;
import java.util.HashSet;

public class NightSkip implements PlayerSleepEvent {

  private JavaPlugin plugin;
  private double neededPercentage;
  private HashSet<Player> playersInBed;

  public NightSkip(JavaPlugin plugin, int neededPercentage)
  {
    this.plugin = plugin;
    this.neededPercentage = ((double)neededPercentage)/100.; 

    playersInBed = new HashSet<Player>();
  }

  @Override
  public void playerEnteredBed(Player player)
  {
    playersInBed.add(player);
    checkSleeping(player.getWorld());
  }

  @Override
  public void playerLeftBed(Player player)
  {
    playersInBed.remove(player);
    checkSleeping(player.getWorld());
  }

  private void checkSleeping(World world)
  {
    Server srv = this.plugin.getServer();
    CustomCommandSender sender = new CustomCommandSender(srv.getConsoleSender());
    int afkPlayers = 0;
    int totalPlayersInWorld = 0;
    int sleepingPlayers = 0;
    for(Player p : srv.getOnlinePlayers())
    {
      if(p.getWorld() == world)
      {
        boolean afk = false;
        try
        {
          srv.dispatchCommand(sender, "afkcheck " + p.getDisplayName());
          afk = !sender.lastMessage.toLowerCase().contains("playing");
        }
        catch(CommandException e)
        {}
        if(afk)
        {
          ++afkPlayers;
        }
        if(playersInBed.contains(p))
        { 
          ++sleepingPlayers;
        }
        ++totalPlayersInWorld;
      }
    }
    this.plugin.getLogger().info("There are " + totalPlayersInWorld + " players in this world. " + sleepingPlayers + " are sleeping and " + afkPlayers + " are afk.");

    int neededPlayers = (int) Math.floor((double) (totalPlayersInWorld - afkPlayers) * neededPercentage);
    neededPlayers = Math.max(neededPlayers, 1); // we need at least one in total
    neededPlayers -= sleepingPlayers;
    if(resetRequired(world))
    {
      if (neededPlayers > 0)
      {
        plugin.getServer().getConsoleSender().sendMessage("Somebody wants to sleep. " + neededPlayers + " more sleeping players needed.");
      }
      else
      {
        resetDay(world);
      }
    }
  }

  public void resetDay(World world) {
    world.setTime(0);
    world.setThundering(false);
    world.setStorm(false);
  }

  public boolean resetRequired(World world) {
    //check if the world is night or thundering
    if(this.isNight(world) || world.isThundering())
        return true;
    return false;
  }

  public boolean isNight(World world) {
    return (world.getTime() > 12541 && world.getTime() < 23850);
  }
}