package org.example.azheng.anticheat.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TargetTracker {

    private static final int HISTORY_SIZE = 40; // ~2 seconds at 20 TPS — enough to
                                                // rewind even very laggy players.

    // entityId -> ring buffer of {x, y, z}
    private final Map<Integer, Deque<double[]>> positions = new ConcurrentHashMap<>();

    // Call once per tick per tracked entity with its CURRENT server position.
    // Recording happens on the main thread; reads happen on the netty thread
    // (packet listener), so guard the per-entity deque with its own monitor.
    public void record(int entityId, double x, double y, double z) {
        Deque<double[]> dq = positions.computeIfAbsent(entityId, k -> new ArrayDeque<>());
        synchronized (dq) {
            dq.addFirst(new double[]{x, y, z});
            while (dq.size() > HISTORY_SIZE) dq.removeLast();
        }
    }

    // Most recent n positions, newest first.
    public List<double[]> getRecentPositions(int entityId, int n) {
        Deque<double[]> dq = positions.get(entityId);
        if (dq == null) return Collections.emptyList();
        List<double[]> out = new ArrayList<>(n);
        synchronized (dq) {
            for (double[] pos : dq) {
                out.add(pos);
                if (out.size() >= n) break;
            }
        }
        return out;
    }

    public void clear(int entityId) { positions.remove(entityId); }
}