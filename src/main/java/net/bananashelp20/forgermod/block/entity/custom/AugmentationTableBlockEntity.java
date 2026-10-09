package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
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
import java.util.List;

public final class AugmentationTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int GEAR = 0, TEMPLATE = 1, SAPPHIRE = 2, OUTPUT = 3;
    private int phase, progress, duration = 100;
    private String first = "", second = "";
    public final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public int getSlotLimit(int slot) { return slot == GEAR || slot == OUTPUT ? 1 : 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case GEAR -> Augmentations.eligible(stack);
                case TEMPLATE -> stack.is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get());
                case SAPPHIRE -> stack.is(ModItems.SAPPHIRE_GEMSTONE.get());
                default -> false;
            };
        }
    };
    public final ContainerData data = new ContainerData() {
        @Override public int getCount() { return 5; }
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> phase; case 1 -> progress; case 2 -> duration;
                case 3 -> offerIndex(first); case 4 -> offerIndex(second); default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) { case 0 -> phase=value; case 1 -> progress=value; case 2 -> duration=value; }
        }
    };
    public AugmentationTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUGMENTATION_TABLE_BE.get(), pos, state);
    }
    private int offerIndex(String key) {
        var pool = Augmentations.pool(inventory.getStackInSlot(GEAR));
        for (int i=0;i<pool.size();i++) if (pool.get(i).id().equals(key)) return i + 1;
        return 0;
    }
    public boolean locked() { return phase != 0; }
    public void tick() {
        if (level == null || level.isClientSide) return;
        if (phase == 0 && inventory.getStackInSlot(OUTPUT).isEmpty()
                && Augmentations.eligible(inventory.getStackInSlot(GEAR))
                && inventory.getStackInSlot(GEAR).getCount() == 1
                && inventory.getStackInSlot(TEMPLATE).is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())
                && inventory.getStackInSlot(SAPPHIRE).is(ModItems.SAPPHIRE_GEMSTONE.get())) {
            List<Augmentations.Ability> offers = Augmentations.offers(inventory.getStackInSlot(GEAR), level.random);
            if (offers.isEmpty()) return; // Fully augmented items never consume ingredients.
            first=offers.get(0).id(); second=offers.get(1).id();
            duration=Augmentations.duration(inventory.getStackInSlot(GEAR)); progress=0; phase=1;
            inventory.extractItem(TEMPLATE,1,false); inventory.extractItem(SAPPHIRE,1,false);
            setChanged();
        } else if (phase == 1) {
            if (++progress >= duration) phase=2;
            setChanged();
            if (progress % 10 == 0 && level instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                        worldPosition.getX()+.5, worldPosition.getY()+1, worldPosition.getZ()+.5,
                        6, .3, .2, .3, .15);
            }
        }
    }
    public boolean choose(int button) {
        if (level == null || level.isClientSide || phase != 2 || button < 0 || button > 1
                || !inventory.getStackInSlot(OUTPUT).isEmpty()) return false;
        ItemStack output=Augmentations.apply(inventory.getStackInSlot(GEAR), button == 0 ? first : second);
        if (output.isEmpty()) return false;
        inventory.setStackInSlot(GEAR, ItemStack.EMPTY); inventory.setStackInSlot(OUTPUT,output);
        phase=0; progress=0; first=""; second=""; setChanged();
        return true;
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);
        tag.put("inventory",inventory.serializeNBT(registries));
        tag.putInt("phase",phase); tag.putInt("progress",progress); tag.putInt("duration",duration);
        tag.putString("first",first); tag.putString("second",second);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries); inventory.deserializeNBT(registries,tag.getCompound("inventory"));
        phase=Math.clamp(tag.getInt("phase"),0,2); duration=Math.max(100,tag.getInt("duration"));
        progress=Math.clamp(tag.getInt("progress"),0,duration); first=tag.getString("first"); second=tag.getString("second");
    }
    @Override public Component getDisplayName() { return Component.translatable("block.forgermod.augmentation_table"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AugmentationTableMenu(id,inventory,this,data);
    }
    public void drops() {
        var container = new net.minecraft.world.SimpleContainer(4);
        for (int i=0;i<4;i++) {
            container.setItem(i,inventory.getStackInSlot(i)); inventory.setStackInSlot(i,ItemStack.EMPTY);
        }
        net.minecraft.world.Containers.dropContents(level,worldPosition,container);
    }
}
