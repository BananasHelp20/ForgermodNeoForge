package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.abilities.PassiveActors;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.ChargeAttackState;
import net.bananashelp20.forgermod.item.custom.abilities.ChainLightningTargets;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class ElectriumWeapon extends SwordItemWithEffect {
    private static final ChargeAttackState CHARGE_ATTACK = new ChargeAttackState();
    public static Holder<MobEffect> effect = MobEffects.MOVEMENT_SLOWDOWN;
    public static int durationInTicks = 100;
    public static int effectAmplifier = 3;
    public String gemstone;
    public String type;

    public ElectriumWeapon(String gemstone, String type) {
        super(ModToolTiers.ELECTRIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "electrium"),
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

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker, EquipmentSlot pSlot) {
        applyMaterialEffect(pTarget, pStack);
        onDaggerHit(pTarget, pAttacker);
        pStack.hurtAndBreak(1, pAttacker, pSlot);
    }

    @Override
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
        if(attacker instanceof Mob && PassiveActors.supported(attacker)){
            if(PassiveActors.has(attacker,"tooltips.forgermod.passive.chain_lightning"))passiveChainLightning(target,attacker);
            return;
        }
        if (!(attacker instanceof ServerPlayer player)) return;
        if (Augmentations.hasPassive(player.getMainHandItem(), "tooltips.forgermod.passive.chain_lightning")) chainLightning(target, player);
        if (!(target instanceof Mob)) return;
        dischargeChargeIfReady(target, player);
    }

    private static void dischargeChargeIfReady(LivingEntity target, ServerPlayer player) {
        int rank=CHARGE_ATTACK.rank(player.getUUID());
        if (!CHARGE_ATTACK.recordMobHit(player.getUUID())) return;
        if(rank>=4) net.bananashelp20.forgermod.item.custom.abilities.LightningRodEvents.chargeStrike(player);
        if(target.isDeadOrDying()) return;

        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        if (target.hurt(player.damageSources().source(DamageTypes.LIGHTNING_BOLT, player), ChargeAttackState.lightningDamage(rank))) {
            sendChargeExplosionParticles(target, player);
            player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
        } else {
            target.invulnerableTime = previousInvulnerableTime;
        }
    }
    public static float chargeMultiplier(ServerPlayer player){return CHARGE_ATTACK.damageMultiplier(player.getUUID());}
    public static void recordLightningHit(Mob target,ServerPlayer player){dischargeChargeIfReady(target,player);}

    private void chainLightning(LivingEntity original, ServerPlayer player) {passiveChainLightning(original,player);}
    public void passiveChainLightning(LivingEntity original, LivingEntity player) {
        Set<UUID> visited = new HashSet<>();
        visited.add(original.getUUID());
        LivingEntity previous = original;
        for (int chain = 0; chain < 10; chain++) {
            if (!ChainLightningTargets.shouldContinue(player.getRandom())) break;
            LivingEntity mob = ChainLightningTargets.next(
                    PassiveActors.level(player).getEntitiesOfClass(LivingEntity.class, previous.getBoundingBox().inflate(10)),
                    candidate -> candidate != player && PassiveActors.canAffect(player,candidate) && candidate.isAlive() && !candidate.isRemoved() && !candidate.isSpectator(),
                    previous::distanceToSqr, visited, LivingEntity::getUUID);
            if (mob == null) break;
            visited.add(mob.getUUID());
            int previousInvulnerableTime = mob.invulnerableTime;
            mob.invulnerableTime = 0;
            if (mob.hurt(player.damageSources().source(DamageTypes.LIGHTNING_BOLT, player),
                    (player instanceof Mob mobActor && mobActor instanceof net.bananashelp20.forgermod.item.custom.abilities.BoneGuardEvents.FriendlyGuard
                            ?net.bananashelp20.forgermod.item.custom.abilities.NecromancerPassiveCombat.weaponDamage(mobActor,net.bananashelp20.forgermod.item.custom.abilities.NecromancerPassiveCombat.striking(mobActor))
                            :(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)) * 0.5F
                            * CHARGE_ATTACK.damageMultiplier(player.getUUID()))) {
                if(player instanceof ServerPlayer serverPlayer)dischargeChargeIfReady(mob,serverPlayer);
                net.bananashelp20.forgermod.particle.LightningArcParticles.emit(PassiveActors.level(player),
                        previous.getBoundingBox().getCenter(),mob.getBoundingBox().getCenter(),-1,5);
            } else {
                mob.invulnerableTime = previousInvulnerableTime;
            }
            previous = mob;
        }
    }

    private static void sendChargeExplosionParticles(LivingEntity target, ServerPlayer player) {
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.5;
        double z = target.getZ();
        var level = player.serverLevel();
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.FLASH, x, y, z, 2, 0.15, 0.25, 0.15, 0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                120, 0.9, 0.9, 0.9, 0.25);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z,
                40, 0.65, 0.65, 0.65, 0.18);
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!Augmentations.ids(stack, true).contains(abilityDescriptionKey(slot))) return false;
        if (!isDagger() || !player.isAlive()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
            int rank=Augmentations.level(stack,"tooltips.forgermod.ability.charge_attack");
            boolean armed = CHARGE_ATTACK.arm(player.getUUID(),rank);
            if (armed) player.displayClientMessage(Component.translatable("message.forgermod.charge_attack.armed",ChargeAttackState.chargingHits(rank)+1), true);
            return armed;
        }
        return net.bananashelp20.forgermod.item.custom.abilities.AreaDischargeEvents.activate(player,stack);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(player.getMainHandItem().getItem() instanceof ElectriumWeapon weapon) || !Augmentations.hasActive(player.getMainHandItem())) return;
        if (event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
            event.setAmount(event.getAmount() * CHARGE_ATTACK.damageMultiplier(player.getUUID()));
        }
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 600 : 2400;
    }

    @Override
    public boolean isAbilityActive(ServerPlayer player, WeaponAbilitySlot slot) {
        return slot == WeaponAbilitySlot.PRIMARY?net.bananashelp20.forgermod.item.custom.abilities.AreaDischargeEvents.active(player):CHARGE_ATTACK.isArmed(player.getUUID());
    }

    @Override
    public void cancelAbility(ServerPlayer player, WeaponAbilitySlot slot) {
        if (slot == WeaponAbilitySlot.SECONDARY) CHARGE_ATTACK.clear(player.getUUID());
        else net.bananashelp20.forgermod.item.custom.abilities.AreaDischargeEvents.cancel(player.getUUID());
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? Augmentations.AREA_DISCHARGE
                : "tooltips.forgermod.ability.charge_attack";
    }

    @Override
    public String passiveDescriptionKey() {
        return isDagger() ? "tooltips.forgermod.passive.chain_lightning" : null;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        CHARGE_ATTACK.clear(event.getEntity().getUUID());
        net.bananashelp20.forgermod.item.custom.abilities.AreaDischargeEvents.cancel(event.getEntity().getUUID());
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
                daggerAttack(pStack, pTarget, pAttacker, EquipmentSlot.MAINHAND);
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
            case "axe" -> "tooltips.forgermod.voltage_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.static_dagger.tooltip";
            default -> "tooltips.forgermod.claymore_of_thunder.tooltip";
        };
        appendWeaponTooltip(pStack, pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
