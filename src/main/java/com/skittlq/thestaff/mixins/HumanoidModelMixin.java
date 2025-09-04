package com.skittlq.thestaff.mixins;

import com.skittlq.thestaff.anim.AnimRenderStateBridge;
import com.skittlq.thestaff.anim.PlayerPoseAnims;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL"))
    private void thestaff$applyCustomPose(HumanoidRenderState state, CallbackInfo ci) {
        var active = state.getRenderData(AnimRenderStateBridge.ACTIVE);
        if (active == null) return;

        var anim = PlayerPoseAnims.get(active.id());
        if (anim == null) return;

        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        float t = active.tSeconds();

        boolean didAny = false;
        if (active.mainHand()) {
            anim.applyThirdPerson(model, null, t, 0f, net.minecraft.world.InteractionHand.MAIN_HAND);
            didAny = true;
        }
        if (active.offHand()) {
            anim.applyThirdPerson(model, null, t, 0f, net.minecraft.world.InteractionHand.OFF_HAND);
            didAny = true;
        }
        if (!didAny) {
            anim.apply(model, null, t, 0f);
        }
    }
}
