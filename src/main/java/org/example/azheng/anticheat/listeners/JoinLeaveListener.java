package org.example.azheng.anticheat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.example.azheng.anticheat.Anticheat;

public class JoinLeaveListener implements Listener {
    // Add/remove players from the dataManager when they join/leave
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Anticheat.instance.dataManager.add(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Anticheat.instance.dataManager.remove(e.getPlayer());
    }
}
