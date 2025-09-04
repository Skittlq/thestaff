package com.skittlq.thestaff.anim;

import com.skittlq.thestaff.TheStaff;
import com.skittlq.thestaff.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = TheStaff.MODID, value = Dist.CLIENT)
public final class AnimRenderStateBridge {

    public record ActiveAnim(ResourceLocation id, float tSeconds, boolean mainHand, boolean offHand) {}

    public static final ContextKey<ActiveAnim> ACTIVE =
            new ContextKey<>(ResourceLocation.fromNamespaceAndPath(TheStaff.MODID, "active_anim"));

    private static float currentPartialTick() {
        long ns = Minecraft.getInstance().getFrameTimeNs();
        float pt = ns / 50_000_000f;
        return pt < 0f ? 0f : (pt > 1f ? 1f : pt);
    }

    @SubscribeEvent
    public static void registerModifiers(RegisterRenderStateModifiersEvent e) {
        e.registerEntityModifier(net.minecraft.client.renderer.entity.player.PlayerRenderer.class, (entity, state) -> {
            if (!(entity instanceof net.minecraft.world.entity.player.Player p)) {
                state.setRenderData(ACTIVE, null);
                return;
            }
            var cur = ClientPlayerAnimRuntime.current(p);
            if (cur == null) { state.setRenderData(ACTIVE, null); return; }

            float t = ClientPlayerAnimRuntime.elapsedSeconds(p, cur, currentPartialTick());

            boolean affectMain = shouldAffectThirdPerson(cur.id(), p, net.minecraft.world.InteractionHand.MAIN_HAND);
            boolean affectOff  = shouldAffectThirdPerson(cur.id(), p, net.minecraft.world.InteractionHand.OFF_HAND);

            state.setRenderData(ACTIVE, new ActiveAnim(cur.id(), t, affectMain, affectOff));
        });
    }

    private static boolean shouldAffectThirdPerson(ResourceLocation animId, net.minecraft.world.entity.player.Player p,
                                                   net.minecraft.world.InteractionHand hand) {
        var stack = hand == net.minecraft.world.InteractionHand.MAIN_HAND ? p.getMainHandItem() : p.getOffhandItem();
        return !stack.isEmpty() && stack.getItem() == ModItems.PURPLE_STAFF.asItem();
    }
}
