package com.skittlq.thestaff.server;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.items.ModItems;
import com.skittlq.thestaff.items.custom.StaffItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TheStaff.MODID)
public final class ModPlayerModes {
    private static final String TAG_FORCED = "thestaff:forcedCreative";
    private static final String TAG_PREV   = "thestaff:prevGameMode";

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;

        boolean holdingIcon =
                isIconOrStaffWithIcon(p.getMainHandItem()) ||
                        isIconOrStaffWithIcon(p.getOffhandItem());

        var data = p.getPersistentData();
        boolean forced = data.getBooleanOr(TAG_FORCED, false);

        if (holdingIcon) {
            if (!forced) {
                data.putInt(TAG_PREV, p.gameMode.getGameModeForPlayer().getId());
                data.putBoolean(TAG_FORCED, true);
                if (p.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
                    p.setGameMode(GameType.CREATIVE);
                }
            }
            return;
        }

        if (forced) {
            int prevId = data.getIntOr(TAG_PREV, GameType.SURVIVAL.getId());
            GameType prev = GameType.byId(prevId);

            data.remove(TAG_FORCED);
            data.remove(TAG_PREV);

            if (prev != null && p.gameMode.getGameModeForPlayer() != prev) {
                p.setGameMode(prev);
            }
        }
    }

    private static boolean isIconOrStaffWithIcon(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.is(ModItems.MINECRAFT_GAME_ICON.get())) {
            return true;
        }

        if (stack.getItem() instanceof StaffItem) {
            ItemStack stored = StaffItem.getStoredBlock(stack);
            return stored.is(ModItems.MINECRAFT_GAME_ICON.get());
        }

        return false;
    }

}
