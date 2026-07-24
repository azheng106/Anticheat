package org.example.azheng.anticheat;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.azheng.anticheat.checks.CheckManager;
import org.example.azheng.anticheat.checks.packet.client.ClientBrand;
import org.example.azheng.anticheat.checks.packet.client.ClientVersion;
import org.example.azheng.anticheat.commands.PingCommand;
import org.example.azheng.anticheat.data.DataManager;
import org.example.azheng.anticheat.listeners.ClientInfoListener;
import org.example.azheng.anticheat.listeners.JoinLeaveListener;
import org.example.azheng.anticheat.listeners.MoveListener;
import org.example.azheng.anticheat.listeners.RotationListener;
import org.example.azheng.anticheat.utils.ServerTick;
import org.example.azheng.anticheat.utils.TargetTracker;

public final class Anticheat extends JavaPlugin {

    public static Anticheat instance;
    public DataManager dataManager;
    public TargetTracker targetTracker;

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
        targetTracker = new TargetTracker();
        CheckManager checkManager = new CheckManager(this);

        Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new ClientInfoListener(), this);

        PacketEvents.getAPI().getEventManager().registerListener(
                new RotationListener(), PacketListenerPriority.LOWEST);
        PacketEvents.getAPI().getEventManager().registerListener(
                new MoveListener(), PacketListenerPriority.LOWEST);

        // Client Brand Information
        PacketEvents.getAPI().getEventManager().registerListener(
                new ClientBrand(), PacketListenerPriority.NORMAL);

        PacketEvents.getAPI().getEventManager().registerListener(
                new ClientVersion(), PacketListenerPriority.NORMAL);

        checkManager.registerChecks();

        // Detects server-process freezes so the Blink check can ignore packet bursts
        // that are caused by the server catching up rather than by client withholding.
        Bukkit.getScheduler().runTaskTimer(this, new ServerTick(), 0L, 1L);

        getCommand("ping").setExecutor(new PingCommand());
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

}
