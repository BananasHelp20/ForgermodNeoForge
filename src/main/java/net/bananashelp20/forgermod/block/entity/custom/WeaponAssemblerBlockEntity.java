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
    private boolean crafting;
    private float previousActivity,activity;
    private double previousTime,animationTime;
    /** Future crafting code owns this flag; inserting a template alone never starts the arms. */
    public void setCrafting(boolean value){
        if(level==null || level.isClientSide || crafting==value)return;
        crafting=value;setChanged();
        level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
    }
    public boolean isCrafting(){return crafting;}
    public void clientAnimationTick(){
        previousActivity=activity;activity=net.minecraft.util.Mth.approach(activity,crafting?1:0,.05F);
        previousTime=animationTime;animationTime+=activity;
    }
    public float visualActivity(float partial){return net.minecraft.util.Mth.lerp(partial,previousActivity,activity);}
    public double animationTime(float partial){return net.minecraft.util.Mth.lerp(partial,previousTime,animationTime);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){
        var tag=new CompoundTag();tag.putBoolean("crafting",crafting);return tag;
    }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void handleUpdateTag(CompoundTag tag,HolderLookup.Provider registries){crafting=tag.getBoolean("crafting");}
    @Override public void onDataPacket(net.minecraft.network.Connection connection,
            net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket packet,HolderLookup.Provider registries){handleUpdateTag(packet.getTag(),registries);}
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
        // Crafting recipes will resume their own work after loading; never persist a purely visual job.
        crafting=false;
    }
    @Override public Component getDisplayName(){return Component.translatable("block.forgermod.weapon_assembler");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new WeaponAssemblerMenu(id,inventory,this);}
    public void drops(){
        var items=new SimpleContainer(inventory.getStackInSlot(0));
        inventory.setStackInSlot(0,net.minecraft.world.item.ItemStack.EMPTY);
        Containers.dropContents(level,worldPosition,items);
    }
}
