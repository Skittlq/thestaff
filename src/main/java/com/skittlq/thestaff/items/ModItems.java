package com.skittlq.thestaff.items;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.blocks.ModBlocks;
import com.skittlq.thestaff.items.custom.DarkMinecraftItem;
import com.skittlq.thestaff.items.custom.LightMinecraftItem;
import com.skittlq.thestaff.items.custom.OmniBlockItem;
import com.skittlq.thestaff.items.custom.StaffItem;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheStaff.MODID);

    public static final DeferredItem<Item> PURPLE_STAFF = ITEMS.registerItem("purple_staff",
            (properties) -> new StaffItem(properties.fireResistant().rarity(Rarity.EPIC).stacksTo(1)));

    public static final DeferredItem<BlockItem> LIGHT_MINECRAFT = ITEMS.registerItem("light_minecraft",
            (properties) -> new LightMinecraftItem(properties.fireResistant().rarity(Rarity.EPIC).stacksTo(1)));

    public static final DeferredItem<BlockItem> DARK_MINECRAFT = ITEMS.registerItem("dark_minecraft",
            (properties) -> new DarkMinecraftItem(properties.fireResistant().rarity(Rarity.EPIC).stacksTo(1)));

    public static final DeferredItem<BlockItem> OMNIBLOCK = ITEMS.registerItem("omniblock",
            (properties) -> new OmniBlockItem(properties.fireResistant().rarity(Rarity.EPIC).stacksTo(1)));

    public static final DeferredItem<BlockItem> WINDOWS = ITEMS.registerSimpleBlockItem("windows", ModBlocks.WINDOWS, new Item.Properties());
    public static final DeferredItem<BlockItem> TASKVIEW = ITEMS.registerSimpleBlockItem("taskview", ModBlocks.TASKVIEW, new Item.Properties());
    public static final DeferredItem<BlockItem> EXPLORER = ITEMS.registerSimpleBlockItem("explorer", ModBlocks.EXPLORER, new Item.Properties());
    public static final DeferredItem<BlockItem> CHROME = ITEMS.registerSimpleBlockItem("chrome", ModBlocks.CHROME, new Item.Properties());
    public static final DeferredItem<BlockItem> ANIMATE = ITEMS.registerSimpleBlockItem("animate", ModBlocks.ANIMATE, new Item.Properties());
    public static final DeferredItem<BlockItem> GAMEICON = ITEMS.registerSimpleBlockItem("game_icon", ModBlocks.GAMEICON, new Item.Properties());
    public static final DeferredItem<BlockItem> AFTER_EFFECTS = ITEMS.registerSimpleBlockItem("after_effects", ModBlocks.AFTER_EFFECTS, new Item.Properties());
    public static final DeferredItem<BlockItem> PREMIERE_PRO = ITEMS.registerSimpleBlockItem("premiere_pro", ModBlocks.PREMIERE_PRO, new Item.Properties());
    public static final DeferredItem<BlockItem> PHOTOSHOP = ITEMS.registerSimpleBlockItem("photoshop", ModBlocks.PHOTOSHOP, new Item.Properties());
    public static final DeferredItem<BlockItem> AUDACITY = ITEMS.registerSimpleBlockItem("audacity", ModBlocks.AUDACITY, new Item.Properties());
    public static final DeferredItem<BlockItem> TASKBAR_ITEMS = ITEMS.registerSimpleBlockItem("taskbar_items", ModBlocks.TASKBAR_ITEMS, new Item.Properties());
    public static final DeferredItem<BlockItem> WALLPAPER = ITEMS.registerSimpleBlockItem("wallpaper", ModBlocks.WALLPAPER, new Item.Properties());
    public static final DeferredItem<BlockItem> TASKBAR_BLOCK = ITEMS.registerSimpleBlockItem("taskbar_block", ModBlocks.TASKBAR_BLOCK, new Item.Properties());
    public static final DeferredItem<BlockItem> RECYCLE_BIN = ITEMS.registerSimpleBlockItem("recycle_bin", ModBlocks.RECYCLE_BIN, new Item.Properties());
    public static final DeferredItem<BlockItem> STORAGE_FOLDER = ITEMS.registerSimpleBlockItem("storage_folder", ModBlocks.STORAGE_FOLDER, new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
