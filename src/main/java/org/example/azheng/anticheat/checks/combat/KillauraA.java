package org.example.azheng.anticheat.checks.combat;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;

import java.util.Arrays;
import java.util.HashSet;

public class KillauraA extends Check implements PacketListener {
    public KillauraA(String name) {
        super(name);
    }

    private final HashSet<PacketTypeCommon> desiredTypes = new HashSet<>(Arrays.asList(
            PacketType.Play.Client.PLAYER_FLYING,
            PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
            PacketType.Play.Client.PLAYER_POSITION,
            PacketType.Play.Client.PLAYER_ROTATION,
            PacketType.Play.Client.INTERACT_ENTITY
    ));

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(e.getPlayer());
        if (desiredTypes.contains(e.getPacketType())) {
            if (e.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                if (System.currentTimeMillis() - data.lastFlying < 5) {
                    if (data.killauraAVerbose++ > 10) {
                        flag(e.getPlayer(), "flying packet sent too late");
                    }
                } else {
                    data.killauraAVerbose = 0;
                }
            } else {
                data.lastFlying = System.currentTimeMillis();
            }
        }
    }
}
