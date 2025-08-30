package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.util.AbilityTrigger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import static com.skittlq.thestaff.util.TickCommand.setTickingRate;

public class CakeAbility implements BlockAbility {
    @Override
    public InteractionResult onRightClick(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResult.PASS;
        ServerLevel server = (ServerLevel) level;

        Vec3 look   = player.getLookAngle().normalize();
        Vec3 eye    = player.getEyePosition();
        Vec3 spawn  = eye.add(look.scale(0));
        BlockPos bp = BlockPos.containing(spawn);

        FallingBlockEntity cake = FallingBlockEntity.fall(server, bp, Blocks.CAKE.defaultBlockState());
        server.playSound(cake, cake.getX(), cake.getY(), cake.getZ(), SoundEvents.UI_TOAST_IN, SoundSource.PLAYERS, 1.0F, 2.0F);

        cake.setHurtsEntities(1, 1);
        cake.dropItem = false;
        cake.setPos(spawn.x, spawn.y, spawn.z);
        cake.setDeltaMovement(look.scale(3).add(0, 0, 0));
        cake.canUsePortal(true);

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onShiftRightClick(Level level, Player player, InteractionHand hand) {
        return onRightClick(level, player, player.getUsedItemHand());
    }

    @Override
    public String getDescription(AbilityTrigger trigger) {
        return switch (trigger) {
            case RIGHT_CLICK -> "Launches cake.";
            default -> BlockAbility.super.getDescription(trigger);
        };
    }

}