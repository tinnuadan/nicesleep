package de.docm77.patreon.nicesleep;

import javax.swing.Timer;

import org.bukkit.World;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NightSkipTimer implements ActionListener {

  private World world;
  private Timer timer;
  private NightSkipEvent skipEvent;

  NightSkipTimer(World world, int delay, NightSkipEvent skipEvent) {
    this.world = world;
    this.timer = new Timer(delay, this);
    this.timer.setRepeats(false);
    this.skipEvent = skipEvent;
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
    skipEvent.nightSkipped(world);
  }

}