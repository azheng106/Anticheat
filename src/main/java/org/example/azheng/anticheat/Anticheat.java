package org.example.azheng.anticheat;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.checks.CheckManager;
import org.example.azheng.anticheat.checks.combat.AimA;
import org.example.azheng.anticheat.checks.combat.KillauraA;
import org.example.azheng.anticheat.checks.combat.KillauraB;
import org.example.azheng.anticheat.checks.movement.NoFallB;
import org.example.azheng.anticheat.checks.movement.NoFallA;
import org.example.azheng.anticheat.checks.movement.SpeedA;
import org.example.azheng.anticheat.checks.packet.Timer;
import org.example.azheng.anticheat.data.DataManager;
import org.example.azheng.anticheat.listeners.JoinLeaveListener;
import org.example.azheng.anticheat.listeners.MoveListener;
import org.example.azheng.anticheat.listeners.RotationListener;

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
        CheckManager checkManager = new CheckManager(this);

        Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(), this);
        PacketEvents.getAPI().getEventManager().registerListener(
                new RotationListener(), PacketListenerPriority.LOWEST);
        PacketEvents.getAPI().getEventManager().registerListener(
                new MoveListener(), PacketListenerPriority.LOWEST);

        checkManager.registerChecks();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

}
