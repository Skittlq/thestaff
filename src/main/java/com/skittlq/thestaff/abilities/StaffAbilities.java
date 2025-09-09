// com/skittlq/thestaff/abilities/StaffAbilities.java
package com.skittlq.thestaff.abilities;

import com.mojang.logging.LogUtils;
import com.skittlq.thestaff.abilities.blocks.*;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StaffAbilities {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<ResourceLocation, BlockAbility> ABILITIES = new ConcurrentHashMap<>();

    private StaffAbilities() {}

    // Public API for other mods
    public static void register(ResourceLocation blockId, BlockAbility ability) {
        if (blockId == null || ability == null) {
            throw new IllegalArgumentException("blockId and ability must not be null");
        }
        var prev = ABILITIES.put(blockId, ability);
        if (prev != null) {
            LOGGER.warn("Overwrote StaffAbility for {} (was {}, now {}).", blockId, prev.getClass().getName(), ability.getClass().getName());
        }
    }

    public static BlockAbility get(ResourceLocation blockId) {
        return ABILITIES.getOrDefault(blockId, BlockAbilityFallback.INSTANCE);
    }

    // Your internal bootstrap (still allowed)
    public static void bootstrapInternal() {
        register(ResourceLocation.parse("minecraft:netherite_block"), new NetheriteBlockAbility());
        register(ResourceLocation.parse("minecraft:diamond_block"),   new DiamondBlockAbility());
        register(ResourceLocation.parse("minecraft:gold_block"),      new GoldBlockAbility());
        register(ResourceLocation.parse("minecraft:iron_block"),      new IronBlockAbility());
        register(ResourceLocation.parse("minecraft:copper_block"),    new CopperBlockAbility());
        register(ResourceLocation.parse("minecraft:coal_block"),      new CoalBlockAbility());
        register(ResourceLocation.parse("minecraft:obsidian"),        new ObsidianBlockAbility());
        register(ResourceLocation.parse("minecraft:cake"),            new CakeAbility());
        register(ResourceLocation.parse("minecraft:tnt"),             new TntAbility());
        register(ResourceLocation.parse("thestaff:minecraft"),        new MinecraftBlockAbility());
        register(ResourceLocation.parse("minecraft:redstone_block"),  new RedstoneBlockAbility());
        register(ResourceLocation.parse("minecraft:cobweb"),          new CobwebAbility());
    }
}
