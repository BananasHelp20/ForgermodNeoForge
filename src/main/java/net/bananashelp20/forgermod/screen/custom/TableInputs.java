package net.bananashelp20.forgermod.screen.custom;
import net.bananashelp20.forgermod.recipe.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
/** Slot acceptance follows synced recipes on both sides, including datapack ingredients/tags. */
public final class TableInputs {
    private TableInputs(){}
    public static boolean accepts(Level level,boolean infusion,int slot,ItemStack stack){
        return MachineCrafting.accepts(level,infusion?ModRecipes.INFUSION_TYPE.get():ModRecipes.FORGE_TYPE.get(),slot,stack);
    }
    public static boolean infusible(Level level,ItemStack stack){return accepts(level,true,1,stack);}
}
