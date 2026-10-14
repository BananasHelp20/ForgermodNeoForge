package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.VulnusiumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.DaggerCriticalEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DeepWoundGameTests {
    @GameTest(template="riftfang_test")
    public static void actualLearnedCriticalAttacksDoubleDamageAndSlowAllFiveVariants(GameTestHelper test) throws Exception {
        for(Item item:new Item[]{ModItems.ASSASSIN_DAGGER.get(),ModItems.ASSASSIN_DAGGER_RUBY.get(),ModItems.ASSASSIN_DAGGER_AMBER.get(),ModItems.ASSASSIN_DAGGER_AMETHYST.get(),ModItems.ASSASSIN_DAGGER_JADE.get()}) {
            var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
            var stack=net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(item),"tooltips.forgermod.ability.deep_wound");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int tick=0;tick<60;tick++)player.doTick();
            var target=target(test,player);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);
            try {
                AbilityTestPackets.use(player,0);test.assertTrue(weapon(player).isAbilityActive(player,WeaponAbilitySlot.PRIMARY),"Packet did not arm Deep Wound");
                player.setOnGround(false);player.fallDistance=1;player.setSprinting(false);
                float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);player.attack(target);
                var slow=target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
                test.assertTrue(Math.abs(target.getHealth()-(1000-damage*3))<.001,"Actual critical lacks doubled damage: "+target.getHealth()+" base="+damage);
                test.assertTrue(slow!=null && slow.getDuration()==100 && slow.getAmplifier()==2,"Actual critical lacks Slowness III");
                test.assertFalse(weapon(player).isAbilityActive(player,WeaponAbilitySlot.PRIMARY),"Actual critical retains charge");
            } finally {
                net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
                DaggerCriticalEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));target.discard();player.server.getPlayerList().remove(player);
            }
        }
        test.succeed();
    }
    private static ServerPlayer player(GameTestHelper test, Item item) {
        ServerPlayer player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        player.setPos(test.getBounds().getCenter());
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(item));
        return player;
    }

    private static Cow target(GameTestHelper test, ServerPlayer player) {
        Cow cow = EntityType.COW.create(test.getLevel());
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.setPos(player.position().add(0, 0, 2));
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        cow.setHealth(100);
        test.getLevel().addFreshEntity(cow);
        return cow;
    }

    private static VulnusiumWeapon weapon(ServerPlayer player) {
        return (VulnusiumWeapon)player.getMainHandItem().getItem();
    }

    private static CriticalHitEvent hit(GameTestHelper test, ServerPlayer player, Cow target, boolean critical) {
        CriticalHitEvent event = new CriticalHitEvent(player, target, critical ? 1.5f : 1f, critical);
        DaggerCriticalEvents.onCriticalHit(event);
        VulnusiumWeapon.onCriticalHit(event);
        test.assertTrue(target.hurt(player.damageSources().playerAttack(player), 10 * event.getDamageMultiplier()), "Test hit dealt no damage");
        weapon(player).postHurtEnemy(player.getMainHandItem(), target, player);
        return event;
    }

    @GameTest(template = "riftfang_test")
    public static void allVariantsSlowOnlyTargetOnChargedCritical(GameTestHelper test) {
        for (Item item : new Item[]{ModItems.ASSASSIN_DAGGER.get(), ModItems.ASSASSIN_DAGGER_RUBY.get(),
                ModItems.ASSASSIN_DAGGER_AMBER.get(), ModItems.ASSASSIN_DAGGER_AMETHYST.get(), ModItems.ASSASSIN_DAGGER_JADE.get()}) {
            ServerPlayer player = player(test, item);
            Cow target = target(test, player);
            test.assertTrue(weapon(player).activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Deep Wound failed to arm");
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "Activating slowed the attacker");
            CriticalHitEvent event = hit(test, player, target, true);
            var slow = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            test.assertTrue(slow != null && slow.getDuration() == 100 && slow.getAmplifier() == 2, "Target did not receive five seconds of Slowness III");
            test.assertTrue(event.getDamageMultiplier() == 3 && target.getHealth() == 70, "Deep Wound lost doubled critical damage");
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "Deep Wound slowed the attacker");
            test.assertFalse(weapon(player).isAbilityActive(player, WeaponAbilitySlot.PRIMARY), "Charge was not consumed");
            test.assertTrue(weapon(player).abilityCooldownTicks(WeaponAbilitySlot.PRIMARY) == 600
                    && weapon(player).abilityCooldownTicks(WeaponAbilitySlot.SECONDARY) == 300, "Cooldowns changed");
            Cow second = target(test, player);
            test.assertTrue(hit(test, player, second, true).getDamageMultiplier() == 1.5f, "Second critical retained double damage");
            test.assertFalse(second.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "Second critical reused the slow charge");
            target.discard();
            second.discard();
            test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void ordinaryHitKeepsChargeAndDoesNotSlow(GameTestHelper test) {
        ServerPlayer player = player(test, ModItems.ASSASSIN_DAGGER.get());
        Cow target = target(test, player);
        weapon(player).activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.PRIMARY);
        hit(test, player, target, false);
        test.assertFalse(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "Ordinary hit applied charged slowness");
        test.assertTrue(weapon(player).isAbilityActive(player, WeaponAbilitySlot.PRIMARY), "Ordinary hit consumed the critical charge");
        weapon(player).cancelAbility(player, WeaponAbilitySlot.PRIMARY);
        test.getLevel().getServer().getPlayerList().remove(player);
        test.succeed();
    }
}
