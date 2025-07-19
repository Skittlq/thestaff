    package com.skittlq.thestaff.abilities.blocks;

    import com.skittlq.thestaff.abilities.BlockAbility;
    import net.minecraft.core.BlockPos;
    import net.minecraft.core.Direction;
    import net.minecraft.world.InteractionResult;
    import net.minecraft.world.entity.player.Player;
    import net.minecraft.world.item.ItemStack;
    import net.minecraft.world.level.Level;
    import net.minecraft.world.level.block.state.BlockState;

    public class ObsidianBlockAbility implements BlockAbility {
        @Override
        public float miningSpeed(ItemStack stack, BlockState state) {
            return 1000F;
        }
        @Override
        public void onBreakBlock(Level level, Player player, BlockPos origin, ItemStack staff) {
            if (level.isClientSide) return;

            var look = player.getLookAngle().normalize();
            int radius = 5;
            int maxPush = 10;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    if (dist > radius + 0.35) continue;

                    var up = Math.abs(look.y) < 0.99 ? new net.minecraft.world.phys.Vec3(0,1,0) : new net.minecraft.world.phys.Vec3(1,0,0);
                    var right = look.cross(up).normalize();
                    var upVec = right.cross(look).normalize();

                    var offset = right.scale(dx).add(upVec.scale(dy));
                    var target = origin.offset((int)Math.round(offset.x), (int)Math.round(offset.y), (int)Math.round(offset.z));
                    BlockState state = level.getBlockState(target);

                    if (!state.isAir()) {
                        int pushStrength = Math.max(1, (int) Math.round(maxPush * (1.0 - dist / (radius + 0.1))));

                        net.minecraft.world.phys.Vec3 pushVec = look;
                        BlockPos pushPos = target;
                        for (int step = 1; step <= pushStrength; step++) {
                            var nextPos = pushPos.offset(
                                    (int)Math.round(pushVec.x),
                                    (int)Math.round(pushVec.y),
                                    (int)Math.round(pushVec.z)
                            );
                            if (level.isEmptyBlock(nextPos)) {
                                level.setBlock(nextPos, state, 3);
                                level.removeBlock(pushPos, false);
                                break;
                            }
                            pushPos = nextPos;
                        }
                    }
                }
            }
        }

        @Override
        public InteractionResult onRightClickBlock(Level level, Player player, BlockPos pos, ItemStack staff) {
            return BlockAbility.super.onRightClickBlock(level, player, pos, staff);
        }
    }
