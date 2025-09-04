package com.skittlq.thestaff;

import com.skittlq.thestaff.abilities.StaffAbilities;
import com.skittlq.thestaff.anim.BuiltinAnims;
import com.skittlq.thestaff.anim.ClientPlayerAnimRuntime;
import com.skittlq.thestaff.blocks.ModBlocks;
import com.skittlq.thestaff.items.custom.StaffItem;
import com.skittlq.thestaff.rendering.DarkMinecraftRenderer;
import com.skittlq.thestaff.rendering.LightMinecraftRenderer;
import com.skittlq.thestaff.rendering.OmniBlockRenderer;
import com.skittlq.thestaff.rendering.StaffRenderer;
import com.skittlq.thestaff.util.StoredBlockModelProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(value = TheStaff.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TheStaff.MODID, value = Dist.CLIENT)
public class TheStaffClient {
    public static final ResourceLocation STAFF_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "staff_layer");

    public TheStaffClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerSelectProperties(RegisterSelectItemModelPropertyEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "stored_block_id"),
                StoredBlockModelProperty.TYPE
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        BuiltinAnims.registerAll();

        e.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(
                        ModBlocks.LIGHT_MINECRAFT.get(),
                        net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT_MIPPED
                )
        );

        e.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(
                        ModBlocks.DARK_MINECRAFT.get(),
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
                ModBlocks.LIGHT_MINECRAFT.get());
        e.register((state, level, pos, idx) ->
                        (idx == 0 || idx == 1)
                                ? (level != null && pos != null
                                ? BiomeColors.getAverageGrassColor(level, pos)
                                : GrassColor.get(0.5D, 1.0D))
                                : -1,
                ModBlocks.DARK_MINECRAFT.get());
    }

    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent e) {
        e.register(ResourceLocation.fromNamespaceAndPath(TheStaff.MODID,"staff_renderer"),
                StaffRenderer.Unbaked.MAP_CODEC);
        e.register(ResourceLocation.fromNamespaceAndPath(TheStaff.MODID,"light_minecraft_renderer"),
                LightMinecraftRenderer.Unbaked.MAP_CODEC);
        e.register(ResourceLocation.fromNamespaceAndPath(TheStaff.MODID,"omniblock_renderer"),
                OmniBlockRenderer.Unbaked.MAP_CODEC);
        e.register(ResourceLocation.fromNamespaceAndPath(TheStaff.MODID,"dark_minecraft_renderer"),
                DarkMinecraftRenderer.Unbaked.MAP_CODEC);

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
