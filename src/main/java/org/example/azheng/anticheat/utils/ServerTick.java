package org.example.azheng.anticheat.utils;

/**
 * Runs once per server tick (scheduled with period 1L in Anticheat.onEnable).
 *
 * Its only job is to notice when the server PROCESS froze. A scheduled every-tick
 * task should fire ~every 50ms; if the wall-clock jump between two runs is much
 * larger, the JVM stalled (GC pause, overloaded main thread, etc.). Such a stall
 * pauses the netty I/O threads too, so packets that queued up on the socket flush
 * in a burst on resume — which looks exactly like a Blink/FakeLag flush.
 *
 * The Blink check reads {@link #lastStallMs} to discard those windows.
 *
 * Written on the main thread, read on the netty thread, so both fields are volatile.
 */
public class ServerTick implements Runnable {
    private static final long STALL_THRESHOLD_MS = 150; // > ~3 missed ticks

    public static volatile long lastTickMs = System.currentTimeMillis();
    public static volatile long lastStallMs = 0; // wall-clock time we last observed a stall

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        if (now - lastTickMs > STALL_THRESHOLD_MS) {
            // The task didn't run for a while -> the process was frozen up until now.
            lastStallMs = now;
        }
        lastTickMs = now;
    }
}
