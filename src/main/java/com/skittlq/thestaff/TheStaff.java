package com.skittlq.thestaff;

import com.skittlq.thestaff.blocks.ModBlocks;
import com.skittlq.thestaff.items.ModCreativeModeTabs;
import com.skittlq.thestaff.items.ModItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(TheStaff.MODID)
public class TheStaff {
    public static final String MODID = "thestaff";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheStaff(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        NeoForge.EVENT_BUS.register(com.skittlq.thestaff.util.ScheduleBatchDestruction.class);
        ModBlocks.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
