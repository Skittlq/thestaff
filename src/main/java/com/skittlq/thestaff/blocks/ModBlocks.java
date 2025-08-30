package com.skittlq.thestaff.blocks;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.blocks.custom.MinecraftGameIconBlock;
import com.skittlq.thestaff.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheStaff.MODID);

    public static final DeferredBlock<Block> MINECRAFT_GAME_ICON = registerBlock("minecraft",
            MinecraftGameIconBlock::new);

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
