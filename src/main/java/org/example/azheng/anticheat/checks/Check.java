package org.example.azheng.anticheat.checks;

import com.github.retrooper.packetevents.event.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Check implements Listener, PacketListener {
    // Create an abstract "base" check class that other checks can extend from
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
            p.sendMessage(
                    ChatColor.AQUA + target.getName() + ChatColor.GRAY + " has failed " +
                    ChatColor.RED + this.name + ChatColor.LIGHT_PURPLE + " (" + formattedInfo + ")");
        }
    }
}
