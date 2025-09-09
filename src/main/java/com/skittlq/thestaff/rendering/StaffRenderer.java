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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.skittlq.thestaff.blocks.ModBlocks;

public final class StaffRenderer implements SpecialModelRenderer<ResourceLocation> {
    private final BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();

    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "textures/misc/glow.png");
    private static final ResourceLocation SHADOW_TEX =
            ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "textures/misc/shadow.png");

    private static final float GLOW_SIZE    = 0.85f;
    private static final float GLOW_ALPHA   = 1f;
    private static final float GLOW_Z_NUDGE = 0.002f;

    private static final ConcurrentHashMap<String, Float> FADE = new ConcurrentHashMap<>();
    private static final float FADE_SPEED = 8f;

    private static final String RENDER_TAG = TheStaff.MODID + ":render";
    private static final String UID_TAG    = "uid";
    private static final String ACTIVE_TAG = "active"; // written server-side on the staff stack

    // Capture the exact staff stack for this render pass
    private static final ThreadLocal<ItemStack> TL_STACK = new ThreadLocal<>();

    @Override @Nullable
    public ResourceLocation extractArgument(ItemStack stack) {
        TL_STACK.set(stack);
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
    public void render(@Nullable ResourceLocation id,
                       ItemDisplayContext ctx,
                       PoseStack pose,
                       MultiBufferSource buf,
                       int light,
                       int overlay,
                       boolean foil) {
        // If extractArgument returned null, clear any stray TL and leave quietly.
        if (id == null) { TL_STACK.remove(); return; }

        // The exact staff stack captured in extractArgument; may be null in odd paths.
        final ItemStack staffStack = TL_STACK.get();
        TL_STACK.remove(); // always clear immediately

        // Resolve the stored item safely; only proceed if it's a BlockItem.
        var holder = BuiltInRegistries.ITEM.get(id);
        if (holder.isEmpty()) return;
        Item item = holder.get().value();
        if (!(item instanceof BlockItem bi)) return;

        // Render the held block model with strict push/pop hygiene.
        BlockState state = bi.getBlock().defaultBlockState();
        if (state.hasProperty(GrassBlock.SNOWY)) {
            state = state.setValue(GrassBlock.SNOWY, Boolean.FALSE);
        }

        pose.pushPose();
        try {
            applySlotTransform(ctx, pose);
            blocks.renderSingleBlock(state, pose, buf, light, overlay);
        } finally {
            pose.popPose();
        }

        // Only hands can show the glow/shadow.
        boolean isHandContext =
                ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND  ||
                        ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND  ||
                        ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        if (!isHandContext) return;

        // If the staff stack couldn't be captured, skip all glow logic safely.
        if (staffStack == null || staffStack.isEmpty()) return;

        // Identify which special block we’re holding.
        boolean isOmni  = (item == ModBlocks.OMNIBLOCK.get().asItem());
        boolean isDark  = (item == ModBlocks.DARK_MINECRAFT.get().asItem());
        boolean isLight = (item == ModBlocks.LIGHT_MINECRAFT.get().asItem());

        // Per-stack fade key (your existing helper).
        String fadeKey = makeFadeKeyFromStack(staffStack, ctx);

        // Active rule: FP uses local flight for Light/Dark; Omni always; TP reads per-stack flag.
        boolean active = switch (ctx) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND ->
                    isOmni || (isLocalPlayerFlying() && (isLight || isDark));
            default -> isStackActive(staffStack);
        };

        float fade = updateFade(fadeKey, active);
        if (fade <= 0f) return;

        pose.pushPose();
        try {
            // Position the billboard near the hand.
            if (ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                pose.translate(8.5/16f, 28/16f, 8/16f);
            } else if (ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                pose.translate(16/16f, 20/16f, -4/16f);
            } else { // FIRST_PERSON_RIGHT_HAND
                pose.translate(0/16f, 20/16f, -6/16f);
            }

            faceCameraReplacingRotation(pose);
            pose.translate(0.0, 0.0, GLOW_Z_NUDGE);

            float eased = 0.5f - 0.5f * (float)Math.cos(Math.PI * fade);
            float size  = GLOW_SIZE  * eased;
            float alpha = GLOW_ALPHA * eased;

            renderBillboardSprite(pose, buf, GLOW_TEX, size, alpha);
            // If you also draw a shadow, call renderBillboardSprite(...) with SHADOW_TEX as needed.
        } finally {
            pose.popPose();
        }
    }

    /** Build/read a persistent uid on the *staff* stack and return "<uid>|<ctx>" */
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

    private static boolean isStackActive(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        CompoundTag root = data.copyTag();
        CompoundTag renderTag = root.getCompound(RENDER_TAG).orElse(new CompoundTag());
        return renderTag.getBoolean(ACTIVE_TAG).orElse(false);
    }

    private static boolean isLocalPlayerFlying() {
        var mc = Minecraft.getInstance();
        var pl = mc.player;
        return pl != null && (pl.isFallFlying() || pl.getAbilities().flying);
    }

    private static float updateFade(String key, boolean active) {
        long ns = Minecraft.getInstance().getFrameTimeNs();
        float dt = ns / 1_000_000_000f;
        float cur = FADE.getOrDefault(key, 0f);
        float step = FADE_SPEED * dt;
        cur = active ? Math.min(1f, cur + step) : Math.max(0f, cur - step);
        FADE.put(key, cur);
        return cur;
    }

    private static void faceCameraReplacingRotation(PoseStack pose) {
        var disp = Minecraft.getInstance().getEntityRenderDispatcher();
        var camQ = new org.joml.Quaternionf(disp.cameraOrientation());

        PoseStack.Pose last = pose.last();
        Matrix4f poseMat   = last.pose();
        Matrix3f normalMat = last.normal();

        Vector3f t = new Vector3f();
        poseMat.getTranslation(t);

        poseMat.identity().translate(t).rotate(camQ);
        normalMat.identity().rotate(camQ);
    }

    @Override
    public void getExtents(Set<Vector3f> out) {
        out.add(new Vector3f(6f/16f, 18f/16f, 6f/16f));
        out.add(new Vector3f(10f/16f, 22f/16f, 10/16f));
    }

    private static void renderBillboardSprite(PoseStack pose,
                                              MultiBufferSource buf,
                                              ResourceLocation tex,
                                              float size,
                                              float alpha) {
        pose.scale(size, size, size);

        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive(tex));
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
