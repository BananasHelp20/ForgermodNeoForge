package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.StrengthenedBonesState;
import net.bananashelp20.forgermod.item.custom.abilities.StoringAngerState;
import net.bananashelp20.forgermod.item.custom.abilities.DeathMarchDamage;
import net.bananashelp20.forgermod.item.custom.abilities.RevengeDamage;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class MorsiumWeapon extends SwordItemWithEffect {
    private static final StrengthenedBonesState BONES = new StrengthenedBonesState();
    private static final StoringAngerState ANGER = new StoringAngerState();
    public static Holder<MobEffect> effect = MobEffects.WEAKNESS;
    public static int durationInTicks = 100;
    public static int effectAmplifier = 3;
    public String gemstone;
    public String type;

    public MorsiumWeapon(String gemstone, String type) {
        super(ModToolTiers.MORSIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "morsium"),
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
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger() || !player.isAlive()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
            boolean armed = ANGER.arm(player.getUUID(), player.level().getGameTime());
            if (armed) player.displayClientMessage(Component.translatable("message.forgermod.storing_anger.active"), true);
            return armed;
        }
        boolean armed = BONES.arm(player.getUUID(), player.level().getGameTime());
        if (armed) player.displayClientMessage(Component.translatable("message.forgermod.strengthened_bones.armed"), true);
        return armed;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 1800 : 1200;
    }

    @Override
    public boolean isAbilityActive(ServerPlayer player, WeaponAbilitySlot slot) {
        return slot == WeaponAbilitySlot.PRIMARY
                ? BONES.isActive(player.getUUID(), player.level().getGameTime())
                : ANGER.isActive(player.getUUID());
    }

    @Override
    public void cancelAbility(ServerPlayer player, WeaponAbilitySlot slot) {
        if (slot == WeaponAbilitySlot.PRIMARY) BONES.clear(player.getUUID());
        else ANGER.clear(player.getUUID());
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.strengthened_bones"
                : "tooltips.forgermod.ability.storing_anger";
    }

    @SubscribeEvent
    public static void onHostileDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(player.getMainHandItem().getItem() instanceof MorsiumWeapon weapon)
                || !weapon.isDagger()) return;
        if (BONES.consumeKill(player.getUUID(), player.level().getGameTime())) {
            player.displayClientMessage(Component.translatable("message.forgermod.strengthened_bones.active"), true);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && BONES.isProtected(player.getUUID(), player.level().getGameTime())) {
            event.setCanceled(true);
        }
        if (event.isCanceled()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || attacker == event.getEntity()) return;
        boolean morsiumDaggerHit = event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && attacker.getMainHandItem().getItem() instanceof MorsiumWeapon weapon
                && weapon.isDagger();
        if (morsiumDaggerHit) {
            event.setAmount(event.getAmount() * DeathMarchDamage.multiplier(
                    event.getEntity().getHealth(), event.getEntity().getMaxHealth())
                    * RevengeDamage.multiplier(event.getEntity().getType().is(EntityTypeTags.UNDEAD)));
        }
        long now = attacker.level().getGameTime();
        if (ANGER.storeIfCharging(attacker.getUUID(), now, event.getAmount())) {
            event.setCanceled(true);
            return;
        }
        if (!morsiumDaggerHit) return;
        float stored = ANGER.releaseOnDaggerHit(attacker.getUUID(), now);
        if (stored >= 0) event.setAmount(event.getAmount() + stored);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BONES.clear(event.getEntity().getUUID());
        ANGER.clear(event.getEntity().getUUID());
    }

    @Override
    public List<String> passiveDescriptionKeys() {
        return isDagger() ? List.of("tooltips.forgermod.passive.death_march",
                "tooltips.forgermod.passive.revenge") : List.of();
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
            case "axe" -> "tooltips.forgermod.ghost_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.deathwisper_dagger.tooltip";
            default -> "tooltips.forgermod.hollow_claymore.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
