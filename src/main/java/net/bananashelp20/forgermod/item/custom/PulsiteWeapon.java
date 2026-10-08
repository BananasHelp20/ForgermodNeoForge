package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.DaggerCriticalEvents;
import net.bananashelp20.forgermod.item.custom.abilities.SonicCritState;
import net.bananashelp20.forgermod.item.custom.abilities.SonicBoomState;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class PulsiteWeapon extends SwordItemWithEffect {
    private static final SonicCritState SONIC_CRIT = new SonicCritState();
    private static final SonicBoomState SONIC_BOOM = new SonicBoomState();
    private static final ResourceLocation SONIC_REACH_ID =
            ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "sonic_boom_reach");
    public static Holder<MobEffect> effect = MobEffects.DARKNESS;
    public static int durationInTicks = 140;
    public static int effectAmplifier = 5;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    public String gemstone;
    public String type;

    public PulsiteWeapon(String gemstone, String type) {
        super(ModToolTiers.PULSITE, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "pulsite"),
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
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer player && SONIC_BOOM.consumeAttack(player.getUUID())) {
            syncSonicReach(player);
            player.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    1, 0, 0, 0, 0);
        }
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger() || !player.isAlive()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
            boolean armed = SONIC_BOOM.arm(player.getUUID());
            if (armed) {
                syncSonicReach(player);
                player.displayClientMessage(Component.translatable("message.forgermod.sonic_boom.armed"), true);
            }
            return armed;
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
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.sonic_crit"
                : "tooltips.forgermod.ability.sonic_boom";
    }

    public static boolean hasSonicReach(ServerPlayer player) {
        return SONIC_BOOM.isArmed(player.getUUID())
                && player.getMainHandItem().getItem() instanceof PulsiteWeapon weapon && weapon.isDagger();
    }

    public static void consumeSonicAirSwing(ServerPlayer player) {
        if (hasSonicReach(player) && SONIC_BOOM.consumeAttack(player.getUUID())) syncSonicReach(player);
    }

    private static void syncSonicReach(ServerPlayer player) {
        AttributeInstance reach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (reach == null) return;
        boolean shouldHaveReach = hasSonicReach(player);
        if (shouldHaveReach && reach.getModifier(SONIC_REACH_ID) == null) {
            reach.addTransientModifier(new AttributeModifier(
                    SONIC_REACH_ID, 4, AttributeModifier.Operation.ADD_VALUE));
        } else if (!shouldHaveReach && reach.getModifier(SONIC_REACH_ID) != null) {
            reach.removeModifier(SONIC_REACH_ID);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.isAlive()) SONIC_BOOM.clear(player.getUUID());
        syncSonicReach(player);
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
        SONIC_BOOM.clear(event.getEntity().getUUID());
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
