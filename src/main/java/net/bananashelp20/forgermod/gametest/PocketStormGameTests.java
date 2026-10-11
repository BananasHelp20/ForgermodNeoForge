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
public class PocketStormGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveAxesSmashNearbyEnemyOnceEvenWhenInitialVictimDies(GameTestHelper test) {
        var player=TestPlayers.survival(test);var p=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(p);var other=EntityType.ZOMBIE.create(test.getLevel());other.setNoAi(true);other.setNoGravity(true);other.getAttribute(Attributes.ARMOR).setBaseValue(0);other.setPos(p.add(1,0,2));test.getLevel().addFreshEntity(other);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon w && w.isAxe();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.POCKET_STORM))==eligible,"Wrong storm pool");if(!eligible) continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.POCKET_STORM),"Fresh weapon arms");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.POCKET_STORM);player.setItemInHand(InteractionHand.MAIN_HAND,stack);other.setHealth(20);other.invulnerableTime=0;other.setDeltaMovement(Vec3.ZERO);other.setOnGround(true);
            var initial=EntityType.ZOMBIE.create(test.getLevel());initial.setNoAi(true);initial.getAttribute(Attributes.ARMOR).setBaseValue(0);initial.setPos(p.add(0,0,2));initial.setHealth(1);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.POCKET_STORM),"Storm refused");initial.hurt(player.damageSources().playerAttack(player),2);PocketStormEvents.finish(new ServerTickEvent.Post(()->true,player.server));
            test.assertTrue(other.getHealth()<20 && other.getDeltaMovement().x>0 && other.getDeltaMovement().y>=.3,"Lethal hit does not smash neighbor");test.assertFalse(PocketStormEvents.active(player),"Storm not consumed");float health=other.getHealth();PocketStormEvents.finish(new ServerTickEvent.Post(()->true,player.server));test.assertTrue(other.getHealth()==health,"Storm repeats");initial.discard();
            PocketStormEvents.activate(player,stack);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(PocketStormEvents.active(player),"Switch keeps storm");
        }} finally {PocketStormEvents.cancel(player.getUUID());other.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped variants");test.succeed();
    }
}
