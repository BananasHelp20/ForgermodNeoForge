package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.LonelyDreamEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LonelyDreamGameTests {
    @GameTest(template="riftfang_test")
    public static void nearbyEnemiesScaleSpeedAndSwitchRestoresIt(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        var a=EntityType.ZOMBIE.create(test.getLevel());a.setNoAi(true);a.setNoGravity(true);a.setPos(point.add(0,0,3));test.getLevel().addFreshEntity(a);
        var b=EntityType.ZOMBIE.create(test.getLevel());b.setNoAi(true);b.setNoGravity(true);b.setPos(point.add(3,0,0));test.getLevel().addFreshEntity(b);
        var neutral=EntityType.COW.create(test.getLevel());neutral.setNoAi(true);neutral.setPos(point.add(-2,0,0));test.getLevel().addFreshEntity(neutral);
        int checked=0;double baseline=player.getAttributeValue(Attributes.ATTACK_SPEED);
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(x->x.id().equals(AdditionalAbilities.LONELY_DREAM))==eligible,"Wrong Lonely Dream assignment");
                if(!eligible) continue;checked++;b.setPos(point.add(3,0,0));player.setItemInHand(InteractionHand.MAIN_HAND,fresh);LonelyDreamEvents.refresh(player);
                test.assertTrue(player.getAttributeValue(Attributes.ATTACK_SPEED)==baseline,"Fresh weapon gains speed");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.LONELY_DREAM);player.setItemInHand(InteractionHand.MAIN_HAND,stack);LonelyDreamEvents.refresh(player);
                test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline*1.1)<.0001,"Two enemies do not grant ten percent speed");
                b.setPos(point.add(0,0,12));LonelyDreamEvents.refresh(player);
                test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline*1.05)<.0001,"Outside enemy still contributes");
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);LonelyDreamEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                test.assertTrue(player.getAttributeValue(Attributes.ATTACK_SPEED)==baseline,"Switch leaves speed modifier");
            }
        } finally {LonelyDreamEvents.clear(player);a.discard();b.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==5,"Skipped Lonely Dream variants");test.succeed();
    }
}
