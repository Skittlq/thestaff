package com.skittlq.thestaff.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.skittlq.thestaff.TheStaff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Set;

import com.skittlq.thestaff.blocks.ModBlocks;

public final class StaffRenderer implements SpecialModelRenderer<ResourceLocation> {
    private final BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();

    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "textures/misc/glow.png");
    private static final ResourceLocation SHADOW_TEX =
            ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "textures/misc/shadow.png");

    private static final float GLOW_SIZE  = 0.85f;
    private static final float GLOW_ALPHA = 1f;
    private static final float GLOW_Z_NUDGE = 0.002f;

    private static float fadeProgress = 0f;

    private static final java.util.concurrent.ConcurrentHashMap<String, Float> FADE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final ThreadLocal<String> TL_KEY = new ThreadLocal<>();
    private static final float FADE_SPEED = 8f;


    @Override @Nullable
    public ResourceLocation extractArgument(ItemStack stack) {
        return com.skittlq.thestaff.items.custom.StaffItem.getStoredBlockId(stack);
    }

    private static void applySlotTransform(ItemDisplayContext ctx, PoseStack pose) {
        switch (ctx) {
            case GUI -> {
                pose.mulPose(Axis.XP.rotationDegrees(-35));
                pose.mulPose(Axis.YP.rotationDegrees(-17));
                pose.mulPose(Axis.ZP.rotationDegrees(-45));
                pose.scale(0.2f, 0.2f, 0.2f);
                pose.translate(8/16f, 70/16f, 8/16f);
            }
            case GROUND -> {
                pose.scale(0.9f, 0.9f, 0.9f);
                pose.mulPose(Axis.XP.rotationDegrees(45));
                pose.mulPose(Axis.YP.rotationDegrees(45));
                pose.translate(5/16f, 44/16f, -8/16f);
            }
            case FIXED -> {
                pose.translate(5.995/16f, 11.55/16f, 6/16f);
                pose.scale(0.25f, 0.25f, 0.25f);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                pose.translate(-7.7/16f, 17.1/16f, -8.6/16f);
                pose.scale(0.7375f, 0.7375f, 0.7375f);
                pose.mulPose(Axis.ZP.rotationDegrees(-30.5f));
                pose.mulPose(Axis.XP.rotationDegrees(-6f));
            }
            case FIRST_PERSON_LEFT_HAND -> {
                pose.translate(13.35/16f, 11.25/16f, -6.975/16f);
                pose.scale(0.7375f, 0.7375f, 0.7375f);
                pose.mulPose(Axis.ZP.rotationDegrees(30.5f));
                pose.mulPose(Axis.XP.rotationDegrees(-5f));
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                pose.translate(1.08/16f, 21.54/16f, 2/16f);
                pose.scale(0.865f, 0.865f, 0.865f);
            }
            default -> {}
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
        if (state.hasProperty(GrassBlock.SNOWY)) {
            state = state.setValue(GrassBlock.SNOWY, Boolean.FALSE);
        }

        pose.pushPose();
        applySlotTransform(ctx, pose);
        blocks.renderSingleBlock(state, pose, buf, light, overlay);
        pose.popPose();

        boolean isHandContext =
                ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND  ||
                        ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND  ||
                        ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        boolean active = isHandContext && (
                (isPlayerFlying() && (item == ModBlocks.LIGHT_MINECRAFT.get().asItem() || item == ModBlocks.DARK_MINECRAFT.get().asItem()))
                        || (item == ModBlocks.OMNIBLOCK.get().asItem())
        );

        updateFade(active);

        if (fadeProgress > 0f) {
            pose.pushPose();

            if (ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                pose.translate(8.5/16f, 28/16f, 8/16f);
            } else if (ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                pose.translate(16/16f, 20/16f, -4/16f);
            } else if (ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                pose.translate(0/16f, 20/16f, -6/16f);
            }
            faceCameraReplacingRotation(pose);

            pose.translate(0.0, 0.0, GLOW_Z_NUDGE);

            float eased = 0.5f - 0.5f * (float)Math.cos(Math.PI * fadeProgress);
            float size  = GLOW_SIZE  * eased;
            float alpha = GLOW_ALPHA * eased;

            pose.scale(size, size, size);
            renderBillboardGlow(pose, buf, 1f, alpha);
            pose.popPose();
        }


    }

    private static void faceCameraReplacingRotation(PoseStack pose) {
        var disp = Minecraft.getInstance().getEntityRenderDispatcher();
        var camQ = new org.joml.Quaternionf(disp.cameraOrientation());

        PoseStack.Pose last = pose.last();
        Matrix4f poseMat   = last.pose();
        Matrix3f normalMat = last.normal();

        Vector3f t = new Vector3f();
        poseMat.getTranslation(t);

        poseMat.identity()
                .translate(t)
                .rotate(camQ);

        normalMat.identity()
                .rotate(camQ);
    }


    @Override
    public void getExtents(Set<Vector3f> out) {
        out.add(new Vector3f(6f/16f, 18f/16f, 6f/16f));
        out.add(new Vector3f(10f/16f, 22f/16f, 10f/16f));
    }

    private static void updateFade(boolean active) {
        long ns = Minecraft.getInstance().getFrameTimeNs();
        float deltaSeconds = ns / 1_000_000_000f;

        float step = FADE_SPEED * deltaSeconds;
        if (active) fadeProgress = Math.min(1f, fadeProgress + step);
        else        fadeProgress = Math.max(0f, fadeProgress - step);
    }

    private static boolean isPlayerFlying() {
        var mc = Minecraft.getInstance();
        var pl = mc.player;
        return pl != null && (pl.isFallFlying() || pl.getAbilities().flying);
    }

    private static void renderBillboardGlow(PoseStack pose, MultiBufferSource buf, float size, float alpha) {
        pose.scale(size, size, size);

        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive(GLOW_TEX));
        var last = pose.last();
        Matrix4f mat = last.pose();

        int light   = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;

        float u0 = 0.01f, v0 = 0.01f, u1 = 0.99f, v1 = 0.99f;

        vc.addVertex(mat, -1f, -1f, 0f).setColor(1f,1f,1f,alpha)
                .setUv(u0,v1).setOverlay(overlay).setLight(light).setNormal(last, 0,0,1);
        vc.addVertex(mat, -1f,  1f, 0f).setColor(1f,1f,1f,alpha)
                .setUv(u0,v0).setOverlay(overlay).setLight(light).setNormal(last, 0,0,1);
        vc.addVertex(mat,  1f,  1f, 0f).setColor(1f,1f,1f,alpha)
                .setUv(u1,v0).setOverlay(overlay).setLight(light).setNormal(last, 0,0,1);
        vc.addVertex(mat,  1f, -1f, 0f).setColor(1f,1f,1f,alpha)
                .setUv(u1,v1).setOverlay(overlay).setLight(light).setNormal(last, 0,0,1);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final com.mojang.serialization.MapCodec<Unbaked> MAP_CODEC =
                com.mojang.serialization.MapCodec.unit(new Unbaked());
        @Override public SpecialModelRenderer<?> bake(EntityModelSet set) { return new StaffRenderer(); }
        @Override public com.mojang.serialization.MapCodec<? extends Unbaked> type() { return MAP_CODEC; }
    }
}
