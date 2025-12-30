package org.example.azheng.anticheat.checks.combat;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.example.azheng.anticheat.checks.Check;

import java.util.*;

public class KillauraB extends Check {
    public KillauraB(String name) {
        super(name);
    }

    public static class BlockData {
        boolean isBlocking = false;

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
        BlockData data = getData(p);
        PacketTypeCommon type = e.getPacketType();


        if (type == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(e);

            if (packet.getBlockPosition().getY() == 4095 && isHoldingSword(p)) {
                data.isBlocking = true;
            }
            //p.sendMessage(String.valueOf(data.isBlocking));
        } else if (type == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(e);
            if (packet.getAction() == DiggingAction.RELEASE_USE_ITEM && isHoldingSword(p)) {
                data.isBlocking = false;
            }
            //p.sendMessage(String.valueOf(data.isBlocking));
        } else if (type == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            data.isBlocking = false;
            //p.sendMessage(String.valueOf(data.isBlocking));
        } else if (type == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(e);
            //p.sendMessage(String.valueOf(packet.getAction()));
            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (data.isBlocking) {
                    flag(p, "autoblock");
                }
            }
            //p.sendMessage(String.valueOf(data.isBlocking));
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
