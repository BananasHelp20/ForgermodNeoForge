package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.FlamingComboState;
import net.bananashelp20.forgermod.item.custom.abilities.PyromaniacState;
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
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class IgnisiumWeapon extends SwordItemWithEffect {
    private static final FlamingComboState FLAMING_COMBO = new FlamingComboState();
    private static final PyromaniacState PYROMANIAC = new PyromaniacState();
    public static Holder<MobEffect> effect = MobEffects.GLOWING;
    public static int durationInTicks = 2000;
    public static int effectAmplifier = 1;
    public String gemstone;
    public String type;

    public IgnisiumWeapon(String gemstone, String type) {
        super(ModToolTiers.IGNISIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "ignisium"),
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
        onDaggerHit(pTarget, pAttacker);
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger()) return false;
        if (slot == WeaponAbilitySlot.PRIMARY) {
            if (FLAMING_COMBO.isActive(player.getUUID(), player.level().getGameTime())) return false;
            FLAMING_COMBO.activate(player.getUUID(), player.level().getGameTime());
            return true;
        }
        return slot == WeaponAbilitySlot.SECONDARY
                && PYROMANIAC.arm(player.getUUID(), player.getRemainingFireTicks() > 0);
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 600 : 1000;
    }

    @Override
    public boolean isAbilityActive(ServerPlayer player, WeaponAbilitySlot slot) {
        return slot == WeaponAbilitySlot.PRIMARY
                ? FLAMING_COMBO.isActive(player.getUUID(), player.level().getGameTime())
                : PYROMANIAC.isArmed(player.getUUID());
    }

    @Override
    public void cancelAbility(ServerPlayer player, WeaponAbilitySlot slot) {
        if (slot == WeaponAbilitySlot.PRIMARY) FLAMING_COMBO.clear(player.getUUID());
        else PYROMANIAC.clear(player.getUUID());
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.flaming_combo"
                : "tooltips.forgermod.ability.pyromaniac";
    }

    @Override
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player)) return;
        if (PYROMANIAC.consume(player.getUUID())) {
            player.heal(5.0F);
            if (!target.isDeadOrDying()) {
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 100));
            }
        }
        if (target.isDeadOrDying()) return;
        int before = target.getRemainingFireTicks();
        int after = FLAMING_COMBO.fireTicksAfterHit(player.getUUID(), player.level().getGameTime(), before);
        if (after != before) target.setRemainingFireTicks(after);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FLAMING_COMBO.clear(event.getEntity().getUUID());
        PYROMANIAC.clear(event.getEntity().getUUID());
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
            case "axe" -> "tooltips.forgermod.molten_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.emberfang_dagger.tooltip";
            default -> "tooltips.forgermod.infernal_claymore.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
