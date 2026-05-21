package com.oni.proj.listeners;

import com.oni.proj.OniProj;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.BoundingBox;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks projectiles we want to "expand" the hitbox for. Each tick, we
 * check for nearby living entities within the configured radius. If we
 * find one, we teleport the projectile onto them so the vanilla collision
 * fires immediately.
 *
 * - Ender pearl: small radius so teleports trigger from glancing hits.
 * - Wind charge: larger radius so close shots count as direct knockback.
 */
public final class ProjectileTracker implements Listener {

    private final OniProj plugin;
    private final Set<UUID> tracked = new HashSet<>();

    public ProjectileTracker(OniProj plugin) { this.plugin = plugin; }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        Projectile proj = e.getEntity();
        if (proj instanceof EnderPearl && plugin.pearlEnabled()) {
            tracked.add(proj.getUniqueId());
        } else if (proj instanceof WindCharge && plugin.windChargeEnabled()) {
            tracked.add(proj.getUniqueId());
        }
    }

    public void tick() {
        if (tracked.isEmpty()) return;
        tracked.removeIf(uuid -> {
            Entity ent = org.bukkit.Bukkit.getEntity(uuid);
            if (ent == null || ent.isDead() || !(ent instanceof Projectile proj)) return true;

            double r;
            if (proj instanceof EnderPearl) r = plugin.getPearlRadius();
            else if (proj instanceof WindCharge) r = plugin.getWindChargeRadius();
            else return true;

            Location loc = proj.getLocation();
            BoundingBox area = BoundingBox.of(loc, r, r, r);

            Player shooter = null;
            if (proj.getShooter() instanceof Player p) shooter = p;

            for (Entity nearby : proj.getWorld().getNearbyEntities(area)) {
                if (!(nearby instanceof LivingEntity victim)) continue;
                if (shooter != null && victim.equals(shooter)) continue;
                if (victim.equals(proj)) continue;

                // Snap projectile onto victim so vanilla collision fires next tick.
                proj.teleport(victim.getEyeLocation());
                return true;  // stop tracking; vanilla handles it from here
            }
            return false;
        });
    }
}
