package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
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
        this(id,inventory,(AugmentationTableBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()),new SimpleContainerData(5));
    }
    public AugmentationTableMenu(int id, Inventory inventory, AugmentationTableBlockEntity table, ContainerData data) {
        super(ModMenuTypes.AUGMENTATION_TABLE_MENU.get(),id); this.table=table; this.data=data;
        int[] x={8,48,88,148};
        for (int i=0;i<4;i++) {
            final int index=i;
            addSlot(new SlotItemHandler(table.inventory,i,x[i],35) {
                @Override public boolean mayPlace(ItemStack stack) { return index != 3 && phase()==0 && super.mayPlace(stack); }
                @Override public boolean mayPickup(Player player) { return index == 3 || phase()==0; }
            });
        }
        for (int row=0;row<3;row++) for (int col=0;col<9;col++)
            addSlot(new Slot(inventory,col+row*9+9,8+col*18,134+row*18));
        for (int col=0;col<9;col++) addSlot(new Slot(inventory,col,8+col*18,192));
        addDataSlots(data);
    }
    public int phase() { return data.get(0); }
    public int progress() { return data.get(1); }
    public int duration() { return data.get(2); }
    public Augmentations.Ability offer(int button) {
        int index=data.get(3+button)-1;
        var pool=Augmentations.pool(getSlot(0).getItem());
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
            int destination=Augmentations.eligible(stack)?0:stack.is(net.bananashelp20.forgermod.item.ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())?1:
                    stack.is(net.bananashelp20.forgermod.item.ModItems.SAPPHIRE_GEMSTONE.get())?2:-1;
            if (destination < 0 || !moveItemStackTo(stack,destination,destination+1,false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player,stack);
        return original;
    }
}
