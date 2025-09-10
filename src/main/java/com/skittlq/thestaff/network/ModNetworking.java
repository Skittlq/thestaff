package com.skittlq.thestaff.network;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.abilities.StaffAbilities;
import com.skittlq.thestaff.anim.ClientPlayerAnimRuntime;
import com.skittlq.thestaff.items.custom.StaffItem;
import com.skittlq.thestaff.network.payloads.PlayPoseAnimPayload;
import com.skittlq.thestaff.util.DesktopPlacedData;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = TheStaff.MODID)
public final class ModNetworking {
    private ModNetworking() {}

    @SubscribeEvent
    public static void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("godestop")
                .requires(src -> src.hasPermission(2)) // only ops can use
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    var rl = ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "desktop");
                    var key = ResourceKey.create(Registries.DIMENSION, rl);
                    ServerLevel target = p.getServer().getLevel(key);
                    if (target == null) return 0;

                    // Drop the player in the void-dimension
                    p.setPortalCooldown(20);
                    p.teleportTo(target, 31.0, 66.0, 6.5,
                            Set.of(), p.getYRot(), p.getXRot(), true);
                    return 1;
                }));
    }

    private static final ResourceLocation DIM_KEY = ResourceLocation.fromNamespaceAndPath("thestaff", "desktop");
    private static final ResourceLocation STRUCT_KEY = ResourceLocation.fromNamespaceAndPath("thestaff", "alan_pc");

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load e) {
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().location().equals(DIM_KEY)) return;

        DesktopPlacedData data = DesktopPlacedData.get(level);
        if (data.isPlaced()) return;

        Optional<StructureTemplate> tmpl = level.getStructureManager().get(STRUCT_KEY);
        if (tmpl.isEmpty()) {
            return;
        }

        BlockPos origin = new BlockPos(0, 64, 0); // where you want it
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setKnownShape(true)
                .setIgnoreEntities(false)
                .setFinalizeEntities(true);

        tmpl.get().placeInWorld(level, origin, origin, settings, level.random, 2);

        data.markPlaced();
    }

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
