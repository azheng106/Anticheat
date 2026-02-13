package org.example.azheng.anticheat.checks;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.example.azheng.anticheat.checks.combat.AimA;
import org.example.azheng.anticheat.checks.combat.KillauraA;
import org.example.azheng.anticheat.checks.combat.KillauraB;
import org.example.azheng.anticheat.checks.movement.NoFallA;
import org.example.azheng.anticheat.checks.movement.NoFallB;
import org.example.azheng.anticheat.checks.movement.SpeedA;
import org.example.azheng.anticheat.checks.packet.InvalidPitch;
import org.example.azheng.anticheat.checks.packet.Timer;

public class CheckManager {
    private final Plugin plugin;

    public CheckManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerChecks() {
        // COMBAT
        PacketEvents.getAPI().getEventManager().registerListener(
                new KillauraA("Aura (A)"), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().getEventManager().registerListener(
                new KillauraB("Aura (B)"), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().getEventManager().registerListener(
                new AimA("Aim (A)"), PacketListenerPriority.NORMAL);

        // MOVEMENT
        PacketEvents.getAPI().getEventManager().registerListener(
                new SpeedA("Speed (A)"), PacketListenerPriority.NORMAL);
        Bukkit.getPluginManager().registerEvents(new NoFallA("NoFall (A)"), plugin);
        Bukkit.getPluginManager().registerEvents(new NoFallB("NoFall (B)"), plugin);


    }
}
