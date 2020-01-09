package de.docm77.patreon.nicesleep;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashSet;
import java.util.HashMap;

public class NightSkip implements NightSkipEventHandler, PlayerSleepEventHandler, PlayerWorldChangeEventHandler {

  private JavaPlugin plugin;
  private double neededPercentage;
  private int skipDelay;
  private HashSet<Player> playersInBed;
  private HashMap<World, NightSkipTimer> nightSkipTimers;

  public NightSkip(JavaPlugin plugin, int neededPercentage, double secondsBeforeSkip) {
    this.plugin = plugin;
    this.neededPercentage = ((double) neededPercentage) / 100.;
    this.skipDelay = (int)(Math.round(secondsBeforeSkip * 1000.0));
    this.nightSkipTimers = new HashMap<World, NightSkipTimer>();

    playersInBed = new HashSet<Player>();
  }

  @Override
  public void playerEnteredBed(Player player) {
    playersInBed.add(player);
    checkSleeping(player.getWorld());
  }

  @Override
  public void playerLeftBed(Player player) {
    playersInBed.remove(player);
    checkSleeping(player.getWorld());
  }

  @Override
  public void worldChanged(Player player, World from, World to) {
    checkSleeping(from);
    checkSleeping(to);
  }

  private void checkSleeping(World world) {
    Server srv = this.plugin.getServer();
    CustomCommandSender sender = new CustomCommandSender(srv.getConsoleSender());
    int afkPlayers = 0;
    int totalPlayersInWorld = 0;
    int sleepingPlayers = 0;
    for (Player p : srv.getOnlinePlayers()) {
      if (p.getWorld() == world) {
        boolean afk = false;
        try {
          srv.dispatchCommand(sender, "afkcheck " + p.getDisplayName());
          afk = !sender.lastMessage.toLowerCase().contains("playing");
        } catch (CommandException e) {
        }
        if (afk) {
          ++afkPlayers;
        }
        if (playersInBed.contains(p)) {
          ++sleepingPlayers;
        }
        ++totalPlayersInWorld;
      }
    }

    int neededPlayers = (int) Math.floor((double) (totalPlayersInWorld - afkPlayers) * neededPercentage);
    neededPlayers = Math.max(neededPlayers, 1); // we need at least one in total
    neededPlayers -= sleepingPlayers;

    NightSkipTimer timer = getTimer(world);
    if (resetRequired(world)) {
      // only broadcast the message if we need more people and there are actually people sleeping
      if (neededPlayers > 0 && sleepingPlayers > 0) {
        this.plugin.getLogger().info("There are " + totalPlayersInWorld + " players in this world. " + sleepingPlayers
            + " are sleeping and " + afkPlayers + " are afk. For skipping the night " + neededPlayers + " are needed.");
        plugin.getServer().broadcastMessage(
            "Someone wants to sleep in the " + world.getName() + ". " + neededPlayers + " more players needed.");
      } else if (!timer.isRunning()) {
        timer.start();
        plugin.getLogger().info("Starting sleep timer");
      }
    }
    if (neededPlayers > 0 && timer.isRunning()) {
      plugin.getLogger().info("Cancelling sleep timer");
      timer.stop();
    }
  }

  public void resetDay(World world) {
    world.setTime(0);
    world.setThundering(false);
    world.setStorm(false);
  }

  public boolean resetRequired(World world) {
    // check if the world is night or thundering
    return (this.isNight(world) || world.isThundering());
  }

  public boolean isNight(World world) {
    return (world.getTime() > 12541 && world.getTime() < 23850);
  }

  @Override
  public void nightSkipped(World world) {
    plugin.getLogger().info("Night skip requested");
    if (resetRequired(world)) {
      resetDay(world);
    }
  }

  private NightSkipTimer getTimer(World world) {
    if (!nightSkipTimers.containsKey(world)) {
      nightSkipTimers.put(world, new NightSkipTimer(world, skipDelay, this));
    }
    return nightSkipTimers.get(world);
  }

}