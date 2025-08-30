package com.skittlq.thestaff.util;

import com.skittlq.thestaff.network.payloads.PlayAnimPayload;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class AnimSender {
    public static void play(ServerPlayer sp, String animName) {
        var payload = new PlayAnimPayload(sp.getUUID(),
                ResourceLocation.fromNamespaceAndPath("thestaff", animName));
        sp.connection.send(new ClientboundCustomPayloadPacket(payload));
        sp.level().getChunkSource().broadcast(sp, new ClientboundCustomPayloadPacket(payload));
    }
}
