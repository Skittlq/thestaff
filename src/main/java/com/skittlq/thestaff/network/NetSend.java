package com.skittlq.thestaff.network;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.network.payloads.PlayPoseAnimPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;


public final class NetSend {

    public static void sendAnimToSelfAndTrackers(ServerPlayer sp, PlayPoseAnimPayload pkt) {
        PacketDistributor.sendToPlayer(sp, pkt);
        PacketDistributor.sendToPlayersTrackingEntity(sp, pkt);
    }
}
