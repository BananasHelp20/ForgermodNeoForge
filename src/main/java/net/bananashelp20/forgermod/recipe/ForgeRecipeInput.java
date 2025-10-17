package net.bananashelp20.forgermod.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ForgeRecipeInput(ItemStack input1, ItemStack input2, ItemStack templateInput) implements RecipeInput {

    @Override
    public ItemStack getItem(int pIndex) {
        if (pIndex == 1) {
            return input1;
        } else if (pIndex == 2) {
            return templateInput;
        }
        return input2; //index: 3
    }

    @Override
    public int size() {
        return 3;
    }
}
