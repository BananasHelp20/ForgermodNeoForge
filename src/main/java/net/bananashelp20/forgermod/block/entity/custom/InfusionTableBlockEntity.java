package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.block.custom.InfusionTableBlock;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import static net.bananashelp20.forgermod.recipe.ModSpecialRecipes.INFUSION_TABLE_RECIPE_INPUTS;
import static net.bananashelp20.forgermod.recipe.ModSpecialRecipes.INFUSION_TABLE_RECIPE_OUTPUTS;

public class InfusionTableBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(4) { //4 -> 4 slots big
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> net.bananashelp20.forgermod.screen.custom.TableInputs.gemstone(stack);
                case 1 -> net.bananashelp20.forgermod.screen.custom.TableInputs.infusible(stack);
                case 2 -> stack.is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get());
                default -> false;
            };
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

//    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int resetMaxProgressTo = 400;
    protected final ContainerData data;
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
        return net.bananashelp20.forgermod.screen.custom.TableInputs.gemstone(stack)?stack:ItemStack.EMPTY;
    }
    public ItemStack displayWeapon() {
        if(level==null || level.isClientSide)return clientDisplayWeapon;
        return inputWeapon();
    }
    private ItemStack inputWeapon(){
        var stack=itemHandler.getStackInSlot(GEAR_SLOT);
        return net.bananashelp20.forgermod.screen.custom.TableInputs.infusible(stack)?stack:ItemStack.EMPTY;
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
//        lazyItemHandler = LazyOptional.of(() -> itemHandler);
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
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.putInt("infusion_table.progress", progress);
        pTag.putInt("infusion_table.max_progress", maxProgress);

        super.saveAdditional(pTag, pRegistries);
    }

    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        progress = pTag.getInt("infusion_table.progress");
        maxProgress = pTag.getInt("infusion_table.max_progress");
    }

//    @Override
//    public void invalidateCaps() {
//        super.invalidateCaps();
//        lazyItemHandler.invalidate();
//    }

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

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        progress ++;
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = resetMaxProgressTo;
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private int recipeUsed = 0;

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (hasRecipe()) {
            setLit(level,blockPos,true);
            increaseCraftingProgress();
            setChanged(level, blockPos, blockState);
            if (hasCraftingFinished()) {
                craftItem(recipeUsed);
                resetProgress();
                setLit(level,blockPos,false);
            }
        } else {
            resetProgress();
            setLit(level,blockPos,false);
        }
        syncDisplay();
    }
    private static void setLit(Level level,BlockPos pos,boolean lit) {
        var state=level.getBlockState(pos);
        if(state.getValue(InfusionTableBlock.LIT)!=lit)level.setBlockAndUpdate(pos,state.setValue(InfusionTableBlock.LIT,lit));
    }

    private void craftItem(int recipeUsed) {
        ItemStack output = INFUSION_TABLE_RECIPE_OUTPUTS[recipeUsed];

        ItemStack input = itemHandler.getStackInSlot(GEAR_SLOT);
        ItemStack augmentedOutput = new ItemStack(output.getItem().builtInRegistryHolder(), 1, input.getComponentsPatch());
        itemHandler.extractItem(GEMSTONE_SLOT, 1, false);
        itemHandler.extractItem(GEAR_SLOT, 1, false);
        itemHandler.setStackInSlot(OUTPUT_SLOT, augmentedOutput);
    }

    private boolean hasRecipe() {
        return ((isValidRecipe(INFUSION_TABLE_RECIPE_INPUTS, INFUSION_TABLE_RECIPE_OUTPUTS)) && canInsertItemIntoOutputSlot(INFUSION_TABLE_RECIPE_OUTPUTS[recipeUsed]) && canInsertAmountIntoOutputSlot(INFUSION_TABLE_RECIPE_OUTPUTS[recipeUsed].getCount()));
    }

    private boolean isValidRecipe(Item[][] recipeInputs, ItemStack[] recipeOutputs) { //ALTERNATIVE ZU JSON DATEIEN
        if (recipeOutputs.length == recipeInputs.length) {
            for (int i = 0; i < recipeInputs.length; i++) {
                if (itemHandler.getStackInSlot(GEMSTONE_SLOT).is(recipeInputs[i][0]) && itemHandler.getStackInSlot(TEMPLATE_SLOT).is(recipeInputs[i][2]) && itemHandler.getStackInSlot(GEAR_SLOT).is(recipeInputs[i][1])
                        && (recipeInputs[i][0] != ModItems.JADE_GEMSTONE.get()
                        || JadeAugments.canInfuse(itemHandler.getStackInSlot(GEAR_SLOT)))) {
                    recipeUsed = i;
                    return true;
                }
            }
        }
        return false;
    }

//    private Optional<RecipeHolder<InfusionTableRecipe>> getCurrentRecipe() {
//        return this.level.getRecipeManager().getRecipeFor(ModRecipes.INFUSION_TABLE_TYPE.get(), new InfusionTableRecipeInput(itemHandler.getStackInSlot(GEMSTONE_SLOT), itemHandler.getStackInSlot(GEAR_SLOT), itemHandler.getStackInSlot(TEMPLATE_SLOT)), level);
//    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64 : itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() == output.getItem();
    }
}
