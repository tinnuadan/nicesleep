package de.docm77.patreon.nicesleep;

import org.bukkit.World;
import org.bukkit.entity.Player;

public interface PlayerWorldChangeEventHandler {
  public void worldChanged(Player player, World from, World to);
}