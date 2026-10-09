package net.bananashelp20.forgermod.screen.custom;

import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.InfusionTableBlockEntity;
import net.bananashelp20.forgermod.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class InfusionTableMenu extends AbstractContainerMenu {
    public final InfusionTableBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public InfusionTableMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public String getCorrectTexture() {
        return blockEntity.getCorrectGemstoneTexturePath();
    }

    public Slot getSlot(int index) {
        return slots.get(index);
    }

    public InfusionTableMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.INFUSION_TABLE_MENU.get(), pContainerId);
        this.blockEntity = ((InfusionTableBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 3, 130, 35)); //output
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 1, 74, 35)); //gear
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 2, 53, 35)); //template
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 0, 12, 35)); //gemstone

        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledArrowProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int arrowPixelSize = 141;

        return (maxProgress != 0 && progress != 0) ? progress * arrowPixelSize / maxProgress : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack sourceStack = source.getItem(), original = sourceStack.copy();
        if (index < 36) {
            int destination = TableInputs.gemstone(sourceStack) ? 39
                    : sourceStack.is(net.bananashelp20.forgermod.item.ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()) ? 38
                    : TableInputs.infusible(sourceStack) ? 37 : -1;
            if (destination < 0 || !moveItemStackTo(sourceStack, destination, destination + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(sourceStack, 0, 36, true)) return ItemStack.EMPTY;
        if (sourceStack.isEmpty()) source.set(ItemStack.EMPTY); else source.setChanged();
        source.onTake(player, sourceStack);
        return original;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.INFUSION_TABLE.get());
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
