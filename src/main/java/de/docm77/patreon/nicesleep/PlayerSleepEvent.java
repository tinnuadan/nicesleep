package de.docm77.patreon.nicesleep;

import org.bukkit.entity.Player;

public interface PlayerSleepEvent {
  public void playerEnteredBed(Player player);

  public void playerLeftBed(Player player);
}