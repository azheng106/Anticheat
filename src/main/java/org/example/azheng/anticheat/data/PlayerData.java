package org.example.azheng.anticheat.data;

import org.bukkit.entity.Player;

public class PlayerData { // This is where we still store information needed in our checks
    public Player player;
    public Object boundingBox;
    public boolean nearGround;
    public int velXTicks, velYTicks, velZTicks;

    public PlayerData(Player player) {
        this.player = player;
    }

    // Information needed for killauraA check
    public long lastFlying;

    // NoFall
    public boolean lastServerGround = true;

    public boolean isVelocityTaken() {
        return velXTicks > 0 || velYTicks > 0 || velZTicks > 0;
    }

    public void reduceVelocity() {
        velXTicks = Math.max(0, velXTicks - 1);
        velYTicks = Math.max(0, velYTicks - 1);
        velZTicks = Math.max(0, velZTicks - 1);
    }
}
