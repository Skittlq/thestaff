package com.skittlq.thestaff.network;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.abilities.StaffAbilities;
import com.skittlq.thestaff.anim.ClientPlayerAnimRuntime;
import com.skittlq.thestaff.items.custom.StaffItem;
import com.skittlq.thestaff.network.payloads.PlayPoseAnimPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
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

                if (payload.play()) ClientPlayerAnimRuntime.play(p, payload.animId());
                else ClientPlayerAnimRuntime.stop(p);
            });
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof StaffItem staff)) return;

        var id = StaffItem.getStoredBlockId(stack);
        if (id != null) {
            if (player.isShiftKeyDown()) {
                StaffAbilities.get(id).onShiftBreakBlock(player.level(), player, event.getPos(), stack);
            } else {
                StaffAbilities.get(id).onBreakBlock(player.level(), player, event.getPos(), stack);
            }
        }
    }
}
