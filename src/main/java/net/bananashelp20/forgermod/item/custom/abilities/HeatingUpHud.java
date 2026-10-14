package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Locale;

/** Compact heat meter shown only while wielding a weapon with Heating Up. */
@EventBusSubscriber(modid = ForgerMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HeatingUpHud {
    private static final int WIDTH = 80;
    private HeatingUpHud() {}

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "heating_up_heat"), HeatingUpHud::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.player.isSpectator()
                || !Augmentations.hasPassive(minecraft.player.getMainHandItem(), RecommendedAbilities.HEATING_UP)) return;

        float heat = Math.clamp(HeatingUpNetwork.clientHeat(), 0, 20);
        int x = (graphics.guiWidth() - WIDTH) / 2;
        int y = graphics.guiHeight() - 67;
        Component label = Component.translatable("hud.forgermod.heating_up",
                String.format(Locale.ROOT, "%.1f", heat));
        graphics.drawString(minecraft.font, label, x, y - 12, 0xFFFFC77A, true);
        graphics.fill(x - 2, y - 2, x + WIDTH + 2, y + 7, 0xCC24130E);
        graphics.fill(x, y, x + WIDTH, y + 5, 0xFF563027);
        int filled = Math.round(WIDTH * heat / 20);
        if (filled > 0) {
            graphics.fill(x, y, x + filled, y + 5, 0xFFE56724);
            graphics.fill(x, y, x + filled, y + 1, 0xFFFFC15F);
        }
    }
}
