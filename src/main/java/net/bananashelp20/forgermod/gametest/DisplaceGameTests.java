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
public class DisplaceGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveVariantsSwapOnlyWithClearDestinations(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var from=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,5)));var to=from.add(0,0,6);
        for(int x=6;x<=10;x++) for(int y=5;y<=9;y++) for(int z=4;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(to);test.getLevel().addFreshEntity(target);
        int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                boolean eligible=entry.get() instanceof InanisiumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.DISPLACE))==eligible,"Wrong Displace assignment");
                if(!eligible) continue;checked++;player.setPos(from);target.setPos(to);player.setYRot(0);player.setXRot(0);
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.DISPLACE),"Fresh dagger swaps");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.DISPLACE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                player.fallDistance=12;target.fallDistance=12;
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Clear swap rejected");
                test.assertTrue(player.position().distanceToSqr(to)<.0001 && target.position().distanceToSqr(from)<.0001,"Swap moves only one participant");
                test.assertTrue(player.fallDistance==0 && target.fallDistance==0,"Swap retains fall damage");
                player.setPos(from);target.setPos(to);player.setYRot(0);
                test.setBlock(new BlockPos(8,6,8),Blocks.STONE);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Swap targets through wall");
                test.assertTrue(player.position().equals(from) && target.position().equals(to),"Rejected swap moves participant");
                test.setBlock(new BlockPos(8,6,8),Blocks.AIR);
                test.setBlock(new BlockPos(8,6,11),Blocks.STONE);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Swap enters blocked body space");
                test.setBlock(new BlockPos(8,6,11),Blocks.AIR);
            }
        } finally {target.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==5,"Skipped swap variants");test.succeed();
    }
}
