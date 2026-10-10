package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.AugmentationAnimation;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.bananashelp20.forgermod.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AugmentationTableMenu extends AbstractContainerMenu {
    public final AugmentationTableBlockEntity table;
    private final ContainerData data;
    public AugmentationTableMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id,inventory,(AugmentationTableBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()),new SimpleContainerData(AugmentationTableBlockEntity.DATA_SIZE));
    }
    public AugmentationTableMenu(int id, Inventory inventory, AugmentationTableBlockEntity table, ContainerData data) {
        super(ModMenuTypes.AUGMENTATION_TABLE_MENU.get(),id); this.table=table; this.data=data;
        int[] x={80,44,117,80};
        for (int i=0;i<4;i++) {
            final int index=i;
            addSlot(new SlotItemHandler(table.inventory,i,x[i],34) {
                @Override public boolean isActive() {
                    return index==0?phase()!=AugmentationAnimation.COMPLETE:index==3?phase()==AugmentationAnimation.COMPLETE:phase()<AugmentationAnimation.EXPAND;
                }
                @Override public boolean mayPlace(ItemStack stack) { return index!=3 && isActive() && phase()==0 && super.mayPlace(stack); }
                @Override public boolean mayPickup(Player player) { return isActive(); }
            });
        }
        for (int row=0;row<3;row++) for (int col=0;col<9;col++)
            addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));
        for (int col=0;col<9;col++) addSlot(new Slot(inventory,col,8+col*18,142));
        addDataSlots(data);
    }
    public int phase() { return data.get(0); }
    public int progress() { return data.get(1); }
    public int duration() { return data.get(2); }
    public int selected() { return data.get(5); }
    public int offerRank(int button) { return data.get(6+button); }
    public Augmentations.Ability offer(int button) {
        if(button<0 || button>1) return null;
        int index=data.get(3+button)-1;
        var pool=Augmentations.pool(getSlot(phase()==AugmentationAnimation.COMPLETE?3:0).getItem());
        return index >= 0 && index < pool.size() ? pool.get(index) : null;
    }
    @Override public boolean clickMenuButton(Player player,int button) {
        return stillValid(player) && table.choose(button);
    }
    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(table.getLevel(),table.getBlockPos()),player,ModBlocks.AUGMENTATION_TABLE.get());
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(), original=stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack,4,40,true)) return ItemStack.EMPTY;
        } else {
            if (phase()!=0) return ItemStack.EMPTY;
            if(Augmentations.eligible(stack)) {
                if(!moveItemStackTo(stack,0,1,false)) return ItemStack.EMPTY;
            } else if(AugmentationTableBlockEntity.ingredient(stack)) {
                // Merge matching stacks first, then use either empty side. Both side slots accept both ingredients.
                boolean moved=false;
                for(int side=1;side<=2;side++) if(ItemStack.isSameItemSameComponents(stack,getSlot(side).getItem()))
                    moved |= moveItemStackTo(stack,side,side+1,false);
                if(!stack.isEmpty()) moved |= moveItemStackTo(stack,1,3,false);
                if(!moved) return ItemStack.EMPTY;
            } else return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player,stack);
        return original;
    }
}
