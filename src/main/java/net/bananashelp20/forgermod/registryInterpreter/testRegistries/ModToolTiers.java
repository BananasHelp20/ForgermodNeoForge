package net.bananashelp20.forgermod.registryInterpreter.testRegistries;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;


public class ModToolTiers {
    //!GENERATE

    public static final Tier STEEL = new SimpleTier(
            ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL,
            350, 6.5f, 3, 12,
            () -> Ingredient.of(ModItems.ITEM1.get())
    );
    public static final Tier MAT = new SimpleTier(
            ModTags.Blocks.INCORRECT_FOR_MAT_TOOL,
            350, 6.5f, 3, 12,
            () -> Ingredient.of(ModItems.ITEM2.get())
    );
}