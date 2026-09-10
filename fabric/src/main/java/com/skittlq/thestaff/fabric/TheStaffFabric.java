package com.skittlq.thestaff.fabric;

import com.skittlq.thestaff.TheStaffCommon;
import net.fabricmc.api.ModInitializer;

public final class TheStaffFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TheStaffCommon.initialize();
        // Register Fabric-specific server/common hooks here.
    }
}

