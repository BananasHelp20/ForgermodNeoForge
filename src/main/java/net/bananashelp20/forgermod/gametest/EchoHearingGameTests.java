package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.EchoHearingAbility;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EchoHearingGameTests {
    private static ServerPlayer player(GameTestHelper test) {
        ServerPlayer player = TestPlayers.survival(test);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setPos(test.getBounds().getCenter());
        return player;
    }

    private static void noise(GameTestHelper test, Holder<GameEvent> event, net.minecraft.world.entity.Entity source) {
        NeoForge.EVENT_BUS.post(new VanillaGameEvent(test.getLevel(), event,
                source.position(), GameEvent.Context.of(source)));
    }

    @GameTest(template = "riftfang_test", batch = "echo_hearing_variants")
    public static void allVariantsReactToNoiseWithoutRevealingTheListener(GameTestHelper test) throws Exception {
        int count = 0;
        for (var item : BuiltInRegistries.ITEM) {
            if (!(item instanceof PulsiteWeapon)) continue;
            count++;
            ServerPlayer wielder = player(test);
            ServerPlayer otherPlayer = player(test);
            var mob = EntityType.ZOMBIE.create(test.getLevel());
            mob.setNoAi(true);
            mob.setNoGravity(true);
            mob.setPos(wielder.getX() + 4, wielder.getY(), wielder.getZ());
            test.getLevel().addFreshEntity(mob);
            var drop = new ItemEntity(test.getLevel(), wielder.getX() + 3, wielder.getY(), wielder.getZ(), new ItemStack(Items.DIAMOND));
            drop.setNoGravity(true);
            drop.setPickUpDelay(32767);
            test.getLevel().addFreshEntity(drop);
            try {
                ItemStack fresh = new ItemStack(item);
                wielder.setItemInHand(InteractionHand.MAIN_HAND, fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(wielder, fresh, RecommendedAbilities.ECHO_HEARING), "Fresh weapon activates");
                ItemStack learned = Augmentations.apply(fresh, RecommendedAbilities.ECHO_HEARING);
                wielder.setItemInHand(InteractionHand.MAIN_HAND, learned);
                AbilityTestPackets.use(wielder, 0);
                test.assertTrue(RecommendedAbilityRuntime.active(wielder, RecommendedAbilities.ECHO_HEARING), "Listener did not start");
                test.assertFalse(wielder.hasEffect(MobEffects.GLOWING) || otherPlayer.hasEffect(MobEffects.GLOWING)
                        || mob.hasEffect(MobEffects.GLOWING) || drop.isCurrentlyGlowing(), "Activation revealed a silent entity");
                noise(test, GameEvent.STEP, mob);
                test.assertTrue(mob.getEffect(MobEffects.GLOWING) != null
                        && mob.getEffect(MobEffects.GLOWING).getDuration() == 40, "Noisy mob did not glow for two seconds");
                noise(test, GameEvent.STEP, otherPlayer);
                test.assertTrue(otherPlayer.hasEffect(MobEffects.GLOWING), "Noisy other player was excluded");
                noise(test, GameEvent.STEP, wielder);
                test.assertFalse(wielder.hasEffect(MobEffects.GLOWING), "Listening player revealed themselves");
                noise(test, GameEvent.ENTITY_ACTION, drop);
                test.assertTrue(drop.isCurrentlyGlowing(), "Noisy nonliving entity was excluded");
                String key = BuiltInRegistries.ITEM.getKey(item) + ":" + RecommendedAbilities.ECHO_HEARING;
                test.assertFalse(wielder.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key), "Cooldown began while listening");
            } finally {
                EchoHearingAbility.cancel(wielder.getUUID());
                mob.discard();
                drop.discard();
                test.getLevel().getServer().getPlayerList().remove(wielder);
                test.getLevel().getServer().getPlayerList().remove(otherPlayer);
            }
        }
        test.assertTrue(count == 15, "Missing Pulsite variants");
        test.succeed();
    }

    @GameTest(template = "riftfang_test", batch = "echo_hearing_motion", timeoutTicks = 100)
    public static void rangeMovesAndSwitchStartsCooldown(GameTestHelper test) throws Exception {
        ServerPlayer wielder = player(test);
        var mob = EntityType.ZOMBIE.create(test.getLevel());
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.setPos(wielder.getX() + 34, wielder.getY(), wielder.getZ());
        test.getLevel().getChunkAt(mob.blockPosition());
        test.getLevel().addFreshEntity(mob);
        ItemStack learned = Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()), RecommendedAbilities.ECHO_HEARING);
        wielder.setItemInHand(InteractionHand.MAIN_HAND, learned);
        AbilityTestPackets.use(wielder, 0);
        noise(test, GameEvent.STEP, mob);
        test.assertFalse(mob.hasEffect(MobEffects.GLOWING), "Mob outside original radius was revealed");
        wielder.setPos(wielder.getX() + 8, wielder.getY(), wielder.getZ());
        noise(test, GameEvent.STEP, mob);
        test.assertTrue(mob.hasEffect(MobEffects.GLOWING), "Moving radius did not follow wielder");
        mob.removeEffect(MobEffects.GLOWING);
        wielder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        test.runAfterDelay(2, () -> {
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(wielder));
            noise(test, GameEvent.STEP, mob);
            test.assertFalse(mob.hasEffect(MobEffects.GLOWING), "Switch left listener active");
            String key = BuiltInRegistries.ITEM.getKey(learned.getItem()) + ":" + RecommendedAbilities.ECHO_HEARING;
            long ready = wielder.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key, 0L);
            test.assertTrue(ready > wielder.level().getGameTime() + 590, "Switch did not begin 30-second cooldown");
            mob.discard();
            test.getLevel().getServer().getPlayerList().remove(wielder);
            test.succeed();
        });
    }

    @GameTest(template = "riftfang_test", batch = "echo_hearing_expiry", timeoutTicks = 230)
    public static void glowExpiresAfterTwoSecondsAndListeningEndsAfterTen(GameTestHelper test) throws Exception {
        ServerPlayer wielder = player(test);
        var mob = EntityType.ZOMBIE.create(test.getLevel());
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.setSilent(true);
        mob.setPos(wielder.getX() + 4, wielder.getY(), wielder.getZ());
        test.getLevel().addFreshEntity(mob);
        ItemStack learned = Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()), RecommendedAbilities.ECHO_HEARING);
        wielder.setItemInHand(InteractionHand.MAIN_HAND, learned);
        AbilityTestPackets.use(wielder, 0);
        noise(test, GameEvent.STEP, mob);
        test.assertTrue(mob.hasEffect(MobEffects.GLOWING), "First noise was not revealed");
        var originalPosition = wielder.position();
        wielder.setPos(originalPosition.add(40, 0, 0));
        test.runAfterDelay(45, () -> {
            test.assertFalse(mob.hasEffect(MobEffects.GLOWING), "Glow outlasted two seconds: ticks=" + mob.tickCount
                    + ", remaining=" + (mob.getEffect(MobEffects.GLOWING) == null ? 0 : mob.getEffect(MobEffects.GLOWING).getDuration()));
            wielder.setPos(originalPosition);
            noise(test, GameEvent.STEP, mob);
            test.assertTrue(mob.hasEffect(MobEffects.GLOWING), "Later noise did not reveal mob again");
        });
        test.runAfterDelay(205, () -> {
            test.assertFalse(RecommendedAbilityRuntime.active(wielder, RecommendedAbilities.ECHO_HEARING), "Listening did not end");
            mob.removeEffect(MobEffects.GLOWING);
            noise(test, GameEvent.STEP, mob);
            test.assertFalse(mob.hasEffect(MobEffects.GLOWING), "Expired listener reacted to noise");
        });
        test.runAfterDelay(210, () -> {
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(wielder));
            String key = BuiltInRegistries.ITEM.getKey(learned.getItem()) + ":" + RecommendedAbilities.ECHO_HEARING;
            long ready = wielder.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key, 0L);
            test.assertTrue(ready > wielder.level().getGameTime() + 580,
                    "Natural expiry did not begin cooldown: ready=" + ready + ", now=" + wielder.level().getGameTime()
                            + ", cooldowns=" + wielder.getData(WeaponCooldownAttachments.COOLDOWNS));
            mob.discard();
            test.getLevel().getServer().getPlayerList().remove(wielder);
            test.succeed();
        });
    }
}
