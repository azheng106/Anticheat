package org.example.azheng.anticheat.checks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class Check {
    // Create an abstract "base" check class that other checks can extend from
    private final String name;
    private final boolean enabled;

    public Check(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public void flag(Player target, String... information) {
        if (!enabled) {
            return;
        }
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
