package org.example.azheng.anticheat.checks.packet;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;
import org.example.azheng.anticheat.utils.ServerTick;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Detects Blink and FakeLag (both Vape modules). They share one packet signature:
 * outgoing flying packets are withheld, then arrive BUNCHED in a burst after silence.
 * Blink = one huge gap flushed at once; FakeLag = shorter bounded gaps, repeated.
 *
 * A vanilla client sends a flying packet ~every 50ms (one per tick). Real lag keeps
 * that spacing (packets resume ~50ms apart); a queued flush dumps several within a
 * few ms. The gap length is used only to label which module it likely was.
 */
public class Blink extends Check {
    public Blink(String name) {
        super(name);
    }

    private static final long GAP_MS          = 200; // silence longer than ~4 ticks
    private static final long BURST_WINDOW_MS = 30;  // a flush lands within this window
    private static final int  BURST_MIN       = 4;   // bunched packets that count as a flush

    private final HashSet<PacketTypeCommon> flyingPackets = new HashSet<>(Arrays.asList(
            PacketType.Play.Client.PLAYER_FLYING,
            PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
            PacketType.Play.Client.PLAYER_POSITION,
            PacketType.Play.Client.PLAYER_ROTATION
    ));

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        if (!flyingPackets.contains(e.getPacketType())) return;

        Player p = e.getPlayer();
        if (p == null) return;
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long gap = now - data.lastFlyingMs;
        data.lastFlyingMs = now;

        if (gap > GAP_MS) {
            // Server-process freeze (GC etc.) also bunches packets on the netty thread.
            // If the process stalled during this silence, the gap isn't the client's fault.
            if (ServerTick.lastStallMs >= now - gap) {
                data.watchingRelease = false;
                return;
            }
            // Silence ended cleanly -> watch whether packets now arrive bunched (flush)
            // or resume at a normal ~50ms cadence (just network lag / AFK).
            data.blinkGap = gap;
            data.blinkReleaseStart = now;
            data.blinkReleaseCount = 1;
            data.watchingRelease = true;
            return;
        }

        if (!data.watchingRelease) return;

        if (now - data.blinkReleaseStart <= BURST_WINDOW_MS) {
            data.blinkReleaseCount++;
            if (data.blinkReleaseCount >= BURST_MIN) {
                data.watchingRelease = false;
                data.blinkBuffer++;
                if (data.blinkBuffer > 3) {
                    String mode = data.blinkGap > 1000 ? "Blink" : "FakeLag";
                    flag(p, mode + ": flushed " + data.blinkReleaseCount + " flying packets in "
                            + (now - data.blinkReleaseStart) + "ms after " + data.blinkGap + "ms silence");
                }
            }
        } else {
            // Packets resumed spaced out -> genuine lag, not a queued flush.
            data.watchingRelease = false;
            data.blinkBuffer = Math.max(0, data.blinkBuffer - 1);
        }
    }
}
