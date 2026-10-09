package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeItems;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.List;

public final class WeaponTooltips {
    private WeaponTooltips() {}

    public static boolean descriptionsVisible() {
        return FMLEnvironment.dist == Dist.CLIENT && Screen.hasShiftDown();
    }

    public static void passive(List<Component> tooltip, String descriptionKey, boolean expanded) {
        tooltip.add(Component.translatable(descriptionKey + ".name").withStyle(ChatFormatting.GRAY));
        if (expanded) tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
    }

    public static void ordinaryPassives(ItemStack stack, List<Component> tooltip, boolean expanded) {
        String key = AxeItems.isAxe(stack) ? "tooltips.forgermod.passive.axe"
                : DaggerItems.isDagger(stack) ? "tooltips.forgermod.passive.dagger" : null;
        if (key == null) return;
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltips.forgermod.passive.heading").withStyle(ChatFormatting.GRAY));
        passive(tooltip, key, expanded);
        if (!expanded) hint(tooltip);
    }

    public static String romanNumeral(int level) {
        StringBuilder result = new StringBuilder();
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        for (int i = 0; i < values.length; i++) {
            while (level >= values[i]) { result.append(symbols[i]); level -= values[i]; }
        }
        return result.toString();
    }

    public static void hint(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltips.forgermod.ability.shift_hint",
                Component.literal("Hold Shift").withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
    }
}
