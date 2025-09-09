package com.skittlq.thestaff.items.custom;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.blocks.ModBlocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;

import javax.annotation.Nullable;

public class DarkMinecraftItem extends BlockItem {
    public DarkMinecraftItem(Properties properties) {
        super(ModBlocks.DARK_MINECRAFT.get(), properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Player player = context.getPlayer();
        InteractionResult result = super.place(context);
        if (!(player instanceof ServerPlayer sp)) return result;

        InteractionHand hand = context.getHand();
        EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND)
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;

        if (result.consumesAction()) {
            if (sp.getAbilities().instabuild && sp.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                ItemStack held = player.getItemInHand(hand);
                held.shrink(1);
                if (held.isEmpty()) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }

        return result;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if ((player.getOffhandItem() == stack) || (player.getMainHandItem() == stack)) {
            access.set(stack);
            return true;
        }
        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }

    // inside the dark item class
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (level.isClientSide) return;

        boolean shouldGlow = false;
        if (entity instanceof Player p) {
            boolean isHeld = p.getMainHandItem() == stack || p.getOffhandItem() == stack;
            if (isHeld) {
                shouldGlow = p.isFallFlying() || p.getAbilities().flying;
            }
        }

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = (data != null) ? data.copyTag() : new CompoundTag();
        CompoundTag renderTag = root.getCompound(TheStaff.MODID + ":render").orElse(new CompoundTag());
        renderTag.putBoolean("active", shouldGlow);
        root.put(TheStaff.MODID + ":render", renderTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }



}
