package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.block.custom.InfusionTableBlock;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.recipe.MachineCrafting;
import net.bananashelp20.forgermod.recipe.ModRecipes;
import net.bananashelp20.forgermod.item.ModItems;

import net.bananashelp20.forgermod.screen.custom.InfusionTableMenu;
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


public class InfusionTableBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(4) { //4 -> 4 slots big
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return MachineCrafting.accepts(level,ModRecipes.INFUSION_TYPE.get(),slot,stack);
        }
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncDisplay();
        }
    };

    private static final int GEMSTONE_SLOT = 0;
    private static final int GEAR_SLOT = 1;
    private static final int TEMPLATE_SLOT = 2;
    private static final int OUTPUT_SLOT = 3;


    private static final int resetMaxProgressTo = 400;
    protected final ContainerData data;
    private final MachineCrafting craftingState=new MachineCrafting();
    private int progress = 0;
    private int maxProgress = resetMaxProgressTo;

    public InfusionTableBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.INFUSION_TABLE_BE.get(), pPos, pBlockState);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch(i) {
                    case 0 -> InfusionTableBlockEntity.this.progress;
                    case 1 -> InfusionTableBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int val) {
                switch (i) {
                    case 0: InfusionTableBlockEntity.this.progress = val;
                    case 1: InfusionTableBlockEntity.this.maxProgress = val;
                }
            }

            @Override
            public int getCount() { //2 variables
                return 2;
            }
        };
    }

    private ItemStack clientDisplayGemstone=ItemStack.EMPTY,clientDisplayWeapon=ItemStack.EMPTY;
    private ItemStack sentDisplayGemstone=ItemStack.EMPTY,sentDisplayWeapon=ItemStack.EMPTY;
    private boolean clientInfusing,sentInfusing;
    private float activity,previousActivity;
    private double animationTime,previousAnimationTime;

    public boolean infusing(){return level!=null && !level.isClientSide?displayActive():clientInfusing;}
    public float visualActivity(float partial){return previousActivity+(activity-previousActivity)*partial;}
    public double animationTime(float partial){return previousAnimationTime+(animationTime-previousAnimationTime)*partial;}
    public void clientTick(){
        previousActivity=activity;previousAnimationTime=animationTime;
        activity=Math.clamp(activity+(clientInfusing?.05F:-.05F),0,1);
        animationTime+=activity;
    }

    private boolean displayActive() { return progress>0 && hasRecipe(); }
    public ItemStack displayGemstone() {
        if(level==null || level.isClientSide)return clientDisplayGemstone;
        return inputGemstone();
    }
    private ItemStack inputGemstone(){
        var stack=itemHandler.getStackInSlot(GEMSTONE_SLOT);
        return MachineCrafting.accepts(level,ModRecipes.INFUSION_TYPE.get(),0,stack)?stack:ItemStack.EMPTY;
    }
    public ItemStack displayWeapon() {
        if(level==null || level.isClientSide)return clientDisplayWeapon;
        return inputWeapon();
    }
    private ItemStack inputWeapon(){
        var stack=itemHandler.getStackInSlot(GEAR_SLOT);
        return MachineCrafting.accepts(level,ModRecipes.INFUSION_TYPE.get(),1,stack)?stack:ItemStack.EMPTY;
    }
    private void syncDisplay() {
        if(level==null || level.isClientSide)return;
        boolean active=displayActive();
        var gem=displayGemstone().copyWithCount(1);
        var weapon=displayWeapon().copyWithCount(1);
        if(ItemStack.matches(gem,sentDisplayGemstone) && ItemStack.matches(weapon,sentDisplayWeapon) && active==sentInfusing)return;
        sentDisplayGemstone=gem;sentDisplayWeapon=weapon;sentInfusing=active;
        level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
    }
    public String getCorrectGemstoneTexturePath() {
        var gemstone=itemHandler.getStackInSlot(GEMSTONE_SLOT);
        if(gemstone.isEmpty())gemstone=clientDisplayGemstone;
        if(gemstone.is(ModItems.RUBY_GEMSTONE.get()))return "textures/gui/infusion_table_progress_ruby.png";
        if(gemstone.is(ModItems.AMBER_GEMSTONE.get()))return "textures/gui/infusion_table_progress_amber.png";
        if(gemstone.is(ModItems.AMETHYST_GEMSTONE.get()))return "textures/gui/infusion_table_progress_amethyst.png";
        if(gemstone.is(ModItems.JADE_GEMSTONE.get()))return "textures/gui/infusion_table_progress_jade.png";
        return "textures/gui/infusion_table_progress.png";
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        craftingState.save(pTag,pRegistries);
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.putInt("infusion_table.progress", progress);
        pTag.putInt("infusion_table.max_progress", maxProgress);

        super.saveAdditional(pTag, pRegistries);
    }

    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        craftingState.load(pTag,pRegistries);
        progress = pTag.getInt("infusion_table.progress");
        maxProgress = pTag.getInt("infusion_table.max_progress");
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("block.forgermod.infusion_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new InfusionTableMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        var tag=new CompoundTag();
        if(!inputGemstone().isEmpty())tag.put("display_gemstone",inputGemstone().copyWithCount(1).save(pRegistries));
        if(!inputWeapon().isEmpty())tag.put("display_weapon",inputWeapon().copyWithCount(1).save(pRegistries));
        tag.putBoolean("infusing",displayActive());
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag,HolderLookup.Provider registries) {
        clientDisplayGemstone=ItemStack.parseOptional(registries,tag.getCompound("display_gemstone"));
        clientDisplayWeapon=ItemStack.parseOptional(registries,tag.getCompound("display_weapon"));
        clientInfusing=tag.getBoolean("infusing");
    }
    @Override public void onDataPacket(net.minecraft.network.Connection connection,ClientboundBlockEntityDataPacket packet,HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(),registries);
    }


    @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}

    public void tick(Level level,BlockPos pos,BlockState state){
        if(level.isClientSide)return;
        var holder=MachineCrafting.find(level,ModRecipes.INFUSION_TYPE.get(),itemHandler);
        if(holder.isPresent() && MachineCrafting.fits(itemHandler,holder.get().value().assemble(MachineCrafting.input(itemHandler),level.registryAccess()))){
            if(!craftingState.accept(holder.get(),itemHandler,level.registryAccess()))progress=0;
            maxProgress=holder.get().value().processingTime();progress++;
            if(progress>=maxProgress){MachineCrafting.craft(level,itemHandler,holder.get().value());resetProgress();}
            setChanged();
        }else{if(progress!=0)setChanged();resetProgress();}
        setLit(level,pos,progress>0);
        syncDisplay();
    }
    private static void setLit(Level level,BlockPos pos,boolean lit){
        var current=level.getBlockState(pos);
        if(current.getValue(InfusionTableBlock.LIT)!=lit)level.setBlockAndUpdate(pos,current.setValue(InfusionTableBlock.LIT,lit));
    }
    private void resetProgress(){progress=0;maxProgress=resetMaxProgressTo;craftingState.clear();}
    private boolean hasRecipe(){
        var recipe=MachineCrafting.find(level,ModRecipes.INFUSION_TYPE.get(),itemHandler);
        return recipe.isPresent() && MachineCrafting.fits(itemHandler,recipe.get().value().assemble(MachineCrafting.input(itemHandler),level.registryAccess()));
    }
}
