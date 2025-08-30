package com.skittlq.thestaff.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;

import java.util.Timer;
import java.util.TimerTask;

import static com.skittlq.thestaff.util.TickCommand.setTickingRate;

public class SlowMotionHelper {
    private static float interpolateTickRate(float fraction) {
        fraction = Math.max(0.0f, Math.min(1.0f, fraction));
        double base = 5.0;
        return (float) (1.0 + (20.0 - 1.0) * (Math.pow(base, fraction) - 1) / (base - 1));
    }

    public static void smoothTickRateReset(CommandSourceStack source, int holdMillis, int rampMillis, ServerLevel level) {
        final int rampSteps = 40;
        final Timer timer = new Timer("TickRateInterpolator", false);
        final long totalMillis = holdMillis + rampMillis;
        final long startTime = System.currentTimeMillis();

        timer.scheduleAtFixedRate(new TimerTask() {
            int currentStep = 0;
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed < holdMillis) {
                    setTickingRate(source, 1);

                } else {
                    float fraction = Math.min(1.0f, (float) (elapsed - holdMillis) / rampMillis);
                    float tickRate = interpolateTickRate(fraction);
                    setTickingRate(source, tickRate);
                }
                currentStep++;
                if (elapsed >= totalMillis) {
                    timer.cancel();
                }
            }
        }, 0, totalMillis / (rampSteps + (holdMillis * rampSteps / rampMillis)));
    }
}
