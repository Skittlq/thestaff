package com.skittlq.thestaff.items.custom;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.abilities.StaffAbilities;
import com.skittlq.thestaff.anim.BuiltinAnims;
import com.skittlq.thestaff.anim.ClientPlayerAnimRuntime;
import com.skittlq.thestaff.network.NetSend;
import com.skittlq.thestaff.network.payloads.PlayPoseAnimPayload;
import com.skittlq.thestaff.util.AbilityTrigger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StaffItem extends Item {
    private static final String BLOCK_ID_KEY = "StaffStoredBlockId";
    private static final TagKey<Item> ALLOWED_BLOCKS_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "allowed_blocks"));

    public StaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var id = getStoredBlockId(stack);
        Item block = getStoredBlock(stack).getItem();
        if (id != null) {
            BuiltInRegistries.ITEM.get(id).ifPresent(item -> {
                Component name = block.getName();
                tooltipAdder.accept(Component.literal("Holding ").append(name).withStyle(ChatFormatting.GRAY));
                tooltipAdder.accept(Component.empty());

                BlockAbility ability = StaffAbilities.get(id);
                if (ability != null) {
                    List<AbilityTrigger> described = Arrays.stream(AbilityTrigger.values())
                            .filter(t -> {
                                String d = ability.getDescription(t);
                                return d != null && !d.isEmpty();
                            })
                            .toList();

                    for (int i = 0; i < described.size(); i++) {
                        AbilityTrigger trigger = described.get(i);
                        String desc = ability.getDescription(trigger);

                        String pretty = Arrays.stream(trigger.toString().toLowerCase().split("_"))
                                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                                .collect(Collectors.joining(" "));

                        tooltipAdder.accept(Component.literal(pretty.equals("Tick") ? "In Hand" : pretty).withStyle(ChatFormatting.GOLD));
                        tooltipAdder.accept(Component.literal(desc).withStyle(ChatFormatting.GRAY));

                        if (i < described.size() - 1) {
                            tooltipAdder.accept(Component.empty());
                        }
                    }
                }

            });
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        ItemStack stored = getStoredBlock(stack);

//        if (action == ClickAction.PRIMARY) {
//            if (stored.getItem() == ModItems.LIGHT_MINECRAFT.asItem()) {
//                if () {
//
//                }
//                player.displayClientMessage(Component.literal(stack + stored.toString()), false);
//
//                return true;
//            }
//        }
        if (action == ClickAction.SECONDARY) {
                if (other.isEmpty() && hasStoredBlock(stack)) {
                    if (!stored.isEmpty() && stored.getItem() != Items.AIR) {
                        access.set(stored);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
                        return true;
                    }
                }

            if (other.getItem().builtInRegistryHolder().is(ALLOWED_BLOCKS_TAG)) {
                if (hasStoredBlock(stack)) {
                    if (!stored.isEmpty() && stored.getItem() != Items.AIR) {
                        access.set(stored);
                    }
                }
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(other.getItem());
                CompoundTag tag = new CompoundTag();
                tag.putString(BLOCK_ID_KEY, id.toString());
                other.shrink(1);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                return true;
            } else {
                player.displayClientMessage(Component.literal("The Staff Rejects This Block."), true);
                return true;
            }
        }

        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResult.PASS;

        ItemStack staff = player.getItemInHand(hand);
        if (staff.getItem() != this) return InteractionResult.PASS;

        var blockId = getStoredBlockId(staff);

        if (blockId != null) {
            if (player.isShiftKeyDown()) {
                if (hand == InteractionHand.MAIN_HAND) {
                    if (player instanceof ServerPlayer sp) {
                        NetSend.sendAnimToSelfAndTrackers(sp,
                                new PlayPoseAnimPayload(player.getUUID(), BuiltinAnims.RAISE_RIGHT, true));
                    }


                    InteractionResult result = StaffAbilities.get(blockId)
                            .onShiftRightClick(level, player, hand);
                    if (result != InteractionResult.PASS) return result;
                }
            } else {
                if (player instanceof ServerPlayer sp) {
                    NetSend.sendAnimToSelfAndTrackers(sp,
                            new PlayPoseAnimPayload(player.getUUID(), BuiltinAnims.RAISE_RIGHT, true));
                }


                InteractionResult result = StaffAbilities.get(blockId)
                        .onRightClick(level, player, hand);
                if (result != InteractionResult.PASS) return result;
            }
        }

        if (hand == InteractionHand.OFF_HAND && player.isShiftKeyDown()) {
            InteractionHand otherHand = InteractionHand.MAIN_HAND;
            ItemStack other = player.getItemInHand(otherHand);

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

            if (other.isEmpty() && hasStoredBlock(staff)) {
                ItemStack stored = getStoredBlock(staff);
                if (!stored.isEmpty() && stored.getItem() != Items.AIR) {
                    player.setItemInHand(otherHand, stored);
                    staff.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
                    return InteractionResult.CONSUME;
                }
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
            StaffAbilities.get(id).onTick(level, player, player.blockPosition(), stack);
        }

        super.inventoryTick(stack, level, entity, slot);
    }

}
