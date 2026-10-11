package net.bananashelp20.forgermod.mixin;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LivingEntity.class)
public abstract class CompostMixin {
    @Shadow private void addEatEffect(FoodProperties food) {throw new AssertionError();}
    @Redirect(method="eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;",
        at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;addEatEffect(Lnet/minecraft/world/food/FoodProperties;)V"),require=1)
    private void forgermod$compost(LivingEntity entity,FoodProperties effects,Level level,ItemStack eaten,FoodProperties original) {
        if(entity instanceof ServerPlayer player && player.isAlive() && !player.isSpectator() && eaten.is(Items.ROTTEN_FLESH)
            && (Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.COMPOST) || Augmentations.hasPassive(player.getOffhandItem(),AdditionalAbilities.COMPOST)))
            effects=new FoodProperties(effects.nutrition(),effects.saturation(),effects.canAlwaysEat(),effects.eatSeconds(),effects.usingConvertsTo(),
                effects.effects().stream().filter(e->!e.effect().getEffect().equals(MobEffects.HUNGER)).toList());
        addEatEffect(effects);
    }
}
