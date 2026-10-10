package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.DreamEchoEntity;
import net.bananashelp20.forgermod.item.custom.abilities.FalseAwakeningEvents;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FalseAwakeningGameTests {
    @GameTest(template="riftfang_test")
    public static void wallsSwitchLethalHitAndLogoutLeaveNoEcho(GameTestHelper test) throws Exception {
        var player=player(test); var stack=Augmentations.apply(new ItemStack(ModItems.NIGHTMARE_DAGGER.get()),RecommendedAbilities.FALSE_AWAKENING);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); var real=enemy(test,player,0,4); real.setTarget(player);
        var wall=player.blockPosition().south(2).above(); test.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState()); AbilityTestPackets.use(player,0);
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.FALSE_AWAKENING;
        test.assertTrue(!FalseAwakeningEvents.active(player) && !player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Wall allows marking or consumes cooldown");
        test.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState()); AbilityTestPackets.use(player,0); var echo=real.getTarget();
        test.assertTrue(echo instanceof DreamEchoEntity,"Unblocked activation fails");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(echo.isRemoved() && real.getTarget()==player && !FalseAwakeningEvents.active(player),"Switch leaves echo/AI state");
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+600,"Switch fails to start cooldown");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.FALSE_AWAKENING);
        var lethalEcho=real.getTarget(); real.setHealth(1); real.hurt(player.damageSources().playerAttack(player),10);
        test.assertTrue(lethalEcho.isRemoved() && !FalseAwakeningEvents.active(player),"Lethal real hit leaves echo");
        var next=enemy(test,player,0,5); next.setTarget(player);
        net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.FALSE_AWAKENING); var logoutEcho=next.getTarget();
        test.assertTrue(logoutEcho instanceof DreamEchoEntity,"Logout fixture cannot mark next target");
        test.getLevel().getServer().getPlayerList().remove(player);
        test.assertTrue(logoutEcho.isRemoved() && next.getTarget()==player,"Logout retains echo or fails target restoration");
        real.discard(); next.discard(); test.succeed();
    }
    private static ServerPlayer player(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setInvulnerable(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
        for(int x=-4;x<=4;x++) for(int y=0;y<=3;y++) for(int z=-2;z<=10;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        return player;
    }
    private static Zombie enemy(GameTestHelper test,ServerPlayer player,double x,double z) {
        var enemy=EntityType.ZOMBIE.create(test.getLevel()); enemy.setNoAi(true); enemy.setNoGravity(true);
        enemy.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN)); enemy.getAttribute(Attributes.ARMOR).setBaseValue(0);
        enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); enemy.setHealth(1000); enemy.setPos(player.position().add(x,0,z)); test.getLevel().addFreshEntity(enemy); return enemy;
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsCreateProtectedEchoAttractAIAndPayOneRealHitBonus(GameTestHelper test) throws Exception {
        for(Item item:new Item[]{ModItems.NIGHTMARE_DAGGER.get(),ModItems.NIGHTMARE_DAGGER_RUBY.get(),ModItems.NIGHTMARE_DAGGER_AMBER.get(),
                ModItems.NIGHTMARE_DAGGER_AMETHYST.get(),ModItems.NIGHTMARE_DAGGER_JADE.get()}) {
            var player=player(test); var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            AbilityTestPackets.use(player,0); test.assertFalse(FalseAwakeningEvents.active(player),"Fresh dagger activates dream");
            ItemStack stack=fresh; for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.FALSE_AWAKENING);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var real=enemy(test,player,0,4); real.setTarget(player); var nearby=enemy(test,player,3,5);
            AbilityTestPackets.use(player,0); test.assertTrue(FalseAwakeningEvents.active(player),"Dream cannot activate");
            test.assertTrue(real.getTarget() instanceof DreamEchoEntity && nearby.getTarget()==real.getTarget(),"Hostile attention does not move to echo");
            var echo=(DreamEchoEntity)real.getTarget();
            test.assertTrue(echo.isInvisible() && !echo.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && !echo.shouldBeSaved(),"Echo lacks visible silhouette or is persistent");
            test.assertFalse(echo.hurt(player.damageSources().playerAttack(player),10),"Echo can be destroyed/looted");
            test.assertTrue(echo.interactAt(player,Vec3.ZERO,InteractionHand.MAIN_HAND)==InteractionResult.FAIL,"Echo equipment can be taken");
            test.assertTrue(FalseAwakeningEvents.active(player),"Hitting echo consumes real-target payoff");
            real.setInvulnerable(true); real.hurt(player.damageSources().playerAttack(player),10); real.setInvulnerable(false);
            test.assertTrue(FalseAwakeningEvents.active(player),"Rejected real hit consumes dream");
            real.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(Math.abs(real.getHealth()-987.5F)<.001,"Real hit lacks rank-IV bonus");
            test.assertTrue(!FalseAwakeningEvents.active(player) && echo.isRemoved() && real.getTarget()==player && nearby.getTarget()==null,"Dream does not clean up/restore AI");
            real.invulnerableTime=0; real.hurt(player.damageSources().playerAttack(player),10);
            test.assertTrue(real.getHealth()==977.5F,"Bonus applies twice");
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            String key=BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.FALSE_AWAKENING;
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+300,"Ending dream fails to start rank-IV cooldown");
            real.discard(); nearby.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=120)
    public static void expiryRemovesEchoAndRestoresPreviousTarget(GameTestHelper test) throws Exception {
        var player=player(test); var stack=Augmentations.apply(new ItemStack(ModItems.NIGHTMARE_DAGGER.get()),RecommendedAbilities.FALSE_AWAKENING);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); var real=enemy(test,player,0,4); real.setTarget(player); AbilityTestPackets.use(player,0);
        var echo=real.getTarget(); test.assertTrue(echo instanceof DreamEchoEntity,"No expiry-test echo");
        test.runAfterDelay(81,()->{
            FalseAwakeningEvents.onTick(new PlayerTickEvent.Post(player)); WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(!FalseAwakeningEvents.active(player) && echo.isRemoved() && real.getTarget()==player,"Expired echo/AI state remains");
            String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.FALSE_AWAKENING;
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+600,"Expiry fails to start full cooldown");
            real.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
        });
    }
}
