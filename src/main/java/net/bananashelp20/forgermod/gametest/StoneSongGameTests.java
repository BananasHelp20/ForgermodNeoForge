package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.StoneSongEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class StoneSongGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveClaymoresGrantExactlyTwoArmorBelowZeroWithoutStackingOrPersistence(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);double original=player.getAttributeValue(Attributes.ARMOR);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof PulsiteWeapon w && !w.isDagger() && !w.isAxe();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.STONE_SONG))==eligible,"Wrong Stone Song pool");if(!eligible)continue;count++;
            player.setPos(player.getX(),-1,player.getZ());player.setItemInHand(InteractionHand.MAIN_HAND,fresh);StoneSongEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(player.getAttributeValue(Attributes.ARMOR)==original,"Fresh weapon gives armor");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.STONE_SONG);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            StoneSongEvents.tick(new PlayerTickEvent.Post(player));StoneSongEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(player.getAttributeValue(Attributes.ARMOR)==original+2,"Armor bonus stacks or is missing");
            player.setPos(player.getX(),0,player.getZ());StoneSongEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(player.getAttributeValue(Attributes.ARMOR)==original,"Y=0 keeps armor");
            player.setPos(player.getX(),-.01,player.getZ());StoneSongEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(player.getAttributeValue(Attributes.ARMOR)==original+2,"Negative fractional Y misses armor");
            StoneSongEvents.travel(new EntityTravelToDimensionEvent(player,Level.NETHER));test.assertTrue(player.getAttributeValue(Attributes.ARMOR)==original,"Dimension travel keeps old modifier");
            StoneSongEvents.tick(new PlayerTickEvent.Post(player));player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,stack);StoneSongEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(player.getAttributeValue(Attributes.ARMOR)==original,"Offhand grants armor");player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        }}finally {StoneSongEvents.end(player);player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped variants");test.succeed();
    }
}
