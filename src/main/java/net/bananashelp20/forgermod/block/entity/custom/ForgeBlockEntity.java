package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.block.custom.ForgeBlock;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.recipe.ForgeRecipe;
import net.bananashelp20.forgermod.recipe.ForgeRecipeInput;
import net.bananashelp20.forgermod.recipe.ModRecipes;
import net.bananashelp20.forgermod.screen.custom.ForgeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ForgeBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler inventory = new ItemStackHandler(4) { //4 -> 4 slots big
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    //label (no functional use)
    private static final int INPUT_SLOT2 = 0; //shards
    private static final int TEMPLATE_SLOT = 2; //template
    private static final int INPUT_SLOT1 = 1; //carbon material
    private static final int OUTPUT_SLOT = 3; //output (ingot)

    private static final int SHARD_CRAFT_COST = 4;

    private static final Item[][] RECIPE_INPUTS = {
            //shard recipes
            {ModItems.ELECTRIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.INANISIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.PULSITE_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.SOMNIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.VULNUSIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.MORSIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.LUSH_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.TAIFUNITE_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            //{ModItems.DEVELOPIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.IGNISIUM_SHARD.get(), ModItems.CARBON_STEEL_INGOT.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},

            //claymore recipes
            {ModItems.ELECTRIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.INANISIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.PULSITE_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.SOMNIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.VULNUSIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.MORSIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.LUSH_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.TAIFUNITE_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.DEVELOPIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()},
            {ModItems.IGNISIUM_INGOT.get(), ModItems.CLAYMORE.get(), ModItems.ANCIENT_UPGRADE_TEMPLATE.get()}
    };

    private static final ItemStack[] RECIPE_OUTPUTS = {
            //shard recipe outputs
            new ItemStack(ModItems.ELECTRIUM_INGOT.get()),
            new ItemStack(ModItems.INANISIUM_INGOT.get()),
            new ItemStack(ModItems.PULSITE_INGOT.get()),
            new ItemStack(ModItems.SOMNIUM_INGOT.get()),
            new ItemStack(ModItems.VULNUSIUM_INGOT.get()),
            new ItemStack(ModItems.MORSIUM_INGOT.get()),
            new ItemStack(ModItems.LUSH_INGOT.get()),
            new ItemStack(ModItems.TAIFUNITE_INGOT.get()),
            //new ItemStack(ModItems.DEVELOPIUM_INGOT.get()),
            new ItemStack(ModItems.IGNISIUM_INGOT.get()),

            //claymore recipe outputs
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()),
            new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),
            new ItemStack(ModItems.DREAMBOUND_CLAYMORE.get()),
            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE.get()),
            new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),
            new ItemStack(ModItems.OVERGROWN_CLAYMORE.get()),
            new ItemStack(ModItems.STORMING_CLAYMORE.get()),
            new ItemStack(ModItems.STUMPFL_BAT.get()),
            new ItemStack(ModItems.INFERNAL_CLAYMORE.get())
    };

//    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private static final int resetMaxProgressTo = 250;
    protected final ContainerData data;
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

//    @Override
//    public void onLoad() {
//        super.onLoad();
//        lazyItemHandler = LazyOptional.of(() -> inventory);
//    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(this.inventory.getSlots());
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            inventory.setItem(i, this.inventory.getStackInSlot(i));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", inventory.serializeNBT(pRegistries));
        pTag.putInt("forge.progress", progress);
        pTag.putInt("forge.max_progress", maxProgress);

        super.saveAdditional(pTag, pRegistries);
    }

    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        inventory.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        progress = pTag.getInt("forge.progress");
        maxProgress = pTag.getInt("forge.max_progress");
    }

//    @Override
//    public void invalidateCaps() {
//        super.invalidateCaps();
//        lazyItemHandler.invalidate();
//    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.forgermod.forge");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ForgeMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static int recipeUsed = 0;
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (hasRecipe()) {
            level.setBlockAndUpdate(blockPos, blockState.setValue(ForgeBlock.LIT, true));
            increaseCraftingProgress();
            setChanged(level, blockPos, blockState);
            if (hasCraftingFinished()) {
                craftItem(recipeUsed);
                resetProgress();
                level.setBlockAndUpdate(blockPos, blockState.setValue(ForgeBlock.LIT, false));
            }
        } else {
            resetProgress();
        }
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = resetMaxProgressTo;
    }

    private void craftItem(int recipeUsed) {
        ItemStack output = RECIPE_OUTPUTS[recipeUsed];

        inventory.extractItem(INPUT_SLOT1, 1, false);
        inventory.extractItem(INPUT_SLOT2, SHARD_CRAFT_COST, false);
        inventory.insertItem(OUTPUT_SLOT, new ItemStack(output.getItem(), 1), false);
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        progress ++;
    }

    private boolean hasRecipe() {
        return ((isValidRecipe((inventory.getStackInSlot(INPUT_SLOT1).is(ModItems.CARBON_STEEL_INGOT.get())), RECIPE_INPUTS, RECIPE_OUTPUTS)) && canInsertItemIntoOutputSlot(RECIPE_OUTPUTS[recipeUsed]) && canInsertAmountIntoOutputSlot(RECIPE_OUTPUTS[recipeUsed].getCount()));
    }

    private boolean isValidRecipe(Boolean isShardRecipe, Item[][] recipeInputs, ItemStack[] recipeOutputs) { //ALTERNATIVE ZU JSON DATEIEN //wenn ein recipe ned geht, geht alles nd
        if (recipeOutputs.length == recipeInputs.length) {
            for (int i = 0; i < recipeInputs.length; i++) {
                if (inventory.getStackInSlot(INPUT_SLOT2).is(recipeInputs[i][0]) && (inventory.getStackInSlot(INPUT_SLOT2).getCount() >= SHARD_CRAFT_COST) && isShardRecipe && inventory.getStackInSlot(TEMPLATE_SLOT).is(recipeInputs[i][2]) && inventory.getStackInSlot(INPUT_SLOT1).is(recipeInputs[i][1])) {
                    recipeUsed = i;
                    return true;
                } else if (inventory.getStackInSlot(INPUT_SLOT2).is(recipeInputs[i][0]) && !isShardRecipe && inventory.getStackInSlot(TEMPLATE_SLOT).is(recipeInputs[i][2]) && inventory.getStackInSlot(INPUT_SLOT1).is(recipeInputs[i][1])) {
                    recipeUsed = i;
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<RecipeHolder<ForgeRecipe>> getCurrentRecipe() {
        return this.level.getRecipeManager().getRecipeFor(ModRecipes.FORGE_TYPE.get(), new ForgeRecipeInput(inventory.getStackInSlot(INPUT_SLOT1), inventory.getStackInSlot(INPUT_SLOT2), inventory.getStackInSlot(TEMPLATE_SLOT)), level);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64 : inventory.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
        int currentCount = inventory.getStackInSlot(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.inventory.getStackInSlot(OUTPUT_SLOT).getItem() == output.getItem();
    }
}
