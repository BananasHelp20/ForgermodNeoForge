package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.GravityWellEvents;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class GravityWellGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveAxesPullWithoutDamageThenSmashOnceAndCancelOnSwitch(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=6;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(origin.add(0,0,5));test.getLevel().addFreshEntity(mob);
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof InanisiumWeapon weapon && weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.GRAVITY_WELL))==eligible,"Wrong Gravity Well assignment");
                if(!eligible) continue;count++;mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);mob.setHealth(20);mob.setPos(origin.add(0,0,5));mob.setDeltaMovement(Vec3.ZERO);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.GRAVITY_WELL),"Fresh axe pulls");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.GRAVITY_WELL);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GRAVITY_WELL),"Learned pull rejected");
                test.assertTrue(mob.position().equals(origin.add(0,0,5)) && Math.abs(mob.getDeltaMovement().z+.6)<.0001,"Pull teleports or has wrong velocity");
                test.assertTrue(mob.getHealth()==20,"Pull itself deals damage");
                var velocity=mob.getDeltaMovement();GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
                test.assertTrue(mob.getDeltaMovement().equals(velocity),"Pull applies force repeatedly instead of one burst");
                mob.setPos(origin.add(0,0,2));GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
                test.assertFalse(GravityWellEvents.active(player),"Arrival does not finish visual smash");test.assertTrue(Math.abs(mob.getHealth()-(20-player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)))<.0001,"Arrival smash does not deal weapon damage");
                float health=mob.getHealth();GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));test.assertTrue(mob.getHealth()==health,"Smash repeats after arrival");
                mob.setPos(origin.add(0,0,5));test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GRAVITY_WELL),"Cannot start switch test");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(GravityWellEvents.active(player),"Switch retains pull state");
            }
        } finally {GravityWellEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Gravity Well variants");test.succeed();
    }
}
