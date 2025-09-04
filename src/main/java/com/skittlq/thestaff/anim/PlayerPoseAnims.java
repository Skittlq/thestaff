package com.skittlq.thestaff.anim;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PlayerPoseAnims {
    private static final Map<ResourceLocation, PlayerPoseAnim> REGISTRY = new HashMap<>();

    private PlayerPoseAnims() {}

    public static void register(ResourceLocation id, PlayerPoseAnim anim) {
        REGISTRY.put(id, anim);
    }

    public static @Nullable PlayerPoseAnim get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static Set<ResourceLocation> ids() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }
}
