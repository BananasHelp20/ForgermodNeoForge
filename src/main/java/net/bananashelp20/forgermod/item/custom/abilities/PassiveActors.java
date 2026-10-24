package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.*;
import java.util.UUID;
/** Passive effects belong to the actual wielding entity, never a proxy player or its summoner. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class PassiveActors {
    private static final java.util.Map<LivingEntity,Entity> KILLERS=new java.util.WeakHashMap<>();
    private PassiveActors(){}
    public static Entity killingActor(LivingEntity victim){return KILLERS.get(victim);}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void deathCredit(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event){
        var actor=attacker(event.getSource());if(!supported(actor)){KILLERS.remove(event.getEntity());return;}
        KILLERS.put(event.getEntity(),actor);
        if(actor instanceof BoneGuardEvents.FriendlyGuard guard && actor instanceof LivingEntity living && WeaponAbilityTargets.hostileTo(living,event.getEntity())){
            event.getEntity().setLastHurtByPlayer(guard.owner());
            ((net.bananashelp20.forgermod.mixin.ReinforcementExperienceAccess)event.getEntity()).forgermod$setExperienceCreditTicks(100);
        }
    }
    public static boolean supported(Entity entity){return entity!=null && !entity.level().isClientSide && (entity instanceof ServerPlayer || entity instanceof Mob && entity instanceof BoneGuardEvents.FriendlyGuard);}
    public static Entity entity(Entity entity){return supported(entity)?entity:null;}
    public static ServerPlayer owner(LivingEntity actor){return actor instanceof ServerPlayer player?player:((BoneGuardEvents.FriendlyGuard)actor).owner();}
    public static ServerLevel level(LivingEntity actor){return (ServerLevel)actor.level();}
    public static int selected(LivingEntity actor){return actor instanceof ServerPlayer player?player.getInventory().selected:0;}
    public static LivingEntity find(MinecraftServer server,UUID id){for(var level:server.getAllLevels())if(level.getEntity(id) instanceof LivingEntity actor && supported(actor))return actor;return null;}
    public static Entity projectileActor(net.minecraft.world.entity.projectile.Projectile projectile){
        if(projectile instanceof AbstractArrow arrow && arrow.getPersistentData().hasUUID("forgermod_bone_guard_shooter"))
            return arrow.level() instanceof ServerLevel level?level.getEntity(arrow.getPersistentData().getUUID("forgermod_bone_guard_shooter")):null;
        return projectile.getOwner();
    }
    public static Entity attacker(DamageSource source){
        if(source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPersistentData().hasUUID("forgermod_bone_guard_shooter"))
            return projectileActor(arrow);
        return source.getEntity();
    }
    @EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD)
    public static final class AttributesRegistration {
        @SubscribeEvent @SuppressWarnings("unchecked") public static void attributes(net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent event){
            for(var choice:NecromancerCatalog.CHOICES){
                var type=(EntityType<? extends LivingEntity>)choice.type();
                if(!event.has(type,net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED))event.add(type,net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,4);
                if(!event.has(type,net.minecraft.world.entity.ai.attributes.Attributes.ARMOR))event.add(type,net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,0);
                if(!event.has(type,net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED))event.add(type,net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,.25);
                if(!event.has(type,net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION))event.add(type,net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION,0);
            }
        }
    }
    public static boolean melee(DamageSource source){return source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK) && source.getEntity() instanceof BoneGuardEvents.FriendlyGuard;}
    public static ItemStack weapon(LivingEntity actor,String id){
        var main=actor.getMainHandItem();var off=actor.getOffhandItem();
        if(actor instanceof BoneGuardEvents.FriendlyGuard && DaggerItems.hasMatchingDaggers(actor) && Augmentations.hasPassive(off,id)
                && (!Augmentations.hasPassive(main,id) || Augmentations.level(off,id)>Augmentations.level(main,id) || Augmentations.level(off,id)==Augmentations.level(main,id) && JadeAugments.isJade(off)))return off;
        return main;
    }
    public static boolean canAffect(LivingEntity actor,LivingEntity target){
        if(target==actor)return false;
        return !(actor instanceof BoneGuardEvents.FriendlyGuard guard) || target!=guard.owner()
                && !(target instanceof BoneGuardEvents.FriendlyGuard) && !WeaponAbilityTargets.ally(guard.owner(),target);
    }
    public static boolean has(LivingEntity actor,String id){return Augmentations.hasPassive(weapon(actor,id),id);}
    @SubscribeEvent public static void playerPre(PlayerTickEvent.Pre event){if(event.getEntity() instanceof ServerPlayer)NeoForge.EVENT_BUS.post(new PassiveActorTickEvent.Pre(event.getEntity()));}
    @SubscribeEvent public static void playerPost(PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer)NeoForge.EVENT_BUS.post(new PassiveActorTickEvent.Post(event.getEntity()));}
    @SubscribeEvent public static void mobPre(EntityTickEvent.Pre event){if(event.getEntity() instanceof Mob && supported(event.getEntity()))NeoForge.EVENT_BUS.post(new PassiveActorTickEvent.Pre(event.getEntity()));}
    @SubscribeEvent public static void mobPost(EntityTickEvent.Post event){if(event.getEntity() instanceof Mob && supported(event.getEntity()))NeoForge.EVENT_BUS.post(new PassiveActorTickEvent.Post(event.getEntity()));}
    @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){
        if(event.getEntity() instanceof Mob && supported(event.getEntity())){NeoForge.EVENT_BUS.post(new PassiveActorTickEvent.Pre(event.getEntity()));NeoForge.EVENT_BUS.post(new PassiveActorTickEvent.Post(event.getEntity()));}
    }
}
