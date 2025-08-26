package com.skittlq.thestaff.rendering;

import com.skittlq.thestaff.items.ModItems;
import com.skittlq.thestaff.items.custom.StaffItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class StaffItemRenderer implements ItemModel {

    public StaffItemRenderer() {
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext context, @Nullable ClientLevel level,
                       @Nullable LivingEntity entity, int seed) {

        // DO NOT re-invoke the base staff model via code.
        // The base model is already handled via the JSON definition.

        // Only render the stored block, if present
        ResourceLocation id = StaffItem.getStoredBlockId(stack);
        if (id != null) {
            var itemOpt = BuiltInRegistries.BLOCK.get(id);
            if (itemOpt.isPresent()) {
                Block block = itemOpt.get().value();
                if (!block.defaultBlockState().isAir()) {
                    ItemStack innerStack = new ItemStack(block);
                    resolver.updateForTopItem(state, innerStack, context, level, entity, seed);
                    state.appendModelIdentityElement(id.toString());
                }
            }
        }
        state.appendModelIdentityElement(context.name());
    }


}

