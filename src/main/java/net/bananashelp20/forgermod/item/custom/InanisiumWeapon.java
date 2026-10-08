package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.abilities.BehindTargetOffset;
import net.bananashelp20.forgermod.item.custom.abilities.VoidStepPath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class InanisiumWeapon extends SwordItemWithEffect {
    public static Holder<MobEffect> effect = MobEffects.BLINDNESS;
    public static int durationInTicks = 80;
    public static int effectAmplifier = 1;
    public static Properties pProperties = new Properties().rarity(Rarity.EPIC);
    public String gemstone;
    public String type;

    public InanisiumWeapon(String gemstone, String type) {
        super(ModToolTiers.INANISIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, pProperties, "inanisium"),
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
        if (!isDagger() || !player.isAlive() || player.isPassenger()) return false;
        return switch (slot) {
            case PRIMARY -> stepThroughVoid(player);
            case SECONDARY -> suddenPresence(player);
        };
    }

    private static boolean stepThroughVoid(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        double horizontalLength = Math.hypot(look.x, look.z);
        if (horizontalLength < 0.001) return false;
        Vec3 direction = new Vec3(look.x / horizontalLength, 0, look.z / horizontalLength);
        Vec3 start = player.position();
        AABB originalBox = player.getBoundingBox();
        int clearSteps = VoidStepPath.furthestClearStep(step -> {
            Vec3 offset = direction.scale(step * VoidStepPath.STEP_DISTANCE);
            AABB box = originalBox.move(offset);
            return player.serverLevel().getWorldBorder().isWithinBounds(box)
                    && player.serverLevel().noCollision(player, box);
        });
        if (clearSteps < 2) return false;

        Vec3 destination = start.add(direction.scale(clearSteps * VoidStepPath.STEP_DISTANCE));
        return teleportWithSound(player, destination, player.getYRot(), player.getXRot());
    }

    private static boolean suddenPresence(ServerPlayer player) {
        Mob target = player.serverLevel().getEntitiesOfClass(Mob.class,
                        player.getBoundingBox().inflate(20),
                        mob -> mob instanceof Enemy && mob.isAlive()
                                && player.distanceToSqr(mob) <= 400.0)
                .stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
        if (target == null) return false;

        Vec3 look = target.getLookAngle();
        for (double distance : new double[] {1.5, 2.0, 2.5}) {
            BehindTargetOffset behind = BehindTargetOffset.fromLook(look.x, look.z, distance);
            if (behind == null) return false;
            Vec3 destination = target.position().add(behind.x(), 0, behind.z());
            AABB box = player.getBoundingBox().move(destination.subtract(player.position()));
            if (!player.serverLevel().getWorldBorder().isWithinBounds(box)
                    || !player.serverLevel().hasChunkAt(BlockPos.containing(destination))
                    || !player.serverLevel().noCollision(player, box)) continue;
            if (!player.serverLevel().getFluidState(BlockPos.containing(destination)).isEmpty()
                    || !player.serverLevel().getFluidState(BlockPos.containing(
                            destination.add(0, player.getBbHeight() - 0.1, 0))).isEmpty()) continue;
            return teleportWithSound(player, destination, target.getYRot(), 0);
        }
        return false;
    }

    private static boolean teleportWithSound(ServerPlayer player, Vec3 destination, float yaw, float pitch) {
        BlockPos departure = player.blockPosition();
        if (!player.teleportTo(player.serverLevel(), destination.x, destination.y, destination.z,
                Set.of(), yaw, pitch)) return false;
        player.fallDistance = 0;
        player.serverLevel().playSound(null, departure, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.7F, 1.2F);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.7F, 1.2F);
        return true;
    }

    @Override
    public int abilityCooldownTicks(WeaponAbilitySlot slot) {
        if (!isDagger()) return 0;
        return slot == WeaponAbilitySlot.PRIMARY ? 100 : 2400;
    }

    @Override
    public String abilityDescriptionKey(WeaponAbilitySlot slot) {
        if (!isDagger()) return null;
        return slot == WeaponAbilitySlot.PRIMARY
                ? "tooltips.forgermod.ability.void_step"
                : "tooltips.forgermod.ability.sudden_presence";
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
            case "axe" -> "tooltips.forgermod.nullified_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.riftfang_dagger.tooltip";
            default -> "tooltips.forgermod.claymore_of_the_void.tooltip";
        };
        appendWeaponTooltip(pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
