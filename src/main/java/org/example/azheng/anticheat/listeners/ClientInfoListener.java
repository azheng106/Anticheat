package org.example.azheng.anticheat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.checks.packet.client.ClientBrand;
import org.example.azheng.anticheat.checks.packet.client.ClientVersion;

public class ClientInfoListener extends Check {

    public ClientInfoListener() {
        super("ClientInfo");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(Anticheat.instance, () -> {
            flagClientInfo(
                    player,
                    ClientVersion.getClientVersion(),
                    ClientBrand.getClientBrand()
            );
        }, 5L); // 250ms delay due to client brand packets not being immediate
    }
}