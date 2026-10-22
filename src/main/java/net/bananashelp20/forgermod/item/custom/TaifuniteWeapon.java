package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class TaifuniteWeapon extends SwordItemWithEffect {
    private static final double DASH_SPEED = 1.2; // Blocks per tick; travel distance follows normal physics.
    public static Holder<MobEffect> effect = MobEffects.LEVITATION;
    public static int durationInTicks = 60;
    public static int effectAmplifier = 2;
    public String gemstone;
    public String type;

    public TaifuniteWeapon(String gemstone, String type) {
        super(ModToolTiers.TAIFUNITE, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "taifunite"),
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
    }

    @Override
    public float getAttackDamageBonus(net.minecraft.world.entity.Entity target,float damage,net.minecraft.world.damagesource.DamageSource source) {
        return super.getAttackDamageBonus(target,damage,source)
            + net.bananashelp20.forgermod.item.custom.abilities.WindchargeEvents.attackBonus(target,source);
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!Augmentations.ids(stack, true).contains(abilityDescriptionKey(slot))) return false;
        if (!isDagger() || !player.isAlive()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
            if (player.isPassenger()) return false;
            player.setDeltaMovement(player.getLookAngle().normalize().scale(DASH_SPEED));
            player.hasImpulse = true;
            player.hurtMarked = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
            player.serverLevel().sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.5, player.getZ(), 12, 0.2, 0.2, 0.2, 0.06);
            return true;
        }
        boolean active = net.bananashelp20.forgermod.item.custom.abilities.EyeOfStormEvents.activate(player,stack);
        if (active) player.displayClientMessage(Component.translatable("message.forgermod.eye_of_the_storm.active"), true);
        return active;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 1200 : 100;
    }

    @Override
    public boolean isAbilityActive(ServerPlayer player, WeaponAbilitySlot slot) {
        return slot == WeaponAbilitySlot.PRIMARY
                && net.bananashelp20.forgermod.item.custom.abilities.EyeOfStormEvents.active(player);
    }

    @Override
    public void cancelAbility(ServerPlayer player, WeaponAbilitySlot slot) {
        if (slot == WeaponAbilitySlot.PRIMARY) net.bananashelp20.forgermod.item.custom.abilities.EyeOfStormEvents.cancel(player.getUUID());
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.eye_of_the_storm"
                : "tooltips.forgermod.ability.windy_dash";
    }

    @Override
    public String passiveDescriptionKey() {
        return isDagger() ? "tooltips.forgermod.passive.double_jump" : null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        net.bananashelp20.forgermod.item.custom.abilities.EyeOfStormEvents.tick(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        net.bananashelp20.forgermod.item.custom.abilities.EyeOfStormEvents.cancel(event.getEntity().getUUID());
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
            case "axe" -> "tooltips.forgermod.skybreaker_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.dead_calm_dagger.tooltip";
            default -> "tooltips.forgermod.storming_claymore.tooltip";
        };
        appendWeaponTooltip(pStack, pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
