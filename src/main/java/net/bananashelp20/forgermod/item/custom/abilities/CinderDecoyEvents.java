package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class CinderDecoyEvents {
    private record Target(Mob mob,LivingEntity previous) {}
    private record Decoy(ServerPlayer owner,ItemStack stack,int selected,CinderDecoyEntity entity,long expires,int rank,Map<UUID,Target> targets) {}
    private static final Map<UUID,Decoy> DECOYS=new HashMap<>();
    private CinderDecoyEvents() {}
    private static ItemStack armor(Item item) {
        var stack=new ItemStack(item); stack.set(DataComponents.DYED_COLOR,new DyedItemColor(0xf57824,false)); return stack;
    }
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        int rank=Augmentations.level(stack,RecommendedAbilities.CINDER_DECOY);
        if(rank<=0) return false;
        var entity=new CinderDecoyEntity(player);
        entity.setNoGravity(true); entity.setSilent(true); entity.setNoBasePlate(true); entity.setShowArms(true);
        entity.setYRot(player.getYRot()); entity.setInvisible(true);
        var head=new ItemStack(Items.PLAYER_HEAD); head.set(DataComponents.PROFILE,new ResolvableProfile(player.getGameProfile()));
        entity.setItemSlot(EquipmentSlot.HEAD,head); entity.setItemSlot(EquipmentSlot.CHEST,armor(Items.LEATHER_CHESTPLATE));
        entity.setItemSlot(EquipmentSlot.LEGS,armor(Items.LEATHER_LEGGINGS)); entity.setItemSlot(EquipmentSlot.FEET,armor(Items.LEATHER_BOOTS));
        if(!player.serverLevel().addFreshEntity(entity)) return false;
        DECOYS.put(player.getUUID(),new Decoy(player,stack,player.getInventory().selected,entity,
                player.level().getGameTime()+80+40L*rank,rank,new HashMap<>()));
        attract(DECOYS.get(player.getUUID())); return true;
    }
    private static void attract(Decoy decoy) {
        for(Mob mob:decoy.entity().level().getEntitiesOfClass(Mob.class,decoy.entity().getBoundingBox().inflate(10),
                mob->mob instanceof Enemy && mob.isAlive() && !mob.isAlliedTo(decoy.owner()) && mob.hasLineOfSight(decoy.entity()))) {
            if(mob.getTarget()==decoy.owner() || mob.getTarget()==null) {
                decoy.targets().putIfAbsent(mob.getUUID(),new Target(mob,mob.getTarget())); mob.setTarget(decoy.entity());
            }
        }
    }
    public static boolean active(ServerPlayer player) {
        Decoy decoy=DECOYS.get(player.getUUID());
        if(decoy==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=decoy.stack()
                || !Augmentations.ids(player.getMainHandItem(),true).contains(RecommendedAbilities.CINDER_DECOY)
                || player.getInventory().selected!=decoy.selected() || player.level()!=decoy.entity().level()
                || decoy.entity().isRemoved() || player.level().getGameTime()>=decoy.expires()) {
            cancel(player.getUUID()); return false;
        }
        return true;
    }
    public static void cancel(UUID owner) {
        Decoy decoy=DECOYS.remove(owner); if(decoy==null) return;
        for(Target target:decoy.targets().values()) if(target.mob().getTarget()==decoy.entity())
            target.mob().setTarget(target.previous()!=null && target.previous().isAlive() && !target.previous().isRemoved()
                    && target.previous().level()==target.mob().level()?target.previous():null);
        decoy.entity().discard();
    }
    public static void detonate(UUID owner) {
        Decoy decoy=DECOYS.get(owner); if(decoy==null || !active(decoy.owner())) return;
        var pos=decoy.entity().position(); var level=decoy.owner().serverLevel();
        cancel(owner);
        level.explode(decoy.owner(),pos.x,pos.y,pos.z,2,false,Level.ExplosionInteraction.NONE);
        for(Mob enemy:level.getEntitiesOfClass(Mob.class,new net.minecraft.world.phys.AABB(pos,pos).inflate(4),
                mob->mob instanceof Enemy && mob.isAlive() && !mob.isAlliedTo(decoy.owner()) && mob.distanceToSqr(pos)<=16))
            if(level.clip(new net.minecraft.world.level.ClipContext(pos.add(0,1,0),enemy.getEyePosition(),
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,decoy.owner()))
                    .getType()==net.minecraft.world.phys.HitResult.Type.MISS)
                enemy.setRemainingFireTicks(Math.max(enemy.getRemainingFireTicks(),(2+decoy.rank())*20));
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        Decoy decoy=DECOYS.get(player.getUUID());
        if(!decoy.entity().level().getEntitiesOfClass(Mob.class,decoy.entity().getBoundingBox().inflate(.15),
                mob->mob instanceof Enemy && mob.isAlive() && !mob.isAlliedTo(player)).isEmpty()) {
            detonate(player.getUUID()); return;
        }
        if(player.level().getGameTime()%5==0) {
            attract(decoy);
            player.serverLevel().sendParticles(ParticleTypes.FLAME,decoy.entity().getX(),decoy.entity().getY()+1,decoy.entity().getZ(),8,.3,.7,.3,.01);
        }
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity().getUUID()); }
}
