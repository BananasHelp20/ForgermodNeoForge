package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CharredEdgeGameTests {
    @GameTest(template="riftfang_test")
    public static void resistantEnemiesReceiveExactlyFifteenPercent(GameTestHelper test) {
        var player=TestPlayers.survival(test);int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                boolean eligible=entry.get() instanceof IgnisiumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.CHARRED_EDGE))==eligible,"Incorrect Charred Edge assignment");
                if(!eligible) continue;checked++;
                var learned=Augmentations.apply(fresh,AdditionalAbilities.CHARRED_EDGE);
                test.assertTrue(Augmentations.apply(learned,AdditionalAbilities.CHARRED_EDGE).isEmpty(),"Fixed passive offers redundant upgrades");
                var target=EntityType.ZOMBIE.create(test.getLevel());target.getAttribute(Attributes.ARMOR).setBaseValue(0);
                target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100,0));
                player.setItemInHand(InteractionHand.MAIN_HAND,learned);
                target.hurt(player.damageSources().playerAttack(player),10);
                test.assertTrue(Math.abs(target.getHealth()-8.5F)<.0001,"Bonus is not fifteen percent");
                target.setHealth(20);target.invulnerableTime=0;target.removeAllEffects();
                target.hurt(player.damageSources().playerAttack(player),10);
                test.assertTrue(target.getHealth()==10,"Bonus applied without Fire Resistance");
                target.setHealth(20);target.invulnerableTime=0;target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100,0));
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                target.hurt(player.damageSources().playerAttack(player),10);
                test.assertTrue(target.getHealth()==10,"Fresh weapon gets passive bonus");
                target.setHealth(20);target.invulnerableTime=0;player.setItemInHand(InteractionHand.MAIN_HAND,learned);
                target.hurt(player.damageSources().indirectMagic(player,player),10);
                test.assertTrue(target.getHealth()==10,"Secondary damage recursively gains melee bonus");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==5,"Skipped gemstone variants");test.succeed();
    }
}
