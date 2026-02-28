package net.bananashelp20.forgermod.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Objects;

public record ForgeRecipe(NonNullList<Ingredient> ingredients, ItemStack output) implements Recipe<ForgeRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(ForgeRecipeInput pInput, Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }

        return ingredients.get(0).test(pInput.getItem(1)) && ingredients.get(1).test(pInput.getItem(3)) && ingredients.get(2).test(pInput.getItem(2));
    }

    @Override
    public ItemStack assemble(ForgeRecipeInput pInput, HolderLookup.Provider pRegistries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORGE_SERIALIZER.get(); //nu-uh, oba ka
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FORGE_TYPE.get(); //nu-uh, oba ka
    }

    public static class Serializer implements RecipeSerializer<ForgeRecipe> { //FUNKTIONIERT NICHT --> ALTERNATIVE IN ForgeBlockEntity.java
        public static final MapCodec<ForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(

                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient1").forGetter((recipe) -> recipe.ingredients.getFirst()),
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient2").forGetter((recipe) -> recipe.ingredients.get(1)),
                Ingredient.CODEC_NONEMPTY.fieldOf("template").forGetter((recipe) -> recipe.ingredients.get(2)),
                ItemStack.CODEC.fieldOf("result").forGetter(ForgeRecipe::output)
        ).apply(inst, (ingredient1, ingredient2, template, output) -> new ForgeRecipe(NonNullList.of(ingredient1, ingredient2, template), output)));

        /*public static final StreamCodec<RegistryFriendlyByteBuf, ForgeRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, (recipe) -> recipe.getIngredients().get(0),
                        Ingredient.CONTENTS_STREAM_CODEC, (recipe) -> recipe.ingredients.get(1),
                        Ingredient.CONTENTS_STREAM_CODEC, (recipe) -> recipe.ingredients.get(2),
                        ItemStack.STREAM_CODEC, ForgeRecipe::output,
                        ForgeRecipe::new);*/

        @Override
        public MapCodec<ForgeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ForgeRecipe> streamCodec() {
            return null;
        }

//        @Override
//        public StreamCodec<RegistryFriendlyByteBuf, ForgeRecipe> streamCodec() {
//            return Objects.requireNonNull(STREAM_CODEC);
//        }
    }
}

