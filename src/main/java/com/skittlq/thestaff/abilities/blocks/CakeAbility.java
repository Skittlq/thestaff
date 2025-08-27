package com.skittlq.thestaff.abilities.blocks;

import com.skittlq.thestaff.abilities.BlockAbility;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import static com.skittlq.thestaff.util.TickCommand.setTickingRate;

public class CakeAbility implements BlockAbility {
    @Override
    public InteractionResult onRightClick(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResult.PASS;
        ServerLevel server = (ServerLevel) level;

        // Spawn a little in front of the player’s eyes
        Vec3 look   = player.getLookAngle().normalize();
        Vec3 eye    = player.getEyePosition();
        Vec3 spawn  = eye.add(look.scale(0.8));
        BlockPos bp = BlockPos.containing(spawn);

        // 1) Create the falling CAKE (with state) — this usually also adds it to the world
        FallingBlockEntity cake = FallingBlockEntity.fall(server, bp, Blocks.CAKE.defaultBlockState());

        // 2) Reposition to the exact vector point and launch it
        cake.setPos(spawn.x, spawn.y + 1, spawn.z);
        cake.setDeltaMovement(look.scale(5.2).add(0, 0, 0)); // forward + a touch of lift

        // (If your mappings’ fall(..) does NOT add the entity, uncomment the next line)
        // server.addFreshEntity(cake);

        // Optional niceties:
        // cake.dropItem = true;         // drop if it can’t place (default true)
        // cake.setHurtsEntities(false); // if available in your mappings

        return InteractionResult.PASS;
    }
}