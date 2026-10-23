package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ChallengeRoarGameTests {
    @GameTest(template="riftfang_test",batch="review_roar_delusion")
    public static void brainTargetLockYieldsToDelusionAndResumesAfterIt(GameTestHelper test){
        var owner=TestPlayers.survival(test);var dreamer=TestPlayers.survival(test);owner.setNoGravity(true);dreamer.setNoGravity(true);owner.setPos(test.getBounds().getCenter());dreamer.setPos(owner.position());
        var piglin=EntityType.PIGLIN.create(test.getLevel());piglin.setNoAi(true);piglin.setNoGravity(true);piglin.setPos(owner.position().add(2,0,0));test.getLevel().addFreshEntity(piglin);
        try{
            var roar=Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),AdditionalAbilities.CHALLENGE_ROAR);owner.setItemInHand(InteractionHand.MAIN_HAND,roar);ChallengeRoarAbility.activate(owner,roar);
            var dream=Augmentations.apply(new ItemStack(ModItems.DREAMBOUND_CLAYMORE.get()),RecommendedAbilities.DELUSION);dreamer.setItemInHand(InteractionHand.MAIN_HAND,dream);DelusionEvents.activate(dreamer,dream);
            piglin.hurt(dreamer.damageSources().playerAttack(dreamer),1);ChallengeRoarAbility.tick(owner);ChallengeRoarAbility.brainTarget(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(piglin));
            test.assertTrue(DelusionEvents.suppressed(piglin) && piglin.getTarget()==null && piglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty(),"Roar overrides disoriented brain");
            DelusionEvents.cancel(dreamer.getUUID());ChallengeRoarAbility.tick(owner);
            test.assertTrue(piglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)==owner,"Roar does not resume after disorientation");
        }finally{DelusionEvents.cancel(dreamer.getUUID());ChallengeRoarAbility.cancel(owner.getUUID());piglin.discard();owner.server.getPlayerList().remove(owner);dreamer.server.getPlayerList().remove(dreamer);}
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_roar_ranges")
    public static void allSuperRangesUseSpheresAndCurrentWhiteValues(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var inside=EntityType.ZOMBIE.create(test.getLevel());var outside=EntityType.ZOMBIE.create(test.getLevel());
        for(var mob:List.of(inside,outside)){mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(player.position());test.getLevel().addFreshEntity(mob);}
        try{for(int rank=5;rank<=8;rank++){
            var stack=Augmentations.setLevel(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),AdditionalAbilities.CHALLENGE_ROAR,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            int range=ReviewedRanks.roarRange(rank);inside.setPos(player.position().add(0,range-.1,0));outside.setPos(player.position().add(0,range+.1,0));inside.setTarget(null);outside.setTarget(null);
            test.assertTrue(ChallengeRoarAbility.activate(player,stack),"Super range cast refused");
            test.assertTrue(inside.getTarget()==player && inside.hasEffect(MobEffects.WEAKNESS) && outside.getTarget()==null && !outside.hasEffect(MobEffects.WEAKNESS),"Wrong spherical super range at "+rank);
            var description=AbilityDescriptions.description(stack,AdditionalAbilities.CHALLENGE_ROAR,rank,false);int[] white={0};
            description.visit((style,text)->{if(style.getColor()!=null && style.getColor().getValue()==0xFFFFFF)white[0]++;return Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
            test.assertTrue(white[0]>=3 && description.getString().contains(range+" blocks") && description.getString().contains("Weakness I"),"Missing current white super values");
            ChallengeRoarAbility.cancel(player.getUUID());
        }}finally{ChallengeRoarAbility.cancel(player.getUUID());List.of(inside,outside).forEach(Entity::discard);player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_intimidating_roar")
    public static void superProgressionLocksTargetsAppliesMovingAuraAndKeepsActivationCooldown(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var zombie=EntityType.ZOMBIE.create(test.getLevel());var arriving=EntityType.ZOMBIE.create(test.getLevel());var cow=EntityType.COW.create(test.getLevel());
        for(var mob:List.of(zombie,arriving,cow)){mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(player.position().add(2,0,0));test.getLevel().addFreshEntity(mob);}
        try{
            for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof PulsiteWeapon){
                for(int rank=5;rank<=8;rank++){
                    var stack=Augmentations.setLevel(new ItemStack(entry.get()),AdditionalAbilities.CHALLENGE_ROAR,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());arriving.setPos(player.position().add(0,0,55));
                    AbilityTestPackets.use(player,0);int jade=JadeAugments.isJade(stack)?1:0;
                    var strength=player.getEffect(MobEffects.DAMAGE_BOOST);
                    test.assertTrue(strength!=null && strength.getAmplifier()==jade && strength.getDuration()==ReviewedRanks.roarTicks(rank)+20*jade,"Wrong super Strength");
                    test.assertTrue(Augmentations.maxRank(stack,AdditionalAbilities.CHALLENGE_ROAR)==8 && Augmentations.displayRank(stack,AdditionalAbilities.CHALLENGE_ROAR,rank)==rank-4,"Wrong super progression");
                    test.assertTrue(zombie.hasEffect(MobEffects.WEAKNESS) && zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && zombie.getEffect(MobEffects.WEAKNESS).getAmplifier()==0,"Missing fixed level-I aura");
                    test.assertFalse(cow.hasEffect(MobEffects.WEAKNESS),"Passive creature gets aura");
                    zombie.setTarget(cow);test.assertTrue(zombie.getTarget()==player,"Taunt allows another target");
                    float health=cow.getHealth();cow.hurt(cow.damageSources().mobAttack(zombie),1);test.assertTrue(cow.getHealth()==health,"Taunted mob damages another target");
                    arriving.setPos(player.position().add(0,0,3));ChallengeRoarAbility.tick(player);
                    test.assertTrue(arriving.hasEffect(MobEffects.WEAKNESS) && arriving.getTarget()==null,"Aura does not affect newcomers or newly taunts them");
                    arriving.setPos(player.position().add(0,0,55));ChallengeRoarAbility.tick(player);test.assertFalse(arriving.hasEffect(MobEffects.WEAKNESS),"Aura persists after leaving range");
                    long ready=player.level().getGameTime()+1200;
                    test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsValue(ready),"Super cooldown is not sixty seconds on activation");
                    player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                    test.assertFalse(ChallengeRoarAbility.active(player) || zombie.hasEffect(MobEffects.WEAKNESS) || player.hasEffect(MobEffects.DAMAGE_BOOST),"Switch leaves owned roar effects");
                    zombie.setTarget(cow);test.assertTrue(zombie.getTarget()==cow,"Target lock survives cancellation");
                    test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsValue(ready),"Switch restarts activation cooldown");
                }
            }
        }finally{ChallengeRoarAbility.cancel(player.getUUID());List.of(zombie,arriving,cow).forEach(Entity::discard);player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_roar_effect_ownership")
    public static void independentEffectsSurviveAndRemovingStrengthEndsTargetLock(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var zombie=EntityType.ZOMBIE.create(test.getLevel());var cow=EntityType.COW.create(test.getLevel());
        for(var mob:List.of(zombie,cow)){mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(player.position().add(2,0,0));test.getLevel().addFreshEntity(mob);}
        try{
            var stack=Augmentations.setLevel(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),AdditionalAbilities.CHALLENGE_ROAR,5);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            zombie.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS,100,0));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_BOOST,100,0));
            ChallengeRoarAbility.activate(player,stack);ChallengeRoarAbility.cancel(player.getUUID());
            test.assertTrue(zombie.getEffect(MobEffects.WEAKNESS).getDuration()==100 && player.getEffect(MobEffects.DAMAGE_BOOST).getDuration()==100,"Cancellation does not restore shorter independent effects");
            var independent=new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS,-1,2);zombie.addEffect(independent);
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_BOOST,800,2));
            test.assertTrue(ChallengeRoarAbility.activate(player,stack),"Cannot roar with independent Strength");
            ChallengeRoarAbility.cancel(player.getUUID());
            test.assertTrue(zombie.getEffect(MobEffects.WEAKNESS).isInfiniteDuration() && zombie.getEffect(MobEffects.WEAKNESS).getAmplifier()==2 && player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier()==2,"Cancellation removes independent effects");
            player.removeEffect(MobEffects.DAMAGE_BOOST);zombie.removeEffect(MobEffects.WEAKNESS);
            ChallengeRoarAbility.activate(player,stack);player.removeEffect(MobEffects.DAMAGE_BOOST);ChallengeRoarAbility.tick(player);zombie.setTarget(cow);
            test.assertTrue(!ChallengeRoarAbility.active(player) && !zombie.hasEffect(MobEffects.WEAKNESS) && zombie.getTarget()==cow,"Removing Strength leaves lock or aura");
        }finally{ChallengeRoarAbility.cancel(player.getUUID());List.of(zombie,cow).forEach(Entity::discard);player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsTauntHostilesThroughWallsIncludingBrainsAndPreserveFrozenTargets(GameTestHelper test) {
        var player=TestPlayers.survival(test);var otherPlayer=TestPlayers.survival(test);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);otherPlayer.setPos(point.add(-2,0,-3));var mobs=new ArrayList<Mob>();
        for(int x=4;x<=16;x++)for(int y=5;y<=9;y++)for(int z=4;z<=14;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        for(int y=5;y<=8;y++)test.setBlock(new BlockPos(8,y,10),Blocks.STONE);
        var visible=EntityType.ZOMBIE.create(test.getLevel());var hidden=EntityType.ZOMBIE.create(test.getLevel());var nearEdge=EntityType.ZOMBIE.create(test.getLevel());var outside=EntityType.ZOMBIE.create(test.getLevel());var cow=EntityType.COW.create(test.getLevel());var pet=EntityType.WOLF.create(test.getLevel());var neutral=EntityType.ENDERMAN.create(test.getLevel());var piglin=EntityType.PIGLIN.create(test.getLevel());var warden=EntityType.WARDEN.create(test.getLevel());
        mobs.addAll(List.of(visible,hidden,nearEdge,outside,cow,pet,neutral,piglin,warden));
        Vec3[] positions={point.add(2,0,0),point.add(0,0,4),point.add(15,0,0),point.add(17,0,0),point.add(-2,0,0),point.add(-2,0,-2),point.add(3,0,-2),point.add(2,0,-2),point.add(0,0,-3)};
        for(int i=0;i<mobs.size();i++){var mob=mobs.get(i);mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(positions[i]);test.getLevel().getChunkAt(mob.blockPosition());test.getLevel().addFreshEntity(mob);}pet.tame(player);
        int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof PulsiteWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.CHALLENGE_ROAR))==eligible,"Wrong Challenge Roar pool");if(!eligible)continue;count++;
            for(var mob:mobs)mob.setTarget(null);neutral.setTarget(otherPlayer);piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            player.removeEffect(MobEffects.DAMAGE_BOOST);
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.CHALLENGE_ROAR),"Fresh weapon taunts");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.CHALLENGE_ROAR);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.CHALLENGE_ROAR),"Roar rejected");
            var strength=player.getEffect(MobEffects.DAMAGE_BOOST);
            int jade=JadeAugments.isJade(stack)?1:0;
            test.assertTrue(strength!=null && strength.getAmplifier()==jade && strength.getDuration()==200+20*jade,"Roar granted the wrong Strength level or duration");
            test.assertTrue(visible.getTarget()==player && hidden.getTarget()==player && nearEdge.getTarget()==player && piglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)==player && piglin.getBrain().getMemory(MemoryModuleType.ANGRY_AT).orElse(null).equals(player.getUUID()) && warden.getTarget()==player,"Ordinary, hidden, distant-in-range or brain-driven mob ignores taunt");
            test.assertTrue(outside.getTarget()==null && cow.getTarget()==null && pet.getTarget()==null && neutral.getTarget()==otherPlayer,"Roar affects out-of-range, passive, neutral or allied mob");
            visible.setTarget(cow);WeaponControlLocks.acquire(visible,player.getUUID(),"taunt-test",20);
            RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.CHALLENGE_ROAR);test.assertTrue(WeaponControlLocks.frozen(visible),"Taunt cancels stun");WeaponControlLocks.release(player.getUUID(),"taunt-test");test.assertTrue(visible.getTarget()==player,"Stun release loses taunt");
}}finally {ChallengeRoarAbility.cancel(player.getUUID());WeaponControlLocks.release(player.getUUID(),"taunt-test");mobs.forEach(Entity::discard);player.server.getPlayerList().remove(player);otherPlayer.server.getPlayerList().remove(otherPlayer);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void fourthRankReaches28BlocksWithInstantCooldown(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);
        var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        player.setPos(point);
        var inside=EntityType.ZOMBIE.create(test.getLevel());
        var outside=EntityType.ZOMBIE.create(test.getLevel());
        inside.setNoAi(true);outside.setNoAi(true);inside.setNoGravity(true);outside.setNoGravity(true);
        inside.setPos(point.add(27,0,0));outside.setPos(point.add(29,0,0));
        test.getLevel().getChunkAt(inside.blockPosition());test.getLevel().getChunkAt(outside.blockPosition());
        test.getLevel().addFreshEntity(inside);test.getLevel().addFreshEntity(outside);
        try {
            ItemStack stack=new ItemStack(ModItems.SHRIEKING_CLAYMORE.get());
            for(int rank=1;rank<=4;rank++)stack=Augmentations.apply(stack,AdditionalAbilities.CHALLENGE_ROAR);
            test.assertTrue(Augmentations.level(stack,AdditionalAbilities.CHALLENGE_ROAR)==4,"Fourth rank was not learned");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            AbilityTestPackets.use(player,0);
            var strength=player.getEffect(MobEffects.DAMAGE_BOOST);
            test.assertTrue(strength!=null && strength.getAmplifier()==0 && strength.getDuration()==200,"Rank IV roar did not grant Strength I for ten seconds");
            test.assertTrue(inside.getTarget()==player && outside.getTarget()==null,"Rank IV range is not 28 blocks");
            String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+AdditionalAbilities.CHALLENGE_ROAR;
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)
                    ==player.level().getGameTime()+300,"Rank IV cooldown did not start on use");
        } finally {ChallengeRoarAbility.cancel(player.getUUID());inside.discard();outside.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
