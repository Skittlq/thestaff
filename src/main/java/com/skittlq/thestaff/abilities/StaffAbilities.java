package com.skittlq.thestaff.abilities;

import com.skittlq.thestaff.abilities.blocks.*;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class StaffAbilities {
    private static final Map<ResourceLocation, BlockAbility> ABILITIES = new HashMap<>();

    static {
        register("minecraft:stone", new StoneAbility());
        register("minecraft:dirt", new DirtAbility());
        register("minecraft:oak_planks", new OakPlanksAbility());
        register("minecraft:command_block", new CommandBlockAbility());
        register("minecraft:netherite_block", new NetheriteBlockAbility());
    }

    private static void register(String blockId, BlockAbility ability) {
        ResourceLocation id = ResourceLocation.tryParse(blockId);
        ABILITIES.put(id, ability);
    }

    public static BlockAbility get(ResourceLocation blockId) {
        return ABILITIES.getOrDefault(blockId, BlockAbilityFallback.INSTANCE);
    }
}
