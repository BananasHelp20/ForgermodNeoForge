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

public record ForgeRecipe(Ingredient inputItem1, Ingredient inputItem2, Ingredient templateItem, ItemStack output) implements Recipe<ForgeRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem1);
        list.add(inputItem2);
        list.add(templateItem);
        return list;
    }

    @Override
    public boolean matches(ForgeRecipeInput pInput, Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }

        return inputItem1.test(pInput.getItem(1)) && inputItem2.test(pInput.getItem(3)) && templateItem.test(pInput.getItem(2));
    }

    @Override
    public ItemStack assemble(ForgeRecipeInput pInput, HolderLookup.Provider pRegistries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FORGE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ForgeRecipe> { //FUNKTIONIERT NICHT --> ALTERNATIVE IN ForgeBlockEntity.java
        public static final MapCodec<ForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(

                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient1").forGetter(ForgeRecipe::inputItem1),
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient2").forGetter(ForgeRecipe::inputItem2),
                Ingredient.CODEC_NONEMPTY.fieldOf("template").forGetter(ForgeRecipe::templateItem),

                ItemStack.CODEC.fieldOf("result").forGetter(ForgeRecipe::output)
        ).apply(inst, ForgeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ForgeRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, ForgeRecipe::inputItem1,
                        Ingredient.CONTENTS_STREAM_CODEC, ForgeRecipe::inputItem2,
                        Ingredient.CONTENTS_STREAM_CODEC, ForgeRecipe::templateItem,
                        ItemStack.STREAM_CODEC, ForgeRecipe::output, ForgeRecipe::new);

        @Override
        public MapCodec<ForgeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ForgeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

}

