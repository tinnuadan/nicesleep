package de.docm77.patreon.nicesleep;

import org.bukkit.plugin.java.JavaPlugin;

public class NiceSleep extends BasePlugin {

  private PlayerBedEventListener bedEventListener;
  private PlayerChangedWorldEventListener changedWorldEventListener;
  private NightSkip nightSkip;
  private DebugCmd debugCmd;
  private LoggerUtil loggerUtil;

  public NiceSleep() {
    bedEventListener = null;
    changedWorldEventListener = null;
    nightSkip = null;
    debugCmd = null;
    loggerUtil = null;
  }

  @Override
  public void onEnable() {
    this.saveDefaultConfig();
    // default implentation of the logger
    Config config = new Config(this.getConfig(), this.getLogger());
    config.load();
    config.log();

    this.loggerUtil = new LoggerUtil(this);
    debugCmd = new DebugCmd(this.loggerUtil);
    this.getCommand("debug").setExecutor(debugCmd);
    nightSkip = new NightSkip(this, config);
    bedEventListener = new PlayerBedEventListener(this, nightSkip);
    changedWorldEventListener = new PlayerChangedWorldEventListener(this, nightSkip);
    logger().info("NiceSleep enabled!");
  }

  @Override
  public void onDisable() {
    logger().info("NiceSleep disabled!");
  }

  public LoggerUtil logger()
  {
    return this.loggerUtil;
  }

}
