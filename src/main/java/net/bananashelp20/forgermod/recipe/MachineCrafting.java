package net.bananashelp20.forgermod.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import java.util.*;

/** Shared transactional crafting and persistent progress identity for both recipe-manager driven machines. */
public final class MachineCrafting {
    private ResourceLocation id;
    private Tag signature;
    private MachineRecipe reference;
    private final ItemStack[] inputs={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};
    public static ForgeRecipeInput input(ItemStackHandler handler){return new ForgeRecipeInput(handler.getStackInSlot(0),handler.getStackInSlot(1),handler.getStackInSlot(2));}
    public static Optional<RecipeHolder<MachineRecipe>> find(Level level,RecipeType<MachineRecipe> type,ItemStackHandler handler){
        if(level==null)return Optional.empty();
        return level.getRecipeManager().getAllRecipesFor(type).stream().sorted(Comparator.comparing(holder->holder.id().toString()))
                .filter(holder->holder.value().matches(input(handler),level)).findFirst();
    }
    public static boolean accepts(Level level,RecipeType<MachineRecipe> type,int slot,ItemStack stack){
        return level!=null && slot>=0 && slot<3 && !stack.isEmpty() && level.getRecipeManager().getAllRecipesFor(type).stream().anyMatch(holder->holder.value().ingredient(slot).test(stack));
    }
    public static boolean fits(ItemStackHandler handler,ItemStack result){
        var existing=handler.getStackInSlot(3);
        return !result.isEmpty() && (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing,result))
                && existing.getCount()+result.getCount()<=Math.min(handler.getSlotLimit(3),result.getMaxStackSize());
    }
    /** False means inputs or the complete recipe definition changed, so progress must start over. */
    public boolean accept(RecipeHolder<MachineRecipe> holder,ItemStackHandler handler,HolderLookup.Provider registry){
        Tag next=reference==holder.value()?signature:holder.value().signature(registry);
        boolean same=holder.id().equals(id) && Objects.equals(signature,next);
        for(int slot=0;slot<3;slot++)same &= ItemStack.isSameItemSameComponents(inputs[slot],handler.getStackInSlot(slot));
        id=holder.id();signature=next;reference=holder.value();
        for(int slot=0;slot<3;slot++)inputs[slot]=handler.getStackInSlot(slot).copyWithCount(1);
        return same;
    }
    public void clear(){id=null;signature=null;reference=null;Arrays.fill(inputs,ItemStack.EMPTY);}
    public static boolean craft(Level level,ItemStackHandler handler,MachineRecipe recipe){
        var input=input(handler);if(!recipe.matches(input,level))return false;
        var result=recipe.assemble(input,level.registryAccess());if(!fits(handler,result))return false;
        var combined=result.copy();combined.grow(handler.getStackInSlot(3).getCount());
        // Validate the complete transaction before changing any slot. Handler callbacks may inspect recipes.
        for(int slot=0;slot<3;slot++)if(recipe.cost(slot)>0)handler.extractItem(slot,recipe.cost(slot),false);
        handler.setStackInSlot(3,combined);return true;
    }
    public void save(CompoundTag tag,HolderLookup.Provider registry){
        if(id==null)return;var state=new CompoundTag();state.putString("Id",id.toString());state.put("Recipe",signature.copy());
        for(int slot=0;slot<3;slot++)state.put("Input"+slot,inputs[slot].save(registry));tag.put("MachineCraft",state);
    }
    public void load(CompoundTag tag,HolderLookup.Provider registry){
        clear();if(!tag.contains("MachineCraft"))return;var state=tag.getCompound("MachineCraft");id=ResourceLocation.tryParse(state.getString("Id"));
        if(id==null || !state.contains("Recipe")){clear();return;}signature=state.get("Recipe").copy();
        for(int slot=0;slot<3;slot++)inputs[slot]=ItemStack.parseOptional(registry,state.getCompound("Input"+slot));
    }
}
