package com.skittlq.thestaff.util;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TickScheduler {
    private static final Queue<ScheduledTask> TASKS = new LinkedList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private static final Map<Integer, ScheduledTask> TASK_MAP = new HashMap<>();

    public static int schedule(Runnable action, int delayTicks) {
        int id = NEXT_ID.getAndIncrement();
        ScheduledTask task = new ScheduledTask(id, action, delayTicks);
        TASKS.add(task);
        TASK_MAP.put(id, task);
        return id;
    }

    public static void cancel(int id) {
        ScheduledTask task = TASK_MAP.remove(id);
        if (task != null) {
            TASKS.remove(task);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<ScheduledTask> it = TASKS.iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next();
            task.ticksLeft--;
            if (task.ticksLeft <= 0) {
                try { task.action.run(); } catch (Exception e) { e.printStackTrace(); }
                it.remove();
                TASK_MAP.remove(task.id);
            }
        }
    }

    private static class ScheduledTask {
        final int id;
        final Runnable action;
        int ticksLeft;

        ScheduledTask(int id, Runnable action, int ticks) {
            this.id = id;
            this.action = action;
            this.ticksLeft = ticks;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScheduledTask that = (ScheduledTask) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }
 }
