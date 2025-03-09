package org.example.azheng.anticheat.data;

import org.bukkit.entity.Player;

public class PlayerData { // This is where we still store information needed in our checks
    public Player player;

    public PlayerData(Player player) {
        this.player = player;
    }

    // Information needed for killauraA check
    public long lastFlying;
    public int killauraAVerbose;
}
