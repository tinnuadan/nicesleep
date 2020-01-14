package de.docm77.patreon.nicesleep;

import java.lang.Class;
import java.util.HashMap;

import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

final class Config {
  public enum Bar {
    Player, OP
  }

  private JavaPlugin plugin;
  public int neededPercentage;
  public double skipDelaySeconds;
  public boolean opsCanOverride;
  public HashMap<Bar, BarColor> barColors;

  Config(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void log() {
    plugin.getLogger().info("Settings:");
    plugin.getLogger().info("\tpercentage_needed: " + neededPercentage);
    plugin.getLogger().info("\tseconds_before_skip: " + skipDelaySeconds);
    plugin.getLogger().info("\tops_can_override: " + opsCanOverride);
  }

  public void load() {
    barColors = new HashMap<Bar, BarColor>();
    FileConfiguration config = plugin.getConfig();

    neededPercentage = loadValue(config, "percentage_needed", int.class, 0);
    neededPercentage = Math.min(100, Math.max(0, neededPercentage));

    skipDelaySeconds = loadValue(config, "seconds_before_skip", double.class, 4.0);
    skipDelaySeconds = Math.min(15., Math.max(0., skipDelaySeconds));

    opsCanOverride = loadValue(config, "ops_can_override", boolean.class, false);

    String tmp;
    tmp = loadValue(config, "barcolor.player", String.class, "WHITE");
    barColors.put(Bar.Player, strToColor(tmp));
    tmp = loadValue(config, "barcolor.op", String.class, "WHITE");
    barColors.put(Bar.OP, strToColor(tmp));
  }

  private BarColor strToColor(String color) {
    BarColor bc = BarColor.WHITE;
    try {
      bc = BarColor.valueOf(color.toUpperCase());
    } catch (IllegalArgumentException e) {
      plugin.getLogger().warning("Unable to convert " + color + " into a bar color. Falling back to white");
    }
    return bc;
  }

  private <T> T loadValue(FileConfiguration config, String path, Class<T> cl, T def) {
    if (config.get(path, null) == null) {
      plugin.getLogger().info("Config for " + path + " not found, using default value (" + def + ")");
      return def;
    }
    return config.getObject(path, cl);
  }
}