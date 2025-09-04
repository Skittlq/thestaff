package com.skittlq.thestaff.items;

import com.skittlq.thestaff.TheStaff;
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

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
