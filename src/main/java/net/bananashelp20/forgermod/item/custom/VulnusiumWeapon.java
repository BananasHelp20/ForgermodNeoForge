package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.DaggerCriticalEvents;
import net.bananashelp20.forgermod.item.custom.abilities.DeepWoundState;
import net.bananashelp20.forgermod.item.custom.abilities.LeechState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class VulnusiumWeapon extends SwordItemWithEffect {
    private static final DeepWoundState DEEP_WOUND = new DeepWoundState();
    private static final LeechState LEECH = new LeechState();
    public static Holder<MobEffect> effect = MobEffects.WITHER;
    public static int durationInTicks = 80;
    public static int effectAmplifier = 1;
    public String gemstone;
    public String type;

    public VulnusiumWeapon(String gemstone, String type) {
        super(ModToolTiers.VULNUSIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "vulnusium"),
                type, gemstone, effect, durationInTicks, effectAmplifier);
        this.gemstone = gemstone;
        this.type = type;
    }

    public void axeAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget, pStack);
    }

    public void claymoreAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget, pStack);

        if (pTarget.isDeadOrDying() && Augmentations.level(pStack, Augmentations.MATERIAL_HIT) > 0) pAttacker.heal(1);
    }

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        applyMaterialEffect(pTarget, pStack);
        onDaggerHit(pTarget, pAttacker);
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !(player.getMainHandItem().getItem() instanceof VulnusiumWeapon weapon)
                || !Augmentations.ids(player.getMainHandItem(), true).contains("tooltips.forgermod.ability.leech") || !LEECH.hasCharge(player.getUUID())) return;
        event.setAmount(event.getAmount() * 1.1F);
    }

    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0 || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !(player.getMainHandItem().getItem() instanceof VulnusiumWeapon weapon)
                || !Augmentations.ids(player.getMainHandItem(), true).contains("tooltips.forgermod.ability.leech") || !LEECH.hasCharge(player.getUUID())) return;
        // The bonus is one eleventh of the final 110% hit.
        float healing = LEECH.availableHealing(player.getUUID(), event.getNewDamage() / 11);
        float before = player.getHealth();
        player.heal(healing);
        LEECH.recordHealing(player.getUUID(), player.getHealth() - before);
        LEECH.consume(player.getUUID());
    }

    @Override
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer player
                && Augmentations.ids(player.getMainHandItem(), true).contains("tooltips.forgermod.ability.deep_wound")
                && DaggerCriticalEvents.consume(player, target, player.getMainHandItem())
                && DEEP_WOUND.consume(player.getUUID()) && !target.isDeadOrDying()) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        }
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!Augmentations.ids(stack, true).contains(abilityDescriptionKey(slot))) return false;
        if (!isDagger() || !player.isAlive()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
            boolean armed = LEECH.arm(player.getUUID());
            if (armed) player.displayClientMessage(Component.translatable("message.forgermod.leech.armed"), true);
            return armed;
        }
        boolean armed = DEEP_WOUND.arm(player.getUUID());
        if (armed) player.displayClientMessage(Component.translatable("message.forgermod.deep_wound.armed"), true);
        return armed;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 600 : 300;
    }

    @Override
    public boolean isAbilityActive(ServerPlayer player, WeaponAbilitySlot slot) {
        return slot == WeaponAbilitySlot.PRIMARY
                ? DEEP_WOUND.isArmed(player.getUUID()) : LEECH.hasCharge(player.getUUID());
    }

    @Override
    public void cancelAbility(ServerPlayer player, WeaponAbilitySlot slot) {
        if (slot == WeaponAbilitySlot.PRIMARY) DEEP_WOUND.clear(player.getUUID());
        else LEECH.clear(player.getUUID());
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.deep_wound"
                : "tooltips.forgermod.ability.leech";
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.isCriticalHit()
                || !DEEP_WOUND.isArmed(player.getUUID())
                || !(player.getMainHandItem().getItem() instanceof VulnusiumWeapon weapon)
                || !Augmentations.ids(player.getMainHandItem(), true).contains("tooltips.forgermod.ability.deep_wound")) return;
        event.setDamageMultiplier(event.getDamageMultiplier() * 2);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DEEP_WOUND.clear(event.getEntity().getUUID());
        LEECH.clear(event.getEntity().getUUID());
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
        if (!isDagger() && pAttacker instanceof ServerPlayer player && Augmentations.eligible(pStack)) {
            Augmentations.canonical(pStack).onDaggerHit(pTarget, player);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        String loreKey = switch (this.type) {
            case "axe" -> "tooltips.forgermod.woundmaker_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.assassin_dagger.tooltip";
            default -> "tooltips.forgermod.curseblood_claymore.tooltip";
        };
        appendWeaponTooltip(pStack, pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

}
