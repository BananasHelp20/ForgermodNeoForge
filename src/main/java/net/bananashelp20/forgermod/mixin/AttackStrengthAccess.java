package net.bananashelp20.forgermod.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets automatic melee abilities use vanilla attacks at full strength. */
@Mixin(LivingEntity.class)
public interface AttackStrengthAccess {
    @Accessor("attackStrengthTicker") int forgermod$getAttackStrengthTicker();
    @Accessor("attackStrengthTicker") void forgermod$setAttackStrengthTicker(int ticks);
}
