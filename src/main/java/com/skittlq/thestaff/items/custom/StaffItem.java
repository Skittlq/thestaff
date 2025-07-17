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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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
import java.util.List;
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
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers base = super.getDefaultAttributeModifiers(stack);
        var id = getStoredBlockId(stack);
        if (id == null) return base;

        var builder = ItemAttributeModifiers.builder();

        for (var entry : base.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
        }

        StaffAbilities.get(id).addModifiers(stack, builder);

        return builder.build();
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
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack staffStack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        ItemStack offhandStack = player.getOffhandItem();

        if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof BlockItem && offhandStack.getCount() > 0) {
            if (!hasStoredBlock(staffStack)) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(offhandStack.getItem());
                System.out.println("Offhand Item ID: " + id);

                if (offhandStack.getItem().builtInRegistryHolder().is(ALLOWED_BLOCKS_TAG)) {
                    CompoundTag tag = new CompoundTag();
                    tag.putString(BLOCK_ID_KEY, id.toString());
                    staffStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    offhandStack.shrink(1);

                    return InteractionResult.CONSUME;
                } else {
                    player.displayClientMessage(Component.literal("The Staff Rejects This Block."), true);
                    return InteractionResult.FAIL;
                }
            }
        }

        if (offhandStack.isEmpty() && hasStoredBlock(staffStack)) {
            CustomData data = staffStack.get(DataComponents.CUSTOM_DATA);

            ItemStack stored = getStoredBlock(staffStack);
            if (!stored.isEmpty() && stored.getItem() != Items.AIR) {
                player.setItemInHand(InteractionHand.OFF_HAND, stored);

                CompoundTag tag = new CompoundTag();
                staffStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
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
    private static ResourceLocation getStoredBlockId(ItemStack stack) {
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
                    if (player.isShiftKeyDown()) {
                        StaffAbilities.get(id).onShiftRightClickBlock(context.getLevel(), player, context.getClickedPos(), context.getItemInHand());
                    } else {
                        StaffAbilities.get(id).onRightClickBlock(context.getLevel(), player, context.getClickedPos(), context.getItemInHand());
                    }
                }
            }
        }
        return super.useOn(context);
    }


}
