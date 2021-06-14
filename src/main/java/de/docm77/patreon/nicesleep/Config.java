package de.docm77.patreon.nicesleep;

import java.lang.Class;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;

public final class Config {
  public enum Bar {
    Player, OP
  }

  private FileConfiguration config;
  private Logger logger;
  public Vector<String> settingKeys = new Vector<String>();
  private HashMap<String, Object> _values = new HashMap<String, Object>();

  public Config(FileConfiguration config, Logger logger) {
    this.config = config;
    this.logger = logger;
  }

  public void log() {
    logger.info("Settings:");
    logger.info("\tpercentage_needed: " + neededPercentage());
    logger.info("\tmax_players: " + maxPlayersNeeded());
    logger.info("\trounding_method: " + roundingMethod());
    logger.info("\tseconds_before_skip: " + skipDelaySeconds());
    logger.info("\tops_can_override: " + opsCanOverride());
    logger.info("\tbarcolor.player: " + barColor(Bar.Player));
    logger.info("\tbarcolor.op: " + barColor(Bar.OP));
  }

  public void load() {
    String tmp;
    HashMap<String, Object> values = new HashMap<String, Object>();

    int neededPercentage = loadValue("percentage_needed", Integer.class, 0);
    neededPercentage = Math.min(100, Math.max(0, neededPercentage));
    values.put("percentage_needed", neededPercentage);

    int maxPlayersNeeded = loadValue("max_players", Integer.class, 0);
    maxPlayersNeeded = Math.max(maxPlayersNeeded, 0);
    values.put("max_players", maxPlayersNeeded);

    double skipDelaySeconds = loadValue("seconds_before_skip", Double.class, 4.0);
    skipDelaySeconds = Math.min(15., Math.max(0., skipDelaySeconds));
    values.put("seconds_before_skip", skipDelaySeconds);

    boolean opsCanOverride = loadValue("ops_can_override", Boolean.class, false);
    values.put("ops_can_override", opsCanOverride);

    blame = loadValue("blame", Boolean.class, true);

    tmp = loadValue("rounding_method", String.class, "HALF_UP");
    RoundingMode roundingMethod = strToRoundingMode(tmp);
    values.put("rounding_method", roundingMethod);

    tmp = loadValue("barcolor.player", String.class, "WHITE");
    BarColor barColor = strToColor(tmp);
    values.put("barcolor.player", barColor);

    tmp = loadValue("barcolor.op", String.class, "WHITE");
    barColor = strToColor(tmp);
    values.put("barcolor.op", barColor);

    _values = values;
  }

  public void reload(FileConfiguration config) {
    this.config = config;
    load();
  }

  public String getStrValue(String key) {
    if(!settingKeys.contains(key)) { 
      return null;
    }
    return String.valueOf(_values.get(key));
  }

  public void setStrValue(String key, String value) {
    //unfortunately we need to know the type... java, am I right... ;)
    if(key == "percentage_needed") {
      setValue(key, Integer.valueOf(value));
    } else if(key == "max_players") {
      setValue(key, Integer.valueOf(value));
    } else if(key == "seconds_before_skip") {
      setValue(key, Double.valueOf(value));
    } else if(key == "max_players") {
      setValue(key, Boolean.valueOf(value));
    } else if(key == "rounding_method") {
      setValue(key, RoundingMode.valueOf(value));
    } else if(key.startsWith("barcolor")) {
      setValue(key, BarColor.valueOf(value));
    } else {
      logger.warning("key " + key + " not found");
    }
  }

  public int neededPercentage() { return getValue("percentage_needed", Integer.class); }
  public int maxPlayersNeeded() { return getValue("max_players", Integer.class); }
  public double skipDelaySeconds() { return getValue("seconds_before_skip", Double.class); }
  public boolean opsCanOverride() { return getValue("ops_can_override", Boolean.class); }
  public RoundingMode roundingMethod() { return getValue("rounding_method", RoundingMode.class); }
  public BarColor barColor(Bar which) {
    String key = which == Bar.Player ? "barcolor.player" : "barcolor.op";
    return getValue(key, BarColor.class);
  }

  private <T> T getValue(String path, Class<T> cl) {
    Object obj = _values.get(path);
    return cl.cast(obj);
  }

  private <T> boolean setValue(String path, T value) {
    if(!_values.containsKey(path)) {
      return false;
    }
    _values.put(path, value);
    return true;
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
    if (res == RoundingMode.UNNECESSARY) {
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
    if (!cl.isInstance(obj)) {
      logger.warning("Unable to cast " + path + " to " + cl + ", returning default value");
      return def;
    }
    return cl.cast(obj);
  }
}
