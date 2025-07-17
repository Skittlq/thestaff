package com.skittlq.thestaff.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface BlockAbility {
    default void onBreakBlock(Level level, Player player, BlockPos pos, ItemStack staff) {}
    default void onRightClickBlock(Level level, Player player, BlockPos pos, ItemStack staff) {}
    default void onHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {}
    default void onShiftRightClickBlock(Level level, Player player, BlockPos pos, ItemStack staff) {}
    default void onShiftBreakBlock(Level level, Player player, BlockPos pos, ItemStack staff) {}
    default void onShiftHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {}

    default void addModifiers(ItemStack stack, ItemAttributeModifiers.Builder builder) {}
}
