package com.skittlq.thestaff.blocks.custom;

import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class MinecraftGameIconBlock extends GrassBlock {
    public MinecraftGameIconBlock(Properties properties) {
        super(properties.mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS));
    }
}
