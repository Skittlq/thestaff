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

    public static final DeferredBlock<Block> WINDOWS = registerBlock("windows", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> TASKVIEW = registerBlock("taskview", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> EXPLORER = registerBlock("explorer", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> CHROME = registerBlock("chrome", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> ANIMATE = registerBlock("animate", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> GAMEICON = registerBlock("game_icon", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> AFTER_EFFECTS = registerBlock("after_effects", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> PREMIERE_PRO = registerBlock("premiere_pro", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> PHOTOSHOP = registerBlock("photoshop", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> AUDACITY = registerBlock("audacity", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> TASKBAR_ITEMS = registerBlock("taskbar_items", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> WALLPAPER = registerBlock("wallpaper", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> TASKBAR_BLOCK = registerBlock("taskbar_block", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> RECYCLE_BIN = registerBlock("recycle_bin", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));
    public static final DeferredBlock<Block> STORAGE_FOLDER = registerBlock("storage_folder", p -> new Block(p.noOcclusion().dynamicShape().strength(100000f)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
