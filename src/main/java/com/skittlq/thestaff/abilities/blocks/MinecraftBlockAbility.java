package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import com.skittlq.thestaff.util.AbilityTrigger;
import com.skittlq.thestaff.util.ScheduleBatchDestruction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.Queue;

import static com.skittlq.thestaff.util.SlowMotionHelper.smoothTickRateReset;

public class MinecraftBlockAbility implements BlockAbility {

//    @Override
//    public String getDescription(AbilityTrigger trigger) {
//        return switch (trigger) {
//            case BREAK_BLOCK -> "Destroy huge chunks of land.";
//            case HIT_ENTITY -> "Deal 500 damage and huge knockback.";
//            default -> BlockAbility.super.getDescription(trigger);
//        };
//    }
}