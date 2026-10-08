package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.AreaDischargeTargets;
import net.bananashelp20.forgermod.item.custom.abilities.StormState;
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
    private static final StormState STORM = new StormState();
    public static Holder<MobEffect> effect = MobEffects.LEVITATION;
    public static int durationInTicks = 60;
    public static int effectAmplifier = 2;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    public String gemstone;
    public String type;

    public TaifuniteWeapon(String gemstone, String type) {
        super(ModToolTiers.TAIFUNITE, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "taifunite"),
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
        boolean active = STORM.activate(player.getUUID(), player.level().getGameTime());
        if (active) player.displayClientMessage(Component.translatable("message.forgermod.eye_of_the_storm.active"), true);
        return active;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY ? 1200 : 0;
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        return isDagger() && slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.eye_of_the_storm" : null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long now = player.level().getGameTime();
        if (!player.isAlive()) {
            STORM.clear(player.getUUID());
            return;
        }
        if (!STORM.isActive(player.getUUID(), now)) return;

        List<Mob> targets = AreaDischargeTargets.select(
                player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(10)),
                mob -> mob instanceof Enemy && mob.isAlive(), player::distanceToSqr, 10);
        for (Mob mob : targets) {
            Vec3 radial = mob.position().subtract(player.position());
            Vec3 flat = new Vec3(radial.x, 0, radial.z);
            if (flat.lengthSqr() < 0.01) flat = new Vec3(1, 0, 0);
            Vec3 outward = flat.normalize();
            Vec3 tangent = new Vec3(-outward.z, 0, outward.x);
            mob.setDeltaMovement(tangent.scale(0.35).subtract(outward.scale(0.08)).add(0, 0.16, 0));
            mob.hasImpulse = true;
            if (now % 10 == 0) mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 15, 0));
            if (now % 5 == 0) player.serverLevel().sendParticles(ParticleTypes.CLOUD,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    2, 0.2, 0.3, 0.2, 0.01);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        STORM.clear(event.getEntity().getUUID());
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
            case "axe" -> "tooltips.forgermod.skybreaker_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.dead_calm_dagger.tooltip";
            default -> "tooltips.forgermod.storming_claymore.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
