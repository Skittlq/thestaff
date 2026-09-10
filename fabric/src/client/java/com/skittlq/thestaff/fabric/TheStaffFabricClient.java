package com.skittlq.thestaff.fabric;

import com.skittlq.thestaff.client.TheStaffClient;
import net.fabricmc.api.ClientModInitializer;

public final class TheStaffFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TheStaffClient.initialize();
        // Register Fabric-specific client hooks here.
    }
}

