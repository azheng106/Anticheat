package org.example.azheng.anticheat.checks.combat;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;

public class AimA extends Check {

    public AimA(String name) {
        super(name);
    }

    /*
    s = in game sensitivity in options.txt
    f = 0.6s + 0.2
    g = 8f^3
    smallest yaw change = 0.15g
     */
    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);
        PacketTypeCommon type = e.getPacketType();

        if (data == null) return;
        if (data.yawGcd == 0 || data.pitchGcd == 0) return; // gcds aren't "learned" yet

        double dyaw = data.deltaYaw;
        double dpitch = data.deltaPitch;
        if (dyaw < 1e-3 || dpitch < 1e-3) return;

        // Only run the check when the player attacks
        if (type != PacketType.Play.Client.INTERACT_ENTITY) return;
        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(e);
        if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

        double yawMod = dyaw % data.yawGcd;
        double pitchMod = dpitch % data.pitchGcd;
        boolean badYaw = yawMod > 1e-2 && data.yawGcd - yawMod > 1e-2;
        boolean badPitch = pitchMod > 1e-2 && data.pitchGcd - pitchMod > 1e-2;

        if (badYaw || badPitch) {
            data.aimABuffer += 4;
        } else {
            data.aimABuffer = Math.max(0, data.aimABuffer - 1);
        }
        p.sendMessage("buffer: " + data.aimABuffer);

        if (data.aimABuffer > 20) {
            flag(p, "Rotation amount during attacking is not a multiple of rotation GCD");
        }
    }
}
