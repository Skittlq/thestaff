package com.skittlq.thestaff;

import com.skittlq.thestaff.util.StoredBlockModelProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
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
    static void onClientSetup(FMLClientSetupEvent event) {
        TheStaff.LOGGER.info("HELLO FROM CLIENT SETUP");
        TheStaff.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
