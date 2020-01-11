package de.docm77.patreon.nicesleep;

import java.util.HashMap;

import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

final class Config {
  public enum Bar {
    Player, OP
  }
  public int neededPercentage;
  public double skipDelaySeconds;
  public boolean opsCanOverride;
  public HashMap<Bar, BarColor> barColors;

  public void log(JavaPlugin plugin) {
    plugin.getLogger().info("Settings:");
    plugin.getLogger().info("\tpercentage_needed: " + neededPercentage);
    plugin.getLogger().info("\tseconds_before_skip: " + skipDelaySeconds);
    plugin.getLogger().info("\tops_can_override: " + opsCanOverride);
  }

  public void load(JavaPlugin plugin) {
    barColors = new HashMap<Bar, BarColor>();
    FileConfiguration config = plugin.getConfig();
    neededPercentage = Math.min(100, Math.max(0, config.getInt("percentage_needed")));
    skipDelaySeconds = Math.max(0.0, config.getDouble("seconds_before_skip"));
    opsCanOverride = config.getBoolean("ops_can_override");
    barColors.put(Bar.Player, BarColor.BLUE);
    barColors.put(Bar.OP, BarColor.PINK);
  }
}