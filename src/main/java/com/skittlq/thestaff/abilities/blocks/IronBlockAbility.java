package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.util.AbilityTrigger;
import com.skittlq.thestaff.util.ScheduleBatchDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.Queue;

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

        Queue<BlockPos> targets = new LinkedList<>();
        int depth = 5, height = 3, width = 3;

        Vec3 look = player.getLookAngle().normalize();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = right.cross(look).normalize();

        for (int d = 0; d < depth; d++) {
            Vec3 forwardStep = look.scale(d);
            BlockPos base = origin.offset((int) forwardStep.x, (int) forwardStep.y, (int) forwardStep.z);

            for (int y = -height; y <= height; y++) {
                for (int x = -width; x <= width; x++) {
                    Vec3 offset = right.scale(x).add(up.scale(y));
                    BlockPos target = base.offset((int) offset.x, (int) offset.y, (int) offset.z);
                    if (!target.equals(origin)) {
                        targets.add(target.immutable());
                    }
                }
            }
        }

        ScheduleBatchDestruction.schedule((ServerLevel) level, targets, BLOCKS_PER_TICK, player);
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