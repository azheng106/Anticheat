package org.example.azheng.anticheat.checks.movement;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;

public class NoFallA extends Check {
    public NoFallA(String name) {
        super(name);
    }

    public static final double blockGCD = 1/64.;

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);

        if (p.getAllowFlight()
                || p.isInsideVehicle()
                || data.isVelocityTaken()) {
            return;
        }

        boolean clientGround = p.isOnGround();
        boolean serverGround = e.getTo().getY() % blockGCD < 0.0001;

        if (clientGround != data.lastServerGround) {
            if (data.nofallABuffer++ > 1 && (!data.nearGround || !data.lastServerGround)) {
                // False positives will occur without buffer unless player isn't near ground or lastServerGround false
                flag(p, "c=" + clientGround, " s=" + data.lastServerGround);
            }
        } else {data.nofallABuffer = Math.max(0, data.nofallABuffer - 1);}

        data.lastServerGround = serverGround;
    }
}
