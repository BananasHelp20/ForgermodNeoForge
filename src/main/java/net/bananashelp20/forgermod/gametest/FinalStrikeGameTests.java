package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.VulnusiumWeapon;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FinalStrikeGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsBonusOnlyLastEnemyStrictlyBelowThreshold(GameTestHelper test) {
        var player=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);var other=EntityType.ZOMBIE.create(test.getLevel());other.setNoAi(true);other.getAttribute(Attributes.ARMOR).setBaseValue(0);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof VulnusiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.FINAL_STRIKE))==eligible,"Wrong Final Strike pool");if(!eligible) continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);mob.setHealth(1);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),.2F);test.assertTrue(Math.abs(mob.getHealth()-.8)<.0001,"Fresh weapon gets bonus");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.FINAL_STRIKE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);mob.setHealth(1);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),.2F);test.assertTrue(Math.abs(mob.getHealth()-.8)<.0001,"First hit gets last-enemy bonus");
            mob.setHealth(1);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),.2F);test.assertTrue(Math.abs(mob.getHealth()-.7)<.0001,"Last enemy below threshold lacks 50 percent bonus");
            mob.setHealth(2);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),.2F);test.assertTrue(Math.abs(mob.getHealth()-1.8)<.0001,"Exactly 10 percent gets bonus");
            other.setHealth(1);other.invulnerableTime=0;other.hurt(player.damageSources().playerAttack(player),.2F);mob.setHealth(1);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),.2F);test.assertTrue(Math.abs(mob.getHealth()-.8)<.0001,"Old target retains bonus after hitting another");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());mob.setHealth(1);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),.2F);test.assertTrue(Math.abs(mob.getHealth()-.8)<.0001,"Switch retains last target");
        }} finally {mob.discard();other.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
