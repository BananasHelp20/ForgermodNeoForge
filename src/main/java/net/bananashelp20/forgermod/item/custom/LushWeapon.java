package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.AreaDischargeTargets;
import net.bananashelp20.forgermod.item.custom.abilities.RootingRootsState;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class LushWeapon extends SwordItemWithEffect {
    private static final RootingRootsState ROOTS = new RootingRootsState();
    public static Holder<MobEffect> effect = MobEffects.HUNGER;
    public static int durationInTicks = 200;
    public static int effectAmplifier = 3;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    private String gemstone;
    public String type;

    public LushWeapon(String gemstone, String type) {
        super(ModToolTiers.LUSH, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "lush"),
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
        List<Mob> targets = AreaDischargeTargets.select(
                player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(5)),
                mob -> mob instanceof Enemy && mob.isAlive(), player::distanceToSqr, 5);
        if (targets.isEmpty()) return false;
        long now = player.level().getGameTime();
        for (Mob mob : targets) {
            ROOTS.root(mob.getUUID(), mob.getX(), mob.getZ(), now);
            mob.getNavigation().stop();
            mob.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            player.serverLevel().sendParticles(ParticleTypes.COMPOSTER,
                    mob.getX(), mob.getY() + 0.2, mob.getZ(), 12, 0.4, 0.1, 0.4, 0.03);
        }
        return true;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY ? 1200 : 0;
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.rooting_roots" : null;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) return;
        RootingRootsState.Anchor anchor = ROOTS.anchor(mob.getUUID(), mob.level().getGameTime());
        if (anchor == null || !mob.isAlive()) return;
        mob.getNavigation().stop();
        mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
        mob.setPos(anchor.x(), mob.getY(), anchor.z());
        mob.hasImpulse = true;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level().getGameTime() % 20 == 0) {
            ROOTS.prune(player.level().getGameTime());
        }
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
            case "axe" -> "tooltips.forgermod.verdant_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.leafcutter_dagger.tooltip";
            default -> "tooltips.forgermod.overgrown_claymore.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
