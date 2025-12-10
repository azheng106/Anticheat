package org.example.azheng.anticheat.checks.movement;

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

public class NoFallB extends Check implements Listener {
    public NoFallB(String name) {
        super(name);
    }

    private static final double MIN_FALL_DISTANCE = 3.0; // blocks before damage
    private static final long DAMAGE_WAIT_MS = 500;      // how long we wait for fall damage

    private static class FallData {
        double fallDistance;
        boolean wasOnGround;
        boolean expectingDamage;
        long expectDamageSince;
    }

    // UUID = unique ID assigned to each Minecraft player
    private final Map<UUID, FallData> fallDataMap = new HashMap<>();

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

        // If landing on a soft block, cancel the check and reset FallData
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

        // On ground now, and not in ground last tick means we just landed
        if (serverGround && !wasGround) {
            // just landed, and should take fall damage soon if we fell over MIN_FALL_DIST blocks
            if (fd.fallDistance >= MIN_FALL_DISTANCE) {
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

    private boolean isSoftLandingBlock(Block block) {
        Material type = block.getType();

        // blocks that mitigate fall damage
        return type == Material.WATER
                || type == Material.STATIONARY_WATER
                || type == Material.WEB
                || type == Material.SLIME_BLOCK
                || type == Material.HAY_BLOCK;

    }

    private boolean isOnGroundServer(Location loc) {
        Block below = loc.clone().subtract(0, 0.05, 0).getBlock();
        Material type = below.getType();

        if (type == Material.AIR) return false;

        if (type == Material.WATER
                || type == Material.STATIONARY_WATER
                || type == Material.SLIME_BLOCK
                || type == Material.WEB
                || type == Material.HAY_BLOCK) {
            return false;
        }

        return type.isSolid();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        FallData fd = getData(p);

        // If player takes fall damage, set expectingDamage to false
        fd.expectingDamage = false;
        fd.fallDistance = 0;
    }
}

