package com.skittlq.thestaff.network.payloads;

import com.skittlq.thestaff.TheStaff;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record PlayPoseAnimPayload(UUID playerId, ResourceLocation animId, boolean play)
        implements CustomPacketPayload {

    public static final Type<PlayPoseAnimPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "play_pose_anim"));

    public static final StreamCodec<FriendlyByteBuf, java.util.UUID> UUID_CODEC =
            StreamCodec.of(
                    (buf, uuid) -> buf.writeUUID(uuid),
                    buf -> buf.readUUID()
            );


    public static final StreamCodec<FriendlyByteBuf, PlayPoseAnimPayload> STREAM_CODEC =
            StreamCodec.composite(UUID_CODEC,
                     PlayPoseAnimPayload::playerId,
                    ResourceLocation.STREAM_CODEC, PlayPoseAnimPayload::animId,
                    ByteBufCodecs.BOOL, PlayPoseAnimPayload::play,
                    PlayPoseAnimPayload::new
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
