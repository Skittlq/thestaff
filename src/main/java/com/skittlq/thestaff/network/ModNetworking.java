package com.skittlq.thestaff.network;

import com.skittlq.thestaff.network.payloads.PlayAnimPayload;
import com.skittlq.thestaff.TheStaffClient;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "thestaff")
public final class ModNetworking {
    private ModNetworking() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToClient(
                PlayAnimPayload.TYPE,
                PlayAnimPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level == null) return;

                    AbstractClientPlayer target =
                            payload.playerId().equals(mc.player != null ? mc.player.getUUID() : null)
                                    ? mc.player
                                    : (mc.level.getPlayerByUUID(payload.playerId()) instanceof AbstractClientPlayer cp ? cp : null);

                    if (target == null) return;

                    var layer = com.zigythebird.playeranim.api.PlayerAnimationAccess
                            .getPlayerAnimationLayer(target, TheStaffClient.STAFF_LAYER_ID);

                    if (layer instanceof com.zigythebird.playeranim.animation.PlayerAnimationController controller) {
                        controller.triggerAnimation(payload.animId());
                    }
                })
        );
    }
}
