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
    public static void fiveDaggersScaleAcrossFiveRanksOnlyAtNight(GameTestHelper test) {
        var player=TestPlayers.survival(test);long time=test.getLevel().getDayTime();int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.SLEEPWALKER))==eligible,"Wrong Sleepwalker assignment");
                if(!eligible) continue;checked++;String id=AdditionalAbilities.SLEEPWALKER;var stack=fresh;
                var target=EntityType.ZOMBIE.create(test.getLevel());target.getAttribute(Attributes.ARMOR).setBaseValue(0);
                for(int rank=1;rank<=5;rank++) {
                stack=Augmentations.apply(stack,id);test.assertTrue(!stack.isEmpty() && Augmentations.level(stack,id)==rank,"Cannot upgrade Sleepwalker");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                for(long clock:new long[]{6000,12999,13000,18000,23000}) {
                    test.getLevel().setDayTime(clock);target.setHealth(20);target.invulnerableTime=0;
                    target.hurt(player.damageSources().playerAttack(player),10);
                    test.assertTrue(Math.abs(target.getHealth()-(clock>=13000 && clock<23000?10-.6F*rank:10))<.001,"Night boundary damage incorrect");
                }
                for(boolean compact:new boolean[]{false,true}) {
                    var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(stack,id,rank,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    String phrase=(6*rank)+"% more melee damage";
                    test.assertTrue(text.getString().equals("Deal "+phrase+" at night.") && white.toString().equals(phrase),"Wrong current description/highlights");
                }
                var changes=net.bananashelp20.forgermod.augmentation.AugmentUpgradeDescriptions.changes(stack,id,rank);
                test.assertTrue(rank==5?changes.isEmpty():changes.size()==1 && changes.getFirst().property().equals("damage_bonus") && ((Number)changes.getFirst().after()).intValue()==6*(rank+1),"Wrong next-rank bonus preview");
                }
                test.assertTrue(Augmentations.apply(stack,id).isEmpty() && !Augmentations.hasSuperForm(stack,id),"Sleepwalker exceeds V or becomes super");
                test.getLevel().setDayTime(18000);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);target.setHealth(20);target.invulnerableTime=0;
                target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(target.getHealth()==10,"Fresh dagger gains night bonus");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);target.setHealth(20);target.invulnerableTime=0;
                target.hurt(player.damageSources().indirectMagic(player,player),10);test.assertTrue(target.getHealth()==10,"Separate magic gains night bonus");
                target.discard();
            }
        } finally {test.getLevel().setDayTime(time);player.server.getPlayerList().remove(player);}
        for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get())) {
            var template=new ItemStack(item);String id=AdditionalAbilities.SLEEPWALKER;
            test.assertTrue(Augmentations.maxRank(template,id)==5 && !Augmentations.setLevel(template,id,5).isEmpty() && Augmentations.setLevel(template,id,6).isEmpty(),"Template bypasses V cap");
        }
        test.assertTrue(checked==5,"Skipped Sleepwalker variants");test.succeed();
    }
}
