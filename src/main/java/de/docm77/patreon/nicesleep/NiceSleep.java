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
    Config config = new Config();
    config.neededPercentage = Math.min(100, Math.max(0, getConfig().getInt("percentage_needed")));
    config.skipDelaySeconds = Math.max(0.0, getConfig().getDouble("seconds_before_skip"));
    config.opsCanOverride = getConfig().getBoolean("ops_can_override");
    config.log(this);
    nightSkip = new NightSkip(this, config);
    bedEventListener = new PlayerBedEventListener(this, nightSkip);
    changedWorldEventListener = new PlayerChangedWorldEventListener(this, nightSkip);
    getLogger().info("NiceSleep enabled!");
  }

  @Override
  public void onDisable() {
    getLogger().info("NiceSleep disabled!");
  }
}
