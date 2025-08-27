package com.skittlq.thestaff.abilities;

import com.skittlq.thestaff.util.AbilityTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockAbility {
    default void onBreakBlock(Level level, Player player, BlockPos pos, ItemStack staff) {}

    default InteractionResult onRightClickBlock(Level level, Player player, BlockPos pos, ItemStack staff) {
        return InteractionResult.PASS;
    }

    default void onHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {}

    default InteractionResult onShiftRightClickBlock(Level level, Player player, BlockPos pos, ItemStack staff) {
        return InteractionResult.PASS;
    }

    default void onShiftBreakBlock(Level level, Player player, BlockPos pos, ItemStack staff) {}

    default void onShiftHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {}

    default float miningSpeed(ItemStack stack, BlockState state) {
        return 1F;
    }

    default void onTick(Level level, Player player, BlockPos pos, ItemStack staff) {}

    default InteractionResult onRightClick(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    default InteractionResult onShiftRightClick(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    default String getDescription(AbilityTrigger trigger) {
        return switch (trigger) {
            case RIGHT_CLICK, SHIFT_RIGHT_CLICK, RIGHT_CLICK_BLOCK, SHIFT_RIGHT_CLICK_BLOCK, HIT_ENTITY,
                 SHIFT_HIT_ENTITY, BREAK_BLOCK, SHIFT_BREAK_BLOCK, TICK, MINING_SPEED -> "";
        };
    }

}
