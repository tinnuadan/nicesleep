package de.docm77.patreon.nicesleep;

import java.lang.Class;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;

public final class Config {
  public enum Bar {
    Player, OP
  }

  private FileConfiguration config;
  private Logger logger;
  public int neededPercentage = 0;
  public double skipDelaySeconds = 0.0;
  public boolean opsCanOverride = false;
  public RoundingMode roundingMethod = RoundingMode.HALF_UP;
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
    logger.info("\trounding_method: " + roundingMethod);
    logger.info("\tseconds_before_skip: " + skipDelaySeconds);
    logger.info("\tops_can_override: " + opsCanOverride);
    logger.info("\barcolor.player: " + barColors.get(Bar.Player));
    logger.info("\barcolor.op: " + barColors.get(Bar.OP));
  }

  public void load() {
    String tmp;

    neededPercentage = loadValue("percentage_needed", Integer.class, 0);
    neededPercentage = Math.min(100, Math.max(0, neededPercentage));

    skipDelaySeconds = loadValue("seconds_before_skip", Double.class, 4.0);
    skipDelaySeconds = Math.min(15., Math.max(0., skipDelaySeconds));

    opsCanOverride = loadValue("ops_can_override", Boolean.class, false);

    tmp = loadValue("rounding_method", String.class, "ROUND");
    roundingMethod = strToRoundingMode(tmp);

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

  private RoundingMode strToRoundingMode(String method) {
    RoundingMode res = RoundingMode.HALF_UP;
    try {
      res = RoundingMode.valueOf(method.toUpperCase());
    } catch (IllegalArgumentException e) {
      logger.warning("Unable to convert " + method + " into a rounding method. Falling back to 'HALF_UP'");
    }
    if(res == RoundingMode.UNNECESSARY) {
      logger.warning("Rounding mode 'UNNECESSARY' is not allowed. Falling back to 'HALF_UP'");
      res = RoundingMode.HALF_UP;
    }
    return res;
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