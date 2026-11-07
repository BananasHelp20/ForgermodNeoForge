package net.bananashelp20.forgermod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A crafting component; its foil is always enabled without adding enchantments. */
public final class AugmentedHeavyCoreItem extends Item {
    public AugmentedHeavyCoreItem(Properties properties) { super(properties); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }
}
