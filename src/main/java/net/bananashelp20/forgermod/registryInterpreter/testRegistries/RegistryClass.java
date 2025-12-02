package net.bananashelp20.forgermod.registryInterpreter.testRegistries;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.minecraft.world.level.ItemLike;
public class RegistryClass {
//!GENERATE METHODS
public static ItemLike getDisplayItemForForgerModItemsTab() {
return ModItems.ITEM1.get();
}
public static ItemLike[] getForgerModItemsTabRegister() {
return new ItemLike[] {
                ModItems.SWORD2.get(),
                ModItems.SWORD1.get(),
                ModItems.ITEM3.get(),
                ModItems.ITEM2.get(),
                ModItems.ITEM1.get(),
};
}
}
