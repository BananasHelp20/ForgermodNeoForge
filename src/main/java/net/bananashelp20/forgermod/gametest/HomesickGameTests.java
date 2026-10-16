package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.HomesickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HomesickGameTests {
    private static final BlockPos FLOOR = new BlockPos(2, 1, 2);
    private static void stand(GameTestHelper test, ServerPlayer player) {
        test.setBlock(FLOOR, Blocks.SCULK);
        player.setPos(Vec3.atBottomCenterOf(test.absolutePos(FLOOR.above())));
        player.setOnGround(true);
        player.tickCount = 20;
    }
    private static void tick(ServerPlayer player) { HomesickEvents.onTick(new PlayerTickEvent.Post(player)); }
    private static void clear(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        player.removeEffect(MobEffects.DAMAGE_BOOST);
    }
    private static void assertBuffs(GameTestHelper test, ServerPlayer player, boolean jade) {
        var speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        var strength = player.getEffect(MobEffects.DAMAGE_BOOST);
        test.assertTrue(speed != null && speed.getAmplifier() == (jade ? 2 : 1)
                && speed.getDuration() == (jade ? 80 : 60), "Wrong Homesick Speed level/duration");
        test.assertTrue(strength != null && strength.getAmplifier() == (jade ? 1 : 0)
                && strength.getDuration() == (jade ? 80 : 60), "Wrong Homesick Strength level/duration");
    }

    @GameTest(template = "riftfang_test")
    public static void allFifteenVariantsLearnAndGrantTheirBuffsOnlyAfterAugmentation(GameTestHelper test) {
        var player = TestPlayers.survival(test);
        stand(test, player);
        int count = 0;
        try {
            for (var entry : ModItems.ITEMS.getEntries()) {
                var fresh = new ItemStack(entry.get());
                boolean eligible = entry.get() instanceof ElectriumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a -> a.id().equals(AdditionalAbilities.HOMESICK)) == eligible,
                        "Homesick assigned to wrong weapon");
                if (!eligible) continue;
                count++;
                clear(player);
                player.setItemInHand(InteractionHand.MAIN_HAND, fresh);
                tick(player);
                test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED) || player.hasEffect(MobEffects.DAMAGE_BOOST),
                        "Fresh weapon grants Homesick");
                var learned = Augmentations.apply(fresh, AdditionalAbilities.HOMESICK);
                player.setItemInHand(InteractionHand.MAIN_HAND, learned);
                tick(player);
                assertBuffs(test, player, JadeAugments.isJade(learned));
                test.assertTrue(JadeAugments.canInfuse(learned), "Homesick omitted from Jade effect-augment support");
            }
            test.assertTrue(count == 15, "Skipped Electrium weapon variants");
        } finally { player.server.getPlayerList().remove(player); }
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void requiresGroundedSculkAndEquippedWeaponAndBuffsExpireNormally(GameTestHelper test) {
        var player = TestPlayers.survival(test);
        stand(test, player);
        var weapon = Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()), AdditionalAbilities.HOMESICK);
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        try {
            player.setOnGround(false); tick(player);
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Air above sculk grants Homesick");
            player.setOnGround(true);
            for (var block : java.util.List.of(Blocks.STONE, Blocks.SCULK_CATALYST, Blocks.SCULK_SENSOR)) {
                test.setBlock(FLOOR, block); tick(player);
                test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Non-sculk support grants Homesick");
            }
            test.setBlock(FLOOR, Blocks.SCULK);
            player.setGameMode(GameType.SPECTATOR); tick(player);
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Spectator grants Homesick");
            player.setGameMode(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, weapon); tick(player);
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Offhand claymore grants Homesick");
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, weapon); tick(player);
            assertBuffs(test, player, false);
            for (int i = 0; i < 81; i++) player.doTick();
            test.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED)
                    && player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() >= 50
                    && player.hasEffect(MobEffects.DAMAGE_BOOST)
                    && player.getEffect(MobEffects.DAMAGE_BOOST).getDuration() >= 50,
                    "Registered Homesick event does not keep refreshing buffs on sculk");
            // Real player ticks exercise the registered event and vanilla potion expiration.
            test.setBlock(FLOOR, Blocks.STONE);
            for (int i = 0; i < 61; i++) player.doTick();
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED) || player.hasEffect(MobEffects.DAMAGE_BOOST),
                    "Buffs do not expire after leaving sculk");
            stand(test, player); tick(player);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (int i = 0; i < 61; i++) player.doTick();
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED) || player.hasEffect(MobEffects.DAMAGE_BOOST),
                    "Buffs continue refreshing after switching weapons");
        } finally { player.server.getPlayerList().remove(player); }
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void matchingDualDaggersShareOnePassiveWithoutStacking(GameTestHelper test) {
        var player = TestPlayers.survival(test);
        stand(test, player);
        var main = new ItemStack(ModItems.STATIC_DAGGER.get());
        var off = Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER_RUBY.get()), AdditionalAbilities.HOMESICK);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, main);
            player.setItemInHand(InteractionHand.OFF_HAND, off); tick(player);
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Offhand shares without learned Dual Wield");
            off = Augmentations.apply(off, Augmentations.DUAL_WIELD);
            player.setItemInHand(InteractionHand.OFF_HAND, off); tick(player);
            assertBuffs(test, player, false);
            clear(player);
            main = Augmentations.apply(main, AdditionalAbilities.HOMESICK);
            player.setItemInHand(InteractionHand.MAIN_HAND, main); tick(player);
            assertBuffs(test, player, false);
            clear(player);
            var jade = Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER_JADE.get()), AdditionalAbilities.HOMESICK);
            jade = Augmentations.apply(jade, Augmentations.DUAL_WIELD);
            player.setItemInHand(InteractionHand.OFF_HAND, jade); tick(player);
            assertBuffs(test, player, true);
            clear(player);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.EMBERFANG_DAGGER.get())); tick(player);
            test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Different-material daggers share Homesick");
        } finally { player.server.getPlayerList().remove(player); }
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void strongerLongerAndInfiniteExternalEffectsArePreserved(GameTestHelper test) {
        var player = TestPlayers.survival(test);
        stand(test, player);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()), AdditionalAbilities.HOMESICK));
        try {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 3));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0)); tick(player);
            test.assertTrue(player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() == 600
                    && player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() == 3,
                    "Homesick overwrites stronger external Speed");
            test.assertTrue(player.getEffect(MobEffects.DAMAGE_BOOST).getDuration() == 600,
                    "Homesick shortens external Strength");
            clear(player);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 1)); tick(player);
            test.assertTrue(player.getEffect(MobEffects.MOVEMENT_SPEED).isInfiniteDuration(), "Homesick shortens infinite Speed");
            clear(player);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 0)); tick(player);
            assertBuffs(test, player, false);
        } finally { player.server.getPlayerList().remove(player); }
        test.succeed();
    }
}

