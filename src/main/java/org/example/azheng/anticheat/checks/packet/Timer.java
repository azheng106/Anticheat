package org.example.azheng.anticheat.checks.packet;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;

import java.util.*;

public class Timer extends Check {
    public Timer(String name) {
        super(name);
    }
    private static final int INCREMENT = 50;

    private final HashSet<PacketTypeCommon> flyingPackets = new HashSet<>(Arrays.asList(
            PacketType.Play.Client.PLAYER_FLYING,
            PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
            PacketType.Play.Client.PLAYER_POSITION,
            PacketType.Play.Client.PLAYER_ROTATION
    ));

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        PacketTypeCommon type = e.getPacketType();
        if (!flyingPackets.contains(type)) return;

        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
        long now = System.currentTimeMillis();

        long elapsed = now - data.lastMs;
        data.lastMs = now;

        long newBalance = data.timerBalance + 50 - elapsed;

        if (newBalance > data.threshold) {
            data.threshold += INCREMENT; // Up the threshold every flag to prevent spamming the check
            flag(p, "too many flying packets");
        }
        data.timerBalance = Math.max(-800, newBalance); // Clamp from below to prevent accumulating a large negative balance w/ a timer speed < 1
        p.sendMessage("Balance: " + ChatColor.GREEN + data.timerBalance);
    }
}

//        long now = System.currentTimeMillis();
//        Player p = e.getPlayer();
//        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
//        if (now - data.lastWindowStart >= WINDOW_MS) {
//            int count = data.flyingsInWindow;
//            //p.sendMessage("# movement packets received in last second: " + count);
//            if (count >= FLYING_LIMIT) {
//                data.timerBuffer += count + 1 - FLYING_LIMIT;
//                p.sendMessage("Buffer: " + data.timerBuffer);
//                if (data.timerBuffer > 12) {
//                    flag(p, "Too many flying packets sent");
//                }
//            } else {
//                data.timerBuffer = Math.max(0, data.timerBuffer - 2);
//            }
//
//            data.lastWindowStart = now;
//            data.flyingsInWindow = 0;
//        }
//        data.flyingsInWindow++;
