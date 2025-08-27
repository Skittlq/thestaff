package com.skittlq.thestaff;

import com.skittlq.thestaff.rendering.StaffSpecialRenderer;
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
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent e) {
        e.register(ResourceLocation.fromNamespaceAndPath("thestaff","staff_block_slot"),
                StaffSpecialRenderer.Unbaked.MAP_CODEC);
    }
}
