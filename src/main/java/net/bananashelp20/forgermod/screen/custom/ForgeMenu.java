package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.ForgeBlockEntity;
import net.bananashelp20.forgermod.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ForgeMenu extends AbstractContainerMenu {
    public final ForgeBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public ForgeMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public ForgeMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.FORGE_MENU.get(), pContainerId);
        this.blockEntity = ((ForgeBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new SlotItemHandler(blockEntity.itemStackHandler, 3, 121, 31)); //output
        this.addSlot(new SlotItemHandler(blockEntity.itemStackHandler, 2, 96, 53)); //template
        this.addSlot(new SlotItemHandler(blockEntity.itemStackHandler, 1, 72, 31)); //ingot
        this.addSlot(new SlotItemHandler(blockEntity.itemStackHandler, 0, 36, 31)); //shards

        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledArrowProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int arrowPixelSize = 24;

        return (maxProgress != 0 && progress != 0) ? progress * arrowPixelSize / maxProgress : 0;
    }

    public int getScaledSomethingProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int somethingPixelSize = 95;

        return (maxProgress != 0 && progress != 0) ? progress * somethingPixelSize / maxProgress : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack sourceStack = source.getItem(), original = sourceStack.copy();
        if (index < 36) {
            if (TableInputs.upgradeTemplate(sourceStack)) {
                if (!moveItemStackTo(sourceStack, 37, 38, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(sourceStack, 38, 40, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(sourceStack, 0, 36, true)) return ItemStack.EMPTY;
        if (sourceStack.isEmpty()) source.set(ItemStack.EMPTY); else source.setChanged();
        source.onTake(player, sourceStack);
        return original;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.FORGE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

}
