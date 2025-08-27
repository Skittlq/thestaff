package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.mixins.FurnaceAccessor;
import com.skittlq.thestaff.mixins.MinecartFurnaceAccessor;
import com.skittlq.thestaff.util.AbilityTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;

public class CoalBlockAbility implements BlockAbility {
    private static final int MAGIC_FUEL = 2400;

    private boolean magicallyFuelFurnace(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return false;

        if (be instanceof AbstractFurnaceBlockEntity furnace) {
            ((FurnaceAccessor) furnace).setLitTimeRemaining(MAGIC_FUEL);
            ((FurnaceAccessor) furnace).setLitTotalTime(MAGIC_FUEL);
            furnace.setChanged();

            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(AbstractFurnaceBlock.LIT)) {
                if (!state.getValue(AbstractFurnaceBlock.LIT)) {
                    level.setBlock(pos, state.setValue(AbstractFurnaceBlock.LIT, true), 3);
                }
            }

            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult onRightClickBlock(Level level, Player player, BlockPos pos, ItemStack staff) {

        if (!level.isClientSide && magicallyFuelFurnace(level, pos)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onShiftRightClickBlock(Level level, Player player, BlockPos pos, ItemStack staff) {

        if (!level.isClientSide && magicallyFuelFurnace(level, pos)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public float miningSpeed(ItemStack stack, BlockState state) {
        return 1.0F;
    }

    @Override
    public String getDescription(AbilityTrigger trigger) {
        return switch (trigger) {
            case SHIFT_RIGHT_CLICK_BLOCK -> "Ignite a furnace for 2 minutes.";
            default -> BlockAbility.super.getDescription(trigger);
        };
    }
}
