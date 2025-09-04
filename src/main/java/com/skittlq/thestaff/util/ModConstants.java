package com.skittlq.thestaff.util;

import com.skittlq.thestaff.items.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class ModConstants {
    public static final List<Item> ALLOWED_ITEMS = List.of(
            Items.NETHERITE_BLOCK,
            Items.DIAMOND_BLOCK,
            Items.GOLD_BLOCK,
            Items.IRON_BLOCK,
            Items.COPPER_BLOCK,
            Items.COAL_BLOCK,
            Items.OBSIDIAN,
            Items.CAKE,
            Items.TNT,
            ModItems.LIGHT_MINECRAFT.asItem(),
            ModItems.OMNIBLOCK.asItem(),
            ModItems.DARK_MINECRAFT.asItem(),
            Items.GRASS_BLOCK
    );

}
