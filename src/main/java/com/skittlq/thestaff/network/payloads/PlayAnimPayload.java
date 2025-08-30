package com.skittlq.thestaff.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlayAnimPayload(java.util.UUID playerId, ResourceLocation animId)
        implements CustomPacketPayload {

    public static final Type<PlayAnimPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("thestaff", "play_anim"));

    public static final StreamCodec<FriendlyByteBuf, java.util.UUID> UUID_CODEC =
            StreamCodec.of(
                    (buf, uuid) -> buf.writeUUID(uuid),
                    buf -> buf.readUUID()
            );

    public static final StreamCodec<FriendlyByteBuf, PlayAnimPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUID_CODEC, PlayAnimPayload::playerId,
                    ResourceLocation.STREAM_CODEC, PlayAnimPayload::animId,
                    PlayAnimPayload::new
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
