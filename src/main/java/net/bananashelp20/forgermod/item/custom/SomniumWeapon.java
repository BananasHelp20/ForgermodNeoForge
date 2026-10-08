package net.bananashelp20.forgermod.item.custom;

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

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class SomniumWeapon extends SwordItemWithEffect {
    private static final NightmareHitState NIGHTMARE = new NightmareHitState();
    private static final LucidDreamingState LUCID = new LucidDreamingState();
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
    }

    public void claymoreAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);
    }

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND);
        applyMaterialEffect(pTarget);
        onDaggerHit(pTarget, pAttacker);
    }

    @Override
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player)) return;
        if (NIGHTMARE.consumeHit(player.getUUID()) && !target.isDeadOrDying()) {
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
        }
        if ((target instanceof Enemy || target instanceof Player other && player.canHarmPlayer(other))
                && LUCID.consumeHit(player.getUUID(), player.level().getGameTime())) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
        }
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
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
            case "axe" -> "tooltips.forgermod.dreamweaver_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.nightmare_dagger.tooltip";
            default -> "tooltips.forgermod.dreambound_claymore.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
