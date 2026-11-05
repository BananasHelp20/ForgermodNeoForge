package net.bananashelp20.forgermod.recipe;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import java.util.List;

/** Data-defined three-slot machine recipe. Slot order: additive, base item, template. */
public record MachineRecipe(Ingredient ingredient1, Ingredient ingredient2, Ingredient template, ItemStack output,
                            int count1, int count2, int templateCount, int processingTime,
                            boolean copyComponents, boolean requiresJadeCompatible, boolean infusion) implements Recipe<ForgeRecipeInput> {
    public MachineRecipe {
        if(count1<1 || count1>64 || count2<1 || count2>64 || templateCount<0 || templateCount>64 || processingTime<1 || processingTime>32767 || output.isEmpty() || output.getCount()>output.getMaxStackSize())
            throw new IllegalArgumentException("Invalid machine recipe counts, time or output stack");
    }
    @Override public NonNullList<Ingredient> getIngredients(){return NonNullList.copyOf(List.of(ingredient1,ingredient2,template));}
    public Ingredient ingredient(int slot){return switch(slot){case 0->ingredient1;case 1->ingredient2;case 2->template;default->throw new IndexOutOfBoundsException(slot);};}
    public int cost(int slot){return switch(slot){case 0->count1;case 1->count2;case 2->templateCount;default->throw new IndexOutOfBoundsException(slot);};}
    @Override public boolean matches(ForgeRecipeInput input,Level level){
        for(int slot=0;slot<3;slot++)if(!ingredient(slot).test(input.getItem(slot)) || input.getItem(slot).getCount()<Math.max(1,cost(slot)))return false;
        return !requiresJadeCompatible || JadeAugments.canInfuse(input.getItem(1));
    }
    @Override public ItemStack assemble(ForgeRecipeInput input,HolderLookup.Provider registry){
        if(!copyComponents)return output.copy();
        var result=new ItemStack(output.getItem().builtInRegistryHolder(),output.getCount(),input.getItem(1).getComponentsPatch());
        result.applyComponents(output.getComponentsPatch());return result;
    }
    @Override public boolean canCraftInDimensions(int width,int height){return width*height>=3;}
    @Override public ItemStack getResultItem(HolderLookup.Provider registry){return output.copy();}
    @Override public boolean isSpecial(){return true;}
    @Override public RecipeSerializer<MachineRecipe> getSerializer(){return infusion?ModRecipes.INFUSION_SERIALIZER.get():ModRecipes.FORGE_SERIALIZER.get();}
    @Override public RecipeType<MachineRecipe> getType(){return infusion?ModRecipes.INFUSION_TYPE.get():ModRecipes.FORGE_TYPE.get();}
    public Tag signature(HolderLookup.Provider registry){return getSerializer().codec().codec().encodeStart(registry.createSerializationContext(NbtOps.INSTANCE),this).getOrThrow();}

    public static final class Serializer implements RecipeSerializer<MachineRecipe> {
        private final MapCodec<MachineRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf,MachineRecipe> stream;
        public Serializer(boolean infusion){
            codec=RecordCodecBuilder.mapCodec(inst->inst.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient1").forGetter(MachineRecipe::ingredient1),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient2").forGetter(MachineRecipe::ingredient2),
                    Ingredient.CODEC_NONEMPTY.fieldOf("template").forGetter(MachineRecipe::template),
                    ItemStack.CODEC.fieldOf("result").forGetter(MachineRecipe::output),
                    Codec.intRange(1,64).optionalFieldOf("count1",1).forGetter(MachineRecipe::count1),
                    Codec.intRange(1,64).optionalFieldOf("count2",1).forGetter(MachineRecipe::count2),
                    Codec.intRange(0,64).optionalFieldOf("template_count",infusion?0:1).forGetter(MachineRecipe::templateCount),
                    Codec.intRange(1,32767).optionalFieldOf("processing_time",infusion?400:250).forGetter(MachineRecipe::processingTime),
                    Codec.BOOL.optionalFieldOf("copy_components",infusion).forGetter(MachineRecipe::copyComponents),
                    Codec.BOOL.optionalFieldOf("requires_jade_compatible",false).forGetter(MachineRecipe::requiresJadeCompatible)
            ).apply(inst,(a,b,t,o,c1,c2,ct,time,copy,jade)->new MachineRecipe(a,b,t,o,c1,c2,ct,time,copy,jade,infusion)));
            stream=new StreamCodec<>(){
                @Override public MachineRecipe decode(RegistryFriendlyByteBuf buffer){
                    return new MachineRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                            ItemStack.STREAM_CODEC.decode(buffer),buffer.readVarInt(),buffer.readVarInt(),buffer.readVarInt(),buffer.readVarInt(),buffer.readBoolean(),buffer.readBoolean(),infusion);
                }
                @Override public void encode(RegistryFriendlyByteBuf buffer,MachineRecipe recipe){
                    for(var ingredient:recipe.getIngredients())Ingredient.CONTENTS_STREAM_CODEC.encode(buffer,ingredient);
                    ItemStack.STREAM_CODEC.encode(buffer,recipe.output);buffer.writeVarInt(recipe.count1);buffer.writeVarInt(recipe.count2);buffer.writeVarInt(recipe.templateCount);
                    buffer.writeVarInt(recipe.processingTime);buffer.writeBoolean(recipe.copyComponents);buffer.writeBoolean(recipe.requiresJadeCompatible);
                }
            };
        }
        @Override public MapCodec<MachineRecipe> codec(){return codec;}
        @Override public StreamCodec<RegistryFriendlyByteBuf,MachineRecipe> streamCodec(){return stream;}
    }
}
