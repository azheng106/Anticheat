package org.example.azheng.anticheat.data;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class DataManager {
    private final Set<PlayerData> dataSet = new HashSet<>();

    public DataManager() {
        Bukkit.getOnlinePlayers().forEach(this::add);
    }

    public void add(Player player) {
        dataSet.add(new PlayerData(player));
    }

    public PlayerData getPlayerData(Player player) {
        return dataSet.stream()
                .filter(playerData -> playerData.player == player)
                .findFirst()
                .orElse(null);
    }

    public void remove(Player player) {
        dataSet.removeIf(playerData -> playerData.player == player);
    }
}
