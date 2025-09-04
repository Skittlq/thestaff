package com.skittlq.thestaff.blocks;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.blocks.custom.DarkMinecraftBlock;
import com.skittlq.thestaff.blocks.custom.LightMinecraftBlock;
import com.skittlq.thestaff.blocks.custom.OmniBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheStaff.MODID);

    public static final DeferredBlock<Block> LIGHT_MINECRAFT = registerBlock("light_minecraft",
            LightMinecraftBlock::new);

    public static final DeferredBlock<Block> DARK_MINECRAFT = registerBlock("dark_minecraft",
            DarkMinecraftBlock::new);

    public static final DeferredBlock<Block> OMNIBLOCK = registerBlock("omniblock",
            OmniBlock::new);

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
