package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SleepwalkerGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveDaggersGainTenPercentOnlyAtNight(GameTestHelper test) {
        var player=TestPlayers.survival(test);long time=test.getLevel().getDayTime();int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.SLEEPWALKER))==eligible,"Wrong Sleepwalker assignment");
                if(!eligible) continue;checked++;var stack=Augmentations.apply(fresh,AdditionalAbilities.SLEEPWALKER);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                var target=EntityType.ZOMBIE.create(test.getLevel());target.getAttribute(Attributes.ARMOR).setBaseValue(0);
                for(long clock:new long[]{6000,12999,13000,18000,23000}) {
                    test.getLevel().setDayTime(clock);target.setHealth(20);target.invulnerableTime=0;
                    target.hurt(player.damageSources().playerAttack(player),10);
                    test.assertTrue(Math.abs(target.getHealth()-(clock>=13000 && clock<23000?9:10))<.001,"Night boundary damage incorrect");
                }
                test.getLevel().setDayTime(18000);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);target.setHealth(20);target.invulnerableTime=0;
                target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(target.getHealth()==10,"Fresh dagger gains night bonus");
            }
        } finally {test.getLevel().setDayTime(time);player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==5,"Skipped Sleepwalker variants");test.succeed();
    }
}
