package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MagneticThrowGameTests {
    @GameTest(template="riftfang_test")
    public static void allElectriumWeaponsUseLookVelocityWithoutDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setYRot(0);player.setXRot(0);
        for(int y=5;y<=8;y++) for(int z=7;z<=14;z++) test.setBlock(new BlockPos(8,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.MAGNETIC_THROW))==eligible,"Wrong Magnetic Throw assignment");
                if(!eligible) continue;count++;mob.setPos(origin.add(0,0,2));mob.setDeltaMovement(Vec3.ZERO);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.MAGNETIC_THROW),"Fresh weapon launches");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.MAGNETIC_THROW);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.MAGNETIC_THROW),"Learned launch rejected");
                test.assertTrue(mob.position().equals(origin.add(0,0,2)) && mob.getHealth()==20 && mob.getDeltaMovement().distanceTo(player.getLookAngle().scale(1.1))<.0001,"Launch teleports, hurts or uses wrong direction/speed");
                player.setXRot(-35);mob.setPos(player.getEyePosition().add(player.getLookAngle().scale(2)).subtract(0,mob.getEyeHeight(),0));
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.MAGNETIC_THROW),"Skyward launch rejected");
                test.assertTrue(mob.getDeltaMovement().y>0 && mob.getDeltaMovement().distanceTo(player.getLookAngle().scale(1.1))<.0001,"Launch ignores vertical look direction");player.setXRot(0);
                mob.setPos(origin.add(0,0,8));test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.MAGNETIC_THROW),"Launch exceeds melee reach");
                mob.setPos(origin.add(0,0,2));test.setBlock(new BlockPos(8,6,9),Blocks.STONE);test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.MAGNETIC_THROW),"Launch targets through wall");test.setBlock(new BlockPos(8,6,9),Blocks.AIR);
            }
        } finally {mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Magnetic Throw variants");test.succeed();
    }
}
