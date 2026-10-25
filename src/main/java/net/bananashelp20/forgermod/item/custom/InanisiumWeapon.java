package net.bananashelp20.forgermod.item.custom;

import net.bananashelp20.forgermod.augmentation.Augmentations;
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
    public boolean activateAbility(ServerPlayer player, ItemStack stack, WeaponAbilitySlot slot) {
        if (!Augmentations.ids(stack, true).contains(abilityDescriptionKey(slot))) return false;
        if (!isDagger() || !player.isAlive() || player.isPassenger()) return false;
        return switch (slot) {
            case PRIMARY -> stepThroughVoid(player);
            case SECONDARY -> suddenPresence(player, stack);
            case TERTIARY -> false;
        };
    }

    private static boolean stepThroughVoid(ServerPlayer player) {
        Vec3 direction = player.getLookAngle().normalize();
        int rank=Augmentations.level(player.getMainHandItem(),net.bananashelp20.forgermod.item.custom.abilities.VoidStepRanges.ID);
        boolean paired=net.bananashelp20.forgermod.item.custom.abilities.VoidStepRanges.paired(player);
        if(net.bananashelp20.forgermod.item.custom.abilities.VoidStepRanges.etherwarpCast(player,player.getMainHandItem()))
            return etherwarp(player,direction,net.bananashelp20.forgermod.item.custom.abilities.VoidStepRanges.etherwarp(rank,paired));
        double range=net.bananashelp20.forgermod.item.custom.abilities.VoidStepRanges.blink(rank,paired);
        ServerLevel level = player.serverLevel();
        Vec3 start = player.position();
        AABB originalBox = player.getBoundingBox();
        Vec3 destination = start;
        double remaining = range;
        while (remaining > 1.0E-6) {
            double length = Math.min(VoidStepPath.STEP_DISTANCE, remaining);
            Vec3 step = direction.scale(length);
            AABB current = originalBox.move(destination.subtract(start));
            double fraction = VoidStepPath.clearFraction(t -> {
                AABB swept = current.expandTowards(step.scale(t));
                return swept.minY >= level.getMinBuildHeight() && swept.maxY <= level.getMaxBuildHeight()
                        && level.getWorldBorder().isWithinBounds(swept)
                        && loadedBox(level, swept);
            });
            // The swept AABB is only broad-phase: a diagonal corner inside that box may miss the actual path.
            if(fraction>0)for(var shape:level.getCollisions(player,current.expandTowards(step.scale(fraction)))){
                for(var obstacle:shape.toAabbs())fraction=Math.min(fraction,VoidStepPath.collisionFraction(current,step,obstacle));
            }
            destination = destination.add(step.scale(fraction));
            if (fraction < 1) break;
            remaining -= length;
        }
        if (remaining > 1.0E-6 && direction.y < -1.0E-6) {
            // A blink may land on the visible surface; it need not sweep the player's feet through the floor.
            Vec3 surface = aimedSurfaceLanding(player, direction, range);
            if (surface != null) destination = surface;
        }
        if (destination.distanceToSqr(start) < 1.0E-8) return false;

        // Zero relative rotation preserves mouse look updated while the teleport packet is in flight.
        return teleportWithSound(player, destination, player.getYRot(), player.getXRot(),
                Set.of(net.minecraft.world.entity.RelativeMovement.Y_ROT,net.minecraft.world.entity.RelativeMovement.X_ROT));
    }

    private static boolean etherwarp(ServerPlayer player,Vec3 direction,double range) {
        var level=player.serverLevel();
        Vec3 eye=player.getEyePosition();
        // Segment the ray so looking into an unloaded chunk never loads or generates it.
        for(double distance=0;distance<range;distance+=VoidStepPath.STEP_DISTANCE) {
            Vec3 from=eye.add(direction.scale(distance));
            Vec3 to=eye.add(direction.scale(Math.min(range,distance+VoidStepPath.STEP_DISTANCE)));
            if(!blinkVolumeLoaded(level,new AABB(from,to).inflate(1.0E-7))) return false;
            var hit=clipBlinkRay(player,from,to);
            if(hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS) continue;
            var pos=hit.getBlockPos();
            var shape=level.getBlockState(pos).getCollisionShape(level,pos,net.minecraft.world.phys.shapes.CollisionContext.of(player));
            if(shape.isEmpty() || hit.isInside()) return false;
            double y=pos.getY()+shape.max(net.minecraft.core.Direction.Axis.Y)+1.0E-6;
            Vec3 landing=new Vec3(pos.getX()+.5,y,pos.getZ()+.5);
            AABB box=player.getBoundingBox().move(landing.subtract(player.position()));
            if(!blinkVolumeLoaded(level,box) || !safeLanding(player,landing)) return false;
            Vec3 departure=player.position();
            if(!teleportWithSound(player,landing,player.getYRot(),player.getXRot(),
                    Set.of(net.minecraft.world.entity.RelativeMovement.Y_ROT,net.minecraft.world.entity.RelativeMovement.X_ROT))) return false;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,departure.x,departure.y+.9,departure.z,40,.35,.7,.35,.12);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,landing.x,landing.y+.9,landing.z,24,.35,.7,.35,.06);
            return true;
        }
        return false;
    }

    private static Vec3 aimedSurfaceLanding(ServerPlayer player, Vec3 direction, double range) {
        var level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        double limit = 0;
        // Trace collision shapes, not selection outlines: grass, flowers and vines are transparent.
        while (limit < range - 1.0E-6) {
            double next = Math.min(range, limit + VoidStepPath.STEP_DISTANCE);
            Vec3 from = eye.add(direction.scale(limit)), to = eye.add(direction.scale(next));
            AABB segment = new AABB(from, to).inflate(1.0E-7);
            if (!blinkVolumeLoaded(level, segment)) break;
            var hit = clipBlinkRay(player, from, to);
            if (hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                limit += from.distanceTo(hit.getLocation());
                break;
            }
            limit = next;
        }
        // Work back from the first obstruction, preserving the furthest supported, unobstructed destination.
        for (double distance = limit; distance > 1.0E-6; distance = Math.max(0, distance - VoidStepPath.STEP_DISTANCE)) {
            Vec3 landing = surfaceAt(player, eye.add(direction.scale(distance)), range);
            if (landing == null) continue;
            double base = distance, span = Math.min(VoidStepPath.STEP_DISTANCE, limit - distance);
            double fraction = VoidStepPath.clearFraction(t -> surfaceAt(player, eye.add(direction.scale(base + span * t)), range) != null);
            Vec3 refined = surfaceAt(player, eye.add(direction.scale(base + span * fraction)), range);
            return refined == null ? landing : refined;
        }
        return null;
    }

    private static Vec3 surfaceAt(ServerPlayer player, Vec3 point, double range) {
        var level = player.serverLevel();
        // Search only between the eye ray and its corresponding foot position, never above the aimed surface.
        var hit = clipBlinkRay(player, point.add(0, 1.0E-5, 0), point.add(0, -player.getEyeHeight() - 1.0E-5, 0));
        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS || hit.isInside() || hit.getDirection() != net.minecraft.core.Direction.UP) return null;
        Vec3 landing = hit.getLocation().add(0, 1.0E-6, 0);
        double distance = landing.distanceToSqr(player.position());
        if (distance < 1.0E-8 || distance > range * range + 1.0E-6) return null;
        AABB box = player.getBoundingBox().move(landing.subtract(player.position()));
        return blinkVolumeLoaded(level, box) && safeLanding(player, landing) ? landing : null;
    }

    private static net.minecraft.world.phys.BlockHitResult clipBlinkRay(ServerPlayer player, Vec3 from, Vec3 to) {
        var level = player.serverLevel();
        var hit = level.clip(new net.minecraft.world.level.ClipContext(from, to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) return hit;
        // Vanilla's ray clip probes 0.1% ahead and may return an inside point just below a floor.
        // Resolve the exact face so safe landings do not retreat unnecessarily or sink into the surface.
        var boxes = level.getBlockState(hit.getBlockPos()).getCollisionShape(level, hit.getBlockPos(),
                net.minecraft.world.phys.shapes.CollisionContext.of(player)).toAabbs();
        var exact = AABB.clip(boxes, from, to, hit.getBlockPos());
        return exact == null ? hit : exact;
    }

    private static boolean blinkVolumeLoaded(ServerLevel level, AABB box) {
        return box.minY >= level.getMinBuildHeight() && box.maxY <= level.getMaxBuildHeight()
                && level.getWorldBorder().isWithinBounds(box) && loadedBox(level, box);
    }

    private static boolean loadedBox(ServerLevel level, AABB box) {
        for (int x = Mth.floor(box.minX) >> 4; x <= (Mth.floor(Math.nextDown(box.maxX)) >> 4); x++) {
            for (int z = Mth.floor(box.minZ) >> 4; z <= (Mth.floor(Math.nextDown(box.maxZ)) >> 4); z++) {
                if (!level.getChunkSource().hasChunk(x, z)) return false;
            }
        }
        return true;
    }

    private static boolean suddenPresence(ServerPlayer player, ItemStack stack) {
        int radius=net.bananashelp20.forgermod.augmentation.ReviewedRanks.suddenPresenceRange(
                Augmentations.level(stack,"tooltips.forgermod.ability.sudden_presence"));
        List<Mob> targets = player.serverLevel().getEntitiesOfClass(Mob.class,
                        player.getBoundingBox().inflate(radius),
                        mob -> mob instanceof Enemy && mob.isAlive()
                                && player.distanceToSqr(mob) <= (double)radius*radius);
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
        return teleportWithSound(player,destination,yaw,pitch,Set.of());
    }

    private static boolean teleportWithSound(ServerPlayer player, Vec3 destination, float yaw, float pitch,
                                             Set<net.minecraft.world.entity.RelativeMovement> relative) {
        BlockPos departure = player.blockPosition();
        if (!player.teleportTo(player.serverLevel(), destination.x, destination.y, destination.z,
                relative, yaw, pitch)) return false;
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
        if (!isDagger() && pAttacker instanceof ServerPlayer player && Augmentations.eligible(pStack)) {
            Augmentations.canonical(pStack).onDaggerHit(pTarget, player);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        String loreKey = switch (this.type) {
            case "axe" -> "tooltips.forgermod.nullified_axe.tooltip";
            case "dagger" -> "tooltips.forgermod.riftfang_dagger.tooltip";
            default -> "tooltips.forgermod.claymore_of_the_void.tooltip";
        };
        appendWeaponTooltip(pStack, pTooltipComponents, loreKey, this.gemstone);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}
