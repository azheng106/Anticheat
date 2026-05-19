package org.example.azheng.anticheat.checks.combat;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;

public class ReachA extends Check {
    public ReachA(String name) {
        super(name);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        if (e.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE) return;

        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);

        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(e);
        if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

        int id = packet.getEntityId();
        Entity target = SpigotConversionUtil.getEntityById(p.getWorld(), id);
        if (!(target instanceof LivingEntity) || target == p) return;

        double MAX_REACH = 3.1;

        // Attacker's eye coordinates
        double eyeX = data.lastX;
        double eyeY = data.lastY + (p.isSneaking() ? 1.54 : 1.62);
        double eyeZ = data.lastZ;

        Location targetLoc = target.getLocation();
        double half = 0.3 + 0.1; // Hitbox is 0.3 wide, add 0.1 for hitbox expansion

        double minX = targetLoc.getX() - half, maxX = targetLoc.getX() + half;
        double minY = targetLoc.getY() - 0.1, maxY = targetLoc.getY() + 1.8 + 0.1;
        double minZ = targetLoc.getZ() - half, maxZ = targetLoc.getZ() + half;

        // Get the points on target's hitbox that are closest to the attacker's eye
        double cx = Math.clamp(eyeX, minX, maxX);
        double cy = Math.clamp(eyeY, minY, maxY);
        double cz = Math.clamp(eyeZ, minZ, maxZ);

        double dx = eyeX - cx;
        double dy = eyeY - cy;
        double dz = eyeZ - cz;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist >= MAX_REACH) {
            data.reachABuffer++;
            if (data.reachABuffer >= 3) {
                flag(p, String.format("dist=%.3f, max=%.2f", dist, MAX_REACH));
            }
        } else {
            data.reachABuffer = Math.max(0, data.reachABuffer - 1);
        }
    }
}