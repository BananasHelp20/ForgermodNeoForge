package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FlamebornGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsUseReviewedBaseAndTemplateBurstDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var target=zombie(test,player,4);
        try {
            for(var item:BuiltInRegistries.ITEM) {
                if(!(item instanceof IgnisiumWeapon))continue;
                for(int rank=1;rank<=2;rank++) {
                    var template=Augmentations.setLevel(new ItemStack(ModItems.SUPER_AUGMENT_TEMPLATE.get()),RecommendedAbilities.FLAMEBORN,rank);
                    var stack=Augmentations.merge(new ItemStack(item),template);
                    test.assertFalse(stack.isEmpty(),"Cannot transfer Flameborn rank");
                    player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
                    target.setHealth(100);target.invulnerableTime=0;target.clearFire();
                    float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*(rank==1?.5F:1.5F);
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.FLAMEBORN),"Ranked burst rejected");
                    test.assertTrue(Math.abs(target.getHealth()-(100-damage))<.001 && target.getRemainingFireTicks()==(rank==1?100:140),"Wrong damage or ignition at rank "+rank);
                    net.bananashelp20.forgermod.item.custom.abilities.FlamebornAbility.cancel(player);
                }
            }
        } finally {
            net.bananashelp20.forgermod.item.custom.abilities.FlamebornAbility.cancel(player);target.discard();player.server.getPlayerList().remove(player);
        }
        test.succeed();
    }
    private static Zombie zombie(GameTestHelper test, ServerPlayer player, double offset) {
        Zombie zombie=EntityType.ZOMBIE.create(test.getLevel());
        zombie.setNoAi(true); zombie.setNoGravity(true);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
        zombie.setHealth(100);
        zombie.setPos(player.getX()+offset,player.getY(),player.getZ());
        test.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    @GameTest(template="riftfang_test")
    public static void allIgnisiumShapesCanLearnFlameborn(GameTestHelper test) {
        int count=0;
        for(var item: BuiltInRegistries.ITEM) {
            if(!(item instanceof IgnisiumWeapon)) continue;
            count++;
            ItemStack fresh=new ItemStack(item);
            test.assertFalse(Augmentations.ids(fresh,true).contains(RecommendedAbilities.FLAMEBORN),"Fresh weapon has Flameborn");
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(RecommendedAbilities.FLAMEBORN)),
                    "Ignisium variant cannot learn Flameborn");
        }
        test.assertTrue(count==15,"Not all fifteen Ignisium variants were checked");
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void fireBurstDamagesAndIgnitesAllNearbyEntitiesWithoutTerrainDamage(GameTestHelper test) throws Exception {
        ServerPlayer player=TestPlayers.survival(test);
        player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        ItemStack fresh=new ItemStack(ModItems.MOLTEN_AXE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
        test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,RecommendedAbilities.FLAMEBORN),"Fresh axe activates Flameborn");
        ItemStack stack=Augmentations.apply(fresh,RecommendedAbilities.FLAMEBORN);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        Zombie near=zombie(test,player,8); Zombie far=zombie(test,player,10.5); Zombie ally=zombie(test,player,-3);
        var team=player.getScoreboard().addPlayerTeam("flame_"+player.getUUID().toString().substring(0,8));
        player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
        player.getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);
        cow.setPos(player.getX(),player.getY(),player.getZ()+3);test.getLevel().addFreshEntity(cow);
        var block=player.blockPosition().east(2);
        var before=test.getLevel().getBlockState(block);
        AbilityTestPackets.use(player,0);
        test.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE)
                && player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration()==600,
                "Wielder lacks thirty seconds of Fire Resistance");
        test.assertTrue(player.getRemainingFireTicks()>=100 && near.getRemainingFireTicks()>=100,
                "Wielder and nearby enemy did not ignite");
        test.assertTrue(far.getRemainingFireTicks()<=0 && ally.getRemainingFireTicks()>=100 && cow.getRemainingFireTicks()>=100,
                "Flameborn excludes allied/neutral entities or exceeds its radius");
        float expectedDamage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*.5F;
        test.assertTrue(Math.abs(near.getHealth()-(100-expectedDamage))<.001 && far.getHealth()==100 && ally.getHealth()<100 && cow.getHealth()<10
                && player.getHealth()==20 && before==test.getLevel().getBlockState(block),
                "Burst damage is wrong, harms caster or changes terrain");
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.FLAMEBORN;
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==0,
                "Flameborn started cooldown before its protection ended");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
        net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
        test.assertFalse(player.hasEffect(MobEffects.FIRE_RESISTANCE),"Switching kept Flameborn protection");
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+6000,
                "Five-minute cooldown did not start on switch");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        near.clearFire(); AbilityTestPackets.use(player,0);
        test.assertTrue(near.getRemainingFireTicks()<=0,"Cooldown allows another fire burst");
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,120));
        test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.FlamebornAbility.activate(player,stack),
                "Existing Fire Resistance prevents activation");
        net.bananashelp20.forgermod.item.custom.abilities.FlamebornAbility.cancel(player);
        test.assertTrue(player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration()==120,
                "Canceling Flameborn removed prior Fire Resistance");
        player.getScoreboard().removePlayerTeam(team);
        near.discard();far.discard();ally.discard();cow.discard();test.getLevel().getServer().getPlayerList().remove(player);
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void cooldownBypassRestartsActiveFlamebornAndAllowsImmediateReuse(GameTestHelper test) throws Exception {
        ServerPlayer player=TestPlayers.survival(test);
        player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        ItemStack stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.FLAMEBORN);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        Zombie target=zombie(test,player,4);
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.FLAMEBORN;
        try {
            AbilityTestPackets.use(player,0);
            test.assertTrue(target.getRemainingFireTicks()>0,"First Flameborn burst missed target");
            target.clearFire();
            AbilityTestPackets.use(player,0);
            test.assertFalse(target.getRemainingFireTicks()>0,"Normal active Flameborn was restarted without bypass");

            player.setData(WeaponCooldownAttachments.DISABLED,true);
            AbilityTestPackets.use(player,0);
            test.assertTrue(target.getRemainingFireTicks()>0 && RecommendedAbilityRuntime.active(player,RecommendedAbilities.FLAMEBORN),
                    "Cooldown bypass did not restart active Flameborn");
            test.assertFalse(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Bypass recorded a cooldown");

            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
            net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(
                    new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
            test.assertFalse(RecommendedAbilityRuntime.active(player,RecommendedAbilities.FLAMEBORN),"Switch kept Flameborn active");
            test.assertFalse(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Switch recorded a cooldown despite bypass");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            target.clearFire();
            AbilityTestPackets.use(player,0);
            test.assertTrue(target.getRemainingFireTicks()>0,"Cooldown bypass did not allow immediate reuse after switch");
        } finally {
            net.bananashelp20.forgermod.item.custom.abilities.FlamebornAbility.cancel(player);
            target.discard();
            test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
}
