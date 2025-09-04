package com.skittlq.thestaff.anim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.model.HumanoidModel;

public final class BuiltinAnims {
    public static final ResourceLocation RAISE_RIGHT = ResourceLocation.fromNamespaceAndPath("thestaff", "raise_right");

    public static void registerAll() {
        PlayerPoseAnims.register(RAISE_RIGHT, new PlayerPoseAnim() {
            @Override public boolean loop() { return false; }
            @Override public float lengthSeconds() { return 0.15f; }
            @Override public float fadeOutSeconds() { return 1f; }
            @Override
            public float fadeInSeconds() {
                return 0.15f;
            }

            @Override
            public void apply(HumanoidModel<?> m, Player p, float t, float pt) {
                float k = weight(t);
                if (k <= 0f) return;
                float a = (float)Math.toRadians(75f) * k;
                m.rightArm.xRot -= a;
                m.rightArm.yRot += a * -0.15f;
                m.rightArm.zRot += a * 0.15f;
            }

            @Override
            public void applyThirdPerson(HumanoidModel<?> m, Player p, float t, float pt, InteractionHand hand) {
                float k = weight(t);
                if (k <= 0f) return;

                float a = (float)Math.toRadians(75f) * k;

                if (hand == InteractionHand.MAIN_HAND) {
                    m.rightArm.xRot -= a;
                    m.rightArm.yRot += a * -0.15f;
                    m.rightArm.zRot += a *  0.15f;
                } else {
                    m.leftArm.xRot  -= a;
                    m.leftArm.yRot  += a *  0.15f;
                    m.leftArm.zRot  += a * -0.15f;
                }
            }

            @Override
            public void applyFirstPerson(FirstPersonContext ctx, Player p, float t, float pt) {
                float k = weight(t);
                if (k <= 0f) return;

                float yawJitter = ctx.randSigned("raise:yaw") * 0.5f;

                if (ctx.hand() == InteractionHand.MAIN_HAND) {
                    ctx.translate(-0f * k, 0.6f * k, -1f * k);
                    ctx.rotateXYZ(-1f * k, (0.5f - yawJitter) * 0.5f * k, (0.5f - yawJitter) * 0.5f * k);
                }
                else if (ctx.hand() == InteractionHand.OFF_HAND) {
                    ctx.translate(-0f * k, 0.6f * k, -1f * k);
                    ctx.rotateXYZ(-1f * k, -((0.5f - yawJitter) * 0.5f * k), -((0.5f - yawJitter) * 0.5f * k));
                }
            }

        });
    }
}
