// CobwebAbility.java
package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.client.webhook.WebHookController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class CobwebAbility implements BlockAbility {

    // CobwebAbility.java
    @Override
    public InteractionResult onRightClick(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            // Instead of "15 blocks out", just anchor at a block in front
            Vec3 hit = eye.add(look.scale(5)); // shorter rope
            WebHookController.INSTANCE.start(hit, 2.0); // start with rope shorter than distance

            player.displayClientMessage(Component.literal("Hooked!"), false);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onTick(Level level, Player player, BlockPos pos, ItemStack staff) {
        if (level.isClientSide) {
            WebHookController.INSTANCE.tick(player);
        }
    }



}
