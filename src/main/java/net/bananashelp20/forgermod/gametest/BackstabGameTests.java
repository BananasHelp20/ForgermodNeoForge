package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
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
public class BackstabGameTests {
    @GameTest(template="riftfang_test")
    public static void everyVariantBackstabsPassiveAndTamedMobsWithApprovedWhitePercentage(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=test.getBounds().getCenter();int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                if(Augmentations.pool(fresh).stream().noneMatch(a->a.id().equals(AdditionalAbilities.BACKSTAB)))continue;
                count++;var learned=Augmentations.apply(fresh,AdditionalAbilities.BACKSTAB);player.setItemInHand(InteractionHand.MAIN_HAND,learned);
                for(var type:java.util.List.of(EntityType.COW,EntityType.WOLF)) {
                    var target=type.create(test.getLevel());target.setPos(origin);target.setYRot(0);target.getAttribute(Attributes.ARMOR).setBaseValue(0);
                    if(target instanceof net.minecraft.world.entity.animal.Wolf wolf)wolf.tame(player);
                    target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
                    player.setPos(origin.add(0,0,-2));target.setHealth(20);target.invulnerableTime=0;
                    target.hurt(player.damageSources().playerAttack(player),10);
                    test.assertTrue(Math.abs(target.getHealth()-8)<.001,"Passive/tamed mob does not receive20% rear bonus");
                }
                for(boolean compact:new boolean[]{false,true}) {
                    var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(learned,AdditionalAbilities.BACKSTAB,1,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    test.assertTrue(text.getString().equals("Deal 20% more damage from behind.") && white.toString().equals("20%"),"Wrong Backstab text/highlighting");
                }
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==40,"Skipped Backstab variant");test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_backstab_target_scope")
    public static void alliedPlayersReceiveBonusWithoutBroadeningOtherDamagePassives(GameTestHelper test) {
        var player=TestPlayers.survival(test);var ally=TestPlayers.survival(test);var origin=test.getBounds().getCenter();
        var scoreboard=player.getScoreboard();String name=player.getScoreboardName(),allyName=ally.getScoreboardName();
        var old=scoreboard.getPlayersTeam(name);var oldAlly=scoreboard.getPlayersTeam(allyName);
        var team=scoreboard.addPlayerTeam("back"+java.util.UUID.randomUUID().toString().substring(0,10));team.setAllowFriendlyFire(true);
        scoreboard.addPlayerToTeam(name,team);scoreboard.addPlayerToTeam(allyName,team);
        boolean pvp=player.server.isPvpAllowed();player.server.setPvpAllowed(true);long day=test.getLevel().getDayTime();test.getLevel().setDayTime(18000);
        try {
            var stack=Augmentations.apply(new ItemStack(ModItems.NIGHTMARE_DAGGER.get()),AdditionalAbilities.BACKSTAB);
            stack=Augmentations.apply(stack,AdditionalAbilities.SLEEPWALKER);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setPos(origin.add(0,0,-2));ally.setPos(origin);ally.setYRot(0);
            test.assertTrue(player.isAlliedTo(ally),"Player fixture is not allied");
            ally.setHealth(20);ally.invulnerableTime=0;ally.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(Math.abs(ally.getHealth()-8)<.001,"Allied player misses Backstab bonus or incorrectly gains Sleepwalker");
            var cow=EntityType.COW.create(test.getLevel());cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);cow.setPos(origin);cow.setYRot(0);cow.setHealth(20);cow.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(Math.abs(cow.getHealth()-8)<.001,"Other damage passive now affects nonhostile cow");
            var hostile=EntityType.ZOMBIE.create(test.getLevel());hostile.setPos(origin);hostile.setYRot(0);hostile.getAttribute(Attributes.ARMOR).setBaseValue(0);hostile.setHealth(20);hostile.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(Math.abs(hostile.getHealth()-7.28)<.001,"Enemy no longer receives existing Sleepwalker and Backstab combination");
        } finally {
            player.server.setPvpAllowed(pvp);test.getLevel().setDayTime(day);scoreboard.removePlayerTeam(team);
            if(old!=null)scoreboard.addPlayerToTeam(name,old);if(oldAlly!=null)scoreboard.addPlayerToTeam(allyName,oldAlly);
            player.server.getPlayerList().remove(ally);player.server.getPlayerList().remove(player);
        }
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void fortyMarkedDaggersRewardRearHitsOnly(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=test.getBounds().getCenter();int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                boolean eligible=Augmentations.eligible(fresh) && Augmentations.canonical(fresh).isDagger()
                        && !(entry.get() instanceof net.bananashelp20.forgermod.item.custom.IgnisiumWeapon);
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.BACKSTAB))==eligible,"Incorrect Backstab weapon assignment");
                if(!eligible) continue;checked++;
                var learned=Augmentations.apply(fresh,AdditionalAbilities.BACKSTAB);
                test.assertTrue(Augmentations.apply(learned,AdditionalAbilities.BACKSTAB).isEmpty(),"Backstab offers identical upgrades");
                var target=EntityType.ZOMBIE.create(test.getLevel());target.setPos(origin);target.setYRot(0);target.getAttribute(Attributes.ARMOR).setBaseValue(0);
                for(var offset:new Vec3[]{new Vec3(0,0,-2),new Vec3(0,0,2),new Vec3(2,0,0)}) {
                    player.setPos(origin.add(offset));player.setItemInHand(InteractionHand.MAIN_HAND,learned);target.setHealth(20);target.invulnerableTime=0;
                    target.hurt(player.damageSources().playerAttack(player),10);
                    test.assertTrue(Math.abs(target.getHealth()-(offset.z<0?8:10))<.001,"Rear/front/side damage is incorrect");
                }
                player.setPos(origin.add(0,0,-2));player.setItemInHand(InteractionHand.MAIN_HAND,fresh);target.setHealth(20);target.invulnerableTime=0;
                target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(target.getHealth()==10,"Unlearned Backstab grants bonus");
                player.setItemInHand(InteractionHand.MAIN_HAND,learned);target.setHealth(20);target.invulnerableTime=0;
                target.hurt(player.damageSources().indirectMagic(player,player),10);test.assertTrue(target.getHealth()==10,"Secondary damage gains Backstab");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==40,"Skipped Backstab family or gemstone");test.succeed();
    }
}
