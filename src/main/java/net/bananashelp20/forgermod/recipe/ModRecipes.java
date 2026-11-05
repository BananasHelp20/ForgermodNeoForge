package net.bananashelp20.forgermod.recipe;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ForgerMod.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ForgerMod.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> FORGE_SERIALIZER =
            SERIALIZERS.register("forge", ()->new MachineRecipe.Serializer(false));

    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> FORGE_TYPE =
            TYPES.register("forge", () -> new RecipeType<MachineRecipe>() {
                @Override
                public String toString() {
                    return "forge";
                }
            }
    );


    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> INFUSION_SERIALIZER =
            SERIALIZERS.register("infusion_table", ()->new MachineRecipe.Serializer(true));
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> INFUSION_TYPE =
            TYPES.register("infusion_table", ()->new RecipeType<MachineRecipe>() { @Override public String toString(){return "infusion_table";} });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }

}
