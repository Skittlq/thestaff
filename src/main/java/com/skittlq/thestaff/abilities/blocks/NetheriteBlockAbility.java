package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.UUID;

public class NetheriteBlockAbility implements BlockAbility {
    @Override
    public void onHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {
        target.hurt(player.damageSources().playerAttack(player), 1000.0f);
        target.knockback(20.0, player.getX() - target.getX(), player.getZ() - target.getZ());
    }

    @Override
    public void onShiftHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {
        onHitEntity(level, player, target, staff);
    }

    @Override
    public void onBreakBlock(Level level, Player player, BlockPos origin, ItemStack staff) {
        if (level.isClientSide) return;

        int depth = 20;
        int height = 5;
        int width = 5;

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
                        level.destroyBlock(target, false);
                    }
                }
            }
        }

    }

    @Override
    public void onShiftBreakBlock(Level level, Player player, BlockPos pos, ItemStack staff) {
        onBreakBlock(level, player, pos, staff);
    }

    @Override
    public void addModifiers(ItemStack stack, ItemAttributeModifiers.Builder builder) {
        builder.add(
                Holder.direct(Attributes.BLOCK_BREAK_SPEED.value()),
                new AttributeModifier(
                        ResourceLocation.withDefaultNamespace("block_break_speed"),
                        50.0,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
        );
    }
}