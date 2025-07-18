package com.skittlq.thestaff.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.util.TimeUtil;

public class TickCommand {
    private static final float MAX_TICKRATE = 10000.0F;
    private static final String DEFAULT_TICKRATE = String.valueOf(20);

    public TickCommand() {
    }

    private static String nanosToMilisString(long nanos) {
        return String.format(Locale.ROOT, "%.1f", (float)nanos / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND);
    }

    public static int setTickingRate(CommandSourceStack source, float tickRate) {
        ServerTickRateManager servertickratemanager = source.getServer().tickRateManager();
        servertickratemanager.setTickRate(tickRate);
        String s = String.format(Locale.ROOT, "%.1f", tickRate);
        source.sendSuccess(() -> {
            return Component.translatable("commands.tick.rate.success", new Object[]{s});
        }, true);
        return (int)tickRate;
    }

    public static int tickQuery(CommandSourceStack source) {
        ServerTickRateManager servertickratemanager = source.getServer().tickRateManager();
        String s = nanosToMilisString(source.getServer().getAverageTickTimeNanos());
        float f = servertickratemanager.tickrate();
        String s1 = String.format(Locale.ROOT, "%.1f", f);
        if (servertickratemanager.isSprinting()) {
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.status.sprinting");
            }, false);
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.query.rate.sprinting", new Object[]{s1, s});
            }, false);
        } else {
            if (servertickratemanager.isFrozen()) {
                source.sendSuccess(() -> {
                    return Component.translatable("commands.tick.status.frozen");
                }, false);
            } else if (servertickratemanager.nanosecondsPerTick() < source.getServer().getAverageTickTimeNanos()) {
                source.sendSuccess(() -> {
                    return Component.translatable("commands.tick.status.lagging");
                }, false);
            } else {
                source.sendSuccess(() -> {
                    return Component.translatable("commands.tick.status.running");
                }, false);
            }

            String s2 = nanosToMilisString(servertickratemanager.nanosecondsPerTick());
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.query.rate.running", new Object[]{s1, s, s2});
            }, false);
        }

        long[] along = Arrays.copyOf(source.getServer().getTickTimesNanos(), source.getServer().getTickTimesNanos().length);
        Arrays.sort(along);
        String s3 = nanosToMilisString(along[along.length / 2]);
        String s4 = nanosToMilisString(along[(int)((double)along.length * 0.95)]);
        String s5 = nanosToMilisString(along[(int)((double)along.length * 0.99)]);
        source.sendSuccess(() -> {
            return Component.translatable("commands.tick.query.percentiles", new Object[]{s3, s4, s5, along.length});
        }, false);
        return (int)f;
    }

    public static int sprint(CommandSourceStack source, int sprintTime) {
        boolean flag = source.getServer().tickRateManager().requestGameToSprint(sprintTime);
        if (flag) {
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.sprint.stop.success");
            }, true);
        }

        source.sendSuccess(() -> {
            return Component.translatable("commands.tick.status.sprinting");
        }, true);
        return 1;
    }

    public static int setFreeze(CommandSourceStack source, boolean frozen) {
        ServerTickRateManager servertickratemanager = source.getServer().tickRateManager();
        if (frozen) {
            if (servertickratemanager.isSprinting()) {
                servertickratemanager.stopSprinting();
            }

            if (servertickratemanager.isSteppingForward()) {
                servertickratemanager.stopStepping();
            }
        }

        servertickratemanager.setFrozen(frozen);
        if (frozen) {
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.status.frozen");
            }, true);
        } else {
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.status.running");
            }, true);
        }

        return frozen ? 1 : 0;
    }

    public static int step(CommandSourceStack source, int ticks) {
        ServerTickRateManager servertickratemanager = source.getServer().tickRateManager();
        boolean flag = servertickratemanager.stepGameIfPaused(ticks);
        if (flag) {
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.step.success", new Object[]{ticks});
            }, true);
        } else {
            source.sendFailure(Component.translatable("commands.tick.step.fail"));
        }

        return 1;
    }

    public static int stopStepping(CommandSourceStack source) {
        ServerTickRateManager servertickratemanager = source.getServer().tickRateManager();
        boolean flag = servertickratemanager.stopStepping();
        if (flag) {
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.step.stop.success");
            }, true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.tick.step.stop.fail"));
            return 0;
        }
    }

    public static int stopSprinting(CommandSourceStack source) {
        ServerTickRateManager servertickratemanager = source.getServer().tickRateManager();
        boolean flag = servertickratemanager.stopSprinting();
        if (flag) {
            source.sendSuccess(() -> {
                return Component.translatable("commands.tick.sprint.stop.success");
            }, true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("commands.tick.sprint.stop.fail"));
            return 0;
        }
    }
}
