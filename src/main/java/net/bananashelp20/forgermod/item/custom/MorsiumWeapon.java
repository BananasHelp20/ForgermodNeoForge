package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.StrengthenedBonesState;
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
    public static Holder<MobEffect> effect = MobEffects.WEAKNESS;
    public static int durationInTicks = 100;
    public static int effectAmplifier = 3;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    public String gemstone;
    public String type;

    public MorsiumWeapon(String gemstone, String type) {
        super(ModToolTiers.MORSIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "morsium"),
                type, gemstone, effect, durationInTicks, effectAmplifier);
        this.gemstone = gemstone;
        this.type = type;
    }

    public void axeAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
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
        if (!isDagger() || slot != WeaponAbilitySlot.PRIMARY || !player.isAlive()) return false;
        boolean armed = BONES.arm(player.getUUID(), player.level().getGameTime());
        if (armed) player.displayClientMessage(Component.translatable("message.forgermod.strengthened_bones.armed"), true);
        return armed;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY ? 1800 : 0;
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.strengthened_bones" : null;
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
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BONES.clear(event.getEntity().getUUID());
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
