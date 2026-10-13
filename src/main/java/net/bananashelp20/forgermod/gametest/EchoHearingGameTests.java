package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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

    @GameTest(template = "riftfang_test")
    public static void allVariantsRevealEveryEntityAndStartIndependentCooldowns(GameTestHelper test) throws Exception {
        int count = 0;
        for (var item : BuiltInRegistries.ITEM) {
            if (!(item instanceof PulsiteWeapon)) continue;
            count++;
            ServerPlayer wielder = player(test);
            ServerPlayer otherPlayer = player(test);
            var nearMob = EntityType.ZOMBIE.create(test.getLevel());
            nearMob.setNoAi(true);
            nearMob.setNoGravity(true);
            nearMob.setPos(wielder.getX() + 4, wielder.getY(), wielder.getZ());
            test.getLevel().addFreshEntity(nearMob);
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
                test.assertTrue(wielder.getEffect(MobEffects.GLOWING).getDuration() == 200, "Wielder was not revealed for ten seconds");
                test.assertTrue(otherPlayer.getEffect(MobEffects.GLOWING).getDuration() == 200, "Other player was excluded");
                test.assertTrue(nearMob.getEffect(MobEffects.GLOWING).getDuration() == 200, "Mob was excluded");
                test.assertTrue(drop.isCurrentlyGlowing(), "Nonliving entity was excluded");
                String key = BuiltInRegistries.ITEM.getKey(item) + ":" + RecommendedAbilities.ECHO_HEARING;
                long ready = wielder.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key, 0L);
                test.assertTrue(ready == wielder.level().getGameTime() + 600, "Instant cooldown did not start");
                nearMob.removeEffect(MobEffects.GLOWING);
                AbilityTestPackets.use(wielder, 0);
                test.assertFalse(nearMob.hasEffect(MobEffects.GLOWING), "Cooldown allowed a second pulse");
            } finally {
                nearMob.discard();
                drop.discard();
                test.getLevel().getServer().getPlayerList().remove(wielder);
                test.getLevel().getServer().getPlayerList().remove(otherPlayer);
            }
        }
        test.assertTrue(count == 15, "Missing Pulsite variants");
        test.succeed();
    }

    @GameTest(template = "riftfang_test", timeoutTicks = 350)
    public static void rangeWallsDurationAndExistingGlow(GameTestHelper test) throws Exception {
        ServerPlayer wielder = player(test);
        var level = test.getLevel();
        var inside = EntityType.ZOMBIE.create(level);
        inside.setNoAi(true);
        inside.setNoGravity(true);
        inside.setPos(wielder.getX() + 30, wielder.getY(), wielder.getZ());
        level.getChunkAt(inside.blockPosition());
        level.addFreshEntity(inside);
        var outside = EntityType.ZOMBIE.create(level);
        outside.setNoAi(true);
        outside.setNoGravity(true);
        outside.setPos(wielder.getX() + 34, wielder.getY(), wielder.getZ());
        level.getChunkAt(outside.blockPosition());
        level.addFreshEntity(outside);
        var priorGlow = new ItemEntity(level, wielder.getX() + 2, wielder.getY(), wielder.getZ(), new ItemStack(Items.EMERALD));
        priorGlow.setNoGravity(true);
        priorGlow.setPickUpDelay(32767);
        priorGlow.setGlowingTag(true);
        level.addFreshEntity(priorGlow);
        var drop = new ItemEntity(level, wielder.getX() + 3, wielder.getY(), wielder.getZ(), new ItemStack(Items.DIAMOND));
        drop.setNoGravity(true);
        drop.setPickUpDelay(32767);
        level.addFreshEntity(drop);
        var wall = wielder.blockPosition().east(2).above();
        level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        ItemStack learned = Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()), RecommendedAbilities.ECHO_HEARING);
        wielder.setItemInHand(InteractionHand.MAIN_HAND, learned);
        AbilityTestPackets.use(wielder, 0);
        test.assertTrue(inside.getEffect(MobEffects.GLOWING).getDuration() == 200, "Thirty-block target behind wall was missed");
        test.assertFalse(outside.hasEffect(MobEffects.GLOWING), "Target outside 32-block sphere was revealed");
        test.assertTrue(priorGlow.isCurrentlyGlowing() && drop.isCurrentlyGlowing(), "Item glow missing");
        wielder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        test.runAfterDelay(190, () -> test.assertTrue(drop.isCurrentlyGlowing(), "Nonliving glow ended early on weapon switch"));
        test.runAfterDelay(210, () -> {
            test.assertFalse(inside.hasEffect(MobEffects.GLOWING), "Living glow lasted beyond ten seconds");
            test.assertFalse(drop.isCurrentlyGlowing(), "Nonliving glow did not expire");
            test.assertTrue(priorGlow.isCurrentlyGlowing(), "Existing glow was removed");
            inside.discard();
            outside.discard();
            priorGlow.discard();
            drop.discard();
            level.getServer().getPlayerList().remove(wielder);
            test.succeed();
        });
    }

    @GameTest(template = "riftfang_test")
    public static void fourthRankReachesFullRadiusAndDuration(GameTestHelper test) throws Exception {
        ServerPlayer wielder = player(test);
        var level = test.getLevel();
        var inside = EntityType.ZOMBIE.create(level);
        inside.setNoAi(true);
        inside.setNoGravity(true);
        inside.setPos(wielder.getX() + 54, wielder.getY(), wielder.getZ());
        level.getChunkAt(inside.blockPosition());
        level.addFreshEntity(inside);
        var outside = EntityType.ZOMBIE.create(level);
        outside.setNoAi(true);
        outside.setNoGravity(true);
        outside.setPos(wielder.getX() + 58, wielder.getY(), wielder.getZ());
        level.getChunkAt(outside.blockPosition());
        level.addFreshEntity(outside);
        try {
            ItemStack learned = new ItemStack(ModItems.SHRIEKING_CLAYMORE.get());
            for (int rank = 1; rank <= 4; rank++) learned = Augmentations.apply(learned, RecommendedAbilities.ECHO_HEARING);
            test.assertTrue(Augmentations.level(learned, RecommendedAbilities.ECHO_HEARING) == 4, "Fourth rank was not learned");
            wielder.setItemInHand(InteractionHand.MAIN_HAND, learned);
            AbilityTestPackets.use(wielder, 0);
            test.assertTrue(inside.getEffect(MobEffects.GLOWING).getDuration() == 320, "Rank IV missed 54-block target or 16-second duration");
            test.assertFalse(outside.hasEffect(MobEffects.GLOWING), "Rank IV exceeded 56-block radius");
            String key = BuiltInRegistries.ITEM.getKey(learned.getItem()) + ":" + RecommendedAbilities.ECHO_HEARING;
            test.assertTrue(wielder.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key, 0L)
                    == wielder.level().getGameTime() + 300, "Rank IV cooldown is not 15 seconds");
        } finally {
            inside.discard();
            outside.discard();
            level.getServer().getPlayerList().remove(wielder);
        }
        test.succeed();
    }
}
