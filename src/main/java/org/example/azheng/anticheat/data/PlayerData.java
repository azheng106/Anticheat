package org.example.azheng.anticheat.data;

import org.bukkit.entity.Player;
import org.example.azheng.anticheat.utils.EvictingList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

public class PlayerData {
    public PlayerData(Player player) {
        this.player = player;
        this.lastX = player.getLocation().getX();
        this.lastY = player.getLocation().getY();
        this.lastZ = player.getLocation().getZ();
    }

    // General
    public Player player;
    public Object boundingBox;
    public double lastX, lastY, lastZ;
    public boolean onGround, clientGround, lastClientGround;
    public double deltaXZ, deltaY;
    public boolean inventoryOpen = false;

    public boolean inLiquid, onStairSlab, onIce, onClimbable, onSlime, underBlock;
    public int liquidTicks, iceTicks, slimeTicks, underBlockTicks;
    public int airTicks, groundTicks;
    public int speedPotionLevel;

    // Aim
    public float lastYaw = 0, lastPitch = 0;
    public float deltaYaw = 0, deltaPitch = 0, lastDeltaYaw = 0, lastDeltaPitch = 0;
    public float yawGcd = 0, pitchGcd = 0, lastYawGcd = 0, lastPitchGcd = 0;
    public LinkedList<Float> yawGcdList = new EvictingList<>(45);
    public LinkedList<Float> pitchGcdList = new EvictingList<>(45);
    // 0.0603

    // Velocity
    public int velXTicks, velYTicks, velZTicks;

    // Killaura A
    public long lastFlying;

    // NoFall A
    public boolean lastServerGround = true;
    public boolean nearGround;

    // Timer
    public long lastMs = System.currentTimeMillis();
    public int threshold = 250;

    // Buffers
    public int auraABuffer = 0, auraBBuffer = 0;
    public int nofallABuffer = 0;
    public int speedABuffer = 0;
    public int aimABuffer = 0;
    public long timerBalance = 0;
    public int invMoveBuffer = 0;

    public boolean isVelocityTaken() {
        return velXTicks > 0 || velYTicks > 0 || velZTicks > 0;
    }

    /**
     * Decrements velXTicks, velYTicks, and velZTicks by 1 if they are positive.
     * This function is called every MoveEvent in listeners.MoveListener
     */
    public void reduceVelocity() {
        velXTicks = Math.max(0, velXTicks - 1);
        velYTicks = Math.max(0, velYTicks - 1);
        velZTicks = Math.max(0, velZTicks - 1);
    }
}
