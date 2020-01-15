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
    Config config = new Config(this.getConfig(), this.getLogger());
    config.load();
    config.log();
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
