package com.skittlq.thestaff.items.custom;

import com.mojang.logging.LogUtils;
import com.skittlq.thestaff.abilities.StaffAbilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class StaffItem extends Item {
    private static final String BLOCK_ID_KEY = "StaffStoredBlockId";
    private static final TagKey<Item> ALLOWED_BLOCKS_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath("thestaff", "allowed_blocks"));

    public StaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        var id = getStoredBlockId(stack);
        if (id != null) {
            var itemOpt = BuiltInRegistries.ITEM.get(id);
            if (itemOpt.isPresent()) {
                Item item = itemOpt.get().value();;
                Component blockName = item.getName();
                return Component.translatable("item.thestaff.purple_staff.with_block", blockName);
            }
        }
        return Component.translatable("item.thestaff.purple_staff");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var id = getStoredBlockId(stack);
        Item block = getStoredBlock(stack).getItem();
        if (id != null) {
            BuiltInRegistries.ITEM.get(id).ifPresent(item -> {
                Component name = block.getName();
                tooltipAdder.accept(Component.literal("Holding ").append(name).withStyle(ChatFormatting.GRAY));
            });
        }
    }


    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResult.PASS;

        ItemStack staff = player.getItemInHand(hand);                         // the stack being used
        if (staff.getItem() != this) return InteractionResult.PASS;           // safety

        InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND)
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);

        // Ability use (no shift)
        var blockId = getStoredBlockId(staff);
        if (blockId != null && !player.isShiftKeyDown()) {
            InteractionResult result = StaffAbilities.get(blockId).onRightClick(level, player, hand);
            if (result != InteractionResult.PASS) return result;
        }

        // From here on, shift-only mechanics (store / extract)
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        if (player.getOffhandItem().getItem() != this) {
            return InteractionResult.PASS;
        }


        // Store a block from the other hand into the staff
        if (!other.isEmpty() && other.getItem() instanceof BlockItem && !hasStoredBlock(staff)) {
            if (other.getItem().builtInRegistryHolder().is(ALLOWED_BLOCKS_TAG)) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(other.getItem());
                CompoundTag tag = new CompoundTag();
                tag.putString(BLOCK_ID_KEY, id.toString());
                staff.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                other.shrink(1);
                return InteractionResult.CONSUME;
            } else {
                player.displayClientMessage(Component.literal("The Staff Rejects This Block."), true);
                return InteractionResult.FAIL;
            }
        }

        // Extract the stored block from the staff into the other hand
        if (other.isEmpty() && hasStoredBlock(staff)) {
            ItemStack stored = getStoredBlock(staff);
            if (!stored.isEmpty() && stored.getItem() != Items.AIR) {
                player.setItemInHand(otherHand, stored);
                // Clear the component (use remove(...) if available in your mappings)
                // staff.remove(DataComponents.CUSTOM_DATA);
                staff.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    private static boolean hasStoredBlock(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        CompoundTag tag = data.copyTag();
        return tag.contains(BLOCK_ID_KEY) && tag.getString(BLOCK_ID_KEY).isPresent();
    }

    public static @Nullable ResourceLocation getStoredBlockId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        CompoundTag tag = data.copyTag();
        return tag.getString(BLOCK_ID_KEY)
                .map(ResourceLocation::tryParse)
                .orElse(null);
    }

    public static ItemStack getStoredBlock(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return ItemStack.EMPTY;

        CompoundTag tag = data.copyTag();
        return tag.getString(BLOCK_ID_KEY)
                .map(ResourceLocation::tryParse)
                .flatMap(BuiltInRegistries.ITEM::get)
                .map(holder -> holder.value())
                .filter(item -> item != Items.AIR)
                .map(item -> new ItemStack(item, 1))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return getStoredBlockId(stack) != null
                ? StaffAbilities.get(getStoredBlockId(stack)).miningSpeed(stack, state)
                : super.getDestroySpeed(stack, state);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            var id = getStoredBlockId(stack);
            if (id != null) {
                if (player.isShiftKeyDown()) {
                    StaffAbilities.get(id).onShiftHitEntity(attacker.level(), player, target, stack);
                } else {
                    StaffAbilities.get(id).onHitEntity(attacker.level(), player, target, stack);
                }
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (entity instanceof Player player) {
            var id = getStoredBlockId(stack);
            if (id != null) {
                if (player.isShiftKeyDown()) {
                    StaffAbilities.get(id).onShiftBreakBlock(level, player, pos, stack);
                } else {
                    StaffAbilities.get(id).onBreakBlock(level, player, pos, stack);
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            var id = getStoredBlockId(context.getItemInHand());
            if (id != null) {
                Player player = context.getPlayer();
                if (player != null) {
                    InteractionResult result;
                    if (player.isShiftKeyDown()) {
                        result = StaffAbilities.get(id).onShiftRightClickBlock(
                                context.getLevel(), player, context.getClickedPos(), context.getItemInHand()
                        );
                    } else {
                        result = StaffAbilities.get(id).onRightClickBlock(
                                context.getLevel(), player, context.getClickedPos(), context.getItemInHand()
                        );
                    }
                    if (result != InteractionResult.PASS) return result;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @org.jetbrains.annotations.Nullable EquipmentSlot slot) {

        var id = getStoredBlockId(stack);
        if (id != null && entity instanceof Player player) {
            player.setForcedPose(Pose.SWIMMING);

            StaffAbilities.get(id).onTick(level, player, player.blockPosition(), stack);
        }

        super.inventoryTick(stack, level, entity, slot);
    }

}
