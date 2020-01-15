package de.docm77.patreon.nicesleep;

import java.lang.Class;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Config {
  public enum Bar {
    Player, OP
  }

  private FileConfiguration config;
  private Logger logger;
  public int neededPercentage = 0;
  public double skipDelaySeconds = 0.0;
  public boolean opsCanOverride = false;
  public HashMap<Bar, BarColor> barColors;

  public Config(FileConfiguration config, Logger logger) {
    this.config = config;
    this.logger = logger;
    barColors = new HashMap<Bar, BarColor>();
    barColors.put(Bar.Player, BarColor.WHITE);
    barColors.put(Bar.OP, BarColor.WHITE);
  }

  public void log() {
    logger.info("Settings:");
    logger.info("\tpercentage_needed: " + neededPercentage);
    logger.info("\tseconds_before_skip: " + skipDelaySeconds);
    logger.info("\tops_can_override: " + opsCanOverride);
  }

  public void load() {
    neededPercentage = loadValue("percentage_needed", Integer.class, 0);
    neededPercentage = Math.min(100, Math.max(0, neededPercentage));

    skipDelaySeconds = loadValue("seconds_before_skip", Double.class, 4.0);
    skipDelaySeconds = Math.min(15., Math.max(0., skipDelaySeconds));

    opsCanOverride = loadValue("ops_can_override", Boolean.class, false);

    String tmp;
    tmp = loadValue("barcolor.player", String.class, "WHITE");
    barColors.put(Bar.Player, strToColor(tmp));
    tmp = loadValue("barcolor.op", String.class, "WHITE");
    barColors.put(Bar.OP, strToColor(tmp));
  }

  private BarColor strToColor(String color) {
    BarColor bc = BarColor.WHITE;
    try {
      bc = BarColor.valueOf(color.toUpperCase());
    } catch (IllegalArgumentException e) {
      logger.warning("Unable to convert " + color + " into a bar color. Falling back to white");
    }
    return bc;
  }

  private <T> T loadValue(String path, Class<T> cl, T def) {
    Object obj = config.get(path, null);
    if (obj == null) {
      logger.info("Config for " + path + " not found, using default value (" + def + ")");
      return def;
    }
    if(!cl.isInstance(obj)) {
      logger.warning("Unable to cast " + path + " to " + cl + ", returning default value");
      return def;
    }
    return cl.cast(obj);
  }
}