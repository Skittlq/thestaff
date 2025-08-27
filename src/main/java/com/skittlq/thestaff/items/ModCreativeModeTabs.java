package com.skittlq.thestaff.items;

import com.skittlq.thestaff.TheStaff;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheStaff.MODID);

    public static final Supplier<CreativeModeTab> THESTAFF_TAB = CREATIVE_MODE_TAB.register("thestaff_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PURPLE_STAFF.get()))
                    .title(Component.translatable("creativetab.thestaff"))
                    .displayItems((itemDisplayParameters, output) -> {
                                output.accept(ModItems.PURPLE_STAFF.get());
                            }
                    ).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }

}
