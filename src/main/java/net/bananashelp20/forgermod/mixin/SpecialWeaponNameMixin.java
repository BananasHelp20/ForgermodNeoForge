package net.bananashelp20.forgermod.mixin;

import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Color the resolved name, including renamed stacks and names shown outside tooltips. */
@Mixin(ItemStack.class)
public abstract class SpecialWeaponNameMixin {
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void forgermod$legendaryWeaponName(CallbackInfoReturnable<Component> callback) {
        ItemStack stack = (ItemStack)(Object)this;
        if (stack.getItem() instanceof SwordItemWithEffect weapon && weapon.hasMaterialEffect())
            callback.setReturnValue(callback.getReturnValue().copy().withStyle(ChatFormatting.GOLD));
    }
}
