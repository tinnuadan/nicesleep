package de.docm77.patreon.nicesleep;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashSet;
import java.math.RoundingMode;
import java.util.HashMap;

public class NightSkip implements NightSkipEventHandler, PlayerSleepEventHandler, PlayerWorldChangeEventHandler {

  private JavaPlugin plugin;
  private double neededPercentage;
  private RoundingMode roundingMethod;
  private int skipDelay;
  private boolean opsCanOverride;
  private HashSet<Player> playersInBed;
  private HashMap<World, NightSkipTimer> nightSkipTimers;
  private HashMap<World, BossBar> bossbars;
  private BarColor playerBarColor;
  private BarColor opBarColor;

  public NightSkip(JavaPlugin plugin, Config config) {
    this.plugin = plugin;
    this.neededPercentage = ((double) config.neededPercentage) / 100.;
    this.roundingMethod = config.roundingMethod;
    this.skipDelay = (int) (Math.round(config.skipDelaySeconds * 1000.0));
    this.opsCanOverride = config.opsCanOverride;
    this.playerBarColor = config.barColors.get(Config.Bar.Player);
    this.opBarColor = config.barColors.get(Config.Bar.OP);
    this.nightSkipTimers = new HashMap<World, NightSkipTimer>();
    this.bossbars = new HashMap<World, BossBar>();

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
    if (hasBossBar(from)) {
      bossbars.get(from).removePlayer(player);
    }
    if (hasBossBar(to)) {
      bossbars.get(to).addPlayer(player);
    }
  }

  private void checkSleeping(World world) {
    Server srv = this.plugin.getServer();
    CustomCommandSender sender = new CustomCommandSender(srv.getConsoleSender());
    int afkPlayers = 0;
    int totalPlayersInWorld = 0;
    int sleepingPlayers = 0;
    HashSet<Player> playersInWorld = new HashSet<Player>();
    boolean opSleeping = false;

    // get players in world and count afk players
    for (Player p : srv.getOnlinePlayers()) {
      if (p.getWorld() == world) {
        boolean afk = false;
        try {
          srv.dispatchCommand(sender, "afkcheck " + p.getDisplayName());
          final String msg = sender.lastMessage.toLowerCase();
          afk = !msg.contains("unknown command") && !msg.contains("playing");
        } catch (CommandException e) {
        }
        if (afk) {
          ++afkPlayers;
        }
        if (playersInBed.contains(p)) {
          ++sleepingPlayers;
          if (p.isOp()) {
            opSleeping = true;
          }
        }
        ++totalPlayersInWorld;
        playersInWorld.add(p);
      }
    }

    final int activePlayers = totalPlayersInWorld - afkPlayers;
    int totalNeededPlayers = Utils.Round(((double) activePlayers) * neededPercentage, roundingMethod);
    totalNeededPlayers = Math.max(Math.min(activePlayers, totalNeededPlayers), 1); // we need at least one in total and no more than active players
    final int neededPlayers = totalNeededPlayers - sleepingPlayers;

    NightSkipTimer timer = getTimer(world);
    final boolean skipTheNight = neededPlayers <= 0 || (opsCanOverride && opSleeping);
    if (resetRequired(world)) {
      // only broadcast the message if we need more people and there are actually
      // people sleeping
      if (sleepingPlayers > 0) {
        final int playersUsedForCalculation = Math.max(neededPlayers, 0) + sleepingPlayers;
        final double perc = Math.min(100., (double) sleepingPlayers / (double) playersUsedForCalculation);
        if (!hasBossBar(world)) {
          createBossBar(world, playersInWorld);
        }
        BossBar bb = bossbars.get(world);
        bb.setTitle("Sleeping: " + sleepingPlayers + " out of " + totalNeededPlayers + " necessary");
        bb.setProgress(perc);
        if (opsCanOverride && opSleeping) {
          bb.setColor(opBarColor);
        } else {
          bb.setColor(playerBarColor);
        }
      }
    }
    if(sleepingPlayers == 0 && hasBossBar(world)) {
      removeBossBar(world);
    }
    plugin.getLogger().fine("Status:");
    plugin.getLogger().fine("\tTotal players in the world: " + totalPlayersInWorld);
    plugin.getLogger().fine("\tSleeping players: " + sleepingPlayers);
    plugin.getLogger().fine("\tTotal needed players for skipping: " + totalNeededPlayers);
    plugin.getLogger().fine("\tTimer running:" + timer.isRunning());
    plugin.getLogger().fine("\tSkip the night:" + skipTheNight);
    if (skipTheNight && !timer.isRunning()) {
      timer.start();
      plugin.getLogger().fine("Starting sleep timer");
    }
    if (!skipTheNight && timer.isRunning()) {
      plugin.getLogger().fine("Cancelling sleep timer");
      timer.stop();
    }
  }

  public void resetDay(World world) {
    plugin.getLogger().fine("Reset to day");
    world.setTime(0);
    world.setStorm(false);
    world.setThundering(false);
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
    plugin.getLogger().fine("Night skip requested");
    Bukkit.getScheduler().runTask(plugin, new Runnable() {
      // the weather clearing must be run in the main thread
      @Override
      public void run() {
        removeBossBar(world);
        removeTimer(world);
        if (resetRequired(world)) {
          resetDay(world);
        }
      }
    });
  }

  private NightSkipTimer getTimer(World world) {
    if (!nightSkipTimers.containsKey(world)) {
      nightSkipTimers.put(world, new NightSkipTimer(world, skipDelay, this));
    }
    return nightSkipTimers.get(world);
  }

  private boolean hasBossBar(World world) {
    return bossbars.containsKey(world);
  }

  private BossBar createBossBar(World world, HashSet<Player> playersInWorld) {
    plugin.getLogger().fine("Creating bossbar");
    BossBar bb = plugin.getServer().createBossBar("Sleeping", playerBarColor, BarStyle.SOLID);
    bb.setProgress(0);
    for (Player p : playersInWorld) {
      bb.addPlayer(p);
    }
    bossbars.put(world, bb);
    return bb;
  }

  private void removeBossBar(World world) {
    if (hasBossBar(world)) {
      plugin.getLogger().fine("Removing bossbar");
      bossbars.get(world).removeAll();
      bossbars.remove(world);
    }
  }

  private void removeTimer(World world) {
    if (nightSkipTimers.containsKey(world)) {
      nightSkipTimers.remove(world);
    }
  }

}