package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

public final class WeaponAbilityTargets {
    private WeaponAbilityTargets() {}
    public static boolean enemy(ServerPlayer owner,LivingEntity target) {
        return target!=owner && target.isAlive() && !target.isAlliedTo(owner) && (target instanceof Enemy
                || target instanceof Player player && owner.server.isPvpAllowed() && owner.canHarmPlayer(player)
                || target instanceof Mob mob && mob.getTarget()==owner);
    }
    public static LivingEntity aimedEnemy(ServerPlayer player,double range) {
        var start=player.getEyePosition(); var end=start.add(player.getLookAngle().scale(range));
        var wall=player.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        if(wall.getType()!=HitResult.Type.MISS) end=wall.getLocation();
        var hit=ProjectileUtil.getEntityHitResult(player,start,end,player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),
                target->target instanceof LivingEntity living && enemy(player,living),start.distanceToSqr(end));
        return hit==null?null:(LivingEntity)hit.getEntity();
    }
}
