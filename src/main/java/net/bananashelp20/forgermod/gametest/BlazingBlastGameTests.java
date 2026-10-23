package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.*;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class BlazingBlastGameTests {
    private static final String ID=RecommendedAbilities.EXPLOSION;
    private static ServerPlayer player(GameTestHelper test){var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());return player;}
    private static List<Fireball> shots(ServerPlayer player){return player.serverLevel().getEntitiesOfClass(Fireball.class,player.getBoundingBox().inflate(100),e->e.getOwner()==player && BlazingBlastEvents.isBurstProjectile(e));}
    private static void cleanup(ServerPlayer player){shots(player).forEach(Entity::discard);WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
    private static void impact(Fireball projectile,HitResult result){
        try{Class<?> type=projectile.getClass();java.lang.reflect.Method method=null;while(type!=null){try{method=type.getDeclaredMethod("onHit",HitResult.class);break;}catch(NoSuchMethodException missing){type=type.getSuperclass();}}if(method==null)throw new IllegalStateException("Missing impact method");method.setAccessible(true);method.invoke(projectile,result);}catch(ReflectiveOperationException failure){throw new RuntimeException(failure);}
    }
    @GameTest(template="riftfang_test",batch="review_blazing_directions")
    public static void allVariantsAndSuperRanksFireTwelveFacingRelativeProjectilesWithInstantCooldowns(GameTestHelper test)throws Exception{
        var player=player(test);player.setYRot(37.5F);boolean terrain=Config.EXPLOSION_TERRAIN.get();Config.EXPLOSION_TERRAIN.set(false);
        try{for(var item:List.of(ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get()))for(int rank=5;rank<=8;rank++){
            var stack=Augmentations.setLevel(new ItemStack(item),ID,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());player.removeEffect(MobEffects.DAMAGE_RESISTANCE);AbilityTestPackets.use(player,0);var shots=shots(player);
            test.assertTrue(shots.size()==12 && shots.stream().filter(e->e instanceof LargeFireball).count()==4 && shots.stream().filter(e->e instanceof SmallFireball).count()==8,"Wrong projectile counts");
            for(int i=0;i<4;i++){var direction=Vec3.directionFromRotation(0,37.5F+90*i);test.assertTrue(shots.stream().anyMatch(e->e instanceof LargeFireball && e.getDeltaMovement().normalize().dot(direction)>.99999),"Missing cardinal fireball");}
            for(int i=0;i<8;i++){var direction=Vec3.directionFromRotation(0,37.5F+22.5F+45*i);test.assertTrue(shots.stream().anyMatch(e->e instanceof SmallFireball && e.getDeltaMovement().normalize().dot(direction)>.99999),"Missing intermediate fireball");}
            int jade=JadeAugments.isJade(stack)?1:0;var resistance=player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            test.assertTrue(resistance!=null && resistance.getAmplifier()==3+jade && resistance.getDuration()==40+20*jade,"Super loses original Resistance");
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsValue(player.level().getGameTime()+ReviewedRanks.cooldown(ID,rank)),"Wrong super activation cooldown");
            test.assertTrue(Augmentations.maxRank(stack,ID)==8 && Augmentations.displayRank(stack,ID,rank)==rank-4,"Wrong super progression");
            var description=AbilityDescriptions.description(stack,ID,rank,false);test.assertTrue(description.getString().contains(AbilityDescriptions.number(100*ReviewedRanks.blastProjectileMultiplier(rank))+"%"),"Missing current damage percentage");
            for(var projectile:shots){test.assertTrue(!projectile.shouldBeSaved() && !projectile.canUsePortal(true) && !projectile.canChangeDimensions(player.level(),player.level()),"Temporary projectile can save/portal-clone");projectile.discard();}
        }}finally{Config.EXPLOSION_TERRAIN.set(terrain);cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_blazing_damage")
    public static void projectileDamageScalesAndSiblingBurstsCannotCollide(GameTestHelper test)throws Exception{
        var player=player(test);var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);zombie.setHealth(200);zombie.setPos(player.position().add(0,0,5));test.getLevel().addFreshEntity(zombie);
        try{for(int rank=5;rank<=8;rank++){
            var stack=Augmentations.setLevel(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),ID,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);BlazingBlastEvents.fire(player,stack);var shots=shots(player);var large=shots.stream().filter(e->e instanceof LargeFireball).findFirst().orElseThrow();var small=shots.stream().filter(e->e instanceof SmallFireball).findFirst().orElseThrow();
            float multiplier=(float)ReviewedRanks.blastProjectileMultiplier(rank);
            zombie.setHealth(200);zombie.invulnerableTime=0;zombie.hurt(player.damageSources().fireball(large,player),6);test.assertTrue(Math.abs(zombie.getHealth()-(200-6*multiplier))<.01 && zombie.getLastHurtByMob()==player,"Wrong ghast damage or owner attribution");
            zombie.setHealth(200);zombie.invulnerableTime=0;impact(small,new EntityHitResult(zombie));test.assertTrue(Math.abs(zombie.getHealth()-(200-5*multiplier))<.01 && zombie.getRemainingFireTicks()==100,"Wrong blaze damage or ignition");
            var method=large.getClass().getDeclaredMethod("canHitEntity",Entity.class);method.setAccessible(true);test.assertFalse((boolean)method.invoke(large,shots.getLast()),"Burst siblings collide");
            test.assertFalse(large.hurt(player.damageSources().fireball(shots.getLast(),player),6),"Sibling impact redirects another fireball");
            for(var projectile:shots){projectile.tickCount=200;projectile.tick();test.assertFalse(projectile.isAlive(),"Projectile outlives bounded active lifetime");}
        }}finally{zombie.discard();cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_blazing_terrain")
    public static void terrainSettingProtectsBlocksAndFireAndEnablesRequestedDestruction(GameTestHelper test){
        var player=player(test);boolean previous=Config.EXPLOSION_TERRAIN.get();var block=player.blockPosition().east(4);var fire=block.above();var center=Vec3.atCenterOf(block);var hit=new BlockHitResult(center.add(0,.5,0),Direction.UP,block,false);
        try{
            test.assertFalse(Config.EXPLOSION_TERRAIN.getDefault(),"Terrain setting does not default off");var stack=Augmentations.setLevel(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),ID,5);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            Config.EXPLOSION_TERRAIN.set(false);BlazingBlastEvents.fire(player,stack);var shots=shots(player);var small=shots.stream().filter(e->e instanceof SmallFireball).findFirst().orElseThrow();var large=shots.stream().filter(e->e instanceof LargeFireball).findFirst().orElseThrow();
            test.getLevel().setBlockAndUpdate(block,Blocks.DIRT.defaultBlockState());test.getLevel().setBlockAndUpdate(fire,Blocks.AIR.defaultBlockState());small.setPos(center.add(0,1,0));impact(small,hit);large.setPos(center);impact(large,hit);
            test.assertTrue(test.getLevel().getBlockState(block).is(Blocks.DIRT) && test.getLevel().getBlockState(fire).isAir(),"Disabled projectiles break or ignite terrain");shots.forEach(Entity::discard);
            Config.EXPLOSION_TERRAIN.set(true);BlazingBlastEvents.fire(player,stack);small=shots(player).stream().filter(e->e instanceof SmallFireball).findFirst().orElseThrow();small.setPos(center.add(0,1,0));impact(small,hit);test.assertTrue(test.getLevel().getBlockState(fire).is(Blocks.FIRE),"Enabled blaze projectile cannot ignite terrain");shots(player).forEach(Entity::discard);test.getLevel().setBlockAndUpdate(fire,Blocks.AIR.defaultBlockState());
            var base=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),ID);player.setItemInHand(InteractionHand.MAIN_HAND,base);var near=player.blockPosition().east();test.getLevel().setBlockAndUpdate(near,Blocks.DIRT.defaultBlockState());
            Config.EXPLOSION_TERRAIN.set(false);RecommendedAbilityRuntime.activate(player,base,ID);test.assertTrue(test.getLevel().getBlockState(near).is(Blocks.DIRT),"Base ignores disabled setting");
            Config.EXPLOSION_TERRAIN.set(true);RecommendedAbilityRuntime.activate(player,base,ID);test.assertFalse(test.getLevel().getBlockState(near).is(Blocks.DIRT),"Enabled base explosion cannot alter terrain");
        }finally{Config.EXPLOSION_TERRAIN.set(previous);test.getLevel().setBlockAndUpdate(block,Blocks.AIR.defaultBlockState());test.getLevel().setBlockAndUpdate(fire,Blocks.AIR.defaultBlockState());cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_blazing_real_impact")
    public static void realBlazeFlightHitsAnEntityWithoutTerrainFire(GameTestHelper test){
        var player=player(test);boolean previous=Config.EXPLOSION_TERRAIN.get();Config.EXPLOSION_TERRAIN.set(false);var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
        try{var stack=Augmentations.setLevel(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),ID,5);player.setItemInHand(InteractionHand.MAIN_HAND,stack);BlazingBlastEvents.fire(player,stack);var shot=shots(player).stream().filter(e->e instanceof SmallFireball).findFirst().orElseThrow();var direction=shot.getDeltaMovement().normalize();zombie.setPos(player.position().add(direction.scale(3)));test.getLevel().addFreshEntity(zombie);
            for(int i=0;i<15 && shot.isAlive();i++)shot.tick();test.assertTrue(!shot.isAlive() && zombie.getHealth()==15 && zombie.getRemainingFireTicks()>0,"Real blaze flight misses or loses vanilla damage/ignition");
        }finally{Config.EXPLOSION_TERRAIN.set(previous);zombie.discard();cleanup(player);}test.succeed();
    }
}
