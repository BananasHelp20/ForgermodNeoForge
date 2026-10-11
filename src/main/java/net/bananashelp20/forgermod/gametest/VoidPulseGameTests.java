package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
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
public class VoidPulseGameTests {
    @GameTest(template="riftfang_test")
    public static void pulsePushesNearbyEnemiesWithoutDamageOrTeleport(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        for(int x=4;x<=12;x++) for(int y=5;y<=8;y++) for(int z=4;z<=13;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var front=EntityType.ZOMBIE.create(test.getLevel());front.setNoAi(true);front.setNoGravity(true);front.setPos(point.add(0,0,3));test.getLevel().addFreshEntity(front);
        var rear=EntityType.ZOMBIE.create(test.getLevel());rear.setNoAi(true);rear.setNoGravity(true);rear.setPos(point.add(0,0,-3));test.getLevel().addFreshEntity(rear);
        var neutral=EntityType.COW.create(test.getLevel());neutral.setNoAi(true);neutral.setNoGravity(true);neutral.setPos(point.add(2,0,0));test.getLevel().addFreshEntity(neutral);
        int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                boolean eligible=entry.get() instanceof InanisiumWeapon weapon && !weapon.isDagger() && !weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.VOID_PULSE))==eligible,"Wrong Void Pulse assignment");
                if(!eligible) continue;checked++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.VOID_PULSE),"Fresh blade uses pulse");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.VOID_PULSE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                front.setDeltaMovement(Vec3.ZERO);rear.setDeltaMovement(Vec3.ZERO);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.VOID_PULSE),"Pulse rejected");
                test.assertTrue(Math.abs(front.getDeltaMovement().z-.6)<.0001 && Math.abs(rear.getDeltaMovement().z+.6)<.0001,"Pulse direction or strength wrong");
                test.assertTrue(front.position().equals(point.add(0,0,3)) && rear.position().equals(point.add(0,0,-3)),"Pulse teleports enemies");
                test.assertTrue(front.getHealth()==20 && rear.getHealth()==20 && neutral.getDeltaMovement().equals(Vec3.ZERO),"Pulse damages enemies or pushes neutral mob");
            }
            test.setBlock(new BlockPos(8,6,10),Blocks.STONE);front.setDeltaMovement(Vec3.ZERO);
            RecommendedAbilityRuntime.activate(player,player.getMainHandItem(),AdditionalAbilities.VOID_PULSE);
            test.assertTrue(front.getDeltaMovement().equals(Vec3.ZERO),"Pulse passes through wall");
        } finally {front.discard();rear.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==5,"Skipped pulse variants");test.succeed();
    }
}
