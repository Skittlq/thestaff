package com.skittlq.thestaff.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nullable;
import java.util.*;

public final class ScheduleBatchDestruction {
    private ScheduleBatchDestruction() {}

    private static final Map<ServerLevel, Task> TASKS = new HashMap<>();

    public static void schedule(ServerLevel level, Queue<BlockPos> targets, int blocksPerTick, @Nullable Player player) {
        Task existing = TASKS.get(level);
        if (existing != null) {
            existing.targets.addAll(targets);
            existing.blocksPerTick = Math.max(existing.blocksPerTick, Math.max(1, blocksPerTick));
            existing.player = player;
            return;
        }
        TASKS.put(level, new Task(new ArrayDeque<>(targets), Math.max(1, blocksPerTick), player));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post e) {
        if (!e.hasTime()) return;
        if (e.getLevel().isClientSide()) return;

        ServerLevel level = (ServerLevel) e.getLevel();
        Task task = TASKS.get(level);
        if (task == null) return;

        int processed = 0;
        while (!task.targets.isEmpty() && processed++ < task.blocksPerTick) {
            BlockPos pos = task.targets.poll();
            if (pos == null) continue;
            if (!level.isLoaded(pos)) continue;
            if (level.isEmptyBlock(pos)) continue;
            if (level.getBlockState(pos).is(Blocks.BEDROCK)) continue;

            boolean drop = task.player == null || !task.player.isCreative();
            if (task.player != null) {
                level.destroyBlock(pos, drop, task.player);
            } else {
                level.destroyBlock(pos, drop);
            }
        }
        if (task.targets.isEmpty()) TASKS.remove(level);
    }

    private static final class Task {
        final Deque<BlockPos> targets;
        int blocksPerTick;
        @Nullable Player player;
        Task(Deque<BlockPos> targets, int blocksPerTick, @Nullable Player player) {
            this.targets = targets;
            this.blocksPerTick = blocksPerTick;
            this.player = player;
        }
    }
}
