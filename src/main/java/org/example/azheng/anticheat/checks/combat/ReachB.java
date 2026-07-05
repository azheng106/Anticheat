package org.example.azheng.anticheat.checks.combat;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;
import org.example.azheng.anticheat.utils.ReflectionUtils;
import org.example.azheng.anticheat.utils.TargetTracker;

import java.util.List;

/**
 * ReachB — ping-and-interpolation-aware reach check.
 *
 * Where {@link ReachA} compares the attacker's eye against the target's
 * position right now on the server, ReachB compares it against where
 * the target actually appeared on the attacker's screen when they
 * clicked. Reconstructing that rendered position requires undoing two delays:
 *
 *
 *   Network latency. The target's position reaches the attacker's
 *       client roughly {@code ping/2} ms after the server knew it. We rewind the
 *       target's recorded position history by that many ticks.
 *   Client interpolation. The Notchian client never snaps other
 *       entities to a freshly received position; it lerps toward it over ~3
 *       ticks (~150ms). So the rendered entity trails the latest received
 *       position by anywhere from 0 to 3 ticks at any instant.
 *
 *
 * Because neither delay is known exactly (ping jitters; we cannot observe
 * where along the interpolation arc the client happened to be), ReachB does not
 * pick a single historical position. It picks a window of historical
 * ticks spanning the plausible range, computes reach against every position in
 * it, and keeps the minimum distance — the single most attacker-friendly
 * interpretation. Only if even that closest candidate is out of range does it
 * accrue violation.
 *
 * Position history
 * The per-tick server position of every player is recorded into the shared
 * {@link TargetTracker} by a 1-tick scheduler task below. History is keyed by
 * entity id and is identical regardless of who is attacking (server-authoritative
 * position); only the rewind amount is per-attacker.
 */
public class ReachB extends Check {

    // ---------- tuning ----------

    /** Vanilla survival reach is 3.0 (the server uses dist² < 9.0). 3.03 absorbs
     *  floating-point slop. Because we lag-compensate, we can run it this tight. */
    private static final double MAX_REACH = 3.03;

    /** Length of the client's entity-interpolation arc, in ticks. The rendered
     *  target trails the latest received position by up to this many ticks. */
    private static final int INTERP_TICKS = 3;

    /** Extra ticks of slack on the OLD end of the rewind window only, to absorb
     *  ping measurement jitter / packet bursts without under-rewinding a laggy
     *  attacker (which would false-positive). We deliberately add NO slack on the
     *  new end: the attacker physically cannot have seen a target position newer
     *  than the network delay, so widening that way only masks real reach. */
    private static final int WINDOW_SLACK = 1;

    /** Cap on how far back we will ever look (ticks), bounded by the tracker's
     *  own history length. Protects against an absurd/garbage ping reading. */
    private static final int MAX_REWIND_TICKS = 30;

    /** Fractional violation buffer (see {@link PlayerData#reachBBuffer}). A hit
     *  adds at most {@link #MAX_PER_HIT}; we flag once it reaches this. Using a
     *  weighted buffer means one borderline reading never flags, but a string of
     *  blatant ones does, quickly. */
    private static final double FLAG_THRESHOLD = 1.5;

    /** Most a single over-reach can add to the buffer, so a single huge spike
     *  (e.g. a teleport we somehow missed) can't instantly trip the flag. */
    private static final double MAX_PER_HIT = 0.5;

    /** Minimum a confirmed over-reach contributes, regardless of how slim. This
     *  keeps the check sensitive to small-but-consistent reach (e.g. 3.1–3.5)
     *  instead of needing the buffer to crawl up in 0.1 increments. */
    private static final double MIN_PER_HIT = 0.34;

    /** How much a legitimate (in-range) hit drains from the buffer. Smaller than
     *  MIN_PER_HIT so genuine reach still wins out against interleaved good hits. */
    private static final double BUFFER_DECAY = 0.05;

    public ReachB(String name) {
        super(name);
        startRecordingLoop();
    }

    // ---------- per-tick position recording ----------

    /**
     * Snapshot every online player's server position once per tick. Runs on the
     * main thread, where reading entity locations is safe and consistent.
     */
    private void startRecordingLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                TargetTracker tracker = Anticheat.instance.targetTracker;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    tracker.record(p.getEntityId(),
                            p.getLocation().getX(),
                            p.getLocation().getY(),
                            p.getLocation().getZ());
                }
            }
        }.runTaskTimer(Anticheat.instance, 1L, 1L);
    }

    // ---------- attack handling ----------
    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        PacketTypeCommon type = e.getPacketType();
        if (type != PacketType.Play.Client.INTERACT_ENTITY) return;

        Player attacker = e.getPlayer();
        if (attacker.getGameMode() == GameMode.CREATIVE) return;

        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(e);
        if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

        int targetId = packet.getEntityId();
        Entity target = SpigotConversionUtil.getEntityById(attacker.getWorld(), targetId);
        if (!(target instanceof LivingEntity) || target == attacker) return;

        PlayerData data = Anticheat.instance.dataManager.getPlayerData(attacker);
        if (data == null) return;

        // Attacker eye position. The attacker authoritatively dictates their own
        // position, so server-now == attacker-now: their most recent flying
        // packet (data.lastX/Y/Z) is the right reference, same as ReachA/B.
        double eyeX = data.lastX;
        double eyeY = data.lastY + (attacker.isSneaking() ? 1.54 : 1.62);
        double eyeZ = data.lastZ;

        // Recorded server positions of the target, newest first.
        List<double[]> history = Anticheat.instance.targetTracker
                .getRecentPositions(targetId, MAX_REWIND_TICKS + 1);
        if (history.isEmpty()) return; // tracker hasn't warmed up for this target yet

        // ── Build the rewind window ────────────────────────────────────────
        // The position the attacker SAW = the target position from
        //   (downstream latency) .. (downstream latency + interpolation lag)
        // ago, where downstream ≈ ping/2 and interpolation spans up to
        // INTERP_TICKS. The window therefore runs from the NEWEST tick the
        // attacker could have rendered, floor(ping/2), back to
        // ceil(ping/2) + INTERP_TICKS (+ a little old-side jitter slack).
        //
        // Critically, the new end is pinned at floor(ping/2): we never compare
        // against positions newer than the network delay allows. The previous
        // version subtracted slack here too, dragging the window toward "now" —
        // and since we take the minimum over the window, any too-recent tick
        // where the target happened to be closer silently erased the violation.
        // That was the false-negative bug: a laggy/moving target could always
        // supply one in-range sample, so only absurd (>5 block) reaches flagged.
        double oneWayTicks = pingMillis(attacker) / 2.0 / 50.0;
        int lo = Math.max(0, (int) Math.floor(oneWayTicks));
        int hi = (int) Math.ceil(oneWayTicks) + INTERP_TICKS + WINDOW_SLACK;
        hi = Math.min(hi, MAX_REWIND_TICKS);

        double best = Double.MAX_VALUE;
        for (int i = lo; i <= hi && i < history.size(); i++) {
            double d = distanceToHitbox(eyeX, eyeY, eyeZ, history.get(i));
            if (d < best) best = d;
        }
        if (best == Double.MAX_VALUE) return; // window fell outside available history

        // ── Accrue / drain the weighted buffer ─────────────────────────────
        double over = best - MAX_REACH;
        if (over > 0) {
            // Clamp into [MIN_PER_HIT, MAX_PER_HIT]: a slim-but-real reach still
            // makes meaningful progress, a blatant one can't instantly flag.
            data.reachBBuffer += Math.min(MAX_PER_HIT, Math.max(MIN_PER_HIT, over));

            if (data.reachBBuffer >= FLAG_THRESHOLD) {
                flag(attacker, String.format(
                        "dist=%.3f max=%.2f rewind=%d-%d ping=%dms buf=%.2f",
                        best, MAX_REACH, lo, hi, pingMillis(attacker), data.reachBBuffer));
                // Bleed off so we don't spam every subsequent packet.
                data.reachBBuffer = FLAG_THRESHOLD / 2.0;
            }
        } else {
            data.reachBBuffer = Math.max(0.0, data.reachBBuffer - BUFFER_DECAY);
        }
    }

    /** Attacker ping in ms, or 0 if reflection fails (then only the interpolation
     *  window applies — still far safer than ReachA's no-compensation). */
    private static int pingMillis(Player p) {
        try {
            return Math.max(0, ReflectionUtils.getPing(p));
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Closest distance from the attacker's eye to the target's expanded AABB at
     * a historical position {@code {x, y, z}}.
     *
     * <p>Vanilla 1.8 player hitbox is 0.6 wide × 1.8 tall, centered on x,z. The
     * attack hit-test expands it by 0.1 per axis, hence the padding.</p>
     */
    private static double distanceToHitbox(double eyeX, double eyeY, double eyeZ, double[] pos) {
        double half = 0.3 + 0.1;
        double minX = pos[0] - half, maxX = pos[0] + half;
        double minY = pos[1] - 0.1,  maxY = pos[1] + 1.8 + 0.1;
        double minZ = pos[2] - half, maxZ = pos[2] + half;

        double cx = Math.max(minX, Math.min(eyeX, maxX));
        double cy = Math.max(minY, Math.min(eyeY, maxY));
        double cz = Math.max(minZ, Math.min(eyeZ, maxZ));

        double dx = eyeX - cx, dy = eyeY - cy, dz = eyeZ - cz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
