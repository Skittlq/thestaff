package com.skittlq.thestaff.mixins;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface FurnaceAccessor {
    @Accessor("litTimeRemaining")
    void setLitTimeRemaining(int value);

    @Accessor("litTotalTime")
    void setLitTotalTime(int value);
}
