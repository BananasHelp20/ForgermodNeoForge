package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.block.custom.ForgeBlock;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.recipe.MachineCrafting;



import net.bananashelp20.forgermod.recipe.ModRecipes;
import net.bananashelp20.forgermod.screen.custom.ForgeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;






public class ForgeBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemStackHandler = new ItemStackHandler(4) { //4 -> 4 slots big
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return MachineCrafting.accepts(level,ModRecipes.FORGE_TYPE.get(),slot,stack);
        }
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int resetMaxProgressTo = 250;
    protected final ContainerData data;
    private final MachineCrafting craftingState=new MachineCrafting();
    private int progress = 0;
    private int maxProgress = resetMaxProgressTo;

    public ForgeBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FORGE_BE.get(), pPos, pBlockState);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch(i) {
                    case 0 -> ForgeBlockEntity.this.progress;
                    case 1 -> ForgeBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int val) {
                switch (i) {
                    case 0: ForgeBlockEntity.this.progress = val;
                    case 1: ForgeBlockEntity.this.maxProgress = val;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.forgermod.forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ForgeMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        craftingState.save(pTag,pRegistries);
        pTag.put("inventory", itemStackHandler.serializeNBT(pRegistries));
        pTag.putInt("forge.progress", progress);
        pTag.putInt("forge.max_progress", maxProgress);

        super.saveAdditional(pTag, pRegistries);
    }

    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        itemStackHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        craftingState.load(pTag,pRegistries);
        progress = pTag.getInt("forge.progress");
        maxProgress = pTag.getInt("forge.max_progress");
    }



    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registry){return saveWithoutMetadata(registry);}
    @Override @Nullable public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}

    public void tick(Level level,BlockPos pos,BlockState state){
        if(level.isClientSide)return;
        var holder=MachineCrafting.find(level,ModRecipes.FORGE_TYPE.get(),itemStackHandler);
        if(holder.isPresent() && MachineCrafting.fits(itemStackHandler,holder.get().value().assemble(MachineCrafting.input(itemStackHandler),level.registryAccess()))){
            if(!craftingState.accept(holder.get(),itemStackHandler,level.registryAccess()))progress=0;
            maxProgress=holder.get().value().processingTime();progress++;
            if(progress>=maxProgress){MachineCrafting.craft(level,itemStackHandler,holder.get().value());resetProgress();}
            setChanged();
        }else{if(progress!=0)setChanged();resetProgress();}
        setLit(level,pos,progress>0);

    }
    private static void setLit(Level level,BlockPos pos,boolean lit){
        var current=level.getBlockState(pos);
        if(current.getValue(ForgeBlock.LIT)!=lit)level.setBlockAndUpdate(pos,current.setValue(ForgeBlock.LIT,lit));
    }
    private void resetProgress(){progress=0;maxProgress=resetMaxProgressTo;craftingState.clear();}
    private boolean hasRecipe(){
        var recipe=MachineCrafting.find(level,ModRecipes.FORGE_TYPE.get(),itemStackHandler);
        return recipe.isPresent() && MachineCrafting.fits(itemStackHandler,recipe.get().value().assemble(MachineCrafting.input(itemStackHandler),level.registryAccess()));
    }
}
