package art.ameliah.fabric.autosprintfix.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockUtil - Utilities for interacting with blocks.
 * Provides safe access to blocks and block states,
 * as well as checks for block type or existence.
 */
public class BlockUtil {

    /**
     * Gets the block at the specified position.
     *
     * @param blockPos The position of the block
     * @return The block at the position, or Blocks.AIR if invalid
     */
    public static Block getBlock(BlockPos blockPos) {
        try {
            if (blockPos == null)
                return Blocks.AIR;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return Blocks.AIR;

            return mc.level.getBlockState(blockPos).getBlock();
        } catch (Exception e) {
            return Blocks.AIR;
        }
    }

    /**
     * Gets the block state at the specified position.
     *
     * @param blockPos The position of the block
     * @return The block state at the position, or AIR state if invalid
     */
    public static BlockState getBlockState(BlockPos blockPos) {
        try {
            if (blockPos == null)
                return Blocks.AIR.defaultBlockState();

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return Blocks.AIR.defaultBlockState();

            return mc.level.getBlockState(blockPos);
        } catch (Exception e) {
            return Blocks.AIR.defaultBlockState();
        }
    }

    /**
     * Gets the block's name as a string.
     *
     * @param blockPos The position of the block
     * @return The block name, "air" if empty, or "unknown" on error
     */
    public static String getBlockName(BlockPos blockPos) {
        try {
            Block block = getBlock(blockPos);
            if (block == Blocks.AIR)
                return "air";

            return block.getName().getString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Checks if a block exists (i.e., not air) at the specified position.
     *
     * @param blockPos The position of the block
     * @return True if a block exists, false if air or invalid
     */
    public static boolean blockExists(BlockPos blockPos) {
        try {
            Block block = getBlock(blockPos);
            return block != Blocks.AIR;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a specific block is at the given position.
     *
     * @param blockPos The position to check
     * @param block    The block to compare
     * @return True if the block matches, false otherwise
     */
    public static boolean isBlock(BlockPos blockPos, Block block) {
        try {
            return getBlock(blockPos) == block;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a block matches a pattern string.
     * Patterns support short form "respawn_anchor" or full form
     * "minecraft:respawn_anchor".
     *
     * @param blockPos The position of the block
     * @param pattern  The block pattern to match
     * @return True if the block matches the pattern, false otherwise
     */
    public static boolean matchesPattern(BlockPos blockPos, String pattern) {
        try {
            Block block = getBlock(blockPos);
            String blockStr = block.toString();

            if (blockStr.equals(pattern))
                return true;

            if (!pattern.contains(":")) {
                return blockStr.equals("Block{minecraft:" + pattern + "}");
            }

            return blockStr.contains(pattern);
        } catch (Exception e) {
            return false;
        }
    }
}
