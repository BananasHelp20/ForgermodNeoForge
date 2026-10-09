package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.LucidDreamingState;
import net.bananashelp20.forgermod.item.custom.abilities.NightmareHitState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class SomniumWeapon extends SwordItemWithEffect {
    private static final NightmareHitState NIGHTMARE = new NightmareHitState();
    private static final LucidDreamingState LUCID = new LucidDreamingState();
    private static final Map<UUID, MobEffectInstance> PREVIOUS_SPEED = new HashMap<>();
    public static Holder<MobEffect> effect = MobEffects.CONFUSION;
    public static int durationInTicks = 200;
    public static int effectAmplifier = 4;
    public String gemstone;
    public String type;

    public SomniumWeapon(String gemstone, String type) {
        super(ModToolTiers.SOMNIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "somnium"),
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
    }

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget, pStack);
        onDaggerHit(pTarget, pAttacker);
    }

    @Override
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player)) return;
        if (NIGHTMARE.consumeHit(player.getUUID()) && !target.isDeadOrDying()) {
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
        }
    }

    @SubscribeEvent
    public static void onEnemyKill(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(player.getMainHandItem().getItem() instanceof SomniumWeapon weapon) || !Augmentations.hasActive(player.getMainHandItem())
                || !(event.getEntity() instanceof Enemy || event.getEntity() instanceof Player other && player.canHarmPlayer(other))
                || !LUCID.consumeKill(player.getUUID(), player.level().getGameTime())) return;
        MobEffectInstance previous = player.getEffect(MobEffects.MOVEMENT_SPEED);
        if (previous != null) PREVIOUS_SPEED.put(player.getUUID(), new MobEffectInstance(previous));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
            if (LUCID.isActive(player.getUUID(), player.level().getGameTime())) return false;
            boolean armed = LUCID.arm(player.getUUID());
            if (armed) player.displayClientMessage(Component.translatable("message.forgermod.lucid.armed"), true);
            return armed;
        }
        boolean armed = NIGHTMARE.arm(player.getUUID());
        if (armed) player.displayClientMessage(Component.translatable("message.forgermod.nightmare.armed"), true);
        return armed;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 400 : 300;
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.absolute_nightmare"
                : "tooltips.forgermod.ability.lucid_dreaming";
    }

    @Override
    public boolean isAbilityActive(ServerPlayer player, WeaponAbilitySlot slot) {
        if (slot == WeaponAbilitySlot.PRIMARY) return NIGHTMARE.isArmed(player.getUUID());
        boolean active = LUCID.isActive(player.getUUID(), player.level().getGameTime());
        if (!active) PREVIOUS_SPEED.remove(player.getUUID());
        return active;
    }

    @Override
    public void cancelAbility(ServerPlayer player, WeaponAbilitySlot slot) {
        if (slot == WeaponAbilitySlot.PRIMARY) NIGHTMARE.clear(player.getUUID());
        else {
            if (LUCID.reducesFallDamage(player.getUUID(), player.level().getGameTime())) {
                MobEffectInstance current = player.getEffect(MobEffects.MOVEMENT_SPEED);
                MobEffectInstance previous = PREVIOUS_SPEED.remove(player.getUUID());
                if (current != null && current.getAmplifier() == 1 && current.getDuration() <= 600) {
                    player.removeEffect(MobEffects.MOVEMENT_SPEED);
                    if (previous != null) player.addEffect(previous);
                }
            }
            PREVIOUS_SPEED.remove(player.getUUID());
            LUCID.clear(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && LUCID.reducesFallDamage(player.getUUID(), player.level().getGameTime())) {
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.5F);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        NIGHTMARE.clear(event.getEntity().getUUID());
        LUCID.clear(event.getEntity().getUUID());
        PREVIOUS_SPEED.remove(event.getEntity().getUUID());
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
            case "axe" -> "tooltips.forgermod.dreamweaver_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.nightmare_dagger.tooltip";
            default -> "tooltips.forgermod.dreambound_claymore.tooltip";
        };
        appendWeaponTooltip(pStack, pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
