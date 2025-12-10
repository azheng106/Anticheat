package org.example.azheng.anticheat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.data.PlayerData;
import org.example.azheng.anticheat.utils.ReflectionUtils;

public class MoveListener implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);

        if (e.getFrom().getX() != e.getTo().getX()
        || e.getFrom().getY() != e.getTo().getY()
        || e.getFrom().getZ() != e.getTo().getZ()) {
            if (data == null) return;
            data.reduceVelocity();

            data.boundingBox = ReflectionUtils.getBoundingBox(p);
            data.nearGround = !ReflectionUtils.getCollidingBlocks(p, ReflectionUtils
                    .modifyBoundingBox(data.boundingBox, 0, -1, 0, 0, 0, 0)).isEmpty();

        }
    }

    @EventHandler
    public void onVelocityTaken(PlayerVelocityEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);

        if (data == null) {
            return;
        }

        data.velXTicks = (int) Math.round(e.getVelocity().getX() * 100);
        data.velYTicks = (int) Math.round(e.getVelocity().getY() * 100);
        data.velZTicks = (int) Math.round(e.getVelocity().getZ() * 100);
    }
}
