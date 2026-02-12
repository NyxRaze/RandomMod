package art.ameliah.fabric.autosprintfix.core.util;

import net.minecraft.world.item.ItemStack;

/**
 * ItemUtil - Utilities for item stack operations.
 * Supports matching by exact name, pattern, and wildcard.
 */
public class ItemUtil {
    /**
     * Get the enchantment level of an item.
     * Returns 0 if the item doesn't have the enchantment.
     * 
     * @param stack       the item stack
     * @param enchantment the enchantment (eg: "sharpness")
     *                    "density")
     * @return the enchantment level, or 0 if not present
     */
    public static int getEnchantment(ItemStack stack, String enchantmentId) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        String enchants = stack.getEnchantments().toString();

        // Normalize input: "sharpness" → "minecraft:sharpness"
        if (!enchantmentId.contains(":")) {
            enchantmentId = "minecraft:" + enchantmentId;
        }

        String pattern = "Reference\\{ResourceKey\\[minecraft:enchantment\\s*/\\s*(minecraft:[^\\]]+)\\][^}]*}\\s*=>\\s*(\\d+)";

        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(enchants);

        while (matcher.find()) {
            String key = matcher.group(1);
            int level;

            try {
                level = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                level = 0;
            }

            if (key.equals(enchantmentId)) {
                return level;
            }
        }

        return 0;
    }
}
