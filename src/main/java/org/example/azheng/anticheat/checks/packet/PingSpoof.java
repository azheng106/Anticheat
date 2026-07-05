package org.example.azheng.anticheat.checks.packet;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Detects ping spoofing: mods that delay ONLY the latency probe (KeepAlive) so the
 * server's measured ping inflates while the player keeps moving smoothly.
 *
 * Signature: high KeepAlive round-trip time, but many flying packets still arrived
 * during that same window. Real lag delays everything uniformly, so it can't produce
 * "laggy probe + smooth movement".
 */
public class PingSpoof extends Check {
    public PingSpoof(String name) {
        super(name);
    }

    private static final long RTT_THRESHOLD_MS = 120;
    private static final int  FLYING_THRESHOLD = 4;

    private final HashSet<PacketTypeCommon> flyingPackets = new HashSet<>(Arrays.asList(
            PacketType.Play.Client.PLAYER_FLYING,
            PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
            PacketType.Play.Client.PLAYER_POSITION,
            PacketType.Play.Client.PLAYER_ROTATION
    ));

    @Override
    public void onPacketSend(PacketSendEvent e) {
        if (e.getPacketType() != PacketType.Play.Server.KEEP_ALIVE) return;

        Player p = e.getPlayer();
        if (p == null) return;
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
        if (data == null) return;

        long id = new WrapperPlayServerKeepAlive(e).getId();
        data.keepAliveSendTimes.put(id, System.currentTimeMillis());
        data.flyingSinceKeepAlive = 0; // reset the window as the probe goes out
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        PacketTypeCommon type = e.getPacketType();

        Player p = e.getPlayer();
        if (p == null) return;
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
        if (data == null) return;

        if (flyingPackets.contains(type)) {
            data.flyingSinceKeepAlive++;
            return;
        }
        if (type != PacketType.Play.Client.KEEP_ALIVE) return;

        long id = new WrapperPlayClientKeepAlive(e).getId();
        Long sent = data.keepAliveSendTimes.remove(id);
        if (sent == null) return;

        long rtt = System.currentTimeMillis() - sent;
        data.lastKeepAliveRtt = rtt;

        if (rtt > RTT_THRESHOLD_MS && data.flyingSinceKeepAlive > FLYING_THRESHOLD) {
            data.pingSpoofBuffer++;
            if (data.pingSpoofBuffer > 5) {
                flag(p, "rtt=" + rtt + "ms but " + data.flyingSinceKeepAlive
                        + " flying packets in window (spoofed latency)");
            }
        } else {
            data.pingSpoofBuffer = Math.max(0, data.pingSpoofBuffer - 1);
        }
    }
}
