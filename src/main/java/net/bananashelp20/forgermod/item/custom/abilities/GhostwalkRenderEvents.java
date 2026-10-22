package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = ForgerMod.MOD_ID, value = Dist.CLIENT)
public final class GhostwalkRenderEvents {
    private GhostwalkRenderEvents() {}

    @SubscribeEvent
    public static void player(RenderPlayerEvent.Pre event) {
        // Cancels the body and every equipment layer: armor, trims, elytra, headgear and both held items.
        if(GhostwalkVisibility.hidden(event.getEntity()))event.setCanceled(true);
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent event) {
        if(GhostwalkVisibility.hidden(Minecraft.getInstance().player))event.setCanceled(true);
    }
}
