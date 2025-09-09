package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.util.AbilityTrigger;
import com.skittlq.thestaff.util.BlockScanDestruction;
import com.skittlq.thestaff.util.ScheduleBatchDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class IronBlockAbility implements BlockAbility {
    private static final int BLOCKS_PER_TICK = 200;

    @Override
    public void onHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {
        target.hurt(player.damageSources().playerAttack(player), 125.0f);
        target.knockback(5.0,
                player.getX() - target.getX(),
                player.getZ() - target.getZ());
        target.setDeltaMovement(target.getDeltaMovement().add(0, 1.25, 0));
        onBreakBlock(level, player, target.blockPosition(), staff);
    }

    @Override
    public void onShiftHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {
        onHitEntity(level, player, target, staff);
    }

    @Override
    public void onBreakBlock(Level level, Player player, BlockPos origin, ItemStack staff) {
        if (level.isClientSide) return;

        // Collect unique targets to avoid duplicates after new rounding
        final Set<BlockPos> targets = new LinkedHashSet<>();

        // Base dimensions (ellipse radii)
        final int depth = 5;
        final int radiusX = 3;
        final int radiusY = 3;

        BlockScanDestruction.blockScanDestruction(player, origin, radiusX, radiusY, depth, targets);

        ScheduleBatchDestruction.schedule((ServerLevel) level, new LinkedList<>(targets), BLOCKS_PER_TICK, player);
    }

    @Override
    public void onShiftBreakBlock(Level level, Player player, BlockPos pos, ItemStack staff) {
        onBreakBlock(level, player, pos, staff);
    }

    @Override
    public float miningSpeed(ItemStack stack, BlockState state) {
        return 500F;
    }

    @Override
    public String getDescription(AbilityTrigger trigger) {
        return switch (trigger) {
            case BREAK_BLOCK -> "Destroy medium chunks of land.";
            case HIT_ENTITY -> "Deal 125 damage and medium knockback.";
            default -> BlockAbility.super.getDescription(trigger);
        };
    }
}