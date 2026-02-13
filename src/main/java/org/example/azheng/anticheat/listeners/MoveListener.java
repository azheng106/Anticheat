package org.example.azheng.anticheat.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.movement.NoFallA;
import org.example.azheng.anticheat.data.PlayerData;
import org.example.azheng.anticheat.utils.PlayerUtils;
import org.example.azheng.anticheat.utils.ReflectionUtils;

import java.util.Arrays;
import java.util.HashSet;


public class MoveListener implements PacketListener, Listener {
    private final HashSet<PacketTypeCommon> movePackets = new HashSet<>(Arrays.asList(
            PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
            PacketType.Play.Client.PLAYER_POSITION
    ));

    // Movement
    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
        PacketTypeCommon type = e.getPacketType();

        if (!movePackets.contains(type)) return;

        WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(e);

        double x = packet.getPosition().getX();
        double y = packet.getPosition().getY();
        double z = packet.getPosition().getZ();

        data.deltaXZ = Math.hypot(x - data.lastX, z - data.lastZ);
        data.deltaY = y - data.lastY;

        data.reduceVelocity();

        data.boundingBox = ReflectionUtils.getBoundingBox(p);
        data.nearGround = !ReflectionUtils.getCollidingBlocks(p, ReflectionUtils
                .modifyBoundingBox(data.boundingBox, 0, -1, 0, 0, 0, 0)).isEmpty();

        data.lastClientGround = data.clientGround;
        data.clientGround = PlayerUtils.isOnGround(p, 0.5) && y % NoFallA.blockGCD < 0.0001;
        data.onGround = PlayerUtils.isOnGround(p);

        data.speedPotionLevel = PlayerUtils.getPotionEffectLevel(p, PotionEffectType.SPEED);

        data.onStairSlab = PlayerUtils.isInStairs(p);
        data.inLiquid = PlayerUtils.isInLiquid(p);
        data.onIce = PlayerUtils.isOnIce(p);
        data.onClimbable = PlayerUtils.isOnClimbable(p);
        data.onSlime = PlayerUtils.isOnSlime(p);
        data.underBlock = PlayerUtils.isUnderBlock(p);

        if (packet.isOnGround()) {
            data.groundTicks++;
            data.airTicks = 0;
        } else {
            data.airTicks++;
            data.groundTicks = 0;
        }

        // Increment differently because different materials are more "aggressive"
        // Want to increase slime quickly because slime instantly causes bounciness/weird movement, so we want check exemptions to increase instantly
        data.iceTicks = Math.max(0, data.onIce ? Math.min(60, data.iceTicks + 3) : data.iceTicks - 1);
        data.slimeTicks = Math.max(0, data.onIce ? Math.min(60, data.slimeTicks + 8) : data.iceTicks - 1);
        data.underBlockTicks = Math.max(0, data.underBlock ? Math.min(60, data.underBlockTicks + 3)  : data.underBlockTicks - 1);
        data.liquidTicks = Math.max(0, data.onIce ? Math.min(40, data.liquidTicks + 1) : data.iceTicks - 1);

        data.lastX = x;
        data.lastY = y;
        data.lastZ = z;
    }

    // Velocity
    @Override
    public void onPacketSend(PacketSendEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
        PacketTypeCommon type = e.getPacketType();

        if (type == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity packet = new WrapperPlayServerEntityVelocity(e);
            data.velXTicks = (int) Math.round(packet.getVelocity().getX() * 100);
            data.velYTicks = (int) Math.round(packet.getVelocity().getY() * 100);
            data.velZTicks = (int) Math.round(packet.getVelocity().getZ() * 100);
        }
    }
}
