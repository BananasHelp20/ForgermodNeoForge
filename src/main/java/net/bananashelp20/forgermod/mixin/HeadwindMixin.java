package net.bananashelp20.forgermod.mixin;

import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayer.class)
public abstract class HeadwindMixin {
    // 1.21.1: swim, underwater walk, water-surface walk, then ground sprint.
    // Alter only the sprint branch; combat and sprint-jump exhaustion stay vanilla.
    @ModifyArg(method="checkMovementStatistics",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V",ordinal=3),index=0,require=1)
    private float forgermod$headwindSprint(float exhaustion) {
        var player=(ServerPlayer)(Object)this;
        return player.isAlive() && !player.isSpectator() && Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.HEADWIND)?exhaustion*.5F:exhaustion;
    }
}
