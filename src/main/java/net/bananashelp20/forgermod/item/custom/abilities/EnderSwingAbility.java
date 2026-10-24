package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.augmentation.ReviewedRanks;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/** Gravity Swing retains its original saved augment ID and lifts through a clear vertical column. */
public final class EnderSwingAbility {
    private EnderSwingAbility() {}

    public static boolean activate(ServerPlayer player, ItemStack stack) {
        if (!player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player)
                || stack != player.getMainHandItem()
                || !(stack.getItem() instanceof InanisiumWeapon weapon) || !weapon.isAxe()
                || !player.getOffhandItem().isEmpty()
                || !Augmentations.ids(stack, true).contains(RecommendedAbilities.ENDER_SWING)) return false;
        strike(player, stack);
        return true;
    }

    private static void strike(ServerPlayer player, ItemStack stack) {
        int rank=Augmentations.level(stack,RecommendedAbilities.ENDER_SWING);
        double radius=ReviewedRanks.gravitySwingRadius(rank);
        int height=ReviewedRanks.gravitySwingHeight(rank);
        float damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (rank > 1) damage *= 1.2f;
        player.swing(InteractionHand.MAIN_HAND);
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                living -> living!=player && living.isAlive() && !living.isRemoved() && (living instanceof net.minecraft.world.entity.Mob || WeaponAbilityTargets.enemy(player,living)) && player.distanceToSqr(living) <= radius * radius)) {
            int previousImmunity = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                stack.getItem().postHurtEnemy(stack, target, player);
                if (target.isAlive()) {double available=availableLift(player,target,height);if(available>.001)lift(player,target,available,rank);}
            } else {
                target.invulnerableTime = previousImmunity;
            }
            if (stack.isEmpty()) break;
        }
        for (int point = 0; point < 16; point++) {
            double angle = 2 * Math.PI * point / 16;
            player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK,
                    player.getX() + Math.cos(angle) * radius, player.getY() + 1,
                    player.getZ() + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1, .7f);
    }

    private static double availableLift(ServerPlayer player,LivingEntity target,int height) {
        if(target.isPassenger() || target.isVehicle())return 0;
        var level=player.serverLevel();var box=target.getBoundingBox();
        double maximum=Math.min(height,level.getMaxBuildHeight()-box.maxY);
        if(maximum<=0 || !level.getWorldBorder().isWithinBounds(box)
                || !level.hasChunksAt(net.minecraft.core.BlockPos.containing(box.minX,box.minY,box.minZ),net.minecraft.core.BlockPos.containing(box.maxX,box.maxY,box.maxZ)))return 0;
        if(!level.getBlockCollisions(target,box.expandTowards(0,maximum,0)).iterator().hasNext())return maximum;
        double low=0,high=maximum;
        for(int i=0;i<14;i++){
            double middle=(low+high)*.5;
            if(level.getBlockCollisions(target,box.expandTowards(0,middle,0)).iterator().hasNext())high=middle;else low=middle;
        }
        return Math.max(0,low-.0001);
    }

    private static void lift(ServerPlayer player, LivingEntity target,double height,int rank) {
        Vec3 start = target.position();
        if (target instanceof ServerPlayer other) {
            if (!other.teleportTo(player.serverLevel(), start.x, start.y + height, start.z,
                    Set.of(), other.getYRot(), other.getXRot())) return;
        } else {
            target.teleportTo(start.x, start.y + height, start.z);
        }
        target.setDeltaMovement(Vec3.ZERO);
        target.fallDistance = 0;
        target.setOnGround(false);
        GravitySwingEffects.apply(target,rank);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, start.x, start.y + 1, start.z,
                24, .4, .8, .4, .1);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1, target.getZ(),
                24, .4, .8, .4, .1);
        player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, .8f, 1.2f);
    }
}
