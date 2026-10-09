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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
    public String gemstone;
    public String type;

    public InanisiumWeapon(String gemstone, String type) {
        super(ModToolTiers.INANISIUM, ModSpecialRegistry.getCorrectAttributes(gemstone, type, new Properties().rarity(Rarity.EPIC), "inanisium"),
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
        Vec3 direction = player.getLookAngle().normalize();
        boolean paired = player.getOffhandItem().getItem() instanceof InanisiumWeapon offhand && offhand.isDagger();
        int maxSteps = paired ? VoidStepPath.PAIRED_STEPS : VoidStepPath.STEPS;
        ServerLevel level = player.serverLevel();
        Vec3 start = player.position();
        AABB originalBox = player.getBoundingBox();
        int clearSteps = VoidStepPath.furthestClearStep(maxSteps, step -> {
            Vec3 offset = direction.scale(step * VoidStepPath.STEP_DISTANCE);
            AABB box = originalBox.move(offset);
            return box.minY >= level.getMinBuildHeight() && box.maxY <= level.getMaxBuildHeight()
                    && level.getWorldBorder().isWithinBounds(box)
                    && loadedBox(level, box) && level.noCollision(player, box);
        });
        if (clearSteps < 2) return false;

        Vec3 destination = start.add(direction.scale(clearSteps * VoidStepPath.STEP_DISTANCE));
        return teleportWithSound(player, destination, player.getYRot(), player.getXRot());
    }

    private static boolean loadedBox(ServerLevel level, AABB box) {
        for (int x = Mth.floor(box.minX) >> 4; x <= (Mth.floor(Math.nextDown(box.maxX)) >> 4); x++) {
            for (int z = Mth.floor(box.minZ) >> 4; z <= (Mth.floor(Math.nextDown(box.maxZ)) >> 4); z++) {
                if (!level.getChunkSource().hasChunk(x, z)) return false;
            }
        }
        return true;
    }

    private static boolean suddenPresence(ServerPlayer player) {
        List<Mob> targets = player.serverLevel().getEntitiesOfClass(Mob.class,
                        player.getBoundingBox().inflate(20),
                        mob -> mob instanceof Enemy && mob.isAlive()
                                && player.distanceToSqr(mob) <= 400.0);
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        for (Mob target : targets) {
            Vec3 look = target.getLookAngle();
            for (double distance : new double[] {1.5, 2.0, 2.5, 3.0}) {
                BehindTargetOffset behind = BehindTargetOffset.fromLook(look.x, look.z, distance);
                if (behind == null) break;
                for (double sideways : new double[] {0, 0.75, -0.75, 1.5, -1.5}) {
                    for (double height : new double[] {0, 0.5, -0.5, 1.0, -1.0, 1.5, 2.0}) {
                        Vec3 destination = target.position().add(
                                behind.x() - behind.z() / distance * sideways,
                                height,
                                behind.z() + behind.x() / distance * sideways);
                        if (safeLanding(player, destination)
                                && teleportWithSound(player, destination, target.getYRot(), 0)) return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean safeLanding(ServerPlayer player, Vec3 destination) {
        BlockPos feet = BlockPos.containing(destination);
        AABB box = player.getBoundingBox().move(destination.subtract(player.position()));
        if (!player.serverLevel().getWorldBorder().isWithinBounds(box)
                || !player.serverLevel().getChunkSource().hasChunk(feet.getX() >> 4, feet.getZ() >> 4)
                || !player.serverLevel().noCollision(player, box)
                || player.serverLevel().noCollision(player, box.move(0, -0.1, 0))) return false;
        return player.serverLevel().getFluidState(feet).isEmpty()
                && player.serverLevel().getFluidState(BlockPos.containing(
                        destination.add(0, player.getBbHeight() - 0.1, 0))).isEmpty();
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
        return slot == WeaponAbilitySlot.PRIMARY ? 140 : 2400;
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
