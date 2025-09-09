package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RedstoneBlockAbility implements BlockAbility {
    private static final double RAY_DIST = 256.0D;

    @Override
    public InteractionResult onRightClick(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockHitResult bhr = raycast(player, level, RAY_DIST);
        if (bhr == null || bhr.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;

        BlockPos pos = bhr.getBlockPos();
        Direction face = bhr.getDirection();

        BlockState state = level.getBlockState(pos);
        BlockState original = state;

        // ENABLED (hoppers) — note: powered hopper => ENABLED=false
        if (state.hasProperty(BlockStateProperties.TRIGGERED)) {
            state = state.setValue(BlockStateProperties.TRIGGERED, !state.getValue(BlockStateProperties.TRIGGERED));
        }

        // --- Toggle properties only if present ---
        // POWER (for dust/repeaters/comparators)
        if (state.hasProperty(BlockStateProperties.POWER)) {
            int cur = state.getValue(BlockStateProperties.POWER);
            state = state.setValue(BlockStateProperties.POWER, cur == 0 ? 15 : 0);
        }

        // POWERED (many blocks)
        if (state.hasProperty(BlockStateProperties.POWERED)) {
            state = state.setValue(BlockStateProperties.POWERED, !state.getValue(BlockStateProperties.POWERED));
        }

        // LIT (lamps, etc.)
        if (state.hasProperty(BlockStateProperties.LIT)) {
            state = state.setValue(BlockStateProperties.LIT, !state.getValue(BlockStateProperties.LIT));
        }

        // OPEN (doors / trapdoors / gates)
        if (state.hasProperty(BlockStateProperties.OPEN)) {
            state = state.setValue(BlockStateProperties.OPEN, !state.getValue(BlockStateProperties.OPEN));
        }

        // ENABLED
        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            state = state.setValue(BlockStateProperties.ENABLED, !state.getValue(BlockStateProperties.ENABLED));
        }

        boolean changed = state != original;
        if (changed) {
            level.setBlockAndUpdate(pos, state);
            level.updateNeighborsAt(pos, state.getBlock());
        }

        return changed ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    // --- Helpers -------------------------------------------------------------
    /** Basic block raycast from the player POV. */
    private static BlockHitResult raycast(Player player, Level level, double distance) {
        Vec3 eye   = player.getEyePosition(0f);
        Vec3 look  = player.getViewVector(0f);
        Vec3 reach = eye.add(look.scale(distance));
        ClipContext ctx = new ClipContext(
                eye, reach,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        );
        HitResult hr = level.clip(ctx);
        return (hr instanceof BlockHitResult bhr) ? bhr : null;
    }
}
