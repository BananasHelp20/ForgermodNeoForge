package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** User-marked recommendations become learnable only after their implementation is complete. */
public final class RecommendedAbilities {
    public static final String QUENCH_POINT="tooltips.forgermod.passive.quench_point";
    private RecommendedAbilities() {}
    public static List<Augmentations.Ability> pool(ItemStack stack) {
        if(stack.getItem() instanceof IgnisiumWeapon weapon && weapon.isDagger())
            return List.of(new Augmentations.Ability(QUENCH_POINT,false));
        return List.of();
    }
}
