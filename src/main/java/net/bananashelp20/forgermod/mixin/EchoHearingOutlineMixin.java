package net.bananashelp20.forgermod.mixin;

import net.bananashelp20.forgermod.item.custom.abilities.EchoHearingHighlights;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Add private outlines to the local viewer's renderer without changing vanilla glowing. */
@Mixin(Minecraft.class)
public abstract class EchoHearingOutlineMixin {
    @Inject(method="shouldEntityAppearGlowing",at=@At("RETURN"),cancellable=true)
    private void forgermod$privateEchoOutline(Entity entity, CallbackInfoReturnable<Boolean> callback) {
        if(!callback.getReturnValue() && EchoHearingHighlights.remainingTicks(Minecraft.getInstance().player,entity)>0)
            callback.setReturnValue(true);
    }
}
