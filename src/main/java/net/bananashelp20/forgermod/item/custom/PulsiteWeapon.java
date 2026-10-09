package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.DaggerCriticalEvents;
import net.bananashelp20.forgermod.item.custom.abilities.SonicCritState;
import net.bananashelp20.forgermod.item.custom.abilities.SonicBoomBlast;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class PulsiteWeapon extends SwordItemWithEffect {
    private static final SonicCritState SONIC_CRIT = new SonicCritState();
    public static Holder<MobEffect> effect = MobEffects.DARKNESS;
    public static int durationInTicks = 140;
    public static int effectAmplifier = 5;
    public String gemstone;
    public String type;

    public PulsiteWeapon(String gemstone, String type) {
        super(ModToolTiers.PULSITE, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "pulsite"),
                type, gemstone, effect, durationInTicks, effectAmplifier);
        this.gemstone = gemstone;
        this.type = type;
    }

    public void axeAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);
    }

    public void claymoreAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);
    }

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        applyMaterialEffect(pTarget);
        if (pAttacker instanceof ServerPlayer player
                && DaggerCriticalEvents.consume(player, pTarget, pStack)
                && SONIC_CRIT.consume(player.getUUID())) {
            player.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM,
                    pTarget.getX(), pTarget.getY() + pTarget.getBbHeight() * 0.5, pTarget.getZ(),
                    1, 0, 0, 0, 0);
        }
        onDaggerHit(pTarget, pAttacker);
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger() || !player.isAlive()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
            SonicBoomBlast.fire(player);
            return true;
        }
        boolean armed = SONIC_CRIT.arm(player.getUUID());
        if (armed) player.displayClientMessage(Component.translatable("message.forgermod.sonic_crit.armed"), true);
        return armed;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 600 : 900;
    }

    @Override
    public boolean isAbilityActive(ServerPlayer player, WeaponAbilitySlot slot) {
        return slot == WeaponAbilitySlot.PRIMARY && SONIC_CRIT.isArmed(player.getUUID());
    }

    @Override
    public void cancelAbility(ServerPlayer player, WeaponAbilitySlot slot) {
        if (slot == WeaponAbilitySlot.PRIMARY) SONIC_CRIT.clear(player.getUUID());
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.sonic_crit"
                : "tooltips.forgermod.ability.sonic_boom";
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !(player.getMainHandItem().getItem() instanceof PulsiteWeapon weapon)
                || !weapon.isDagger() || !SONIC_CRIT.isArmed(player.getUUID())
                || !DaggerCriticalEvents.matches(player, event.getEntity(), player.getMainHandItem())) return;
        event.setAmount(event.getAmount() + SonicCritState.bonusDamage(event.getEntity().getMaxHealth()));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SONIC_CRIT.clear(event.getEntity().getUUID());
    }

    @Override
    public void postHurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        switch (this.type) {
            case "claymore":
                claymoreAttack(pStack, pTarget, pAttacker);
                break;
            case "axe":
                axeAttack(pStack, pTarget, pAttacker);
                break;
            case "dagger":
                daggerAttack(pStack, pTarget, pAttacker);
                break;
            default: claymoreAttack(pStack, pTarget, pAttacker);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        String loreKey = switch (this.type) {
            case "axe" -> "tooltips.forgermod.echoing_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.wardens_needle.tooltip";
            default -> "tooltips.forgermod.shrieking_claymore.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
