package com.skittlq.thestaff.entities;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.items.custom.StaffItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(TheStaff.MODID);

//    public static final Supplier<EntityType<WebHookEntity>> WEB_HOOK = ENTITY_TYPES.registerEntityType(
//            "web_hook", WebHookEntity::new, MobCategory.MISC
//    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
