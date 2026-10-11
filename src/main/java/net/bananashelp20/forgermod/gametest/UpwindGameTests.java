package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class UpwindGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsLaunchNextMeleeOnceAfterNormalKnockback(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setPos(origin.add(0,0,2));int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.UPWIND))==eligible,"Wrong Upwind assignment");
                if(!eligible) continue;count++;mob.setHealth(20);mob.invulnerableTime=0;mob.setOnGround(true);mob.setDeltaMovement(Vec3.ZERO);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.UPWIND),"Fresh weapon arms gust");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.UPWIND);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.UPWIND),"Learned gust rejected");
                mob.hurt(player.damageSources().indirectMagic(player,player),1);UpwindEvents.finish(new ServerTickEvent.Post(()->true,player.server));test.assertTrue(UpwindEvents.active(player),"Spell consumes melee gust");
                mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);UpwindEvents.finish(new ServerTickEvent.Post(()->true,player.server));
                test.assertTrue(mob.getHealth()==19 && Math.abs(mob.getDeltaMovement().y-.6)<.0001 && mob.getDeltaMovement().z>0,"Gust fails to launch after normal hit or adds damage");
                test.assertFalse(UpwindEvents.active(player),"Successful hit does not consume gust");var motion=mob.getDeltaMovement();UpwindEvents.finish(new ServerTickEvent.Post(()->true,player.server));test.assertTrue(mob.getDeltaMovement().equals(motion),"Gust repeats after consumption");
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.UPWIND),"Rearm rejected");player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(UpwindEvents.active(player),"Switch retains gust");
            }
        } finally {UpwindEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Upwind variants");test.succeed();
    }
}
