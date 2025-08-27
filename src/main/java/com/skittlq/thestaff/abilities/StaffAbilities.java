package com.skittlq.thestaff.abilities;

import com.skittlq.thestaff.abilities.blocks.*;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class StaffAbilities {
    private static final Map<ResourceLocation, BlockAbility> ABILITIES = new HashMap<>();

    static {
        register("minecraft:netherite_block", new NetheriteBlockAbility());
        register("minecraft:diamond_block", new DiamondBlockAbility());
        register("minecraft:gold_block", new GoldBlockAbility());
        register("minecraft:iron_block", new IronBlockAbility());
        register("minecraft:copper_block", new CopperBlockAbility());
        register("minecraft:coal_block", new CoalBlockAbility());
        register("minecraft:obsidian", new ObsidianBlockAbility());
        register("minecraft:cake", new CakeAbility());
    }

    private static void register(String blockId, BlockAbility ability) {
        ResourceLocation id = ResourceLocation.tryParse(blockId);
        ABILITIES.put(id, ability);
    }

    public static BlockAbility get(ResourceLocation blockId) {
        return ABILITIES.getOrDefault(blockId, BlockAbilityFallback.INSTANCE);
    }
}
