package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.WeaponAssemblerBlockEntity;
import net.bananashelp20.forgermod.item.custom.AncientCraftingTemplateItem;
import net.bananashelp20.forgermod.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class WeaponAssemblerMenu extends AbstractContainerMenu {
    public final WeaponAssemblerBlockEntity table;
    public WeaponAssemblerMenu(int id,Inventory inventory,FriendlyByteBuf buffer){
        this(id,inventory,(WeaponAssemblerBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }
    public WeaponAssemblerMenu(int id,Inventory inventory,WeaponAssemblerBlockEntity table){
        super(ModMenuTypes.WEAPON_ASSEMBLER_MENU.get(),id);this.table=table;
        addSlot(new SlotItemHandler(table.inventory,0,80,34));
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,8+col*18,100+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,158));
    }
    public String specialization(){return AncientCraftingTemplateItem.specialization(getSlot(0).getItem());}
    @Override public boolean stillValid(Player player){return stillValid(ContainerLevelAccess.create(table.getLevel(),table.getBlockPos()),player,ModBlocks.WEAPON_ASSEMBLER.get());}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0 || index>=slots.size())return ItemStack.EMPTY;
        Slot slot=getSlot(index);if(!slot.hasItem() || !slot.mayPickup(player))return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),original=stack.copy();
        if(index==0){if(!moveItemStackTo(stack,1,37,true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;
    }
}
