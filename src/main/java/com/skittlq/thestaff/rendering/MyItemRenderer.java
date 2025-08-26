package com.skittlq.thestaff.rendering;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.registries.BuiltInRegistries;

public class MyItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final Minecraft mc = Minecraft.getInstance();

    public MyItemRenderer() {
        super(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("StoredBlock")) {
            // Get block from the NBT string
            String blockId = tag.getString("StoredBlock");
            Block block = BuiltInRegistries.BLOCK.getOptional(new net.minecraft.resources.ResourceLocation(blockId)).orElse(null);
            if (block != null) {
                ItemStack blockStack = new ItemStack(block);

                poseStack.pushPose();
                poseStack.scale(0.5f, 0.5f, 0.5f); // Scale down the block
                poseStack.translate(0.5, 0.5, 0.5); // Center it
                mc.getItemRenderer().renderStatic(blockStack, transformType, light, overlay, poseStack, buffer, mc.level, 0);
                poseStack.popPose();
            }
        }
        // Optionally: render the base item as well, if needed
    }
}
