package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CommandBlockAbility implements BlockAbility {
    @Override
    public void onHitEntity(Level level, Player player, LivingEntity target, ItemStack staff) {
        target.hurt(player.damageSources().playerAttack(player), 1000.0f);
        player.displayClientMessage(Component.literal("You hit them with COMMAND POWER."), true);
    }
}