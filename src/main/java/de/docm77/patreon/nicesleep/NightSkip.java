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

  private Config config;
  private HashSet<Player> playersInBed;
  private HashMap<World, NightSkipTimer> nightSkipTimers;
  private HashMap<World, BossBar> bossbars;
  private final BasePlugin plugin;

  public NightSkip(BasePlugin plugin, Config config) {
    this.plugin = plugin;
    this.config = config;
    this.playersInBed = new HashSet<Player>();
    this.nightSkipTimers = new HashMap<World, NightSkipTimer>();
    this.bossbars = new HashMap<World, BossBar>();
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
      this.bossbars.get(from).removePlayer(player);
    }
    if (hasBossBar(to)) {
      this.bossbars.get(to).addPlayer(player);
    }
  }

  private void checkSleeping(World world) {
    // get settings
    final double neededPercentage = ((double) config.neededPercentage()) / 100.;
    final int maxPlayersNeeded = config.maxPlayersNeeded();
    final RoundingMode roundingMethod = config.roundingMethod();
    final int skipDelay = (int) (Math.round(config.skipDelaySeconds() * 1000.0));
    final boolean opsCanOverride = config.opsCanOverride();
    final BarColor playerBarColor = config.barColor(Config.Bar.Player);
    final BarColor opBarColor = config.barColor(Config.Bar.OP);

    // initialize vars
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

        if (afk.get() || p.getGameMode() == GameMode.SPECTATOR) {
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
    totalNeededPlayers = Math.min(activePlayers, Math.max(totalNeededPlayers, 1)); // we need at least one in total
    if(maxPlayersNeeded > 0) { // if a limit is set
      totalNeededPlayers = Math.min(totalNeededPlayers, maxPlayersNeeded);
    }
    final int neededPlayers = totalNeededPlayers - sleepingPlayers;

    NightSkipTimer timer = getTimer(world, skipDelay);
    final boolean skipTheNight = neededPlayers <= 0 || (opsCanOverride && opSleeping.get());
    if (resetRequired(world)) {
      // only broadcast the message if we need more people and there are actually
      // people sleeping
      if (sleepingPlayers > 0) {
        final int playersUsedForCalculation = Math.max(neededPlayers, 0) + sleepingPlayers;
        final double perc = Math.min(100., (double) sleepingPlayers / (double) playersUsedForCalculation);
        final BarColor barColor = (opsCanOverride && opSleeping.get()) ? opBarColor : playerBarColor;
        if (!hasBossBar(world)) {
          createBossBar(world, playersInWorld, barColor);
        }
        BossBar bb = this.bossbars.get(world);
        bb.setTitle("Sleeping: " + sleepingPlayers + " out of " + totalNeededPlayers + " necessary");
        bb.setProgress(perc);
        bb.setColor(barColor);
      }
    }
    if (sleepingPlayers == 0 && hasBossBar(world)) {
      removeBossBar(world);
    }
    plugin.logger().fine("Status:");
    plugin.logger().fine("\tTotal players in the world: " + totalPlayersInWorld);
    plugin.logger().fine("\tActive players: " + activePlayers);
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
    if(this.config.blame())
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
    if(!this.config.blame())
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

  private NightSkipTimer getTimer(World world, int skipDelay) {
    if (!nightSkipTimers.containsKey(world)) {
      nightSkipTimers.put(world, new NightSkipTimer(world, skipDelay, this));
    }
    return nightSkipTimers.get(world);
  }

  private boolean hasBossBar(World world) {
    return this.bossbars.containsKey(world);
  }

  private BossBar createBossBar(World world, HashSet<Player> playersInWorld, BarColor barColor) {
    plugin.getLogger().fine("Creating bossbar");
    BossBar bb = plugin.getServer().createBossBar("Sleeping", barColor, BarStyle.SOLID);
    bb.setProgress(0);
    for (Player p : playersInWorld) {
      bb.addPlayer(p);
    }
    this.bossbars.put(world, bb);
    return bb;
  }

  private void removeBossBar(World world) {
    if (hasBossBar(world)) {
      plugin.logger().fine("Removing bossbar");
      this.bossbars.get(world).removeAll();
      this.bossbars.remove(world);
    }
  }

  private void removeTimer(World world) {
    nightSkipTimers.remove(world);
  }

}
