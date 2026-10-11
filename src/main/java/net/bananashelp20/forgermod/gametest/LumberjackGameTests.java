package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LumberjackGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveAxesSaveLogDurabilityButNotStoneOrCombat(GameTestHelper test) {
        var player=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());int count=0;var pos=test.absolutePos(new BlockPos(8,5,8));
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon w && w.isAxe();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.LUMBERJACK))==eligible,"Wrong Lumberjack pool");if(!eligible) continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);fresh.mineBlock(test.getLevel(),Blocks.OAK_LOG.defaultBlockState(),pos,player);test.assertTrue(fresh.getDamageValue()>0,"Fresh axe saves durability");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.LUMBERJACK);stack.setDamageValue(10);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            for(var log:java.util.List.of(Blocks.OAK_LOG,Blocks.STRIPPED_OAK_LOG,Blocks.SPRUCE_LOG,Blocks.CRIMSON_STEM)) stack.mineBlock(test.getLevel(),log.defaultBlockState(),pos,player);
            test.assertTrue(stack.getDamageValue()==10,"Learned axe wears on a tagged log");stack.mineBlock(test.getLevel(),Blocks.STONE.defaultBlockState(),pos,player);test.assertTrue(stack.getDamageValue()>10,"Stone gets free durability");
            int before=stack.getDamageValue();((LushWeapon)stack.getItem()).axeAttack(stack,mob,player);test.assertTrue(stack.getDamageValue()==before+1,"Combat gets free durability");
            stack.setDamageValue(stack.getMaxDamage()-1);stack.mineBlock(test.getLevel(),Blocks.OAK_LOG.defaultBlockState(),pos,player);test.assertTrue(!stack.isEmpty() && stack.getDamageValue()==stack.getMaxDamage()-1,"Last durability breaks on a log");
        }} finally {mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped variants");test.succeed();
    }
}
