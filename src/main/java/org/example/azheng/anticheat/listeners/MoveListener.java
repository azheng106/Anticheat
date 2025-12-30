package org.example.azheng.anticheat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;
import org.example.azheng.anticheat.Anticheat;
import org.example.azheng.anticheat.checks.movement.NoFallA;
import org.example.azheng.anticheat.data.PlayerData;
import org.example.azheng.anticheat.utils.PlayerUtils;
import org.example.azheng.anticheat.utils.ReflectionUtils;


public class MoveListener implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);

        // Cancel conditions
        if (e.getFrom().getX() == e.getTo().getX()
                && e.getFrom().getY() == e.getTo().getY()
                && e.getFrom().getZ() == e.getTo().getZ()) return;
        if (data == null) return;

        data.reduceVelocity();

        data.boundingBox = ReflectionUtils.getBoundingBox(p);
        data.nearGround = !ReflectionUtils.getCollidingBlocks(p, ReflectionUtils
                .modifyBoundingBox(data.boundingBox, 0, -1, 0, 0, 0, 0)).isEmpty();

        data.lastClientGround = data.clientGround;
        data.clientGround = PlayerUtils.isOnGround(p, 0.5) && e.getTo().getY() % NoFallA.blockGCD < 0.0001;
        data.onGround = PlayerUtils.isOnGround(p);

        data.speedPotionLevel = PlayerUtils.getPotionEffectLevel(p, PotionEffectType.SPEED);

        data.onStairSlab = PlayerUtils.isInStairs(p);
        data.inLiquid = PlayerUtils.isInLiquid(p);
        data.onIce = PlayerUtils.isOnIce(p);
        data.onClimbable = PlayerUtils.isOnClimbable(p);
        data.onSlime = PlayerUtils.isOnSlime(p);
        data.underBlock = PlayerUtils.isUnderBlock(p);

        if (e.getPlayer().isOnGround()) {
            data.groundTicks++;
            data.airTicks = 0;
        } else {
            data.airTicks++;
            data.groundTicks = 0;
        }

        // Increment differently because different materials are more "aggressive"
        // Want to increase slime quickly because slime instantly causes bounciness/weird movement, so we want check exemptions to increase instantly
        data.iceTicks = Math.max(0, data.onIce ? Math.min(60, data.iceTicks + 3) : data.iceTicks - 1);
        data.slimeTicks = Math.max(0, data.onIce ? Math.min(60, data.slimeTicks + 8) : data.iceTicks - 1);
        data.underBlockTicks = Math.max(0, data.underBlock ? Math.min(60, data.underBlockTicks + 3)  : data.underBlockTicks - 1);
        data.liquidTicks = Math.max(0, data.onIce ? Math.min(40, data.liquidTicks + 1) : data.iceTicks - 1);
    }

    @EventHandler
    public void onVelocityTaken(PlayerVelocityEvent e) {
        Player p = e.getPlayer();
        PlayerData data = Anticheat.instance.dataManager.getPlayerData(p);

        if (data == null) {
            return;
        }

        data.velXTicks = (int) Math.round(e.getVelocity().getX() * 100);
        data.velYTicks = (int) Math.round(e.getVelocity().getY() * 100);
        data.velZTicks = (int) Math.round(e.getVelocity().getZ() * 100);
    }
}
