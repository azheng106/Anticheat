package org.example.azheng.anticheat.data;

import org.bukkit.entity.Player;

public class PlayerData { // This is where we still store information needed in our checks
    public Player player;
    public Object boundingBox;
    public boolean nearGround;

    public PlayerData(Player player) {
        this.player = player;
    }

    // Information needed for killauraA check
    public long lastFlying;
    public int killauraAVerbose;

    // NoFall
    public boolean lastServerGround = true;
}
