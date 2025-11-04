package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.screen.custom.ForgeMenu;
import net.bananashelp20.forgermod.screen.custom.InfusionTableMenu;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class InfusionTableBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(4) { //4 -> 4 slots big
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int GEMSTONE_SLOT = 0;
    private static final int GEAR_SLOT = 1;
    private static final int TEMPLATE_SLOT = 2;
    private static final int OUTPUT_SLOT = 3;

    private static final Item[][] RECIPE_INPUTS = {
            {ModItems.RUBY_GEMSTONE.get(), ModItems.CLAYMORE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.CLAYMORE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.CLAYMORE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.CLAYMORE_OF_THUNDER.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.INFERNAL_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.INFERNAL_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.INFERNAL_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.INFERNAL_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.CLAYMORE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.CLAYMORE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.CLAYMORE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.CLAYMORE_OF_THE_VOID.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.OVERGROWN_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.OVERGROWN_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.OVERGROWN_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.OVERGROWN_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.HOLLOW_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.HOLLOW_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.HOLLOW_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.HOLLOW_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.CURSEBLOOD_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.CURSEBLOOD_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.CURSEBLOOD_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.CURSEBLOOD_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.DREAMBOUND_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.DREAMBOUND_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.DREAMBOUND_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.DREAMBOUND_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.SHRIEKING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.SHRIEKING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.SHRIEKING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.SHRIEKING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},

            {ModItems.RUBY_GEMSTONE.get(), ModItems.STORMING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMBER_GEMSTONE.get(), ModItems.STORMING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.AMETHYST_GEMSTONE.get(), ModItems.STORMING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
            {ModItems.JADE_GEMSTONE.get(), ModItems.STORMING_CLAYMORE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()},
    };

    private static final ItemStack[] RECIPE_OUTPUTS = {
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER_RUBY.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER_AMBER.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER_AMETHYST.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THUNDER_JADE.get()),

            new ItemStack(ModItems.INFERNAL_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.INFERNAL_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.INFERNAL_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.INFERNAL_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID_RUBY.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID_AMBER.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get()),
            new ItemStack(ModItems.CLAYMORE_OF_THE_VOID_JADE.get()),

            new ItemStack(ModItems.OVERGROWN_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.OVERGROWN_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.OVERGROWN_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.OVERGROWN_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.HOLLOW_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.HOLLOW_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.HOLLOW_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.HOLLOW_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.CURSEBLOOD_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.DREAMBOUND_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.DREAMBOUND_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.DREAMBOUND_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.DREAMBOUND_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.SHRIEKING_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.SHRIEKING_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.SHRIEKING_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.SHRIEKING_CLAYMORE_JADE.get()),

            new ItemStack(ModItems.STORMING_CLAYMORE_RUBY.get()),
            new ItemStack(ModItems.STORMING_CLAYMORE_AMBER.get()),
            new ItemStack(ModItems.STORMING_CLAYMORE_AMETHYST.get()),
            new ItemStack(ModItems.STORMING_CLAYMORE_JADE.get()),
    };

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

    public String getCorrectGemstoneTexturePath() {
        if (RECIPE_INPUTS[recipeUsed][0] == ModItems.RUBY_GEMSTONE.get()) {
            return "textures/gui/infusion_table_progress_ruby.png";
        } else if (RECIPE_INPUTS[recipeUsed][0] == ModItems.AMBER_GEMSTONE.get())  {
            return "textures/gui/infusion_table_progress_amber.png";
        } else if (RECIPE_INPUTS[recipeUsed][0] == ModItems.AMETHYST_GEMSTONE.get()) {
            return "textures/gui/infusion_table_progress_amethyst.png";
        } else if (RECIPE_INPUTS[recipeUsed][0] == ModItems.JADE_GEMSTONE.get()) {
            return "textures/gui/infusion_table_progress_jade.png";
        }
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
        return saveWithoutMetadata(pRegistries);
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

    public static int recipeUsed = 0;

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (hasRecipe()) {
            increaseCraftingProgress();
            setChanged(level, blockPos, blockState);
            if (hasCraftingFinished()) {
                craftItem(recipeUsed);
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void craftItem(int recipeUsed) {
        ItemStack output = RECIPE_OUTPUTS[recipeUsed];

        itemHandler.extractItem(GEMSTONE_SLOT, 1, false);
        itemHandler.extractItem(GEAR_SLOT, 1, false);
        itemHandler.insertItem(OUTPUT_SLOT, new ItemStack(output.getItem(), 1), false);
    }

    private boolean hasRecipe() {
        return ((isValidRecipe(RECIPE_INPUTS, RECIPE_OUTPUTS)) && canInsertItemIntoOutputSlot(RECIPE_OUTPUTS[recipeUsed]) && canInsertAmountIntoOutputSlot(RECIPE_OUTPUTS[recipeUsed].getCount()));
    }

    private boolean isValidRecipe(Item[][] recipeInputs, ItemStack[] recipeOutputs) { //ALTERNATIVE ZU JSON DATEIEN
        if (recipeOutputs.length == recipeInputs.length) {
            for (int i = 0; i < recipeInputs.length; i++) {
                if (itemHandler.getStackInSlot(GEMSTONE_SLOT).is(recipeInputs[i][0]) && itemHandler.getStackInSlot(TEMPLATE_SLOT).is(recipeInputs[i][2]) && itemHandler.getStackInSlot(GEAR_SLOT).is(recipeInputs[i][1])) {
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