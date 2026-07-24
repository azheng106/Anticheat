package org.example.azheng.anticheat.checks;

import com.github.retrooper.packetevents.event.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Check implements Listener, PacketListener {
    private final String name;

    public Check(String name) {
        this.name = name;
    }

    public void flag(Player target, String... information) {
        StringBuilder formattedInfo = new StringBuilder();
        for (String str : information) {
            formattedInfo.append(str);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.AQUA + target.getName() + ChatColor.GRAY + " has failed " +
                    ChatColor.RED + this.name + ChatColor.LIGHT_PURPLE + " (" + formattedInfo + ")");
        }
    }

    public void flagClientInfo(Player target, String version, String clientBrand, String... information) {
        StringBuilder formattedInfo = new StringBuilder();

        for (String str : information) {
            formattedInfo.append(str
                    .replace("%client%", clientBrand)
                    .replace("%version%", version));
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.AQUA + target.getName() +
                    ChatColor.GRAY + " has joined using " +
                    ChatColor.LIGHT_PURPLE + clientBrand +
                    ChatColor.GRAY + " (" +
                    ChatColor.RED + version +
                    ChatColor.GRAY + ")");
        }
    }
}
