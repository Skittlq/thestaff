package com.skittlq.thestaff.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.skittlq.thestaff.TheStaff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Set;

public final class OmniBlockRenderer implements SpecialModelRenderer<Boolean> {

    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "textures/misc/glow.png");

    private static final float GLOW_SIZE     = 0.60f;
    private static final float GLOW_ALPHA    = 1.0f;
    private static final float GLOW_Z_NUDGE  = 0.002f;

    @Override public @Nullable Boolean extractArgument(ItemStack stack) { return Boolean.TRUE; }

    @Override
    public void render(@Nullable Boolean arg, ItemDisplayContext ctx, PoseStack pose,
                       MultiBufferSource buf, int light, int overlay, boolean foil) {

        final boolean isHand =
                ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND  ||
                        ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND  ||
                        ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        if (!isHand) return;

        pose.pushPose();

        switch (ctx) {
            case FIRST_PERSON_RIGHT_HAND -> {
                pose.mulPose(Axis.ZP.rotationDegrees(-8));
                pose.mulPose(Axis.XP.rotationDegrees(22));
                pose.mulPose(Axis.YP.rotationDegrees(64));
                pose.translate(-5/16f, 11/16f, 14/16f);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                pose.mulPose(Axis.ZP.rotationDegrees(8));
                pose.mulPose(Axis.XP.rotationDegrees(-22));
                pose.mulPose(Axis.YP.rotationDegrees(-64));
                pose.translate(15/16f, -1/16f, 2/16f);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                pose.translate(7/16f, 7/16f, 5/16f);
                pose.scale(.3f, .3f, .3f);
                faceCameraHere(pose);
            }
            default -> {}
        }

        pose.translate(0.0, 0.0, GLOW_Z_NUDGE);

        float size  = GLOW_SIZE;
        float alpha = GLOW_ALPHA;

        renderBillboardQuad(pose, buf, size, alpha);
        pose.popPose();
    }

    @Override public void getExtents(Set<Vector3f> out) {}

    private static void faceCameraHere(PoseStack pose) {
        var last = pose.last();
        Matrix4f m = new Matrix4f(last.pose());

        float tx = m.m30(), ty = m.m31(), tz = m.m32();

        Quaternionf camQ = new Quaternionf(
                Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        Matrix4f camOnly = new Matrix4f()
                .translation(tx, ty, tz)
                .rotate(camQ);

        last.pose().set(camOnly);
    }

    private static void renderBillboardQuad(PoseStack pose, MultiBufferSource buf, float size, float alpha) {
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
        @Override public SpecialModelRenderer<?> bake(EntityModelSet set) { return new OmniBlockRenderer(); }
        @Override public com.mojang.serialization.MapCodec<? extends Unbaked> type() { return MAP_CODEC; }
    }
}
