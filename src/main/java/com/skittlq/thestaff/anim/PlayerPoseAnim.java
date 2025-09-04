package com.skittlq.thestaff.anim;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;

@FunctionalInterface
public interface PlayerPoseAnim {
    void apply(HumanoidModel<?> model, Player player, float timeSeconds, float partialTick);

    default void applyFirstPerson(FirstPersonContext ctx, Player player, float timeSeconds, float partialTick) {}

    /** New: hand-aware 3P hook. Default delegates to the legacy method. */
    default void applyThirdPerson(HumanoidModel<?> model, Player player,
                                  float timeSeconds, float partialTick,
                                  InteractionHand hand) {
        apply(model, player, timeSeconds, partialTick);
    }

    default boolean loop() { return true; }
    default float lengthSeconds() { return 1_000f; }
    default float fadeInSeconds() { return 0f; }
    default float fadeOutSeconds() { return 0f; }

    /** 0→1 during fade-in, 1 while “playing”, 1→0 during fade-out, else 0. */
    default float weight(float t) {
        final float L   = lengthSeconds();
        final float in  = Math.max(0f, fadeInSeconds());
        final float out = Math.max(0f, fadeOutSeconds());

        if (t >= 0f && t <= L) {
            float kIn  = in  <= 0f ? 1f : Math.min(1f, t / in);
            float kOut = 1f;
            return smooth(kIn) * kOut;
        }

        if (t > L && out > 0f && t <= L + out) {
            float u = 1f - ((t - L) / out);
            return smooth(u);
        }

        return 0f;
    }

    private static float smooth(float x) { return x * x * (3f - 2f * x); }

    record FirstPersonContext(
            InteractionHand hand,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            float partialTick,
            float interpolatedPitch,
            float swingProgress,
            float equipProgress,
            long seed
    ) {
        public void rotateXYZ(float x, float y, float z) {
            poseStack.mulPose(com.mojang.math.Axis.XP.rotation(x));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotation(y));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(z));
        }
        public void translate(float x, float y, float z) { poseStack.translate(x, y, z); }

        public float rand01(String key) {
            long h = mix(seed ^ key.hashCode());
            return ((h >>> 11) & ((1L<<53)-1)) / (float)(1L<<53);
        }
        public float randSigned(String key) { return rand01(key) * 2f - 1f; }

        private static long mix(long z) {
            z += 0x9E3779B97F4A7C15L;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            return z ^ (z >>> 31);
        }
    }
}
