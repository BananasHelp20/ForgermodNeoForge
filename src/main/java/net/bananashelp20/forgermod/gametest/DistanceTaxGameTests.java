package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.DistanceTaxEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DistanceTaxGameTests {
    @GameTest(template="riftfang_test",timeoutTicks=250)
    public static void freshWeaponsCannotMarkAndLogoutCancelsExistingTax(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setInvulnerable(true); player.setPos(test.getBounds().getCenter());
        var fresh=new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
        var unlearned=enemy(test,player); unlearned.hurt(player.damageSources().playerAttack(player),1);
        var stack=Augmentations.apply(fresh,RecommendedAbilities.DISTANCE_TAX); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var canceled=enemy(test,player); canceled.hurt(player.damageSources().playerAttack(player),1); player.setPos(canceled.position().add(20,0,0));
        DistanceTaxEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
        test.runAfterDelay(201,()->{
            DistanceTaxEvents.onTick(new PlayerTickEvent.Post(player));
            test.assertTrue(unlearned.getHealth()==999 && canceled.getHealth()==999,"Fresh gear marks or logout retains tax");
            unlearned.discard(); canceled.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
        });
    }
    private static Zombie enemy(GameTestHelper test,ServerPlayer player) {
        var enemy=EntityType.ZOMBIE.create(test.getLevel()); enemy.setNoAi(true); enemy.setNoGravity(true);
        enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); enemy.setHealth(1000); enemy.getAttribute(Attributes.ARMOR).setBaseValue(0);
        enemy.setPos(player.position().add(0,0,1)); return enemy;
    }
    @GameTest(template="riftfang_test",timeoutTicks=450)
    public static void allVariantsPayOnceAfterOriginalTenSecondDeadline(GameTestHelper test) {
        var players=new ArrayList<ServerPlayer>(); var enemies=new ArrayList<Zombie>();
        for(Item item:new Item[]{ModItems.CLAYMORE_OF_THE_VOID.get(),ModItems.CLAYMORE_OF_THE_VOID_RUBY.get(),ModItems.CLAYMORE_OF_THE_VOID_AMBER.get(),
                ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get(),ModItems.CLAYMORE_OF_THE_VOID_JADE.get()}) {
            var player=TestPlayers.survival(test); player.setNoGravity(true); player.setInvulnerable(true); player.setPos(test.getBounds().getCenter());
            var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.DISTANCE_TAX); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var enemy=enemy(test,player); enemy.hurt(player.damageSources().playerAttack(player),1);
            player.setPos(enemy.position().add(20,0,0)); players.add(player); enemies.add(enemy);
        }
        test.runAfterDelay(100,()->{
            for(int i=0;i<players.size();i++) { var enemy=enemies.get(i); enemy.invulnerableTime=0; enemy.hurt(players.get(i).damageSources().playerAttack(players.get(i)),1); }
        });
        test.runAfterDelay(199,()->{ for(var player:players) DistanceTaxEvents.onTick(new PlayerTickEvent.Post(player)); for(var enemy:enemies) test.assertTrue(enemy.getHealth()==998,"Tax pays before ten seconds"); });
        test.runAfterDelay(201,()->{
            for(var player:players) DistanceTaxEvents.onTick(new PlayerTickEvent.Post(player));
            for(var enemy:enemies) test.assertTrue(Math.abs(enemy.getHealth()-988)<.001,"Tax resets deadline or uses wrong distance damage: health="+enemy.getHealth());
        });
        test.runAfterDelay(403,()->{
            for(var player:players) DistanceTaxEvents.onTick(new PlayerTickEvent.Post(player));
            for(int i=0;i<players.size();i++) {
                test.assertTrue(enemies.get(i).getHealth()==988,"Tax recursively marks/pays twice");
                enemies.get(i).discard(); test.getLevel().getServer().getPlayerList().remove(players.get(i));
            }
            test.succeed();
        });
    }
    @GameTest(template="riftfang_test",timeoutTicks=250)
    public static void maxRankCapTargetReplacementAndSwitchCancellation(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setInvulnerable(true); player.setPos(test.getBounds().getCenter());
        ItemStack stack=new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()); for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.DISTANCE_TAX);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var old=enemy(test,player); var latest=enemy(test,player); old.hurt(player.damageSources().playerAttack(player),1); latest.hurt(player.damageSources().playerAttack(player),1);
        player.setPos(latest.position().add(100,0,0));
        var switched=TestPlayers.survival(test); switched.setNoGravity(true); switched.setInvulnerable(true); switched.setPos(test.getBounds().getCenter());
        var switchedStack=Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()),RecommendedAbilities.DISTANCE_TAX); switched.setItemInHand(InteractionHand.MAIN_HAND,switchedStack);
        var canceled=enemy(test,switched); canceled.hurt(switched.damageSources().playerAttack(switched),1);
        switched.setItemInHand(InteractionHand.MAIN_HAND,switchedStack.copy()); DistanceTaxEvents.onTick(new PlayerTickEvent.Post(switched));
        test.assertTrue(DistanceTaxEvents.damage(10,1)==5 && DistanceTaxEvents.damage(10,2)==7.5F && DistanceTaxEvents.damage(10,3)==10 && DistanceTaxEvents.damage(10,4)==12.5F,"Rank conversion wrong");
        test.runAfterDelay(201,()->{
            DistanceTaxEvents.onTick(new PlayerTickEvent.Post(player)); DistanceTaxEvents.onTick(new PlayerTickEvent.Post(switched));
            test.assertTrue(old.getHealth()==999 && latest.getHealth()==959,"Wrong target or max damage exceeds 40: old="+old.getHealth()+" latest="+latest.getHealth()+" distance="+player.distanceTo(latest)+" held="+Augmentations.ids(player.getMainHandItem(),false)+" alive="+player.isAlive());
            test.assertTrue(canceled.getHealth()==999,"Switch fails to cancel tax");
            old.discard(); latest.discard(); canceled.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.getLevel().getServer().getPlayerList().remove(switched); test.succeed();
        });
    }
}
