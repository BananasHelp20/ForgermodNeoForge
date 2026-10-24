package net.bananashelp20.forgermod.mixin;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
/** Use vanilla's normal player-credit XP eligibility without replacing the real damage actor. */
@Mixin(LivingEntity.class)
public interface ReinforcementExperienceAccess {
    @Accessor("lastHurtByPlayerTime") void forgermod$setExperienceCreditTicks(int ticks);
}
