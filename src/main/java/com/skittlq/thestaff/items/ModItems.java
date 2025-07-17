package com.skittlq.thestaff.items;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.items.custom.StaffItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheStaff.MODID);

    public static final DeferredItem<Item> PURPLE_STAFF = ITEMS.registerItem("purple_staff",
            (properties) -> new StaffItem(properties.fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
