package de.docm77.patreon.nicesleep;

import javax.swing.Timer;

import org.bukkit.World;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NightSkipTimer implements ActionListener {

  private final World world;
  private final Timer timer;
  private final NightSkipEventHandler skipEventHandler;

  NightSkipTimer(World world, int delay, NightSkipEventHandler skipEvent) {
    this.world = world;
    this.timer = new Timer(delay, this);
    this.timer.setRepeats(false);
    this.skipEventHandler = skipEvent;
  }

  void start() {
    timer.start();
  }

  void stop() {
    timer.stop();
  }

  boolean isRunning() {
    return timer.isRunning();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    skipEventHandler.nightSkipped(world);
  }

}
