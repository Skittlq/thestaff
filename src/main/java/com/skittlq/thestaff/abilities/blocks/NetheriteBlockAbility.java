package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.skittlq.thestaff.util.TickCommand.setTickingRate;

public class NetheriteBlockAbility implements BlockAbility {
    private static final int BLOCKS_PER_TICK = 200;

    @Override
    public void onHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {
        target.hurt(player.damageSources().playerAttack(player), 500.0f);
        target.knockback(20.0,
                player.getX() - target.getX(),
                player.getZ() - target.getZ());
        target.setDeltaMovement(target.getDeltaMovement().add(0, 5, 0));
        onBreakBlock(level, player, target.blockPosition(), staff);
        CommandSourceStack source = player.createCommandSourceStackForNameResolution(((ServerLevel) level));
        smoothTickRateReset(source, 1500, 1000, (ServerLevel) level);
    }

    @Override
    public void onShiftHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {
        onHitEntity(level, player, target, staff);
    }

    @Override
    public void onBreakBlock(Level level, Player player, BlockPos origin, ItemStack staff) {
        if (level.isClientSide) return;

        Queue<BlockPos> targets = new LinkedList<>();
        int depth = 20, height = 6, width = 6;

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

        scheduleBatchDestruction((ServerLevel) level, targets);
    }

    @Override
    public void onShiftBreakBlock(Level level, Player player, BlockPos pos, ItemStack staff) {
        onBreakBlock(level, player, pos, staff);
    }

    @Override
    public float miningSpeed(ItemStack stack, BlockState state) {
        return 1000F;
    }

    private void scheduleBatchDestruction(ServerLevel level, Queue<BlockPos> targets) {
        level.getServer().execute(() -> {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int count = 0;
                    while (!targets.isEmpty() && count++ < BLOCKS_PER_TICK) {
                        BlockPos pos = targets.poll();
                        if (pos != null) {
                            level.destroyBlock(pos, false);
                        }
                    }
                    if (targets.isEmpty()) {
                        this.cancel();
                    }
                }
            }, 0, 50);
        });
    }

    private static float interpolateTickRate(float fraction) {
        fraction = Math.max(0.0f, Math.min(1.0f, fraction));
        double base = 5.0;
        return (float) (1.0 + (20.0 - 1.0) * (Math.pow(base, fraction) - 1) / (base - 1));
    }

    public static void smoothTickRateReset(CommandSourceStack source, int holdMillis, int rampMillis, ServerLevel level) {
        final int rampSteps = 40;
        final Timer timer = new Timer("TickRateInterpolator", false);
        final long totalMillis = holdMillis + rampMillis;
        final long startTime = System.currentTimeMillis();

        timer.scheduleAtFixedRate(new TimerTask() {
            int currentStep = 0;
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed < holdMillis) {
                    setTickingRate(source, 1);

                } else {
                    float fraction = Math.min(1.0f, (float) (elapsed - holdMillis) / rampMillis);
                    float tickRate = interpolateTickRate(fraction);
                    setTickingRate(source, tickRate);
                }
                currentStep++;
                if (elapsed >= totalMillis) {
                    timer.cancel();
                }
            }
        }, 0, totalMillis / (rampSteps + (holdMillis * rampSteps / rampMillis)));
    }

}