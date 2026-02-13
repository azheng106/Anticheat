package org.example.azheng.anticheat.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.data.PlayerData;
import org.example.azheng.anticheat.utils.EvictingList;
import org.example.azheng.anticheat.utils.MathUtils;

import java.util.LinkedList;

public class RotationListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
        PacketTypeCommon type = e.getPacketType();

        if (type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
        || type == PacketType.Play.Client.PLAYER_ROTATION) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(e);
            float yaw = packet.getLocation().getYaw();
            float pitch = packet.getLocation().getPitch();

            data.deltaYaw = Math.abs(MathUtils.angleDiff(yaw, data.lastYaw));
            data.deltaPitch = Math.abs(pitch - data.lastPitch);

            float yawGcd = (data.lastYawGcd == 0) ? data.deltaYaw : MathUtils.gcd(data.deltaYaw, data.lastDeltaYaw);
            float pitchGcd = (data.lastPitchGcd == 0) ? data.deltaPitch : MathUtils.gcd(data.deltaPitch, data.lastDeltaPitch);

            if (yawGcd >= 0.0096 && yawGcd <= 0.65) {
                data.yawGcdList.add(yawGcd);
            }
            if (pitchGcd >= 0.0096 && pitchGcd <= 0.65) {
                data.pitchGcdList.add(pitchGcd);
            }

            if (data.yawGcdList.size() > 25) {
                data.yawGcd = MathUtils.getMode(data.yawGcdList);
            }
            if (data.pitchGcdList.size() > 25) {
                data.pitchGcd = MathUtils.getMode(data.pitchGcdList);
            }

            data.lastYaw = yaw;
            data.lastPitch = pitch;
            data.lastDeltaYaw = data.deltaYaw;
            data.lastDeltaPitch = data.deltaPitch;
            data.lastYawGcd = yawGcd;
            data.lastPitchGcd = pitchGcd;
        }
    }
}
