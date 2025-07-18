package com.skittlq.thestaff.mixins;

import net.minecraft.world.entity.vehicle.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecartFurnace.class)
public interface MinecartFurnaceAccessor {
    @Accessor("fuel")
    void setFuel(int fuel);

    @Accessor("fuel")
    int getFuel();

    @Invoker("setHasFuel")
    void invokeSetHasFuel(boolean hasFuel);
}
