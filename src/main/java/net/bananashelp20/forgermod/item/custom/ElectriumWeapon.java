package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.AreaDischargeTargets;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public class ElectriumWeapon extends SwordItemWithEffect {
    private static final ChargeAttackState CHARGE_ATTACK = new ChargeAttackState();
    public static Holder<MobEffect> effect = MobEffects.MOVEMENT_SLOWDOWN;
    public static int durationInTicks = 100;
    public static int effectAmplifier = 3;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    public String gemstone;
    public String type;

    public ElectriumWeapon(String gemstone, String type) {
        super(ModToolTiers.ELECTRIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "electrium"),
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

    public void daggerAttack(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker, EquipmentSlot pSlot) {
        applyMaterialEffect(pTarget);
        onDaggerHit(pTarget, pAttacker);
        pStack.hurtAndBreak(1, pAttacker, pSlot);
    }

    @Override
    public void onDaggerHit(LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player)) return;
        chainLightning(target, player);
        if (!(target instanceof Mob) || !CHARGE_ATTACK.recordMobHit(player.getUUID()) || target.isDeadOrDying()) return;

        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        if (target.hurt(player.damageSources().source(DamageTypes.LIGHTNING_BOLT, player), 20.0F)) {
            player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
        } else {
            target.invulnerableTime = previousInvulnerableTime;
        }
    }

    private void chainLightning(LivingEntity original, ServerPlayer player) {
        List<Mob> chained = ChainLightningTargets.select(
                player.serverLevel().getEntitiesOfClass(Mob.class, original.getBoundingBox().inflate(10)),
                mob -> mob != original && mob instanceof Enemy && mob.isAlive(), original::distanceToSqr);
        for (Mob mob : chained) {
            int previousInvulnerableTime = mob.invulnerableTime;
            mob.invulnerableTime = 0;
            if (mob.hurt(player.damageSources().source(DamageTypes.LIGHTNING_BOLT, player), 5.0F)) {
                for (int step = 1; step <= 6; step++) {
                    double fraction = step / 7.0;
                    player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            original.getX() + (mob.getX() - original.getX()) * fraction,
                            original.getY() + original.getBbHeight() * 0.5
                                    + (mob.getY() + mob.getBbHeight() * 0.5
                                    - original.getY() - original.getBbHeight() * 0.5) * fraction,
                            original.getZ() + (mob.getZ() - original.getZ()) * fraction,
                            1, 0, 0, 0, 0);
                }
            } else {
                mob.invulnerableTime = previousInvulnerableTime;
            }
        }
    }

    @Override
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!isDagger() || !player.isAlive()) return false;
        if (slot == WeaponAbilitySlot.SECONDARY) {
            boolean armed = CHARGE_ATTACK.arm(player.getUUID());
            if (armed) player.displayClientMessage(Component.translatable("message.forgermod.charge_attack.armed"), true);
            return armed;
        }
        List<Mob> targets = AreaDischargeTargets.select(
                player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(5)),
                mob -> mob instanceof Enemy && mob.isAlive(), player::distanceToSqr, 5);
        if (targets.isEmpty()) return false;

        int struck = 0;
        for (Mob target : targets) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.serverLevel());
            if (bolt == null) continue;
            bolt.moveTo(target.position());
            bolt.setVisualOnly(true);
            bolt.setCause(player);
            if (player.serverLevel().addFreshEntity(bolt)) {
                target.hurt(player.damageSources().source(DamageTypes.LIGHTNING_BOLT, player, bolt), 5.0F);
                struck++;
            }
        }
        return struck > 0;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 6000 : 2400;
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.area_discharge"
                : "tooltips.forgermod.ability.charge_attack";
    }

    @Override
    public String passiveDescriptionKey() {
        return isDagger() ? "tooltips.forgermod.passive.chain_lightning" : null;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        CHARGE_ATTACK.clear(event.getEntity().getUUID());
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
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        String loreKey = switch (this.type) {
            case "axe" -> "tooltips.forgermod.voltage_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.static_dagger.tooltip";
            default -> "tooltips.forgermod.claymore_of_thunder.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
