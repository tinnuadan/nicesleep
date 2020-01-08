package de.docm77.patreon.nicesleep;

import org.bukkit.plugin.java.JavaPlugin;

public class NiceSleep extends JavaPlugin {

    private PlayerBedEventListener _bedEventListener;

    public NiceSleep()
    {
        _bedEventListener = null;
    }

    @Override
    public void onEnable() {
        _bedEventListener = new PlayerBedEventListener(this);
        getLogger().info("NiceSleep enabled!");
    }
    @Override
    public void onDisable() {
        getLogger().info("NiceSleep disabled!");
    }
}
