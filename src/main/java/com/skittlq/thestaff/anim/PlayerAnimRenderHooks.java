package com.skittlq.thestaff.anim;

import com.skittlq.thestaff.TheStaff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.IdentityHashMap;
import java.util.Map;

@EventBusSubscriber(modid = TheStaff.MODID, value = Dist.CLIENT)
public final class PlayerAnimRenderHooks {
    private record Rot(float x, float y, float z) {}
    private record SavedPose(Rot head, Rot body, Rot rArm, Rot lArm, Rot rLeg, Rot lLeg) {}

    private static final Map<LivingEntityRenderState, SavedPose> POSE_CACHE = new IdentityHashMap<>();

    private PlayerAnimRenderHooks() {}

    @SubscribeEvent
    public static void onPre(net.neoforged.neoforge.client.event.RenderLivingEvent.Pre<?, ?, ?> event) {
        var active = event.getRenderState().getRenderData(AnimRenderStateBridge.ACTIVE);
        if (active == null) return;

        if (event.getRenderer().getModel() instanceof net.minecraft.client.model.HumanoidModel<?> m) {
        }
    }

    @SubscribeEvent
    public static void onPost(net.neoforged.neoforge.client.event.RenderLivingEvent.Post<?, ?, ?> event) {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent e) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;

        var state = ClientPlayerAnimRuntime.current(player);
        if (state == null) return;

        var anim = PlayerPoseAnims.get(state.id());
        if (anim == null) return;

        float t  = ClientPlayerAnimRuntime.elapsedSeconds(player, state, e.getPartialTick());

        var ps = e.getPoseStack();
        ps.pushPose();
        try {
            var ctx = new PlayerPoseAnim.FirstPersonContext(
                    e.getHand(), ps, e.getMultiBufferSource(), e.getPackedLight(),
                    e.getPartialTick(), e.getInterpolatedPitch(),
                    e.getSwingProgress(), e.getEquipProgress(), state.seed()
            );
            anim.applyFirstPerson(ctx, player, t, e.getPartialTick());
        } finally {
            ps.popPose();
        }
    }

    private static SavedPose save(HumanoidModel<?> m) {
        return new SavedPose(
                rot(m.head), rot(m.body),
                rot(m.rightArm), rot(m.leftArm),
                rot(m.rightLeg), rot(m.leftLeg)
        );
    }
    private static Rot rot(ModelPart p) { return new Rot(p.xRot, p.yRot, p.zRot); }
    private static void restore(HumanoidModel<?> m, SavedPose s) {
        apply(m.head, s.head); apply(m.body, s.body);
        apply(m.rightArm, s.rArm); apply(m.leftArm, s.lArm);
        apply(m.rightLeg, s.rLeg); apply(m.leftLeg, s.lLeg);
    }
    private static void apply(ModelPart p, Rot r) { p.xRot = r.x; p.yRot = r.y; p.zRot = r.z; }
}
