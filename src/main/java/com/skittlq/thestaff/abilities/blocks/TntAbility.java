package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.mixins.TntExplosionRadiusAccessor;
import com.skittlq.thestaff.util.AbilityTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.logging.Logger;

public class TntAbility implements BlockAbility {
    @Override
    public InteractionResult onRightClick(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResult.PASS;
        ServerLevel server = (ServerLevel) level;

        Vec3 look   = player.getLookAngle().normalize();
        Vec3 eye    = player.getEyePosition();
        Vec3 spawn  = eye.add(look.scale(0.8));

        PrimedTnt tnt = new PrimedTnt(EntityType.TNT, server);
        ((TntExplosionRadiusAccessor) tnt).setExplosionPower(8);
        tnt.setFuse(40);
        tnt.setPos(spawn.x, spawn.y + 1, spawn.z);
        tnt.setDeltaMovement(look.scale(1).add(0, 0, 0));

        server.playSound(tnt, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        server.addFreshEntity(tnt);

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onShiftRightClick(Level level, Player player, InteractionHand hand) {
        return onRightClick(level, player, player.getUsedItemHand());
    }

    @Override
    public String getDescription(AbilityTrigger trigger) {
        return switch (trigger) {
            case RIGHT_CLICK -> "Launches TNT with stronger blast power.";
            default -> BlockAbility.super.getDescription(trigger);
        };
    }

}