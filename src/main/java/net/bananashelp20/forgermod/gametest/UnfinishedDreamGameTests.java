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
public class UnfinishedDreamGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenWeaponsHealOnlyForEnemiesThatNeverDamagedOwner(GameTestHelper test) {
        var player=TestPlayers.survival(test);int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.UNFINISHED_DREAM))==eligible,"Wrong Unfinished Dream assignment");
                if(!eligible) continue;checked++;var stack=Augmentations.apply(fresh,AdditionalAbilities.UNFINISHED_DREAM);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                var untouched=EntityType.ZOMBIE.create(test.getLevel());untouched.getAttribute(Attributes.ARMOR).setBaseValue(0);player.setHealth(10);
                untouched.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==11,"Clean kill does not heal exactly half a heart");
                var attacker=EntityType.ZOMBIE.create(test.getLevel());attacker.getAttribute(Attributes.ARMOR).setBaseValue(0);player.invulnerableTime=0;player.setHealth(10);
                player.hurt(player.damageSources().mobAttack(attacker),1);float injured=player.getHealth();attacker.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==injured,"Enemy that injured owner still heals on kill");
                var absorbed=EntityType.ZOMBIE.create(test.getLevel());absorbed.getAttribute(Attributes.ARMOR).setBaseValue(0);player.invulnerableTime=0;player.setHealth(10);player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,100,0));player.setAbsorptionAmount(4);
                player.hurt(player.damageSources().mobAttack(absorbed),1);absorbed.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==10,"Absorbed hit treated as untouched enemy");player.removeEffect(net.minecraft.world.effect.MobEffects.ABSORPTION);player.setAbsorptionAmount(0);
                var unlearned=EntityType.ZOMBIE.create(test.getLevel());unlearned.getAttribute(Attributes.ARMOR).setBaseValue(0);player.setHealth(10);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                unlearned.hurt(player.damageSources().playerAttack(player),100);test.assertTrue(player.getHealth()==10,"Fresh gear heals on kill");
                var neutral=EntityType.COW.create(test.getLevel());player.setItemInHand(InteractionHand.MAIN_HAND,stack);neutral.hurt(player.damageSources().playerAttack(player),100);
                test.assertTrue(player.getHealth()==10,"Neutral kill heals owner");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==15,"Skipped Somnium family or gemstone");test.succeed();
    }
}
