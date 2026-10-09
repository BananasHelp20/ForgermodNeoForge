package net.bananashelp20.forgermod.item.custom;

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
        applyMaterialEffect(pTarget);
    }

    public void claymoreAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);

        if (pTarget.isDeadOrDying()) pAttacker.heal(1); //heals 1hp?
    }

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        applyMaterialEffect(pTarget);
        if (pAttacker instanceof ServerPlayer player
                && DaggerCriticalEvents.consume(player, pTarget, pStack)
                && DEEP_WOUND.consume(player.getUUID()) && !pTarget.isDeadOrDying()) {
            pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        }
        onDaggerHit(pTarget, pAttacker);
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player) || target.isDeadOrDying()
                || !LEECH.hasCharge(player.getUUID())) return;
        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        float healthBefore = target.getHealth();
        if (target.hurt(player.damageSources().playerAttack(player), target.getMaxHealth() * 0.1F)) {
            float drained = Math.max(0, healthBefore - target.getHealth());
            if (drained > 0) {
                player.heal(drained);
                LEECH.consume(player.getUUID());
            }
        } else {
            target.invulnerableTime = previousInvulnerableTime;
        }
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
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
                || !weapon.isDagger()) return;
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
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        String loreKey = switch (this.type) {
            case "axe" -> "tooltips.forgermod.woundmaker_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.assassin_dagger.tooltip";
            default -> "tooltips.forgermod.curseblood_claymore.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

}
