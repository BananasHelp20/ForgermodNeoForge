package net.bananashelp20.forgermod.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

public class OvergrownWeapon extends SwordItemWithEffect {
    public static Holder<MobEffect> effect;
    public static int durationInTicks;
    public static int effectAmplifier;

    public OvergrownWeapon(Tier pTier, Holder<MobEffect> effect, int durationInTicks, int amplifier, Properties pProperties) {
        super(pTier, pProperties);
        this.effect = effect;
        this.durationInTicks = durationInTicks;
        this.effectAmplifier = amplifier;
    }

    @Override
    public void postHurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        if (!pTarget.isDeadOrDying()) pTarget.addEffect(new MobEffectInstance(effect, durationInTicks, effectAmplifier)); //duration -> Ticks, AMPLIFIER
    }

}
