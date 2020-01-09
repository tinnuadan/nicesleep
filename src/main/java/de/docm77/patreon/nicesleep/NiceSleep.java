package de.docm77.patreon.nicesleep;

import org.bukkit.plugin.java.JavaPlugin;

public class NiceSleep extends JavaPlugin {

  private PlayerBedEventListener bedEventListener;
  private PlayerChangedWorldEventListener changedWorldEventListener;
  private NightSkip nightSkip;

  public NiceSleep() {
    bedEventListener = null;
    changedWorldEventListener = null;
    nightSkip = null;
  }

  @Override
  public void onEnable() {
    int percentageNeeded = Math.min(100, Math.max(0, getConfig().getInt("percentage_needed")));
    double sBeforeSkip = Math.max(0.0, getConfig().getDouble("seconds_before_skip"));
    nightSkip = new NightSkip(this, percentageNeeded, sBeforeSkip);
    bedEventListener = new PlayerBedEventListener(this, nightSkip);
    changedWorldEventListener = new PlayerChangedWorldEventListener(this, nightSkip);
    getLogger().info("NiceSleep enabled!");
  }

  @Override
  public void onDisable() {
    getLogger().info("NiceSleep disabled!");
  }
}
