package org.example.azheng.anticheat.checks.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NoFallB extends Check implements Listener {

    private static final double MIN_FALL_DISTANCE = 3.0; // blocks before damage
    private static final long DAMAGE_WAIT_MS = 500;      // how long we wait for fall damage

    private static class FallData {
        double fallDistance;
        boolean wasOnGround;
        boolean expectingDamage;
        long expectDamageSince;
    }

    private final Map<UUID, FallData> fallDataMap = new HashMap<>();

    public NoFallB(String name, boolean enabled) {
        super(name, enabled);
    }

    private FallData getData(Player p) {
        return fallDataMap.computeIfAbsent(p.getUniqueId(), id -> new FallData());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(player);
        FallData fd = getData(player);

        // Basic exemptions
        if (player.getAllowFlight()
                || player.isInsideVehicle()
                || data.isVelocityTaken()) {
            fd.fallDistance = 0;
            fd.wasOnGround = true;
            fd.expectingDamage = false;
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        Block currentBlock = to.clone().subtract(0, 0.05, 0).getBlock();

        // If we're in a soft landing block (water, slime, cobweb, etc),
        // treat this as "fall cancelled" and reset.
        if (isSoftLandingBlock(currentBlock)) {
            fd.fallDistance = 0;
            fd.expectingDamage = false;
            fd.wasOnGround = false; // still "in fluid", not solid ground
            return;
        }

        double dy = to.getY() - from.getY();

        boolean serverGround = isOnGroundServer(to);
        boolean wasGround = fd.wasOnGround;

        // Accumulate fall distance when we're falling
        if (!serverGround && dy < 0) {
            fd.fallDistance -= dy;
        }

        // Landing detection: in air last tick, now on solid ground
        if (serverGround && !wasGround) {
            // We just landed
            if (fd.fallDistance >= MIN_FALL_DISTANCE) {
                // We should receive a FALL damage event very soon
                fd.expectingDamage = true;
                fd.expectDamageSince = System.currentTimeMillis();
            }
            fd.fallDistance = 0;
        }

        // if expecting damage but didn't get any in time, flag NoFall
        if (fd.expectingDamage) {
            long now = System.currentTimeMillis();
            if (now - fd.expectDamageSince > DAMAGE_WAIT_MS) {
                flag(player,
                        "fd=" + fd.fallDistance,
                        ", y=" + to.getY());

                fd.expectingDamage = false; // avoid spamming
            }
        }

        fd.wasOnGround = serverGround;
    }

    /**
     * Server-side ground check using landing block
     */
    private boolean isOnGroundServer(Location loc) {
        // Slightly below feet to avoid rounding issues
        Block below = loc.clone().subtract(0, 0.05, 0).getBlock();
        Material type = below.getType();

        if (type == Material.AIR) return false;

        // Ignore legit "soft landing" blocks that prevent damage
        if (type == Material.WATER
                || type == Material.LAVA
                || type == Material.SLIME_BLOCK
                || type == Material.LADDER
                || type == Material.VINE
                || type == Material.WEB
                || type == Material.HAY_BLOCK) {
            return false;
        }

        return type.isSolid();
    }

    private boolean isSoftLandingBlock(Block block) {
        Material type = block.getType();

        // blocks that mitigate fall damage
        return type == Material.WATER
                || type == Material.STATIONARY_WATER
                || type == Material.WEB
                || type == Material.SLIME_BLOCK
                || type == Material.HAY_BLOCK;

    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player p = (Player) event.getEntity();
        FallData fd = getData(p);

        // If player takes fall damage, set expectingDamage to false
        fd.expectingDamage = false;
        fd.fallDistance = 0;
    }
}

