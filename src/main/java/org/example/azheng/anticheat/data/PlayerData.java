package org.example.azheng.anticheat.data;

import org.bukkit.entity.Player;

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

    public boolean inLiquid, onStairSlab, onIce, onClimbable, onSlime, underBlock;
    public int liquidTicks, iceTicks, slimeTicks, underBlockTicks;
    public int airTicks, groundTicks;
    public int speedPotionLevel;

    // Velocity
    public int velXTicks, velYTicks, velZTicks;

    // Killaura A
    public long lastFlying;

    // NoFall A
    public boolean lastServerGround = true;
    public boolean nearGround;

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
