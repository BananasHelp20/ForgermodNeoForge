package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.SlipstreamEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SlipstreamGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveDaggersBoostFormationRunningAndStopAtOvertakeOrWall(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setSprinting(true);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));var wolf=EntityType.WOLF.create(test.getLevel());wolf.setTame(true,true);wolf.setOwnerUUID(player.getUUID());wolf.setNoAi(true);wolf.setNoGravity(true);test.getLevel().addFreshEntity(wolf);
        for(int y=5;y<=8;y++) for(int z=7;z<=15;z++) test.setBlock(new BlockPos(8,y,z),Blocks.AIR);double baseline=player.getAttributeValue(Attributes.MOVEMENT_SPEED);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.SLIPSTREAM))==eligible,"Wrong Slipstream assignment");
                if(!eligible) continue;count++;player.setPos(origin);wolf.setPos(origin.add(0,0,2));wolf.setDeltaMovement(0,0,.3);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);SlipstreamEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED)==baseline,"Fresh dagger grants slipstream");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.SLIPSTREAM);player.setItemInHand(InteractionHand.MAIN_HAND,stack);SlipstreamEvents.tick(new PlayerTickEvent.Post(player));player.setPos(origin.add(0,0,.3));wolf.setPos(origin.add(0,0,2.3));SlipstreamEvents.tick(new PlayerTickEvent.Post(player));
                test.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED)-baseline*1.1)<.0001,"Formation running does not grant 10 percent base speed");
                SlipstreamEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED)-baseline)<.0001,"Stopping keeps bonus");
                player.setPos(origin.add(0,0,3));wolf.setPos(origin.add(0,0,2.6));SlipstreamEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED)-baseline)<.0001,"Overtaking keeps bonus");
                player.setPos(origin);wolf.setPos(origin.add(0,0,2));SlipstreamEvents.tick(new PlayerTickEvent.Post(player));test.setBlock(new BlockPos(8,6,9),Blocks.STONE);player.setPos(origin.add(0,0,.3));wolf.setPos(origin.add(0,0,2.3));SlipstreamEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED)-baseline)<.0001,"Wall retains slipstream");test.setBlock(new BlockPos(8,6,9),Blocks.AIR);
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);SlipstreamEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED)-baseline)<.0001,"Putting dagger away retains modifier");
            }
        } finally {SlipstreamEvents.end(player);wolf.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Slipstream variants");test.succeed();
    }
}
