// src/client/java/com/skittlq/thestaff/client/StaffSpecialRenderer.java
package com.skittlq.thestaff.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Set;

public final class StaffSpecialRenderer implements SpecialModelRenderer<ResourceLocation> {
    private final BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();

    // We only need the stored block id from the stack.
    @Override @Nullable
    public ResourceLocation extractArgument(ItemStack stack) {
        return com.skittlq.thestaff.items.custom.StaffItem.getStoredBlockId(stack);
    }

    private static void applySlotTransform(ItemDisplayContext ctx, PoseStack pose) {
        switch (ctx) {
            case GUI -> {                   // Inventory / hotbar / recipe book
                pose.mulPose(Axis.XP.rotationDegrees(-35));
                pose.mulPose(Axis.YP.rotationDegrees(-17));
                pose.mulPose(Axis.ZP.rotationDegrees(-45));
                pose.scale(0.2f, 0.2f, 0.2f);
                pose.translate(8/16f, 70/16f, 8/16f);
            }
            case GROUND -> {                // Dropped on ground
                pose.scale(0.9f, 0.9f, 0.9f);
                pose.mulPose(Axis.ZP.rotationDegrees(0));
                pose.mulPose(Axis.XP.rotationDegrees(45));
                pose.mulPose(Axis.YP.rotationDegrees(45));
                pose.translate(5/16f, 44/16f, -8/16f); // up into the “cage” area
            }
            case FIXED -> {                 // Item frames / lecterns
                pose.translate(5.995/16f, 11.55/16f, 6/16f);
                pose.scale(0.25f, 0.25f, 0.25f);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                pose.translate(-7.7/16f, 17.1/16f, -8.6/16f); // up into the “cage” area
                pose.scale(0.7375f, 0.7375f, 0.7375f);
                pose.mulPose(Axis.ZP.rotationDegrees(-30.5f));
                pose.mulPose(Axis.XP.rotationDegrees(-6f));
            }
            case FIRST_PERSON_LEFT_HAND -> {
                pose.translate(13.35/16f, 11.25/16f, -6.975/16f); // up into the “cage” area
                pose.scale(0.7375f, 0.7375f, 0.7375f);
                pose.mulPose(Axis.ZP.rotationDegrees(30.5f));
                pose.mulPose(Axis.XP.rotationDegrees(-5f));
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                pose.translate(1.08/16f, 21.54/16f, 2/16f);
                pose.scale(0.865f, 0.865f, 0.865f);
//                pose.mulPose(Axis.YP.rotationDegrees(15));
            }
            case THIRD_PERSON_LEFT_HAND -> {
                pose.translate(1.08/16f, 21.54/16f, 2/16f);
                pose.scale(0.865f, 0.865f, 0.865f);
//                pose.mulPose(Axis.YP.rotationDegrees(15));
            }
            default -> { // fall back
                pose.translate(0,0,0);
                pose.scale(0,0,0);
            }
        }
    }

    @Override
    public void render(@Nullable ResourceLocation id, ItemDisplayContext ctx, PoseStack pose,
                       MultiBufferSource buf, int light, int overlay, boolean foil) {
        if (id == null) return;

        var holder = BuiltInRegistries.ITEM.get(id);
        if (holder.isEmpty()) return;
        Item item = holder.get().value();
        if (!(item instanceof BlockItem bi)) return;

        BlockState state = bi.getBlock().defaultBlockState();

        pose.pushPose();
        applySlotTransform(ctx, pose);

        blocks.renderSingleBlock(state, pose, buf, light, overlay);
        pose.popPose();
    }

    // GUI extents for oversized rendering bounds (rough cube around the gem)
    @Override
    public void getExtents(Set<Vector3f> out) {
        out.add(new Vector3f(6f/16f, 18f/16f, 6f/16f));
        out.add(new Vector3f(10f/16f, 22f/16f, 10f/16f));
    }

    // ----- Unbaked inner type for JSON hookup -----
    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final com.mojang.serialization.MapCodec<Unbaked> MAP_CODEC =
                com.mojang.serialization.MapCodec.unit(new Unbaked());
        @Override public SpecialModelRenderer<?> bake(EntityModelSet set) { return new StaffSpecialRenderer(); }
        @Override public com.mojang.serialization.MapCodec<? extends Unbaked> type() { return MAP_CODEC; }
    }
}
