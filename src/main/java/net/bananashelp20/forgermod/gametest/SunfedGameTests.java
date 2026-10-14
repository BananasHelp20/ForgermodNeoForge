package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.SunfedEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SunfedGameTests {
    private static void ticks(net.minecraft.server.level.ServerPlayer player,int ticks) {for(int i=0;i<ticks;i++) SunfedEvents.tick(new PlayerTickEvent.Pre(player));}
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsFeedOnlyOutdoorsByDayWithSaturation(GameTestHelper test) {
        var player=TestPlayers.survival(test);var p=test.absolutePos(new BlockPos(8,5,8));player.setPos(Vec3.atBottomCenterOf(p));long oldTime=test.getLevel().getDayTime();int count=0;
        for(int y=p.getY();y<test.getLevel().getMaxBuildHeight();y++) test.getLevel().setBlockAndUpdate(new BlockPos(p.getX(),y,p.getZ()),Blocks.AIR.defaultBlockState());test.getLevel().setDayTime(1000);test.getLevel().updateSkyBrightness();
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.SUNFED))==eligible,"Wrong Sunfed pool");if(!eligible) continue;count++;
            SunfedEvents.end(player.getUUID());player.getFoodData().setFoodLevel(18);player.getFoodData().setSaturation(2);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);ticks(player,300);test.assertTrue(player.getFoodData().getFoodLevel()==18,"Fresh weapon restores food");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.SUNFED);player.setItemInHand(InteractionHand.MAIN_HAND,stack);ticks(player,299);test.assertTrue(player.getFoodData().getFoodLevel()==18,"Feeds before 15 seconds");ticks(player,1);test.assertTrue(player.getFoodData().getFoodLevel()==19 && player.getFoodData().getSaturationLevel()==3,"Food or saturation wrong");ticks(player,600);test.assertTrue(player.getFoodData().getFoodLevel()==20,"Food exceeds cap");
            player.getFoodData().setSaturation(0);ticks(player,300);test.assertTrue(player.getFoodData().getSaturationLevel()==1,"Full hunger blocks saturation");
            player.getFoodData().setSaturation(19.5F);ticks(player,300);test.assertTrue(player.getFoodData().getSaturationLevel()==20,"Saturation exceeds cap");
            player.getFoodData().setFoodLevel(18);player.getFoodData().setSaturation(2);test.getLevel().setBlockAndUpdate(p.above(3),Blocks.STONE.defaultBlockState());ticks(player,300);test.assertTrue(player.getFoodData().getFoodLevel()==18,"Roof allows Sunfed");test.getLevel().setBlockAndUpdate(p.above(3),Blocks.AIR.defaultBlockState());
            test.getLevel().setDayTime(18000);test.getLevel().updateSkyBrightness();ticks(player,300);test.assertTrue(player.getFoodData().getFoodLevel()==18,"Night allows Sunfed");test.getLevel().setDayTime(1000);test.getLevel().updateSkyBrightness();ticks(player,200);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());ticks(player,100);test.assertTrue(player.getFoodData().getFoodLevel()==18,"Switch carries timer");
        }} finally {SunfedEvents.end(player.getUUID());test.getLevel().setDayTime(oldTime);test.getLevel().updateSkyBrightness();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
