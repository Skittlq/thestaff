package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.util.AbilityTrigger;
import com.skittlq.thestaff.util.ScheduleBatchDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.Queue;

public class CopperBlockAbility implements BlockAbility {
    private static final int BLOCKS_PER_TICK = 200;

    @Override
    public void onHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {
        target.hurt(player.damageSources().playerAttack(player), 31.25f);
        target.knockback(2.5,
                player.getX() - target.getX(),
                player.getZ() - target.getZ());
        target.setDeltaMovement(target.getDeltaMovement().add(0, 0.3125, 0));
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
        int depth = 2, height = 2, width = 2;

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
    public void onTick(Level level, Player player, BlockPos pos, ItemStack staff) {
        if (!level.isClientSide) {
            boolean holdingStaff = player.getMainHandItem() == staff || player.getOffhandItem() == staff;
            if (holdingStaff && level.getGameTime() % 20 == 0) {
                if (level.isThundering()) {
                    if (level.random.nextInt(10) == 0) {
                        var lightning = new net.minecraft.world.entity.LightningBolt(
                                net.minecraft.world.entity.EntityType.LIGHTNING_BOLT,
                                level
                        );
                        lightning.setPos(player.position());
                        level.addFreshEntity(lightning);

                        ((ServerLevel) level).sendParticles(
                                net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                                player.getX(), player.getY() + 1.0, player.getZ(),
                                100, 0, 0, 0, 10
                        );

                        double radius = 7.0;
                        var area = new net.minecraft.world.phys.AABB(
                                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                                player.getX() + radius, player.getY() + radius, player.getZ() + radius
                        );
                        for (Entity entity : level.getEntities(player, area, e -> e instanceof LivingEntity && e != player)) {
                            level.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,
                                    net.minecraft.sounds.SoundSource.WEATHER, 1.0F, 1.0F);
                            entity.setRemainingFireTicks(60);

                            entity.hurt(level.damageSources().lightningBolt(), 10.0f);

                        }

                    }
                }
            }
        }
        BlockAbility.super.onTick(level, player, pos, staff);
    }

    @Override
    public String getDescription(AbilityTrigger trigger) {
        return switch (trigger) {
            case TICK -> "You are more likely to be struck by lightning during thunderstorms, but also damage mobs around you in a radius.";
            case BREAK_BLOCK -> "Destroy tiny chunks of land.";
            case HIT_ENTITY -> "Deal 30 damage with tiny knockback.";
            default -> BlockAbility.super.getDescription(trigger);
        };
    }


}