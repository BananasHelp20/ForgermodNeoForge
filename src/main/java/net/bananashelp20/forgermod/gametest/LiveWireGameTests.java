package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LiveWireGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveDaggersReflectOnlySuccessfulMeleeAndCancel(GameTestHelper test) {
        var player=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.LIVE_WIRE))==eligible,"Wrong Live Wire assignment");
                if(!eligible) continue;count++;mob.setHealth(20);mob.invulnerableTime=0;player.setHealth(20);player.invulnerableTime=0;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.LIVE_WIRE),"Fresh dagger reflects");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.LIVE_WIRE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LIVE_WIRE),"Learned reflection rejected");
                test.assertTrue(player.hurt(player.damageSources().mobAttack(mob),2),"Melee hit rejected");test.assertTrue(mob.getHealth()==18,"Reflection does not deal exactly two damage");
                player.invulnerableTime=0;player.hurt(player.damageSources().indirectMagic(mob,mob),2);test.assertTrue(mob.getHealth()==18,"Magic hit reflects recursively");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(LiveWireEvents.active(player),"Switch retains Live Wire");
                player.invulnerableTime=0;player.hurt(player.damageSources().mobAttack(mob),2);test.assertTrue(mob.getHealth()==18,"Cancelled reflection persists");
            }
        } finally {LiveWireEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Live Wire variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=220)
    public static void reflectionExpiresAfterEightSeconds(GameTestHelper test) {
        var player=TestPlayers.survival(test);var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.LIVE_WIRE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LIVE_WIRE),"Timed reflection rejected");
        test.runAfterDelay(160,()->{try {test.assertFalse(LiveWireEvents.active(player),"Reflection outlasts eight seconds");test.succeed();}finally {LiveWireEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}});
    }
}
