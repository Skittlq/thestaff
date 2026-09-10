package com.skittlq.thestaff.neoforge;

import com.skittlq.thestaff.TheStaffCommon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(TheStaffCommon.MOD_ID)
public final class TheStaffNeoForge {
    public TheStaffNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        TheStaffCommon.initialize();
        // Register NeoForge-specific content and listeners with modEventBus.
    }
}

