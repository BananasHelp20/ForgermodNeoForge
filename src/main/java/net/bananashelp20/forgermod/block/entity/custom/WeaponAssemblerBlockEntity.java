package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.item.custom.AncientCraftingTemplateItem;
import net.bananashelp20.forgermod.screen.custom.WeaponAssemblerMenu;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Persists the template-selection stage; layouts/recipes await the user's design. */
public final class WeaponAssemblerBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler inventory=new ItemStackHandler(1){
        @Override public int getSlotLimit(int slot){return 1;}
        @Override public boolean isItemValid(int slot,net.minecraft.world.item.ItemStack stack){
            return !AncientCraftingTemplateItem.specialization(stack).isEmpty();
        }
        @Override protected void onContentsChanged(int slot){setChanged();}
    };
    public WeaponAssemblerBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.WEAPON_ASSEMBLER_BE.get(),pos,state);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);tag.put("inventory",inventory.serializeNBT(registries));
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);inventory.deserializeNBT(registries,tag.getCompound("inventory"));
    }
    @Override public Component getDisplayName(){return Component.translatable("block.forgermod.weapon_assembler");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new WeaponAssemblerMenu(id,inventory,this);}
    public void drops(){
        var items=new SimpleContainer(inventory.getStackInSlot(0));
        inventory.setStackInSlot(0,net.minecraft.world.item.ItemStack.EMPTY);
        Containers.dropContents(level,worldPosition,items);
    }
}
