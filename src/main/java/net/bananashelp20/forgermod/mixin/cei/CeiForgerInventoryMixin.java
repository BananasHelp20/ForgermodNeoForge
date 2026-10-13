package net.bananashelp20.forgermod.mixin.cei;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.compat.cei.CeiAugments;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerInventory", remap = false)
public abstract class CeiForgerInventoryMixin {
    @Inject(method = "calculateResult", at = @At("HEAD"), cancellable = true, require = 0)
    private void forgermod$calculateResult(ItemStack base, ItemStack addition, CallbackInfoReturnable<Object> callback) {
        Object result = CeiAugments.forgerResult(this, base, addition);
        if (result != null) callback.setReturnValue(result);
    }
}
