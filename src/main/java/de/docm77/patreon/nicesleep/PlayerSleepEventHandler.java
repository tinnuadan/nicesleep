package de.docm77.patreon.nicesleep;

import org.bukkit.entity.Player;

public interface PlayerSleepEventHandler {

  public void playerEnteredBed(Player player);

  public void playerLeftBed(Player player);

}