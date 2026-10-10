package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.AugmentationAnimation;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.screen.custom.AugmentationTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class AugmentationTableBlockEntity extends BlockEntity implements MenuProvider {
    // Keep save indices. GEAR and OUTPUT occupy the same middle screen position.
    public static final int GEAR=0, TEMPLATE=1, SAPPHIRE=2, OUTPUT=3, DATA_SIZE=8;
    private int phase,progress,duration=100,selected=-1,firstRank,secondRank;
    private String first="",second="";
    private boolean internalMutation,legacyPaid;
    public final ItemStackHandler inventory=new ItemStackHandler(4) {
        @Override protected void onContentsChanged(int slot) {
            if(!internalMutation) {
                if(phase==AugmentationAnimation.COMPLETE && slot==OUTPUT && getStackInSlot(OUTPUT).isEmpty()) reset();
                else if(phase>0 && phase<AugmentationAnimation.COMPLETE && slot!=OUTPUT) reset();
            }
            setChanged();
        }
        @Override public int getSlotLimit(int slot) { return slot==GEAR || slot==OUTPUT?1:64; }
        @Override public boolean isItemValid(int slot,ItemStack stack) {
            return slot==GEAR?Augmentations.eligible(stack):(slot==TEMPLATE || slot==SAPPHIRE) && ingredient(stack);
        }
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate) {
            if(!internalMutation && (slot==OUTPUT || phase==AugmentationAnimation.COMPLETE
                    || slot==GEAR && phase!=0 || side(slot) && phase>=AugmentationAnimation.EXPAND)) return stack;
            return super.insertItem(slot,stack,simulate);
        }
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate) {
            if(!internalMutation && side(slot) && phase>=AugmentationAnimation.EXPAND) return ItemStack.EMPTY;
            return super.extractItem(slot,amount,simulate);
        }
    };
    public final ContainerData data=new ContainerData() {
        @Override public int getCount() { return DATA_SIZE; }
        @Override public int get(int index) {
            return switch(index) {
                case 0 -> phase; case 1 -> progress; case 2 -> duration; case 3 -> offerIndex(first); case 4 -> offerIndex(second);
                case 5 -> selected; case 6 -> firstRank; case 7 -> secondRank; default -> 0;
            };
        }
        @Override public void set(int index,int value) {
            switch(index) { case 0 -> phase=value; case 1 -> progress=value; case 2 -> duration=value; case 5 -> selected=value; }
        }
    };
    public AugmentationTableBlockEntity(BlockPos pos,BlockState state) { super(ModBlockEntities.AUGMENTATION_TABLE_BE.get(),pos,state); }
    private static boolean side(int slot) { return slot==TEMPLATE || slot==SAPPHIRE; }
    public static boolean ingredient(ItemStack stack) {
        return stack.is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()) || stack.is(ModItems.SAPPHIRE_GEMSTONE.get());
    }
    private boolean ingredientsReady() {
        ItemStack left=inventory.getStackInSlot(TEMPLATE),right=inventory.getStackInSlot(SAPPHIRE);
        return left.is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()) && right.is(ModItems.SAPPHIRE_GEMSTONE.get())
                || right.is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()) && left.is(ModItems.SAPPHIRE_GEMSTONE.get());
    }
    private boolean inputsReady() {
        ItemStack gear=inventory.getStackInSlot(GEAR);
        return gear.getCount()==1 && Augmentations.eligible(gear) && ingredientsReady() && inventory.getStackInSlot(OUTPUT).isEmpty();
    }
    private int offerIndex(String key) {
        var pool=Augmentations.pool(inventory.getStackInSlot(phase==AugmentationAnimation.COMPLETE?OUTPUT:GEAR));
        for(int i=0;i<pool.size();i++) if(pool.get(i).id().equals(key)) return i+1;
        return 0;
    }
    public boolean locked() { return phase!=0; }
    private void reset() {
        phase=0; progress=0; selected=-1; first=""; second=""; firstRank=0; secondRank=0; setChanged();
    }
    private void rollOffers() {
        var gear=inventory.getStackInSlot(GEAR);
        var offers=Augmentations.offers(gear,level.random);
        if(offers.isEmpty()) { reset(); return; }
        first=offers.get(0).id(); second=offers.get(1).id();
        firstRank=Augmentations.level(gear,first); secondRank=Augmentations.level(gear,second);
    }
    public void tick() {
        if(level==null || level.isClientSide) return;
        if(legacyPaid) {
            // Old saves spent a pair on start. Return it once before the new workflow.
            internalMutation=true;
            try { refund(TEMPLATE,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())); refund(SAPPHIRE,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get())); }
            finally { internalMutation=false; }
            legacyPaid=false; reset();
        }
        if(phase==AugmentationAnimation.COMPLETE) {
            if(inventory.getStackInSlot(OUTPUT).isEmpty()) reset();
            return;
        }
        if(!inputsReady()) { if(phase!=0) reset(); return; }
        var gear=inventory.getStackInSlot(GEAR);
        if(phase==0) {
            if(Augmentations.available(gear).isEmpty()) return;
            rollOffers(); duration=Math.min(10000,Augmentations.duration(gear)); progress=0; phase=AugmentationAnimation.ANALYZE; setChanged(); return;
        }
        first=Augmentations.migrateId(first); second=Augmentations.migrateId(second);
        var legal=Augmentations.available(gear);
        if(legal.stream().noneMatch(a -> a.id().equals(first)) || legal.stream().noneMatch(a -> a.id().equals(second))) {
            rollOffers(); setChanged(); if(phase==0) return;
        }
        if(phase>=AugmentationAnimation.ANALYZE && phase<=AugmentationAnimation.FINALIZE) {
            if(phase==AugmentationAnimation.ANALYZE && (progress+1)%10==0 && level instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,worldPosition.getX()+.5,
                        worldPosition.getY()+1,worldPosition.getZ()+.5,6,.3,.2,.3,.15);
            }
            if(++progress>=AugmentationAnimation.stageTicks(phase,duration)) { phase++; progress=0; }
            setChanged();
        }
    }
    private void refund(int slot,ItemStack stack) {
        ItemStack remainder=inventory.insertItem(slot,stack,false);
        if(!remainder.isEmpty()) net.minecraft.world.Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,remainder);
    }
    public boolean choose(int button) {
        if(level==null || level.isClientSide || phase!=AugmentationAnimation.CHOOSE || button<0 || button>1 || !inputsReady()) return false;
        ItemStack output=Augmentations.apply(inventory.getStackInSlot(GEAR),button==0?first:second);
        if(output.isEmpty()) return false;
        internalMutation=true;
        try {
            inventory.extractItem(TEMPLATE,1,false); inventory.extractItem(SAPPHIRE,1,false);
            inventory.setStackInSlot(GEAR,ItemStack.EMPTY); inventory.setStackInSlot(OUTPUT,output);
            selected=button; phase=AugmentationAnimation.COMPLETE; progress=0; setChanged();
        } finally { internalMutation=false; }
        return true;
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries); tag.put("inventory",inventory.serializeNBT(registries));
        tag.putInt("workflow",2); tag.putBoolean("legacyPaid",legacyPaid);
        tag.putInt("phase",phase); tag.putInt("progress",progress); tag.putInt("duration",duration); tag.putInt("selected",selected);
        tag.putInt("firstRank",firstRank); tag.putInt("secondRank",secondRank); tag.putString("first",first); tag.putString("second",second);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries); internalMutation=true;
        try { inventory.deserializeNBT(registries,tag.getCompound("inventory")); } finally { internalMutation=false; }
        duration=Math.clamp(tag.getInt("duration"),100,10000);
        first=tag.getString("first"); second=tag.getString("second"); firstRank=Math.clamp(tag.getInt("firstRank"),0,3); secondRank=Math.clamp(tag.getInt("secondRank"),0,3);
        selected=Math.clamp(tag.getInt("selected"),-1,1);
        if(tag.getInt("workflow")<2) {
            legacyPaid=tag.getInt("phase")>0; phase=inventory.getStackInSlot(OUTPUT).isEmpty()?0:AugmentationAnimation.COMPLETE;
            progress=0; selected=-1;
        } else {
            legacyPaid=tag.getBoolean("legacyPaid"); phase=Math.clamp(tag.getInt("phase"),0,AugmentationAnimation.COMPLETE);
            progress=Math.clamp(tag.getInt("progress"),0,AugmentationAnimation.stageTicks(phase,duration));
        }
    }
    @Override public Component getDisplayName() { return Component.translatable("block.forgermod.augmentation_table"); }
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player) { return new AugmentationTableMenu(id,inventory,this,data); }
    public void drops() {
        if(legacyPaid && level!=null && !level.isClientSide) {
            legacyPaid=false; internalMutation=true;
            try { refund(TEMPLATE,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())); refund(SAPPHIRE,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get())); }
            finally { internalMutation=false; }
        }
        var container=new net.minecraft.world.SimpleContainer(4); internalMutation=true;
        try { for(int i=0;i<4;i++) { container.setItem(i,inventory.getStackInSlot(i)); inventory.setStackInSlot(i,ItemStack.EMPTY); } }
        finally { internalMutation=false; }
        net.minecraft.world.Containers.dropContents(level,worldPosition,container);
    }
}
