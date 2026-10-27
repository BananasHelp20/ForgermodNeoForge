package net.bananashelp20.forgermod.item.custom.attacks.axe;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
/** Ordinary metal tools get visuals only, never weapon augments or Vertical Strike. */
public final class AxeAnimationItems {
    private AxeAnimationItems(){}
    public static boolean isModAxe(ItemStack stack){return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(ForgerMod.MOD_ID)
            && (stack.getItem() instanceof AxeItem || stack.getItem() instanceof SwordItemWithEffect weapon && weapon.isAxe());}
}
