package com.oni.proj;

import com.oni.proj.listeners.ProjectileTracker;
import org.bukkit.plugin.java.JavaPlugin;

public final class OniProj extends JavaPlugin {

    private ProjectileTracker tracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.tracker = new ProjectileTracker(this);
        getServer().getPluginManager().registerEvents(tracker, this);
        // Tick every game tick to expand-detect hits
        getServer().getScheduler().runTaskTimer(this, tracker::tick, 1L, 1L);
        getLogger().info("\u9B3C OniProj enabled \u00B7 expanded hitboxes for windcharge + pearl");
    }

    public double getPearlRadius() {
        return getConfig().getDouble("pearl.radius", 0.6);
    }

    public double getWindChargeRadius() {
        return getConfig().getDouble("windcharge.radius", 1.5);
    }

    public boolean pearlEnabled() {
        return getConfig().getBoolean("pearl.enabled", true);
    }

    public boolean windChargeEnabled() {
        return getConfig().getBoolean("windcharge.enabled", true);
    }
}
