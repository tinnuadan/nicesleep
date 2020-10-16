package de.docm77.patreon.nicesleep;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class NightSkip implements NightSkipEventHandler, PlayerSleepEventHandler, PlayerWorldChangeEventHandler
{

  private final BasePlugin plugin;
  private final double neededPercentage;
  private final RoundingMode roundingMethod;
  private final int skipDelay;
  private final boolean opsCanOverride;
  private final boolean blame;
  private final HashSet<Player> playersInBed;
  private final HashMap<World, NightSkipTimer> nightSkipTimers;
  private final HashMap<World, BossBar> bossBars;
  private final BarColor playerBarColor;
  private final BarColor opBarColor;

  public NightSkip(BasePlugin plugin, Config config) {
    this.plugin = plugin;
    this.neededPercentage = ((double) config.neededPercentage) / 100.;
    this.roundingMethod = config.roundingMethod;
    this.skipDelay = (int) (Math.round(config.skipDelaySeconds * 1000.0));
    this.opsCanOverride = config.opsCanOverride;
    this.blame = config.blame;
    this.playerBarColor = config.barColors.get(Config.Bar.Player);
    this.opBarColor = config.barColors.get(Config.Bar.OP);
    this.nightSkipTimers = new HashMap<World, NightSkipTimer>();
    this.bossBars = new HashMap<World, BossBar>();

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
      bossBars.get(from).removePlayer(player);
    }
    if (hasBossBar(to)) {
      bossBars.get(to).addPlayer(player);
    }
  }

  private void checkSleeping(World world) {
    Server srv = this.plugin.getServer();
    CustomCommandSender sender = new CustomCommandSender(srv.getConsoleSender());
    int afkPlayers = 0;
    int totalPlayersInWorld = 0;
    int sleepingPlayers = 0;
    HashSet<Player> playersInWorld = new HashSet<Player>();
    AtomicBoolean opSleeping = new AtomicBoolean(false);

    // get players in world and count afk players
    for (Player p : srv.getOnlinePlayers()) {
      if (p.getWorld() == world) {
        AtomicBoolean afk = new AtomicBoolean(false);
        try {
          srv.dispatchCommand(sender, "afkcheck " + p.getDisplayName());
          final String msg = sender.lastMessage.toLowerCase();
          afk.set(!msg.contains("unknown command") && !msg.contains("playing"));
        }
        catch (CommandException ignored) {}

        if (afk.get() || p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
          ++afkPlayers;
        }
        if (playersInBed.contains(p)) {
          ++sleepingPlayers;
          if (p.isOp()) {
            opSleeping.set(true);
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
    final boolean skipTheNight = neededPlayers <= 0 || (opsCanOverride && opSleeping.get());
    if (resetRequired(world)) {
      // only broadcast the message if we need more people and there are actually
      // people sleeping
      if (sleepingPlayers > 0) {
        final int playersUsedForCalculation = Math.max(neededPlayers, 0) + sleepingPlayers;
        final double perc = Math.min(100., (double) sleepingPlayers / (double) playersUsedForCalculation);
        if (!hasBossBar(world)) {
          createBossBar(world, playersInWorld);
        }
        BossBar bb = bossBars.get(world);
        bb.setTitle("Sleeping: " + sleepingPlayers + " out of " + totalNeededPlayers + " necessary");
        bb.setProgress(perc);
        if (opsCanOverride && opSleeping.get()) {
          bb.setColor(opBarColor);
        }
        else {
          bb.setColor(playerBarColor);
        }
      }
    }
    if (sleepingPlayers == 0 && hasBossBar(world)) {
      removeBossBar(world);
    }
    plugin.logger().fine("Status:");
    plugin.logger().fine("\tTotal players in the world: " + totalPlayersInWorld);
    plugin.logger().fine("\tSleeping players: " + sleepingPlayers);
    plugin.logger().fine("\tTotal needed players for skipping: " + totalNeededPlayers);
    plugin.logger().fine("\tTimer running:" + timer.isRunning());
    plugin.logger().fine("\tSkip the night:" + skipTheNight);
    if (skipTheNight && !timer.isRunning()) {
      timer.start();
      plugin.logger().fine("Starting sleep timer");
    }
    if (!skipTheNight && timer.isRunning()) {
      plugin.logger().fine("Cancelling sleep timer");
      timer.stop();
    }
  }

  public void resetDay(World world) {
    plugin.logger().fine("Reset to day");
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
    plugin.logger().fine("Night skip requested");
    // collect the blame messages before the skip so that people are still in bed
    final Vector<String> sleepMessages = collectSleepMessages(world);
    Bukkit.getScheduler().runTask(plugin, new Runnable()
    {
      // the weather clearing must be run in the main thread
      @Override
      public void run() {
        removeBossBar(world);
        removeTimer(world);
        if (resetRequired(world)) {
          resetDay(world);
          blame(world, sleepMessages);
        }
      }
    });
  }

  private Vector<String> collectSleepMessages(World world)
  {
    Vector<String> sleepers = new Vector<String>();
    if(this.blame)
    {
      for(Player p : playersInBed)
      {
        if(p.getWorld() == world)
        {
          sleepers.add(String.format("§6%s§e went to bed. Sweet Dreams.", p.getDisplayName()));
        }
      }
    }
    return sleepers;
  }

  private void blame(World world, Vector<String> sleepMessages) {
    if(!this.blame)
    {
      return;
    }
    for(Player p : world.getPlayers())
    {
      for(String msg : sleepMessages)
      {
        p.sendMessage(msg);
      }
    }
  }

  private NightSkipTimer getTimer(World world) {
    if (!nightSkipTimers.containsKey(world)) {
      nightSkipTimers.put(world, new NightSkipTimer(world, skipDelay, this));
    }
    return nightSkipTimers.get(world);
  }

  private boolean hasBossBar(World world) {
    return bossBars.containsKey(world);
  }

  private void createBossBar(World world, HashSet<Player> playersInWorld) {
    plugin.logger().fine("Creating bossbar");
    BossBar bb = plugin.getServer().createBossBar("Sleeping", playerBarColor, BarStyle.SOLID);
    bb.setProgress(0);
    for (Player p : playersInWorld) {
      bb.addPlayer(p);
    }
    bossBars.put(world, bb);
  }

  private void removeBossBar(World world) {
    if (hasBossBar(world)) {
      plugin.logger().fine("Removing bossbar");
      bossBars.get(world).removeAll();
      bossBars.remove(world);
    }
  }

  private void removeTimer(World world) {
    nightSkipTimers.remove(world);
  }

}
