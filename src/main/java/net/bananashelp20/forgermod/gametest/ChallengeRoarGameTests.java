package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ChallengeRoarGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsTauntVisibleHostilesIncludingBrainsAndPreserveFrozenTargets(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);var mobs=new ArrayList<Mob>();
        for(int x=4;x<=16;x++)for(int y=5;y<=9;y++)for(int z=4;z<=14;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        for(int y=5;y<=8;y++)test.setBlock(new BlockPos(8,y,10),Blocks.STONE);
        var visible=EntityType.ZOMBIE.create(test.getLevel());var hidden=EntityType.ZOMBIE.create(test.getLevel());var outside=EntityType.ZOMBIE.create(test.getLevel());var cow=EntityType.COW.create(test.getLevel());var pet=EntityType.WOLF.create(test.getLevel());var piglin=EntityType.PIGLIN.create(test.getLevel());var warden=EntityType.WARDEN.create(test.getLevel());
        mobs.addAll(List.of(visible,hidden,outside,cow,pet,piglin,warden));
        Vec3[] positions={point.add(2,0,0),point.add(0,0,4),point.add(7,0,0),point.add(-2,0,0),point.add(-2,0,-2),point.add(2,0,-2),point.add(0,0,-3)};
        for(int i=0;i<mobs.size();i++){var mob=mobs.get(i);mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(positions[i]);test.getLevel().addFreshEntity(mob);}pet.tame(player);
        int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof PulsiteWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.CHALLENGE_ROAR))==eligible,"Wrong Challenge Roar pool");if(!eligible)continue;count++;
            for(var mob:mobs)mob.setTarget(null);piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.CHALLENGE_ROAR),"Fresh weapon taunts");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.CHALLENGE_ROAR);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.CHALLENGE_ROAR),"Roar rejected");
            test.assertTrue(visible.getTarget()==player && piglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)==player && piglin.getBrain().getMemory(MemoryModuleType.ANGRY_AT).orElse(null).equals(player.getUUID()) && warden.getTarget()==player,"Ordinary or brain-driven mob ignores taunt");
            test.assertTrue(hidden.getTarget()==null && outside.getTarget()==null && cow.getTarget()==null && pet.getTarget()==null,"Roar affects hidden/distant/neutral/allied mob");
            visible.setTarget(cow);WeaponControlLocks.acquire(visible,player.getUUID(),"taunt-test",20);
            RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.CHALLENGE_ROAR);test.assertTrue(WeaponControlLocks.frozen(visible),"Taunt cancels stun");WeaponControlLocks.release(player.getUUID(),"taunt-test");test.assertTrue(visible.getTarget()==player,"Stun release loses taunt");
        }}finally {WeaponControlLocks.release(player.getUUID(),"taunt-test");mobs.forEach(Entity::discard);player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
