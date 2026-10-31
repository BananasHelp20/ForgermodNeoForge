package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class MetalShovelGameTests {
    @GameTest(template="riftfang_test")
    public static void allEightShovelsMineFlattenRepairAndUseVanillaTags(GameTestHelper test) {
        var player=TestPlayers.survival(test);var pos=test.absolutePos(new BlockPos(8,5,8));int count=0;
        try {
            for(var entry:ModItems.TOOL_SETS.entrySet()) {
                var item=entry.getValue().shovel().get();var stack=new ItemStack(item);count++;
                test.assertTrue(item instanceof ShovelItem && stack.is(ItemTags.SHOVELS),"Not a tagged shovel: "+entry.getKey());
                test.assertFalse(Augmentations.eligible(stack),"Ordinary shovel can learn weapon abilities");
                test.assertTrue(item.isValidRepairItem(stack,new ItemStack(entry.getValue().material().get())),"Wrong repair ingredient: "+entry.getKey());
                test.assertTrue(stack.getMaxDamage()==entry.getValue().pickaxe().get().getDefaultInstance().getMaxDamage(),"Shovel uses wrong tier durability");
                test.assertTrue(stack.getDestroySpeed(Blocks.SAND.defaultBlockState())==entry.getValue().pickaxe().get().getDefaultInstance().getDestroySpeed(Blocks.STONE.defaultBlockState()),"Shovel uses wrong tier mining speed");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(pos.north()));
                test.getLevel().setBlockAndUpdate(pos,Blocks.DIRT.defaultBlockState());test.getLevel().setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());
                var context=new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
                test.assertTrue(item.useOn(context).consumesAction() && test.getLevel().getBlockState(pos).is(Blocks.DIRT_PATH),"Shovel cannot flatten dirt: "+entry.getKey());
                test.assertTrue(stack.getDamageValue()==1,"Flattening does not consume one durability");
                stack.mineBlock(test.getLevel(),Blocks.SAND.defaultBlockState(),pos,player);
                test.assertTrue(stack.getDamageValue()==2,"Digging does not consume one durability");
                test.assertTrue(player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)>0,"Shovel has invalid attack speed");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==8,"Missing approved metal shovels");test.succeed();
    }
}
