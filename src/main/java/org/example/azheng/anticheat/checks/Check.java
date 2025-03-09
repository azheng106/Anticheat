package org.example.azheng.anticheat.checks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class Check {
    // Create an abstract "base" check class that other checks can extend from
    private String name;

    public Check(String name) {
        this.name = name;
    }

    public void flag(Player target, String information) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(
                    ChatColor.AQUA + target.getName() + ChatColor.GRAY + " has failed " +
                    ChatColor.RED + this.name + ChatColor.LIGHT_PURPLE + " (" + information + ")");
        }
    }
}
