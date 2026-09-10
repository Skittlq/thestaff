package com.skittlq.thestaff.neoforge;

import com.skittlq.thestaff.TheStaffCommon;
import com.skittlq.thestaff.client.TheStaffClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = TheStaffCommon.MOD_ID, dist = Dist.CLIENT)
public final class TheStaffNeoForgeClient {
    public TheStaffNeoForgeClient(ModContainer container) {
        TheStaffClient.initialize();
        // Register NeoForge-specific client hooks here.
    }
}
