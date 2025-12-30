package org.example.azheng.anticheat.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerUtils {
    public static boolean isOnGround(Player p) {
        return isOnGround(p, 0.25);
    }

    public static boolean isOnGround(Player p, double yExpand) {
        Object box = ReflectionUtils.modifyBoundingBox(ReflectionUtils.getBoundingBox(p), 0, -yExpand, 0, 0, 0, 0);
        return !ReflectionUtils.getCollidingBlocks(p, box).isEmpty();
    }

    public static boolean isInLiquid(Player p) {
        Object box = ReflectionUtils.getBoundingBox(p);

        double minX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "a"), box);
        double minY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "b"), box);
        double minZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "c"), box);
        double maxX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "d"), box);
        double maxY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "e"), box);
        double maxZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "f"), box);

        for (double x = minX; x < maxX; x++) {
            for (double y = minY; y < maxY; y++) {
                for (double z = minZ; z < maxZ; z++) {
                    Block block = new Location(p.getWorld(), x, y, z).getBlock();

                    if (BlockUtils.isLiquid(block)) return true;
                }
            }
        }
        return false;
    }

    public static boolean isOnSlime(Player p) {
        Object box = ReflectionUtils.modifyBoundingBox(ReflectionUtils.getBoundingBox(p), 0, -0.4f,0,0,0,0);

        double minX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "a"), box);
        double minY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "b"), box);
        double minZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "c"), box);
        double maxX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "d"), box);
        double maxY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "e"), box);
        double maxZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "f"), box);

        for(double x = minX ; x < maxX ; x++) {
            for(double y = minY ; y < maxY ; y++) {
                for(double z = minZ ; z < maxZ ; z++) {
                    Block block = new Location(p.getWorld(), x, y, z).getBlock();

                    if(block.getType().toString().contains("SLIME")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isInStairs(Player p) {
        Object box = ReflectionUtils.modifyBoundingBox(ReflectionUtils.getBoundingBox(p), 0, -0.5,0,0,0,0);

        double minX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "a"), box);
        double minY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "b"), box);
        double minZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "c"), box);
        double maxX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "d"), box);
        double maxY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "e"), box);
        double maxZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "f"), box);

        for(double x = minX ; x < maxX ; x++) {
            for(double y = minY ; y < maxY ; y++) {
                for(double z = minZ ; z < maxZ ; z++) {
                    Block block = new Location(p.getWorld(), x, y, z).getBlock();

                    if(BlockUtils.isStair(block)
                            || BlockUtils.isSlab(block)
                            || block.getType().equals(Material.SKULL)
                            || block.getType().equals(Material.CAKE_BLOCK)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isOnIce(Player p) {
        Object box = ReflectionUtils.modifyBoundingBox(ReflectionUtils.getBoundingBox(p), 0, -0.5,0,0,0,0);

        double minX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "a"), box);
        double minY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "b"), box);
        double minZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "c"), box);
        double maxX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "d"), box);
        double maxY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "e"), box);
        double maxZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "f"), box);

        for(double x = minX ; x < maxX ; x++) {
            for(double y = minY ; y < maxY ; y++) {
                for(double z = minZ ; z < maxZ ; z++) {
                    Block block = new Location(p.getWorld(), x, y, z).getBlock();

                    if(block.getType().equals(Material.ICE)
                            || block.getType().equals(Material.PACKED_ICE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isOnClimbable(Player p) {
        return BlockUtils.isClimbable(p.getLocation().getBlock())
                || BlockUtils.isClimbable(p.getLocation().add(0, 1, 0).getBlock());
    }

    public static boolean isUnderBlock(Player p) {
        Object box = ReflectionUtils.modifyBoundingBox(ReflectionUtils.getBoundingBox(p), 0, 1,0,0,0.5,0);

        double minX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "a"), box);
        double minY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "b"), box);
        double minZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "c"), box);
        double maxX = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "d"), box);
        double maxY = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "e"), box);
        double maxZ = (double) ReflectionUtils.getInvokedField(ReflectionUtils.getField(box.getClass(), "f"), box);

        for(double x = minX ; x < maxX ; x++) {
            for(double y = minY ; y < maxY ; y++) {
                for(double z = minZ ; z < maxZ ; z++) {
                    Block block = new Location(p.getWorld(), x, y, z).getBlock();

                    if(block.getType().isSolid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static int getPotionEffectLevel(Player p, PotionEffectType effect) {
        for (PotionEffect pe : p.getActivePotionEffects()) {
            if (pe.getType().getName().equals(effect.getName())) {
                return pe.getAmplifier() + 1;
            }
        }
        return 0;
    }
}
