package com.skittlq.thestaff.client;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.util.StoredBlockModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;

@EventBusSubscriber(modid = TheStaff.MODID,  value = Dist.CLIENT)
public class TheStaffClientEvents {
    @SubscribeEvent
    public static void registerSelectProperties(RegisterSelectItemModelPropertyEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath("thestaff", "stored_block_id"),
                StoredBlockModelProperty.TYPE
        );
    }
}
