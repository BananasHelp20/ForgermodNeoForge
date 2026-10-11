package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
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
public class JustADreamGameTests {
    @GameTest(template="riftfang_test")
    public static void crossDimensionAnchorReturnPreservesItsCharge(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var nether=player.server.getLevel(net.minecraft.world.level.Level.NETHER);
        var anchor=new BlockPos(16,200,16);
        var saved=new java.util.HashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        for(int x=14;x<=18;x++) for(int z=14;z<=18;z++) for(int y=200;y<=203;y++) {
            var pos=new BlockPos(x,y,z);saved.put(pos,nether.getBlockState(pos));nether.setBlockAndUpdate(pos,y==200?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        }
        nether.setBlockAndUpdate(anchor,Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(net.minecraft.world.level.block.RespawnAnchorBlock.CHARGE,1));
        try {
            player.setRespawnPosition(nether.dimension(),anchor,0,false,false);
            var stack=Augmentations.apply(new ItemStack(ModItems.DREAMBOUND_CLAYMORE.get()),AdditionalAbilities.JUST_A_DREAM);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.JUST_A_DREAM),"Cross-dimension anchor return rejected");
            test.assertTrue(player.level()==nether && player.position().distanceToSqr(Vec3.atCenterOf(anchor))<16,"Return ignores respawn dimension");
            test.assertTrue(nether.getBlockState(anchor).getValue(net.minecraft.world.level.block.RespawnAnchorBlock.CHARGE)==1,"Return consumes anchor charge");
        } finally {player.server.getPlayerList().remove(player);saved.forEach(nether::setBlockAndUpdate);}
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void fifteenWeaponsReturnToForcedRespawnWithoutFreshOrMountedUse(GameTestHelper test) {
        var player=TestPlayers.survival(test);var spawn=test.absolutePos(new BlockPos(8,6,8));var from=Vec3.atBottomCenterOf(spawn.offset(0,0,-5));
        for(int x=6;x<=10;x++) for(int y=6;y<=9;y++) for(int z=2;z<=10;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        test.setBlock(new BlockPos(8,5,8),Blocks.STONE);player.setRespawnPosition(test.getLevel().dimension(),spawn,0,true,false);
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.JUST_A_DREAM))==eligible,"Wrong respawn ability assignment");
                if(!eligible) continue;count++;player.setPos(from);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.JUST_A_DREAM),"Fresh weapon returns home");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.JUST_A_DREAM);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.fallDistance=12;player.setDeltaMovement(new Vec3(1,-1,0));
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.JUST_A_DREAM),"Clear respawn rejected");
                var expected=new Vec3(spawn.getX()+.5,spawn.getY()+.1,spawn.getZ()+.5);
                test.assertTrue(player.position().distanceToSqr(expected)<.0001 && player.fallDistance==0 && player.getDeltaMovement().equals(Vec3.ZERO),"Wrong respawn position or motion reset");
                player.setPos(from);test.setBlock(new BlockPos(8,7,8),Blocks.STONE);
                // The forced spawn no longer fits, so vanilla falls back; test safety helper directly for the rejected body.
                test.assertFalse(net.bananashelp20.forgermod.item.custom.abilities.WeaponTeleportSafety.clear(player,test.getLevel(),expected,null),"Blocked respawn body considered safe");
                test.setBlock(new BlockPos(8,7,8),Blocks.AIR);
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Somnium family or gemstone");test.succeed();
    }
}
