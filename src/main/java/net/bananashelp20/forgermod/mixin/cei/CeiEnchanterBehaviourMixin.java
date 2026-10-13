package net.bananashelp20.forgermod.mixin.cei;

import net.bananashelp20.forgermod.compat.cei.CeiAugments;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchanterBehaviour", remap = false)
public abstract class CeiEnchanterBehaviourMixin {
    @Shadow public abstract boolean isTemplateMode();

    @Inject(method = "canProcess", at = @At("HEAD"), cancellable = true, require = 0)
    private void forgermod$canProcess(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (!isTemplateMode() && CeiAugments.canEnchant(stack, this)) callback.setReturnValue(true);
    }

    @Inject(method = "getResult", at = @At("HEAD"), cancellable = true, require = 0)
    private void forgermod$getResult(ItemStack stack, CallbackInfoReturnable<ItemStack> callback) {
        if (!isTemplateMode() && CeiAugments.canEnchant(stack, this))
            callback.setReturnValue(CeiAugments.enchant(stack, this));
    }

    @Inject(method = "getExperienceCost", at = @At("HEAD"), cancellable = true, require = 0)
    private void forgermod$getCost(CallbackInfoReturnable<Integer> callback) {
        if (!isTemplateMode() && CeiAugments.canEnchant(CeiAugments.heldItem(this), this)) callback.setReturnValue(250);
    }
}
