package org.example.azheng.anticheat.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.Set;

public class BlockUtils {
    public static boolean isLiquid(Block block) {
        Material type = block.getType();

        Set<Material> liquids = EnumSet.of(
                Material.WATER,
                Material.STATIONARY_WATER,
                Material.LAVA,
                Material.STATIONARY_LAVA
        );

        return liquids.contains(type);
    }

    public static boolean isSlab(Block block) {
        Material type = block.getType();

        Set<Material> slabs = EnumSet.of(
                Material.STONE_SLAB2,
                Material.WOOD_STEP,
                Material.STEP
        );
        return slabs.contains(type);
    }

    public static boolean isStair(Block block) {
        Material type = block.getType();

        Set<Material> stairs = EnumSet.of(
                Material.COBBLESTONE_STAIRS,
                Material.BRICK_STAIRS,
                Material.SMOOTH_STAIRS,
                Material.SANDSTONE_STAIRS,
                Material.RED_SANDSTONE_STAIRS,
                Material.WOOD_STAIRS,
                Material.ACACIA_STAIRS,
                Material.SPRUCE_WOOD_STAIRS,
                Material.BIRCH_WOOD_STAIRS,
                Material.JUNGLE_WOOD_STAIRS,
                Material.DARK_OAK_STAIRS,
                Material.NETHER_BRICK_STAIRS,
                Material.QUARTZ_STAIRS
        );
        return stairs.contains(type);
    }

    public static boolean isClimbable(Block block) {
        Material type = block.getType();

        Set<Material> climbables = EnumSet.of(
                Material.VINE,
                Material.LADDER
        );

        return climbables.contains(type);
    }
}
