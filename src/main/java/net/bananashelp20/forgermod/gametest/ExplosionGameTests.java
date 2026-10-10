package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.lang.reflect.Proxy;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ExplosionGameTests {
    private static void use(ServerPlayer player) throws Exception {
        var method=WeaponAbilityNetwork.class.getDeclaredMethod("handleAbility",WeaponAbilityNetwork.UseAbilityPayload.class,IPayloadContext.class);
        method.setAccessible(true);
        var context=(IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(),new Class<?>[]{IPayloadContext.class},
                (proxy,called,args)->called.getName().equals("player")?player:null);
        method.invoke(null,new WeaponAbilityNetwork.UseAbilityPayload(0,player.getInventory().selected,
                BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString()),context);
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsExplodeSafelyAndRespectIndependentCooldowns(GameTestHelper test) throws Exception {
        var player=test.makeMockServerPlayerInLevel(); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        var stone=player.blockPosition().below(); test.getLevel().setBlockAndUpdate(stone,Blocks.STONE.defaultBlockState());
        for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),
                ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get()}) {
            ItemStack fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,RecommendedAbilities.EXPLOSION),"Fresh claymore has Explosion");
            var stack=Augmentations.apply(fresh,RecommendedAbilities.EXPLOSION); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var cow=EntityType.COW.create(test.getLevel()); cow.setNoAi(true); cow.setNoGravity(true);
            cow.setPos(player.position().add(1.5,0,0)); cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); cow.setHealth(200);
            test.getLevel().addFreshEntity(cow); player.setHealth(1); player.setDeltaMovement(0,0,0);
            use(player);
            test.assertTrue(player.isAlive() && player.getHealth()==1 && player.getDeltaMovement().lengthSqr()==0,"Explosion harms or pushes its wielder");
            var resistance=player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            test.assertTrue(resistance!=null && resistance.getAmplifier()==3 && resistance.getDuration()==40,"Resistance IV duration wrong");
            test.assertTrue(cow.getHealth()<200,"Explosion deals no enemy damage");
            test.assertTrue(test.getLevel().getBlockState(stone).is(Blocks.STONE),"Explosion destroys terrain");
            long now=player.level().getGameTime();
            String key=BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.EXPLOSION;
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)==now+1200,"Instant cooldown missing");
            cow.setHealth(200); cow.invulnerableTime=0; use(player);
            test.assertTrue(cow.getHealth()==200,"Repeated packet bypasses Explosion cooldown");
            cow.discard();
        }
        test.assertTrue(Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.EXPLOSION).isEmpty(),"Explosion assigned to unmarked axe");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void rankFourCooldownAndResistanceExpire(GameTestHelper test) throws Exception {
        var player=test.makeMockServerPlayerInLevel(); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        ItemStack stack=new ItemStack(ModItems.INFERNAL_CLAYMORE.get());
        for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.EXPLOSION);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); use(player);
        test.assertTrue(Augmentations.cooldown(stack,WeaponAbilitySlot.PRIMARY,Augmentations.canonical(stack))==900,"Rank IV cooldown not 45 seconds");
        test.runAfterDelay(41,()->{
            // Mock players are not automatically added to the level's player tick list.
            for(int tick=0;tick<41;tick++) player.doTick();
            test.assertFalse(player.hasEffect(MobEffects.DAMAGE_RESISTANCE),"Explosion resistance persists beyond two seconds");
            test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
        });
    }
}
