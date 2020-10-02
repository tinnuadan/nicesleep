package de.docm77.patreon.nicesleep;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {

  private final JavaPlugin plugin;
  private final Logger debugLogger;
  private Level logLevel = Level.INFO;

  public LoggerUtil(JavaPlugin plugin) {
    this.plugin = plugin;
    debugLogger = Logger.getLogger(this.plugin.getName() + "-debug");
  }

  public void log(Level level, String msg) {
    if (level.intValue() < logLevel.intValue()) {
      return;
    }
    if (level.intValue() < Level.INFO.intValue()) {
      debugLogger.info(msg);
      return;
    }
    this.plugin.getLogger().info(msg);
  }

  public void warning(String msg) {
    log(Level.WARNING, msg);
  }

  public void info(String msg) {
    log(Level.INFO, msg);
  }

  public void fine(String msg) {
    log(Level.FINE, msg);
  }

  public Level getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(Level logLevel) {
    this.logLevel = logLevel;
  }

  public void startUpText() {
    String[] startupText = {
      "§2    ___§3  __",
      "§2   /__§3  /__)   §NiceSleep - v" + this.plugin.getDescription().getVersion(),
      "§2  /  §3  /       §tinnuadan",
      ""
    };
    Arrays.stream(startupText).forEach(Bukkit.getServer().getConsoleSender()::sendMessage);
  }

}
