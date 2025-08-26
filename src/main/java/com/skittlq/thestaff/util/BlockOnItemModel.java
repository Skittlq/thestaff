package com.skittlq.thestaff.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BlockOnItemModel implements ItemModel {
    private final ItemModel original;

    public BlockOnItemModel(ItemModel original) {
        this.original = original;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
        // Render the original item model first
        original.update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, livingEntity, i);

        // Fetch the stored block from the staff's NBT
        var storedBlockStack = com.skittlq.thestaff.items.custom.StaffItem.getStoredBlock(itemStack);

        if (!storedBlockStack.isEmpty() && storedBlockStack.getItem() instanceof BlockItem blockItem) {
            // Render the stored block atop the staff
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    blockItem.getBlock().defaultBlockState(),
                    itemStackRenderState.poseStack,
                    itemStackRenderState.bufferSource,
                    15728880, // Max light, adjust if you wish
                    OverlayTexture.NO_OVERLAY
            );
        }
    }
}
