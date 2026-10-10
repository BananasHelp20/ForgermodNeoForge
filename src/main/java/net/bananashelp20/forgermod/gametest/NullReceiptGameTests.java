package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.NullReceiptEvents;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeHeavyNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class NullReceiptGameTests {
    private static ServerPlayer player(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
        for(int x=-3;x<=3;x++) for(int y=0;y<=3;y++) for(int z=-3;z<=3;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        return player;
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsProtectFiveHitsAndReleaseOnlyWithRealVerticalSlam(GameTestHelper test) throws Exception {
        for(Item item:new Item[]{ModItems.NULLIFIED_AXE.get(),ModItems.NULLIFIED_AXE_RUBY.get(),ModItems.NULLIFIED_AXE_AMBER.get(),
                ModItems.NULLIFIED_AXE_AMETHYST.get(),ModItems.NULLIFIED_AXE_JADE.get()}) {
            var player=player(test); var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            test.assertFalse(net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.activate(player,fresh,RecommendedAbilities.NULL_RECEIPT),"Fresh axe arms receipt");
            var stack=Augmentations.apply(Augmentations.apply(fresh,RecommendedAbilities.NULL_RECEIPT),RecommendedAbilities.SWING_ATTACK);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.doTick(); AbilityTestPackets.use(player,0);
            var attacker=EntityType.ZOMBIE.create(test.getLevel()); attacker.setPos(player.position().add(1,0,0));
            player.setInvulnerable(true); test.assertFalse(player.hurt(player.damageSources().mobAttack(attacker),1),"Rejected-hit fixture succeeds"); player.setInvulnerable(false);
            Vec3 original=new Vec3(.2,.1,.3);
            for(int hit=0;hit<5;hit++) {
                player.invulnerableTime=0; player.setOnGround(true); player.setDeltaMovement(original);
                test.assertTrue(player.hurt(player.damageSources().mobAttack(attacker),1),"Received test hit rejected");
                player.knockback(.2,1,0); // A second impulse belongs to the same damaging hit, not another charge.
                test.assertTrue(player.getDeltaMovement().equals(original),"One of the first five hits applies knockback");
            }
            test.assertTrue(player.getHealth()==15,"Null Receipt prevents damage instead of only knockback");
            player.invulnerableTime=0; player.hurt(player.damageSources().mobAttack(attacker),1);
            test.assertFalse(player.getDeltaMovement().equals(original),"Sixth damaging hit remains protected");
            test.assertTrue(NullReceiptEvents.active(player),"Stored force vanishes before vertical slam");
            net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.SWING_ATTACK);
            test.assertTrue(NullReceiptEvents.active(player),"Ordinary circular swing consumes vertical-strike receipt");
            var target=EntityType.ZOMBIE.create(test.getLevel()); target.setNoAi(true); target.setNoGravity(true); target.setOnGround(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); target.setHealth(1000); target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setPos(player.position().add(1,0,1)); test.getLevel().addFreshEntity(target);
            var type=Class.forName(AxeHeavyNetwork.class.getName()+"$PendingSlam");
            var constructor=type.getDeclaredConstructor(ServerLevel.class,Item.class,long.class,BlockPos.class,float.class); constructor.setAccessible(true);
            float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            var slam=constructor.newInstance(test.getLevel(),item,player.level().getGameTime(),player.blockPosition(),damage);
            var method=AxeHeavyNetwork.class.getDeclaredMethod("landSlam",ServerPlayer.class,type); method.setAccessible(true); method.invoke(null,player,slam);
            double horizontal=target.getDeltaMovement().horizontalDistance();
            // Normal damage knockback contributes 0.4/2 before the slam's 0.5 + stored 3.0.
            test.assertTrue(Math.abs(horizontal-3.7)<.001,"Vertical slam fails to release recorded force: "+horizontal);
            test.assertTrue(Math.abs(target.getHealth()-(1000-damage))<.001,"Receipt changes slam damage");
            test.assertFalse(NullReceiptEvents.active(player),"Vertical slam does not consume receipt");
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            String key=BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.NULL_RECEIPT;
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+800,"Release fails to start cooldown");
            target.discard(); attacker.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void absorptionHitsCountAndForceCannotExceedCap(GameTestHelper test) throws Exception {
        var player=player(test); var stack=Augmentations.apply(new ItemStack(ModItems.NULLIFIED_AXE.get()),RecommendedAbilities.NULL_RECEIPT);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); AbilityTestPackets.use(player,0);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,200,2));
        var attacker=EntityType.ZOMBIE.create(test.getLevel()); attacker.setPos(player.position().add(1,0,0));
        for(int hit=0;hit<6;hit++) {
            player.invulnerableTime=0; player.setDeltaMovement(Vec3.ZERO); player.hurt(player.damageSources().mobAttack(attacker),1);
            if(hit<5) {
                player.knockback(100,1,0);
                test.assertTrue(player.getDeltaMovement().equals(Vec3.ZERO),"Absorbed hit fails to suppress knockback");
            } else test.assertTrue(player.getDeltaMovement().lengthSqr()>0,"Absorbed hits do not consume five protection charges");
        }
        test.assertTrue(player.getHealth()==20 && player.getAbsorptionAmount()==6,"Absorption fixture did not absorb damage normally");
        test.assertTrue(NullReceiptEvents.release(player)==5,"Recorded impulse exceeds chosen cap");
        attacker.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void explosionImpulseIsCanceledAndSwitchStartsCooldown(GameTestHelper test) throws Exception {
        var player=player(test); var stack=Augmentations.apply(new ItemStack(ModItems.NULLIFIED_AXE.get()),RecommendedAbilities.NULL_RECEIPT);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); AbilityTestPackets.use(player,0);
        var creeper=EntityType.CREEPER.create(test.getLevel()); creeper.setPos(player.position().add(1.5,0,0));
        var original=new Vec3(.2,.1,.3); player.setDeltaMovement(original);
        player.level().explode(creeper,creeper.getX(),creeper.getY(),creeper.getZ(),1,false,Level.ExplosionInteraction.NONE);
        test.assertTrue(player.isAlive() && player.getHealth()<20,"Explosion fixture fails to deal survivable damage");
        test.assertTrue(player.getDeltaMovement().equals(original),"Explosion impulse bypasses receipt");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        test.assertFalse(NullReceiptEvents.active(player),"Switch keeps receipt");
        test.assertTrue(NullReceiptEvents.release(player)==0,"Canceled force can be released later");
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.NULL_RECEIPT;
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+800,"Switch fails to start cooldown");
        creeper.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
