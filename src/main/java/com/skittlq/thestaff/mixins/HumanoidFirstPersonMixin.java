package com.skittlq.thestaff.mixins;

import com.skittlq.thestaff.anim.ClientPlayerAnimRuntime;
import com.skittlq.thestaff.anim.PlayerPoseAnim;
import com.skittlq.thestaff.anim.PlayerPoseAnims;
import com.skittlq.thestaff.items.ModItems;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HumanoidFirstPersonMixin {

    @Inject(
            method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER
            )
    )
    private void thestaff$applyFirstPersonAnim(
            net.minecraft.client.player.AbstractClientPlayer player,
            float partialTicks,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            net.minecraft.world.item.ItemStack stack,
            float equippedProgress,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            CallbackInfo ci) {
        var state = ClientPlayerAnimRuntime.current(player);
        if (state == null) return;

        if (!isOurAnimItem(stack)) return;

        var anim = PlayerPoseAnims.get(state.id());
        if (anim == null) return;

        float t = ClientPlayerAnimRuntime.elapsedSeconds(player, state, partialTicks);

        var ctx = new PlayerPoseAnim.FirstPersonContext(
                hand, poseStack, buffers, packedLight,
                partialTicks, pitch, swingProgress, equippedProgress,
                state.seed()
        );

        anim.applyFirstPerson(ctx, player, t, partialTicks);
    }

    /** Decide which items should receive the first-person anim. */
    private static boolean isOurAnimItem(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() == ModItems.PURPLE_STAFF.asItem();
    }


}
