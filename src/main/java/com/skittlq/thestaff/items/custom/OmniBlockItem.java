package com.skittlq.thestaff.items.custom;

import com.skittlq.thestaff.blocks.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class OmniBlockItem extends BlockItem {
    public OmniBlockItem(Properties properties) {
        super(ModBlocks.OMNIBLOCK.get(), properties);
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

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof LivingEntity living) {
            if (!level.isClientSide) {
                if (living instanceof Player p && (p.getMainHandItem() != stack && p.getOffhandItem() != stack)) {
                    return;
                }

                level.playSound(null, living.blockPosition(), SoundEvents.GENERIC_BURN, living.getSoundSource(), 0.1F, 0.5F + (float) Math.random());
                living.hurt(living.damageSources().fellOutOfWorld(), 0.5F);
            }
        }
        super.inventoryTick(stack, level, entity, slot);
    }
}
