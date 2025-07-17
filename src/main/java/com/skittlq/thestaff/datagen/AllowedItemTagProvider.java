package com.skittlq.thestaff.datagen;

import com.skittlq.thestaff.TheStaff;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

import static com.skittlq.thestaff.util.ModConstants.ALLOWED_ITEMS;
import static com.skittlq.thestaff.util.ModTags.Items.ALLOWED_BLOCKS;

public class AllowedItemTagProvider extends ItemTagsProvider {

    public AllowedItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TheStaff.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Item item : ALLOWED_ITEMS) {
            this.tag(ALLOWED_BLOCKS).add(item);
        }
    }

}
