package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** An instantaneous, block-clipped piercing ray using the Warden's sonic effects. */
public final class SonicBoomBlast {
    public static final double RANGE = 50;

    private SonicBoomBlast() {}

    private static AABB beamSpaceBox(AABB worldBox, Vec3 origin, Vec3 right, Vec3 up, Vec3 forward) {
        double minX = Double.POSITIVE_INFINITY, minY = minX, minZ = minX;
        double maxX = Double.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
        for (double x : new double[]{worldBox.minX, worldBox.maxX}) {
            for (double y : new double[]{worldBox.minY, worldBox.maxY}) {
                for (double z : new double[]{worldBox.minZ, worldBox.maxZ}) {
                    Vec3 point = new Vec3(x, y, z).subtract(origin);
                    double a = point.dot(right), b = point.dot(up), c = point.dot(forward);
                    minX = Math.min(minX, a); maxX = Math.max(maxX, a);
                    minY = Math.min(minY, b); maxY = Math.max(maxY, b);
                    minZ = Math.min(minZ, c); maxZ = Math.max(maxZ, c);
                }
            }
        }
        return new AABB(minX - 1.5, minY - 1.5, minZ, maxX + 1.5, maxY + 1.5, maxZ);
    }

    public static void fire(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle().normalize();
        double range = RANGE;
        // Stop before an unloaded chunk rather than loading terrain for an ability.
        for (double distance = 0; distance <= RANGE; distance += .25) {
            if (!level.hasChunkAt(BlockPos.containing(start.add(direction.scale(distance))))) {
                range = Math.max(0, distance - .25);
                break;
            }
        }
        HitResult block = level.clip(new ClipContext(start, start.add(direction.scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = block.getLocation();
        double length = start.distanceTo(end);
        // A square cross-section perpendicular to the aim, including vertical shots.
        Vec3 right = Math.abs(direction.y) > .999 ? new Vec3(1, 0, 0)
                : direction.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = right.cross(direction).normalize();
        float damage = (float)(2 * player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(start, end).inflate(2.13),
                entity -> entity != player && entity.isAlive() && !entity.isSpectator()
                        && !(entity instanceof ItemEntity)
                        && (!(entity instanceof Player other) || player.canHarmPlayer(other)))) {
            AABB box = beamSpaceBox(target.getBoundingBox(), start, right, up, direction);
            Vec3 beamStart = Vec3.ZERO;
            Vec3 hit = box.contains(beamStart) ? beamStart : box.clip(beamStart, new Vec3(0, 0, length)).orElse(null);
            if (hit == null || (block.getType() != HitResult.Type.MISS && hit.z >= length)) continue;
            if (target.hurt(level.damageSources().sonicBoom(player), damage)) {
                double resistance = target instanceof LivingEntity living
                        ? 1 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 1;
                target.push(direction.x * 2.5 * resistance, direction.y * .5 * resistance,
                        direction.z * 2.5 * resistance);
            }
        }
        // Vanilla sonic rings, one block apart; never draw through the blocking surface.
        // Long-distance packets let nearby viewers see the entire 50-block trail.
        var viewers = level.players().stream()
                .filter(viewer -> viewer.position().distanceToSqr(start) <= (RANGE + 32) * (RANGE + 32))
                .toList();
        for (int distance = 1; distance < length; distance++) {
            Vec3 point = start.add(direction.scale(distance));
            for (ServerPlayer viewer : viewers) {
                level.sendParticles(viewer, ParticleTypes.SONIC_BOOM, true, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3, 1);
    }
}
