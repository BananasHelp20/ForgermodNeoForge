package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EyeOfStormGameTests {
    @GameTest(template="riftfang_test",batch="moving_storm_review")
    public static void fiveRanksCarryAndConfineEnemiesWithMatchingLevitationDuration(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setNoGravity(true);var origin=test.getBounds().getCenter();player.setPos(origin);
        for(int x=-4;x<=20;x++)for(int y=0;y<5;y++)for(int z=-3;z<=3;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(origin.add(0,0,3));test.getLevel().addFreshEntity(mob);
        String id=net.bananashelp20.forgermod.item.custom.abilities.EyeOfStormEvents.ID;
        try{
            for(var item:List.of(ModItems.DEAD_CALM_DAGGER.get(),ModItems.DEAD_CALM_DAGGER_RUBY.get(),ModItems.DEAD_CALM_DAGGER_AMBER.get(),ModItems.DEAD_CALM_DAGGER_AMETHYST.get(),ModItems.DEAD_CALM_DAGGER_JADE.get()))for(int rank=1;rank<=5;rank++){
                player.setPos(origin);mob.setPos(origin.add(0,0,3));mob.setDeltaMovement(Vec3.ZERO);
                var stack=net.bananashelp20.forgermod.augmentation.Augmentations.setLevel(new ItemStack(item),id,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                var weapon=(TaifuniteWeapon)item;
                test.assertTrue(weapon.activateAbility(player,stack,WeaponAbilitySlot.PRIMARY),"Cannot start rank "+rank);
                int expected=net.bananashelp20.forgermod.augmentation.ReviewedRanks.stormTicks(rank)+(net.bananashelp20.forgermod.augmentation.JadeAugments.isJade(stack)?20:0);
                test.assertTrue(mob.getEffect(MobEffects.LEVITATION)!=null && mob.getEffect(MobEffects.LEVITATION).getDuration()==expected,"Levitation does not match full storm duration");
                player.setPos(origin.add(15,0,0));TaifuniteWeapon.onPlayerTick(new PlayerTickEvent.Post(player));
                double radius=net.bananashelp20.forgermod.augmentation.ReviewedRanks.stormRadius(rank);
                test.assertTrue(player.distanceToSqr(mob)<=radius*radius,"Moving player leaves captured enemy behind");
                mob.setPos(player.position().add(-radius-4,0,0));TaifuniteWeapon.onPlayerTick(new PlayerTickEvent.Post(player));
                test.assertTrue(player.distanceToSqr(mob)<=radius*radius,"Captured enemy escapes storm");
                weapon.cancelAbility(player,WeaponAbilitySlot.PRIMARY);
                test.assertFalse(mob.hasEffect(MobEffects.LEVITATION),"Cancellation leaves owned levitation");
            }
            player.setPos(origin);mob.setPos(origin.add(0,0,3));
            var stack=net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(ModItems.DEAD_CALM_DAGGER_JADE.get()),id);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.LEVITATION,900,0));
            var weapon=(TaifuniteWeapon)stack.getItem();weapon.activateAbility(player,stack,WeaponAbilitySlot.PRIMARY);weapon.cancelAbility(player,WeaponAbilitySlot.PRIMARY);
            test.assertTrue(mob.getEffect(MobEffects.LEVITATION).getDuration()==900 && mob.getEffect(MobEffects.LEVITATION).getAmplifier()==0,"Storm fails to restore prior levitation");
            mob.removeEffect(MobEffects.LEVITATION);mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.LEVITATION,900,4));
            var external=mob.getEffect(MobEffects.LEVITATION);weapon.activateAbility(player,stack,WeaponAbilitySlot.PRIMARY);weapon.cancelAbility(player,WeaponAbilitySlot.PRIMARY);
            test.assertTrue(mob.getEffect(MobEffects.LEVITATION)==external && external.getDuration()==900,"Storm changes stronger independent levitation");
            mob.removeEffect(MobEffects.LEVITATION);weapon.activateAbility(player,stack,WeaponAbilitySlot.PRIMARY);
            mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.LEVITATION,-1,1));weapon.cancelAbility(player,WeaponAbilitySlot.PRIMARY);
            test.assertTrue(mob.getEffect(MobEffects.LEVITATION)!=null && mob.getEffect(MobEffects.LEVITATION).getDuration()==-1,"Cancellation removes an independent infinite replacement");
        }finally{
            net.bananashelp20.forgermod.item.custom.abilities.EyeOfStormEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template = "riftfang_test")
    public static void stormAffectsOnlyEnemiesInsideItsCurrentRadius(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        Vec3 center = test.getBounds().getCenter();
        player.setPos(center);
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(ModItems.DEAD_CALM_DAGGER.get()));
        var other = test.makeMockServerPlayerInLevel();
        var team=player.getScoreboard().addPlayerTeam("storm_"+player.getUUID().toString().substring(0,8));
        player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
        player.getScoreboard().addPlayerToTeam(other.getScoreboardName(),team);
        other.setNoGravity(true);
        other.setPos(center.add(2, 0, 0));
        var cow = EntityType.COW.create(test.getLevel());
        cow.setPos(center.add(-2, 0, 0));
        var zombie = EntityType.ZOMBIE.create(test.getLevel());
        zombie.setNoAi(true);zombie.setNoGravity(true);
        zombie.setPos(center.add(0, 0, 2));
        var arrow = EntityType.ARROW.create(test.getLevel());
        arrow.setPos(center.add(0, 0, -2));
        var item = new ItemEntity(test.getLevel(), center.x + 1, center.y, center.z + 1, new ItemStack(Items.DIRT));
        var orb = new ExperienceOrb(test.getLevel(), center.x - 1, center.y, center.z - 1, 1);
        var outside = EntityType.ZOMBIE.create(test.getLevel());outside.setNoAi(true);outside.setNoGravity(true);
        outside.setPos(center.add(5.1, 0, 0));
        List<Entity> targets = List.of(other, cow, zombie, arrow, item, orb);
        for (Entity target : List.of(cow, zombie, arrow, item, orb, outside)) test.getLevel().addFreshEntity(target);
        for (Entity target : targets) { target.setDeltaMovement(Vec3.ZERO); target.hurtMarked = false; }
        player.setDeltaMovement(Vec3.ZERO);
        outside.setDeltaMovement(Vec3.ZERO);
        test.assertTrue(zombie.startRiding(cow,true),"Mounted enemy fixture failed");
        TaifuniteWeapon weapon = (TaifuniteWeapon)player.getMainHandItem().getItem();
        test.assertTrue(weapon.activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Storm did not activate");
        TaifuniteWeapon.onPlayerTick(new PlayerTickEvent.Post(player));
        test.assertFalse(zombie.isPassenger(),"Mounted enemy bypasses capture");
        for (Entity target : targets) {
            if(target==zombie){
                test.assertTrue(target.getDeltaMovement().y==.16 && target.getDeltaMovement().horizontalDistanceSqr()>0,"Storm ignores enemy");
                test.assertTrue(target.hurtMarked && target.hasImpulse,"Storm fails to synchronize enemy motion");
            }else test.assertTrue(target.getDeltaMovement().equals(Vec3.ZERO),"Storm moves a nonenemy: "+target.getType());
        }
        test.assertTrue(player.getDeltaMovement().equals(Vec3.ZERO), "Storm moved its caster");
        test.assertTrue(outside.getDeltaMovement().equals(Vec3.ZERO), "Storm exceeded five blocks");
        test.assertTrue(weapon.abilityCooldownTicks(WeaponAbilitySlot.PRIMARY) == 1200, "Storm cooldown changed");
        // Embedded mock connections do not drive normal ServerPlayer ticks.
        for (int tick = 1; tick <= 12; tick++) {
            test.runAfterDelay(tick, () -> TaifuniteWeapon.onPlayerTick(new PlayerTickEvent.Post(player)));
        }
        test.runAfterDelay(13, () -> {
            test.assertTrue(!other.hasEffect(MobEffects.LEVITATION) && !cow.hasEffect(MobEffects.LEVITATION)
                    && zombie.hasEffect(MobEffects.LEVITATION), "Storm levitates nonenemies or ignores its enemy");
            test.assertFalse(player.hasEffect(MobEffects.LEVITATION), "Storm levitated its caster");
            weapon.cancelAbility(player, WeaponAbilitySlot.PRIMARY);
            for(var target:List.of(cow,zombie,arrow,item,orb,outside))target.discard();player.getScoreboard().removePlayerTeam(team);
            test.getLevel().getServer().getPlayerList().remove(other);
            test.getLevel().getServer().getPlayerList().remove(player);
            test.succeed();
        });
    }
}
