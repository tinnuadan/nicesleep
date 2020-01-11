package de.docm77.patreon.nicesleep;

import org.bukkit.plugin.java.JavaPlugin;

final class Config {
  public int neededPercentage;
  public double skipDelaySeconds;
  public boolean opsCanOverride;

  public void log(JavaPlugin plugin) {
    plugin.getLogger().info("Settings:");
    plugin.getLogger().info("\tpercentage_needed: " + neededPercentage);
    plugin.getLogger().info("\tseconds_before_skip: " + skipDelaySeconds);
    plugin.getLogger().info("\tops_can_override: " + opsCanOverride);
  }
}