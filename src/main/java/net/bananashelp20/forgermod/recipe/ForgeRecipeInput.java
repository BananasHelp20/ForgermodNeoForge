package net.bananashelp20.forgermod.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ForgeRecipeInput(ItemStack input1, ItemStack input2, ItemStack templateInput) implements RecipeInput {

    @Override
    public ItemStack getItem(int pIndex) {
        return switch(pIndex){case 0->input1;case 1->input2;case 2->templateInput;default->throw new IndexOutOfBoundsException(pIndex);};
    }

    @Override
    public int size() {
        return 3;
    }
}
