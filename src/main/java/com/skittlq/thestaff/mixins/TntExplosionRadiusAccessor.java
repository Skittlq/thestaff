package com.skittlq.thestaff.mixins;

import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PrimedTnt.class)
public interface TntExplosionRadiusAccessor {
    @Accessor("explosionPower")
    float getExplosionPower();

    @Accessor("explosionPower")
    void setExplosionPower(float power);
}
