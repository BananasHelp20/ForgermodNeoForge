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
@Mixin(targets = "plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity", remap = false)
public abstract class CeiForgerBlockEntityMixin {
    @Inject(method = "isForgingAddition", at = @At("HEAD"), cancellable = true, require = 0)
    private void forgermod$addition(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (matching(stack) && !Augmentations.learned(stack).isEmpty()) callback.setReturnValue(true);
    }

    @Inject(method = "isExtractingSource", at = @At("HEAD"), cancellable = true, require = 0)
    private void forgermod$source(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (Augmentations.eligible(stack) && !Augmentations.learned(stack).isEmpty()) callback.setReturnValue(true);
    }

    @Inject(method = "isBlankMatchingTemplate", at = @At("HEAD"), cancellable = true, require = 0)
    private void forgermod$blank(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (matching(stack) && Augmentations.learned(stack).isEmpty()) callback.setReturnValue(true);
    }

    private boolean matching(ItemStack stack) {
        return Augmentations.isTemplate(stack) && Augmentations.isSuperTemplate(stack) == CeiAugments.machineSpecial(this);
    }
}
