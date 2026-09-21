package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

import java.util.List;

public class LushWeapon extends SwordItemWithEffect {
    public static Holder<MobEffect> effect = MobEffects.HUNGER;
    public static int durationInTicks = 200;
    public static int effectAmplifier = 3;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    private String gemstone;

    public LushWeapon(String gemstone, String type) {
        super(ModToolTiers.LUSH, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "lush"));
        if (gemstone.equals("jade")) {
            effectAmplifier += 1;
            durationInTicks += 20;
        }
        this.gemstone = gemstone;
    }

    @Override
    public void postHurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        if (!pTarget.isDeadOrDying()) pTarget.addEffect(new MobEffectInstance(effect, durationInTicks, effectAmplifier)); //duration -> Ticks, AMPLIFIER
        System.out.println(pTarget.getActiveEffects());
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("tooltips.forgermod.overgrown_claymore.tooltip"));
        pTooltipComponents.add(Component.translatable("tooltips.forgermod."+ this.gemstone + ".tooltip_extra"));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
