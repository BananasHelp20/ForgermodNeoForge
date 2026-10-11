package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PillowArmorGameTests {
    @GameTest(template="riftfang_test")
    public static void allAxesReduceOnlyLearnedMainHandFallDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon weapon && weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.PILLOW_ARMOR))==eligible,"Wrong Pillow Armor assignment");
                if(!eligible) continue;count++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.setHealth(20);player.invulnerableTime=0;
                test.assertTrue(player.hurt(player.damageSources().fall(),8),"Fresh fall hit rejected");test.assertTrue(player.getHealth()==12,"Fresh axe reduces fall damage");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.PILLOW_ARMOR);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setHealth(20);player.invulnerableTime=0;
                test.assertTrue(player.hurt(player.damageSources().fall(),8),"Learned fall hit rejected");test.assertTrue(player.getHealth()==14,"Fall reduction is not 25 percent");
                player.setHealth(20);player.invulnerableTime=0;player.hurt(player.damageSources().generic(),8);test.assertTrue(player.getHealth()==12,"Nonfall damage is reduced");
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,stack);player.setHealth(20);player.invulnerableTime=0;player.hurt(player.damageSources().fall(),8);
                test.assertTrue(player.getHealth()==12,"Offhand grants main-hand fall protection");player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Pillow Armor variants");test.succeed();
    }
}
