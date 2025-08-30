package com.skittlq.thestaff.items.custom;

import com.skittlq.thestaff.blocks.ModBlocks;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public class MinecraftGameIconItem extends BlockItem {
    public MinecraftGameIconItem(Properties properties) {
        super(ModBlocks.MINECRAFT_GAME_ICON.get(), properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.getAbilities().instabuild && serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                ItemStack itemStack = context.getItemInHand().copy();
                InteractionResult result = super.place(context);
                if (result.consumesAction()) {
                    ItemStack heldItem = context.getItemInHand();
                    heldItem.shrink(1);
                    if (heldItem.isEmpty()) {
                        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    }
                }
                return result;
            }
        }

        return super.place(context);
    }
}
