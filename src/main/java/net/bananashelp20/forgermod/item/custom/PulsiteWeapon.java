package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.item.ModToolTiers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class PulsiteWeapon extends SwordItemWithEffect {
    public static Holder<MobEffect> effect = MobEffects.DARKNESS;
    public static int durationInTicks = 140;
    public static int effectAmplifier = 5;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    public String gemstone;

    public PulsiteWeapon(String gemstone) {
        super(ModToolTiers.LUSH, ((gemstone.equals("amber") ? pProperties.fireResistant() : pProperties).attributes(SwordItem.createAttributes(ModToolTiers.LUSH, (gemstone.equals("ruby")) ? 5 : 4, (gemstone.equals("amethyst")) ? 2.4f : 2.8f))));
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
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("tooltips.forgermod.overgrown_claymore.tooltip"));
        pTooltipComponents.add(Component.translatable("tooltips.forgermod."+ this.gemstone + ".tooltip_extra"));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
