package org.example.azheng.anticheat;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.azheng.anticheat.checks.KillauraA;
import org.example.azheng.anticheat.data.DataManager;
import org.example.azheng.anticheat.listeners.JoinLeaveListener;

public final class Anticheat extends JavaPlugin {

    public static Anticheat instance;
    public DataManager dataManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    @Override
    public void onEnable() {
        instance = this;
        PacketEvents.getAPI().init();
        dataManager = new DataManager();
        Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(), this);
        PacketEvents.getAPI().getEventManager().registerListener(
                new KillauraA("Aura (A)"), PacketListenerPriority.NORMAL);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

}
