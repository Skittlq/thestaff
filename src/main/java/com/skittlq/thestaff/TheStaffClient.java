package com.skittlq.thestaff;

import com.skittlq.thestaff.rendering.ExampleSpecialRenderer;
import com.skittlq.thestaff.rendering.StaffItemRendererUnbaked;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = TheStaff.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TheStaff.MODID, value = Dist.CLIENT)
public class TheStaffClient {
    public TheStaffClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath("thestaff", "block_staff"),
                StaffItemRendererUnbaked.MAP_CODEC
        );
    }
}
