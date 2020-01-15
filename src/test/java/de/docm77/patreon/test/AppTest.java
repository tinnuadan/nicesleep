package de.docm77.patreon.test;

import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.docm77.patreon.nicesleep.Config;
import de.docm77.patreon.nicesleep.Config.Bar;

/**
 * Unit test for simple App.
 */
public class AppTest {
  /**
   * Rigorous Test.
   */
  @Test
  public void testApp() {
    testConfig();
  }

  public void testConfig() {
    Logger log = Logger.getLogger("my.logger");
    log.setLevel(Level.ALL);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(new SimpleFormatter());
    handler.setLevel(Level.ALL);
    log.addHandler(handler);
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("config.yml").getFile());
    FileConfiguration fconfig = YamlConfiguration.loadConfiguration(file);

    Config config = new Config(fconfig, log);
    config.load();

    assertEquals(55, config.neededPercentage);
    assertEquals(4.1, config.skipDelaySeconds, 0.1);
    assertEquals(true, config.opsCanOverride);
    assertEquals(BarColor.BLUE, config.barColors.get(Bar.Player));
    assertEquals(BarColor.PINK, config.barColors.get(Bar.OP));
  }
}
