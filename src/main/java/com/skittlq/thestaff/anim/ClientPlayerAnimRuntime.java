package com.skittlq.thestaff.anim;

import com.skittlq.thestaff.TheStaff;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientPlayerAnimRuntime {
    private ClientPlayerAnimRuntime() {}
    public record State(ResourceLocation id, long startGameTime,
                        boolean loop, float lengthSeconds, float fadeOutSeconds, long seed) {}

    private static final Map<UUID, State> STATES = new HashMap<>();

    public static void play(Player player, ResourceLocation id) {
        var anim = PlayerPoseAnims.get(id);
        if (anim == null) return;
        long now  = nowGameTime(player);
        long seed = java.util.concurrent.ThreadLocalRandom.current().nextLong();

        STATES.put(player.getUUID(),
                new State(id, now, anim.loop(), anim.lengthSeconds(), anim.fadeOutSeconds(), seed));

        TheStaff.LOGGER.info("[ANIM] RUNTIME  ▶ start  uuid={} name={} anim={} t={}",
                player.getUUID(), player.getGameProfile().getName(), id, now);
    }

    public static State current(Player player) {
        var s = STATES.get(player.getUUID());
        if (s == null) return null;

        float elapsedSec = gameTimeToSeconds(nowGameTime(player) - s.startGameTime);
        if (!s.loop && elapsedSec > (s.lengthSeconds + s.fadeOutSeconds)) {
            STATES.remove(player.getUUID());
            return null;
        }
        return s;
    }

    /** Stop any animation for this player. */
    public static void stop(Player player) {
        STATES.remove(player.getUUID());
        TheStaff.LOGGER.info("[ANIM] RUNTIME  ■ stop   uuid={} name={}", player.getUUID(), player.getGameProfile().getName());

    }

    public static float elapsedSeconds(Player player, State s, float partialTick) {
        long now = nowGameTime(player);
        return gameTimeToSeconds((now - s.startGameTime)) + partialTick / 20f;
    }

    private static long nowGameTime(Player player) {
        var lvl = player.level();
        return lvl != null ? lvl.getGameTime() : Minecraft.getInstance().level.getGameTime();
    }

    private static float gameTimeToSeconds(long gt) { return gt / 20f; }
}
