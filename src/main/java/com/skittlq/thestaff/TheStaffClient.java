package com.skittlq.thestaff;

import com.skittlq.thestaff.abilities.StaffAbilities;
import com.skittlq.thestaff.blocks.ModBlocks;
import com.skittlq.thestaff.items.custom.StaffItem;
import com.skittlq.thestaff.rendering.GameIconItemGlowRenderer;
import com.skittlq.thestaff.rendering.StaffSpecialRenderer;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(value = TheStaff.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TheStaff.MODID, value = Dist.CLIENT)
public class TheStaffClient {
    public static final ResourceLocation STAFF_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath("thestaff", "staff_layer");

    public TheStaffClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                STAFF_LAYER_ID,
                1600,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> PlayState.STOP)
        );

        e.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(
                        ModBlocks.MINECRAFT_GAME_ICON.get(),
                        net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT_MIPPED
                )
        );
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block e) {
        e.register((state, level, pos, idx) ->
                        (idx == 0 || idx == 1)
                                ? (level != null && pos != null
                                ? BiomeColors.getAverageGrassColor(level, pos)
                                : GrassColor.get(0.5D, 1.0D))
                                : -1,
                ModBlocks.MINECRAFT_GAME_ICON.get());
    }

    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent e) {
        e.register(ResourceLocation.fromNamespaceAndPath("thestaff","staff_block_slot"),
                StaffSpecialRenderer.Unbaked.MAP_CODEC);
        e.register(ResourceLocation.fromNamespaceAndPath("thestaff","game_icon_item_glow"),
                GameIconItemGlowRenderer.Unbaked.MAP_CODEC);

    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof StaffItem staff)) return;

        var id = StaffItem.getStoredBlockId(stack);
        if (id != null) {
            if (player.isShiftKeyDown()) {
                StaffAbilities.get(id).onShiftBreakBlock(player.level(), player, event.getPos(), stack);
            } else {
                StaffAbilities.get(id).onBreakBlock(player.level(), player, event.getPos(), stack);
            }
        }
    }

}
