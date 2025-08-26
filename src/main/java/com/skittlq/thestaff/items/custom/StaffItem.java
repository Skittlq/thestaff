package com.skittlq.thestaff.items.custom;

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

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (offHand.getItem() != this) return InteractionResult.PASS;

        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        if (!mainHand.isEmpty() && mainHand.getItem() instanceof BlockItem && !hasStoredBlock(offHand)) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(mainHand.getItem());
            if (mainHand.getItem().builtInRegistryHolder().is(ALLOWED_BLOCKS_TAG)) {
                CompoundTag tag = new CompoundTag();
                tag.putString(BLOCK_ID_KEY, id.toString());
                offHand.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                mainHand.shrink(1);
                return InteractionResult.CONSUME;
            } else {
                player.displayClientMessage(Component.literal("The Staff Rejects This Block."), true);
                return InteractionResult.FAIL;
            }
        }

        if (mainHand.isEmpty() && hasStoredBlock(offHand)) {
            ItemStack stored = getStoredBlock(offHand);
            if (!stored.isEmpty() && stored.getItem() != Items.AIR) {
                player.setItemInHand(InteractionHand.MAIN_HAND, stored);

                CompoundTag tag = new CompoundTag();
                offHand.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    private static CompoundTag getOrCreateCustomDataTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static boolean hasStoredBlock(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        return tag.contains(BLOCK_ID_KEY);
    }

    public static ItemStack getStoredBlock(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains(BLOCK_ID_KEY)) {
                var blockIdOpt = tag.getString(BLOCK_ID_KEY);
                if (blockIdOpt.isPresent()) {
                    String blockId = blockIdOpt.get();
                    ResourceLocation id = ResourceLocation.tryParse(blockId);
                    if (id != null) {
                        var holderOpt = BuiltInRegistries.ITEM.get(id);
                        if (holderOpt.isPresent()) {
                            Item item = holderOpt.get().value();
                            if (item != null && item != Items.AIR) {
                                return new ItemStack(item, 1);
                            }
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public static ResourceLocation getStoredBlockId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            var opt = tag.getString(BLOCK_ID_KEY);
            if (opt.isPresent()) {
                return ResourceLocation.tryParse(opt.get());
            }
        }
        return null;
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
