package net.bananashelp20.forgermod.mixin;
import net.bananashelp20.forgermod.item.custom.abilities.InfiniteBurnEvents;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class InfiniteBurnMixin {
    @Inject(method="setRemainingFireTicks",at=@At("HEAD"),require=1)
    private void forgermod$clearInfiniteBurn(int ticks,CallbackInfo callback) {
        if(ticks<=0)InfiniteBurnEvents.extinguished((Entity)(Object)this);
    }
}
