package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.CinderDecoyEntity;
import net.bananashelp20.forgermod.item.custom.abilities.CinderDecoyEvents;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.lang.reflect.Proxy;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CinderDecoyGameTests {
    @GameTest(template="riftfang_test")
    public static void hostileProjectileDetonatesAndLogoutRemovesReplacement(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        var stack=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.CINDER_DECOY);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.CINDER_DECOY); var decoy=decoy(test,player);
        var skeleton=EntityType.SKELETON.create(test.getLevel()); skeleton.setNoAi(true); skeleton.setNoGravity(true);
        skeleton.setPos(player.position().add(3,0,0)); test.getLevel().addFreshEntity(skeleton);
        // Move the wielder away from its silhouette so the arrow tests the decoy's collision box.
        player.setPos(player.position().add(0,0,6));
        var arrow=EntityType.ARROW.create(test.getLevel()); arrow.setOwner(skeleton); arrow.setNoGravity(true);
        arrow.setPos(decoy.position().add(0,1,-2)); arrow.setDeltaMovement(0,0,3); test.getLevel().addFreshEntity(arrow); arrow.tick();
        test.assertTrue(decoy.isRemoved(),"Real hostile arrow collision does not detonate decoy");
        arrow.discard(); skeleton.discard();
        RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.CINDER_DECOY); var replacement=decoy(test,player);
        CinderDecoyEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
        test.assertTrue(replacement.isRemoved(),"Logout leaves decoy in world");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    private static CinderDecoyEntity decoy(GameTestHelper test,ServerPlayer player) {
        return test.getLevel().getEntitiesOfClass(CinderDecoyEntity.class,player.getBoundingBox().inflate(2)).getFirst();
    }
    private static void use(ServerPlayer player,int slot) throws Exception {
        var method=WeaponAbilityNetwork.class.getDeclaredMethod("handleAbility",WeaponAbilityNetwork.UseAbilityPayload.class,IPayloadContext.class);
        method.setAccessible(true);
        var context=(IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(),new Class<?>[]{IPayloadContext.class},
                (proxy,called,args)->called.getName().equals("player")?player:null);
        method.invoke(null,new WeaponAbilityNetwork.UseAbilityPayload(slot,player.getInventory().selected,
                BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString()),context);
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsAttractDetonateAndRestoreTargets(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),
                ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get()}) {
            var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
            var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.CINDER_DECOY); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoAi(true); zombie.setNoGravity(true);
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); zombie.setHealth(200);
            zombie.setPos(player.position().add(2,0,0)); zombie.setTarget(player); test.getLevel().addFreshEntity(zombie);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.CINDER_DECOY),"Decoy activation failed");
            var decoy=decoy(test,player);
            test.assertTrue(zombie.getTarget()==decoy,"Hostile mob was not attracted");
            test.assertFalse(decoy.shouldBeSaved(),"Decoy can be saved as permanent equipment");
            test.assertTrue(decoy.interactAt(player,Vec3.ZERO,InteractionHand.MAIN_HAND)==InteractionResult.FAIL,"Decoy equipment can be stolen");
            decoy.hurt(player.damageSources().playerAttack(player),100);
            test.assertTrue(RecommendedAbilityRuntime.active(player,RecommendedAbilities.CINDER_DECOY),"Player attack detonated or destroyed decoy");
            decoy.hurt(zombie.damageSources().mobAttack(zombie),1);
            test.assertTrue(decoy.isRemoved() && !RecommendedAbilityRuntime.active(player,RecommendedAbilities.CINDER_DECOY),"Hostile attack did not finish/remove decoy");
            test.assertTrue(zombie.getHealth()<200 && zombie.getRemainingFireTicks()>=60,"Decoy blast did not damage and ignite enemy");
            test.assertTrue(zombie.getTarget()==player,"Decoy did not restore mob's previous target");
            test.assertTrue(player.getHealth()==20,"Decoy blast damages owner");
            zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void contactAndSwitchCleanupStartIndependentCooldowns(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        var stack=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.CINDER_DECOY);
        stack=Augmentations.apply(stack,RecommendedAbilities.EXPLOSION); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        use(player,0); var decoy=decoy(test,player); use(player,1);
        test.assertTrue(RecommendedAbilityRuntime.active(player,RecommendedAbilities.CINDER_DECOY),"Instant secondary cancels primary decoy");
        String item=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(item+":"+RecommendedAbilities.EXPLOSION),"Secondary cooldown missing");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.CLAYMORE.get()));
        WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(decoy.isRemoved(),"Switch leaves an orphaned decoy");
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(item+":"+RecommendedAbilities.CINDER_DECOY)==player.level().getGameTime()+900,"Cancellation cooldown missing");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.setData(WeaponCooldownAttachments.DISABLED,true); use(player,0);
        decoy=decoy(test,player);
        var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoAi(true); zombie.setNoGravity(true);
        zombie.setPos(player.position()); test.getLevel().addFreshEntity(zombie);
        CinderDecoyEvents.onTick(new PlayerTickEvent.Post(player));
        test.assertTrue(decoy.isRemoved(),"Enemy contact did not trigger decoy");
        zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=180)
    public static void expirationRemovesDecoyWithoutExplosion(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        var stack=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.CINDER_DECOY);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); use(player,0); var decoy=decoy(test,player);
        test.runAfterDelay(121,()->{
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(decoy.isRemoved(),"Expired decoy remains in world");
            test.assertFalse(RecommendedAbilityRuntime.active(player,RecommendedAbilities.CINDER_DECOY),"Expired decoy remains active");
            test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
        });
    }
}
