package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.EnderSwingAbility;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EnderSwingGameTests {
    private static Zombie zombie(GameTestHelper test, Vec3 pos) {
        Zombie zombie=EntityType.ZOMBIE.create(test.getLevel());
        zombie.setNoAi(true); zombie.setNoGravity(true);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
        zombie.setHealth(100); zombie.setPos(pos);
        test.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    @GameTest(template="riftfang_test")
    public static void inanisiumAxesLearnEnderSwing(GameTestHelper test) {
        int count=0;
        for(var item: BuiltInRegistries.ITEM) {
            if(!(item instanceof InanisiumWeapon weapon) || !weapon.isAxe()) continue;
            count++;
            ItemStack fresh=new ItemStack(item);
            test.assertFalse(Augmentations.ids(fresh,true).contains(RecommendedAbilities.ENDER_SWING),"Fresh axe has Ender Swing");
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(RecommendedAbilities.ENDER_SWING)),
                    "Inanisium axe variant cannot learn Ender Swing");
        }
        test.assertTrue(count==5,"Not all five Inanisium axe variants were checked");
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void activationImmediatelyHitsOnlyNearbyEnemiesAndLiftsThroughClearSpace(GameTestHelper test) throws Exception {
        ServerPlayer player=TestPlayers.survival(test);
        player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        BlockPos feet=player.blockPosition();
        for(int x=-3;x<=3;x++) for(int z=-3;z<=3;z++) for(int y=0;y<=23;y++)
            test.getLevel().setBlockAndUpdate(feet.offset(x,y,z),Blocks.AIR.defaultBlockState());
        ItemStack fresh=new ItemStack(ModItems.NULLIFIED_AXE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
        test.assertFalse(EnderSwingAbility.activate(player,fresh),"Fresh axe activates Ender Swing");
        ItemStack stack=Augmentations.apply(fresh,RecommendedAbilities.ENDER_SWING);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
        Zombie lifted=zombie(test,player.position().add(.8,0,0));
        Zombie blocked=zombie(test,player.position().add(-.8,0,0));
        Zombie outside=zombie(test,player.position().add(1.5,0,0));
        Zombie ally=zombie(test,player.position().add(0,0,.8));
        var team=player.getScoreboard().addPlayerTeam("ender_"+player.getUUID().toString().substring(0,8));
        player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
        player.getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        var obstruction=BlockPos.containing(blocked.position()).above(4);
        test.getLevel().setBlockAndUpdate(obstruction,Blocks.STONE.defaultBlockState());
        double liftedY=lifted.getY(),blockedY=blocked.getY();
        float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        AbilityTestPackets.use(player,0);
        test.assertFalse(net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.active(player,RecommendedAbilities.ENDER_SWING),"Instant swing leaves an armed state");
        test.assertTrue(Math.abs(lifted.getHealth()-(100-damage))<.001 && Math.abs(lifted.getY()-(liftedY+20))<.001,
                "Nearby enemy was not struck and lifted exactly twenty blocks");
        test.assertTrue(Math.abs(blocked.getHealth()-(100-damage))<.001 && Math.abs(blocked.getY()-blockedY)<.001,
                "Blocked enemy was not damaged or passed through a ceiling");
        test.assertTrue(outside.getHealth()==100 && ally.getHealth()==100 && player.getHealth()==20,
                "Swing damaged an outside target, ally or wielder");
        WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.ENDER_SWING;
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+1200,
                "Sixty-second cooldown did not start after the swing");
        test.getLevel().setBlockAndUpdate(obstruction,Blocks.AIR.defaultBlockState());
        player.getScoreboard().removePlayerTeam(team);
        lifted.discard();blocked.discard();outside.discard();ally.discard();test.getLevel().getServer().getPlayerList().remove(player);
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void allVariantsStartCooldownImmediatelyWithoutOtherAugmentsOrTargets(GameTestHelper test) throws Exception {
        ServerPlayer player=TestPlayers.survival(test);player.setPos(test.getBounds().getCenter());
        try {
            for(var item:java.util.List.of(ModItems.NULLIFIED_AXE.get(),ModItems.NULLIFIED_AXE_RUBY.get(),ModItems.NULLIFIED_AXE_AMBER.get(),ModItems.NULLIFIED_AXE_AMETHYST.get(),ModItems.NULLIFIED_AXE_JADE.get())) {
                ItemStack stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.ENDER_SWING);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                test.assertTrue(Augmentations.learned(stack).size()==1,"Fixture has another augment");
                AbilityTestPackets.use(player,0);
                String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.ENDER_SWING;
                long ready=player.level().getGameTime()+1200;
                test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==ready,"Cooldown does not begin immediately without a followup attack");
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);AbilityTestPackets.use(player,0);
                test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)==ready,"Switching or repeated activation changes cooldown");
                test.assertFalse(net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.active(player,RecommendedAbilities.ENDER_SWING),"Swing remains armed");
            }
        } finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void tertiarySwingKeepsItsCooldownEvenIfTheWeaponBreaks(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var stack=new ItemStack(ModItems.NULLIFIED_AXE.get());
        for(var id:java.util.List.of(RecommendedAbilities.NULL_RECEIPT,RecommendedAbilities.SWING_ATTACK,RecommendedAbilities.ENDER_SWING))stack=Augmentations.apply(stack,id);
        stack.setDamageValue(stack.getMaxDamage()-1);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
        var target=zombie(test,player.position().add(.8,0,0));
        try {
            String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.ENDER_SWING;
            AbilityTestPackets.use(player,2);
            test.assertTrue(target.getHealth()<100 && stack.isEmpty(),"Tertiary activation does not strike immediately or break the last-durability axe");
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+1200,"Breaking the axe loses its instant cooldown");
            var replacement=Augmentations.apply(new ItemStack(ModItems.NULLIFIED_AXE.get()),RecommendedAbilities.ENDER_SWING);player.setItemInHand(InteractionHand.MAIN_HAND,replacement);target.setPos(player.position().add(.8,0,0));target.invulnerableTime=0;float health=target.getHealth();AbilityTestPackets.use(player,0);
            test.assertTrue(target.getHealth()==health,"Replacement axe bypasses cooldown");
        } finally {target.discard();WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }

}
