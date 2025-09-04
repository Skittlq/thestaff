package com.skittlq.thestaff.network;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.anim.ClientPlayerAnimRuntime;
import com.skittlq.thestaff.network.payloads.PlayPoseAnimPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = TheStaff.MODID)
public final class ModNetworking {
    private ModNetworking() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent e) {
        var reg = e.registrar(TheStaff.MODID);
        reg.playToClient(PlayPoseAnimPayload.TYPE, PlayPoseAnimPayload.STREAM_CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                var mc = Minecraft.getInstance();
                if (mc.level == null) return;
                var p = mc.level.getPlayerByUUID(payload.playerId());
                if (p == null) return;
                TheStaff.LOGGER.info("[ANIM] RECEIVED ← uuid={} name={} anim={} play={}",
                                             payload.playerId(), p.getGameProfile().getName(), payload.animId(), payload.play());

                if (payload.play()) ClientPlayerAnimRuntime.play(p, payload.animId());
                else ClientPlayerAnimRuntime.stop(p);
            });
        });
    }
}
