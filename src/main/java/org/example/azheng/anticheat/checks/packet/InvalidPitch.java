package org.example.azheng.anticheat.checks.packet;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.checks.Check;

public class InvalidPitch extends Check {
    public InvalidPitch(String name) {
        super(name);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        Player p = e.getPlayer();
        PacketTypeCommon type = e.getPacketType();

        if (type == PacketType.Play.Client.PLAYER_ROTATION ||
        type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(e);
            float pitch = packet.getLocation().getPitch();

            if (Math.abs(pitch) > 90) {
                flag(p, "invalid pitch");
            }
        }
    }
}
