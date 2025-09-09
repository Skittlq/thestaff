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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OmniBlockRenderer implements SpecialModelRenderer<Boolean> {

    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "textures/misc/glow.png");

    private static final float GLOW_SIZE     = 0.60f;
    private static final float GLOW_ALPHA    = 1.0f;
    private static final float GLOW_Z_NUDGE  = 0.002f;

    private static final float FADE_SPEED = 8f;

    // Per-item fades keyed by <uid>|<hand-context>
    private static final ConcurrentHashMap<String, Float> FADE = new ConcurrentHashMap<>();
    private static final String RENDER_TAG = TheStaff.MODID + ":render";
    private static final String UID_TAG    = "uid";

    // Capture the exact stack being rendered (may belong to another player)
    private static final ThreadLocal<ItemStack> TL_STACK = new ThreadLocal<>();

    @Override
    public @Nullable Boolean extractArgument(ItemStack stack) {
        TL_STACK.set(stack);
        return Boolean.TRUE;
    }

    @Override
    public void render(@Nullable Boolean arg, ItemDisplayContext ctx, PoseStack pose,
                       MultiBufferSource buf, int light, int overlay, boolean foil) {

        final boolean isHand =
                ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND  ||
                        ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND  ||
                        ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        if (!isHand) { TL_STACK.remove(); return; }

        final ItemStack stack = TL_STACK.get();
        TL_STACK.remove();

        // Always active while held; fade is per *stack*
        String fadeKey = makeFadeKeyFromStack(stack, ctx);
        float fadeProgress = updateFade(fadeKey, true);
        if (fadeProgress <= 0f) return;

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

        float eased = 0.5f - 0.5f * (float)Math.cos(Math.PI * fadeProgress);
        float size  = GLOW_SIZE  * eased;
        float alpha = GLOW_ALPHA * eased;

        renderBillboardQuad(pose, buf, size, alpha);
        pose.popPose();
    }

    @Override public void getExtents(Set<Vector3f> out) {}

    private static float updateFade(String key, boolean active) {
        float dt = Minecraft.getInstance().getFrameTimeNs() / 1_000_000_000f;
        float step = FADE_SPEED * dt;
        float cur = FADE.getOrDefault(key, 0f);
        cur = active ? Math.min(1f, cur + step) : Math.max(0f, cur - step);
        FADE.put(key, cur);
        return cur;
    }

    private static String makeFadeKeyFromStack(ItemStack stack, ItemDisplayContext ctx) {
        if (stack == null || stack.isEmpty()) return "no-stack|" + ctx.name();

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = (data != null) ? data.copyTag() : new CompoundTag();

        CompoundTag renderTag = root.getCompound(RENDER_TAG).orElse(new CompoundTag());
        String uid = renderTag.getString(UID_TAG).orElse("");
        if (uid.isEmpty()) {
            uid = UUID.randomUUID().toString();
            renderTag.putString(UID_TAG, uid);
            root.put(RENDER_TAG, renderTag);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        }
        return uid + "|" + ctx.name();
    }

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
