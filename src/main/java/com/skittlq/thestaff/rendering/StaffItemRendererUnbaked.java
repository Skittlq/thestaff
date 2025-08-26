package com.skittlq.thestaff.rendering;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;

public record StaffItemRendererUnbaked(ResourceLocation model) implements ItemModel.Unbaked {

    public static final MapCodec<StaffItemRendererUnbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("model").forGetter(StaffItemRendererUnbaked::model)
            ).apply(instance, StaffItemRendererUnbaked::new)
    );

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(model); // so base staff model is baked
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context) {
        return new StaffItemRenderer();
    }

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type() {
        return MAP_CODEC;
    }
}
