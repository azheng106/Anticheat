package org.example.azheng.anticheat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Ref;

public class PingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /ping <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        try {
            int ping = ReflectionUtils.getPing(target);
            sender.sendMessage(ChatColor.AQUA + target.getName() + ChatColor.GRAY
                    + "'s ping: " + ChatColor.GREEN + ping + "ms");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to get ping.");
            e.printStackTrace();
        }
        return true;
    }
}