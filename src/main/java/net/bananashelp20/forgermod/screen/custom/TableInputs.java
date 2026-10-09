package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.recipe.ModSpecialRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;

public final class TableInputs {
    private TableInputs() {}

    public static boolean gemstone(ItemStack stack) {
        return stack.is(ModItems.RUBY_GEMSTONE.get()) || stack.is(ModItems.AMBER_GEMSTONE.get())
                || stack.is(ModItems.AMETHYST_GEMSTONE.get()) || stack.is(ModItems.JADE_GEMSTONE.get());
    }

    public static boolean infusible(ItemStack stack) {
        for (var recipe : ModSpecialRecipes.INFUSION_TABLE_RECIPE_INPUTS) {
            if (stack.is(recipe[1])) return true;
        }
        return false;
    }

    public static boolean upgradeTemplate(ItemStack stack) {
        return stack.getItem() instanceof SmithingTemplateItem || stack.is(ModItems.ANCIENT_UPGRADE_TEMPLATE.get())
                || stack.is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get());
    }
}
