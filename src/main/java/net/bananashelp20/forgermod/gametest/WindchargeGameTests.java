package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.*;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class WindchargeGameTests {
    @GameTest(template="riftfang_test")
    public static void skybreakerSuperIVRetainsLaunchAndUpgradesFallStrike(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setPos(test.getBounds().getCenter());
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setPos(player.position().add(1,0,0));test.getLevel().addFreshEntity(mob);
        try {
            var stack=new ItemStack(ModItems.SKYBREAKER_AXE.get());for(int i=0;i<7;i++)stack=Augmentations.apply(stack,AdditionalAbilities.WINDCHARGE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(WindchargeEvents.activate(player,stack) && WindchargeEvents.active(player),"Super IV flight missing");
            test.assertTrue(Math.abs(player.getDeltaMovement().y-WindchargeEvents.launchVelocity(30))<.001,"Super IV changed launch height");
            player.fallDistance=4;mob.hurt(player.damageSources().playerAttack(player),1);
            test.assertTrue(Math.abs(mob.getHealth()-.8f)<.001 && !WindchargeEvents.active(player),"Super IV fall strike not upgraded or repeated");
        } finally {WindchargeEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void launchesAllVariantsAndSkybreakerSmashesGroundOnce(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);int count=0;
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setPos(origin.add(1,0,0));test.getLevel().addFreshEntity(mob);
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.WINDCHARGE))==eligible,"Wrong Windcharge pool");if(!eligible) continue;count++;
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.WINDCHARGE),"Fresh weapon launches");
                ItemStack stack=fresh;boolean axe=((SwordItemWithEffect)fresh.getItem()).isAxe();
                for(int rank=1;rank<=4;rank++) {
                    stack=Augmentations.apply(stack,AdditionalAbilities.WINDCHARGE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setDeltaMovement(.23,-.4,.37);player.fallDistance=12;
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.WINDCHARGE),"Learned launch refused");var v=player.getDeltaMovement();double expected=axe?new int[]{10,16,23,30}[rank-1]:5*rank,y=0;
                    for(double speed=v.y;speed>0;speed=(speed-.08)*.98) y+=speed;
                    test.assertTrue(Math.abs(y-expected)<.001 && v.x==.23 && v.z==.37 && player.fallDistance==0,"Launch height/momentum wrong");
                    test.assertTrue(Augmentations.abilityName(stack,AdditionalAbilities.WINDCHARGE,rank).getString().equals(axe && rank==4?"Skybreaker":"Windcharge"),"Rank title wrong");
                    if(axe && rank==4) {
                        test.assertTrue(WindchargeEvents.active(player),"Skybreaker flight absent");mob.setHealth(20);mob.invulnerableTime=0;
                        var fall=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,4,1);WindchargeEvents.landing(fall);
                        test.assertTrue(fall.getDamageMultiplier()==0 && mob.getHealth()<20 && !WindchargeEvents.active(player),"Ground smash fails or hurts owner");float health=mob.getHealth();WindchargeEvents.landing(new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,4,1));test.assertTrue(mob.getHealth()==health,"Ground smash repeats");
                        WindchargeEvents.activate(player,stack);player.fallDistance=4;mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);
                        test.assertTrue(mob.getHealth()==5 && !WindchargeEvents.active(player) && player.fallDistance==0,"Falling direct strike misses mace bonus or double-hits victim");
                        WindchargeEvents.activate(player,stack);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(WindchargeEvents.active(player),"Switch keeps smash");
                    }
                }
            }
        } finally {WindchargeEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.assertTrue(WindchargeEvents.fallBonus(3)==12 && WindchargeEvents.fallBonus(8)==22 && WindchargeEvents.fallBonus(30)==44,"Mace damage differs from vanilla");test.succeed();
    }
}
