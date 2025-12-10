package org.example.azheng.anticheat.checks.movement;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;

public class NoFallA extends Check implements Listener {
    public NoFallA(String name) {
        super(name);
    }

    private static final double blockGCD = 1/64.;
    private int buffer = 0;

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);

        boolean clientGround = p.isOnGround();
        boolean serverGround = e.getTo().getY() % blockGCD < 0.0001;

        if (clientGround != data.lastServerGround) {
            if (buffer++ > 1 && (!data.nearGround || !data.lastServerGround)) {
                // False positives will occur without buffer unless player isn't near ground or lastServerGround false
                flag(p, "c=" + clientGround, " s=" + data.lastServerGround);
            }
        } else if (buffer > 0) buffer--;

        data.lastServerGround = serverGround;
    }
}
