package org.example.azheng.anticheat.checks.movement;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;
import org.example.azheng.anticheat.utils.PlayerUtils;

import java.util.Arrays;
import java.util.HashSet;

public class SpeedA extends Check {
    public SpeedA(String name) {
        super(name);
    }

    // Movement packets
    private final HashSet<PacketTypeCommon> desiredTypes = new HashSet<>(Arrays.asList(
            PacketType.Play.Client.PLAYER_FLYING,
            PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
            PacketType.Play.Client.PLAYER_POSITION,
            PacketType.Play.Client.PLAYER_ROTATION
    ));

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        if (!desiredTypes.contains(e.getPacketType())) return;

        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);

        if (p.getAllowFlight() || p.isInsideVehicle() || data.isVelocityTaken()) {
            return;
        }

        // Flying client wrapper always exposes onGround state.
        // Position fields are only present for POSITION or ROTATION packets so we must check for existence of these
        WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(e);

        boolean clientGround = flying.isOnGround();
        boolean hasPosition = e.getPacketType() == PacketType.Play.Client.PLAYER_POSITION
                || e.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION; // PLAYER_POSITION or PLAYER_POSITION_AND_ROTATION has position data

        data.lastClientGround = data.clientGround;
        data.clientGround = clientGround;

        if (!hasPosition) { // Skip check if no position data
            return;
        }

        double x = flying.getLocation().getX();
        double z = flying.getLocation().getZ();
        double y = flying.getLocation().getY();

        double deltaXZ = Math.hypot(x - data.lastX, z - data.lastZ);
        double deltaY = y - data.lastY;

        float threshold = data.clientGround ? 0.31f : 0.341f;
        int speedLevel = PlayerUtils.getPotionEffectLevel(p, PotionEffectType.SPEED);

        threshold += (data.slimeTicks > 0) ? 0.07f : 0;
        threshold += (data.groundTicks < 5) ? speedLevel * 0.06f : speedLevel * 0.046f;

        if (data.onStairSlab) threshold *= 1.8f;
        if (data.iceTicks > 0 && data.groundTicks < 5) threshold *= 1.7f;
        if (data.underBlockTicks > 0 && deltaY != 0.0) threshold *= 2.0f; // directly under a block while spamming jump makes u go much faster

        if (deltaXZ > threshold) {
            data.speedABuffer += 1;
            if (data.speedABuffer > 13) {
                flag(p, String.format("dxz=%.3f, threshold=%.3f, b=" + data.speedABuffer, deltaXZ, threshold));
            }
        } else {
            data.speedABuffer = Math.max(0, data.speedABuffer -= 1);
        }

        data.lastX = x;
        data.lastY = y;
        data.lastZ = z;
    }
}
