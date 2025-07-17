package com.skittlq.thestaff.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public record StoredBlockModelProperty() implements SelectItemModelProperty<String> {

    public static final SelectItemModelProperty.Type<StoredBlockModelProperty, String> TYPE =
            SelectItemModelProperty.Type.create(
                    MapCodec.<StoredBlockModelProperty>unit(new StoredBlockModelProperty()),
                    Codec.STRING
            );

    @Nullable
    @Override
    public String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            var tag = data.copyTag();
            return tag.getString("StaffStoredBlockId").orElse(null);
        }
        return null;
    }


    @Override
    public Codec<String> valueCodec() {
        return null;
    }

    @Override
    public Type<StoredBlockModelProperty, String> type() {
        return TYPE;
    }
}
