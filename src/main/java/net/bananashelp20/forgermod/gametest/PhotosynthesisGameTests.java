package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.PhotosynthesisEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PhotosynthesisGameTests {
    private static void ticks(net.minecraft.server.level.ServerPlayer player,int ticks) {for(int i=0;i<ticks;i++) PhotosynthesisEvents.tick(new PlayerTickEvent.Pre(player));}
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsRepairOnlyLearnedHeldWeaponDuringDay(GameTestHelper test) {
        var player=TestPlayers.survival(test);long oldTime=test.getLevel().getDayTime();test.getLevel().setDayTime(1000);test.getLevel().updateSkyBrightness();int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.PHOTOSYNTHESIS))==eligible,"Wrong Photosynthesis pool");if(!eligible) continue;count++;
            PhotosynthesisEvents.end(player.getUUID());fresh.setDamageValue(10);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);ticks(player,300);test.assertTrue(fresh.getDamageValue()==10,"Fresh weapon repairs");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.PHOTOSYNTHESIS);player.setItemInHand(InteractionHand.MAIN_HAND,stack);ticks(player,299);test.assertTrue(stack.getDamageValue()==10,"Repairs before interval");ticks(player,1);test.assertTrue(stack.getDamageValue()==9 && Augmentations.hasPassive(stack,AdditionalAbilities.PHOTOSYNTHESIS),"Durability or augmentation data damaged");
            ticks(player,3000);test.assertTrue(stack.getDamageValue()==0,"Repairs below zero");stack.setDamageValue(3);test.getLevel().setDayTime(18000);test.getLevel().updateSkyBrightness();ticks(player,300);test.assertTrue(stack.getDamageValue()==3,"Night repairs");
            test.getLevel().setDayTime(1000);test.getLevel().updateSkyBrightness();ticks(player,200);var replacement=stack.copy();player.setItemInHand(InteractionHand.MAIN_HAND,replacement);ticks(player,100);test.assertTrue(replacement.getDamageValue()==3,"Switch retains repair timer");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,replacement);ticks(player,300);test.assertTrue(replacement.getDamageValue()==3,"Offhand repairs");
        }} finally {PhotosynthesisEvents.end(player.getUUID());test.getLevel().setDayTime(oldTime);test.getLevel().updateSkyBrightness();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
