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
    @GameTest(template="riftfang_test")
    public static void allRanksAndVariantsHaveBoundedExplosionRadiusOnCancellation(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var pos=new net.minecraft.core.BlockPos(8,6,8);
        for(int x=1;x<=16;x++)for(int y=5;y<=10;y++)for(int z=1;z<=16;z++)test.setBlock(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);
        test.setBlock(pos.below(),net.minecraft.world.level.block.Blocks.STONE);
        Vec3 origin=Vec3.atBottomCenterOf(test.absolutePos(pos));player.setPos(origin);
        try {
            for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get()}) {
                ItemStack stack=new ItemStack(item);
                for(int rank=1;rank<=4;rank++) {
                    stack=Augmentations.apply(stack,RecommendedAbilities.CINDER_DECOY);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    var near=EntityType.ZOMBIE.create(test.getLevel());var far=EntityType.ZOMBIE.create(test.getLevel());
                    double radius=rank+1;
                    for(var mob:java.util.List.of(near,far)){mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);test.getLevel().addFreshEntity(mob);}
                    near.setPos(origin.add(radius-.5,0,0));far.setPos(origin.add(radius+.75,0,0));
                    try {
                        test.assertTrue(CinderDecoyEvents.activate(player,stack),"Decoy activation rejected");var entity=decoy(test,player);
                        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);test.assertFalse(CinderDecoyEvents.active(player),"Switch does not end decoy");
                        test.assertTrue(entity.isRemoved() && near.getHealth()<20 && near.getRemainingFireTicks()>0,"Switch does not explode/ignite inside rank radius");
                        test.assertTrue(far.getHealth()==20 && far.getRemainingFireTicks()<=0,"Explosion exceeds rank radius: rank="+rank+" health="+far.getHealth()+" fire="+far.getRemainingFireTicks());
                        float health=near.getHealth();near.invulnerableTime=0;CinderDecoyEvents.cancel(player.getUUID());CinderDecoyEvents.detonate(player.getUUID());test.assertTrue(near.getHealth()==health,"Decoy explodes twice");
                        test.assertTrue(player.getHealth()==20,"Explosion harms owner");test.assertBlockPresent(net.minecraft.world.level.block.Blocks.STONE,pos.below());
                    } finally {near.discard();far.discard();CinderDecoyEvents.cancel(player.getUUID());}
                }
            }
        } finally {CinderDecoyEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=180)
    public static void expirationExplodesOnceAndStartsCooldown(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        var stack=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.CINDER_DECOY);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); use(player,0); var decoy=decoy(test,player);
        var victim=EntityType.ZOMBIE.create(test.getLevel());victim.setNoAi(true);victim.setNoGravity(true);victim.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));victim.setPos(decoy.position().add(1,0,0));test.getLevel().addFreshEntity(victim);
        test.runAfterDelay(119,()->{
            test.assertTrue(victim.getHealth()==20 && !decoy.isRemoved(),"Decoy explodes before expiry");
        });
        test.runAfterDelay(121,()->{
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(decoy.isRemoved(),"Expired decoy remains in world");
            test.assertFalse(RecommendedAbilityRuntime.active(player,RecommendedAbilities.CINDER_DECOY),"Expired decoy remains active");
            test.assertTrue(victim.getHealth()<20,"Expiry did not deal explosion damage");
            String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.CINDER_DECOY;
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)>player.level().getGameTime(),"Expiry does not start cooldown");
            victim.discard();test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
        });
    }
}
