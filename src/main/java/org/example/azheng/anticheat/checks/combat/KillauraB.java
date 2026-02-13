package org.example.azheng.anticheat.checks.combat;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.Check;
import org.example.azheng.anticheat.data.PlayerData;

import java.util.*;

public class KillauraB extends Check {
    public KillauraB(String name) {
        super(name);
    }

    public static class BlockData {
        boolean isBlocking = false;
        long lastUnblock = System.currentTimeMillis();
    }

    private final Map<UUID, BlockData> blockDataMap = new HashMap<>();

    private BlockData getData(Player p) { return blockDataMap.computeIfAbsent(p.getUniqueId(), id -> new BlockData()); }

    /*
    PLAYER_BLOCK_PLACEMENT packet is sent when player blocks with a sword.
    The packet will be sent with a dummy location (y = 4095) and BlockFace OTHER.
    PLAYER_DIGGING packet is sent when player unblocks with a sword, with action type RELEASE_USE_ITEM
    INTERACT_ENTITY packet is sent when player attacks an entity
     */
    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        Player p = e.getPlayer();
        if (!Bukkit.getOnlinePlayers().contains(p)) return;

        BlockData data = getData(p);
        PlayerData playerData = Anticheat.instance.dataManager.getPlayerData(p);
        PacketTypeCommon type = e.getPacketType();


        if (type == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(e);

            if (packet.getBlockPosition().getY() == 4095 && isHoldingSword(p)) {
                // Blocking sword
                data.isBlocking = true;
            }
        } else if (type == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(e);
            DiggingAction action = packet.getAction();
            if (isHoldingSword(p) &&
                    (action == DiggingAction.RELEASE_USE_ITEM || action == DiggingAction.DROP_ITEM)) {
                // Unblocking sword or dropping their sword while blocking
                data.isBlocking = false;
                data.lastUnblock = System.currentTimeMillis();
            }
        } else if (type == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            // Switch item slot
            data.isBlocking = false;
            data.lastUnblock = System.currentTimeMillis();
        } else if (type == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(e);

            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                long timeSinceLastUnblock = System.currentTimeMillis() - data.lastUnblock;

                if (data.isBlocking) {
                    playerData.auraBBuffer++;
                    if (playerData.auraBBuffer > 3) {
                        flag(p, "attacking while blocking");
                    }
                } else if (timeSinceLastUnblock < 2) {
                    playerData.auraBBuffer++;
                    if (playerData.auraBBuffer > 2) {
                        flag(p, "unblocked <2ms before attacking");
                    }
                } else {
                    playerData.auraBBuffer = 0;
                }
            }
        }
    }

    public static boolean isHoldingSword(Player p) {
        Material type = p.getItemInHand().getType();
        return type == Material.DIAMOND_SWORD
                || type == Material.IRON_SWORD
                || type == Material.GOLD_SWORD
                || type == Material.STONE_SWORD
                || type == Material.WOOD_SWORD;
    }
}
