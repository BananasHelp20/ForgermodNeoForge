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
        }}finally {WeaponControlLocks.release(player.getUUID(),"taunt-test");mobs.forEach(Entity::discard);player.server.getPlayerList().remove(player);otherPlayer.server.getPlayerList().remove(otherPlayer);}
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
        } finally {inside.discard();outside.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
