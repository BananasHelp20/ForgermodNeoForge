package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeHeavyNetwork;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DualWieldNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Proxy;
import java.util.List;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MaterialEffectGameTests {
    private static List<SwordItemWithEffect> weapons() {
        return ModItems.ITEMS.getEntries().stream().map(entry -> entry.get())
                .filter(item -> item instanceof SwordItemWithEffect)
                .map(item -> (SwordItemWithEffect)item).toList();
    }

    private static String gem(Item item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        for (String gem : new String[]{"ruby", "amber", "amethyst", "jade"}) if (id.endsWith("_" + gem)) return gem;
        return "no_gemstone";
    }

    private static Cow target(GameTestHelper test, Vec3 position) {
        Cow cow = EntityType.COW.create(test.getLevel());
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        cow.setHealth(100);
        cow.setPos(position);
        test.getLevel().addFreshEntity(cow);
        return cow;
    }

    private static double equip(Player player, Item item) {
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(item));
        player.tick();
        return player.getAttributeValue(Attributes.ATTACK_SPEED);
    }

    private static MobEffectInstance effect(GameTestHelper test, Cow target) {
        test.assertTrue(target.getActiveEffects().size() == 1, "Expected exactly one material effect");
        return new MobEffectInstance(target.getActiveEffects().iterator().next());
    }

    @GameTest(template = "riftfang_test")
    public static void everyMaterialAndGemstoneUsesSpeedAdjustedClaymoreEffect(GameTestHelper test) {
        Player player = test.makeMockPlayer(GameType.CREATIVE);
        player.setNoGravity(true);
        player.setPos(test.getBounds().getCenter());
        Cow target = target(test, player.position().add(0, 0, 2));
        List<SwordItemWithEffect> weapons = weapons();
        int checked = 0;
        for (SwordItemWithEffect claymore : weapons) {
            if (claymore.isAxe() || claymore.isDagger()) continue;
            double claymoreSpeed = equip(player, claymore);
            target.removeAllEffects();
            claymore.postHurtEnemy(player.getMainHandItem(), target, player);
            MobEffectInstance baseline = effect(test, target);
            for (SwordItemWithEffect weapon : weapons) {
                if (weapon.getClass() != claymore.getClass() || !gem(weapon).equals(gem(claymore))) continue;
                double speed = equip(player, weapon);
                target.removeAllEffects();
                weapon.postHurtEnemy(player.getMainHandItem(), target, player);
                MobEffectInstance actual = effect(test, target);
                int expected = Math.max(1, (int)Math.round(baseline.getDuration() * claymoreSpeed / speed));
                test.assertTrue(actual.getEffect().equals(baseline.getEffect()) && actual.getAmplifier() == baseline.getAmplifier(), "Material effect/strength differs from its gemstone claymore: " + weapon);
                test.assertTrue(actual.getDuration() == expected, "Duration does not follow actual attack speed: " + weapon + " " + actual.getDuration() + " expected " + expected);
                if (weapon.isAxe()) test.assertTrue(actual.getDuration() > baseline.getDuration(), "Axe effect is not longer");
                if (weapon.isDagger()) test.assertTrue(actual.getDuration() < baseline.getDuration(), "Dagger effect is not shorter");
                checked++;
            }
        }
        test.assertTrue(checked == 135, "Skipped material weapons or gemstone variants: " + checked);
        target.discard();
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void offhandDaggersApplyAdjustedEffects(GameTestHelper test) throws ReflectiveOperationException {
        var handle = DualWieldNetwork.class.getDeclaredMethod("handleAttack", DualWieldNetwork.OffhandAttackPayload.class, IPayloadContext.class);
        handle.setAccessible(true);
        int checked = 0;
        for (SwordItemWithEffect dagger : weapons()) {
            if (!dagger.isDagger()) continue;
            ServerPlayer player = test.makeMockServerPlayerInLevel();
            player.setNoGravity(true);
            player.setPos(test.getBounds().getCenter());
            equip(player, dagger);
            player.setItemInHand(InteractionHand.OFF_HAND, TestWeapons.ready(dagger));
            Cow target = target(test, player.position().add(0, 0, 2));
            dagger.applyMaterialEffect(target);
            MobEffectInstance expected = effect(test, target);
            target.removeAllEffects();
            player.setLastHurtMob(target);
            IPayloadContext context = (IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(), new Class<?>[]{IPayloadContext.class},
                    (proxy, method, args) -> { if (method.getName().equals("player")) return player; throw new UnsupportedOperationException(method.getName()); });
            handle.invoke(null, new DualWieldNetwork.OffhandAttackPayload(target.getId()), context);
            MobEffectInstance actual = effect(test, target);
            test.assertTrue(actual.getEffect().equals(expected.getEffect()) && actual.getDuration() == expected.getDuration()
                    && actual.getAmplifier() == expected.getAmplifier(), "Offhand material effect differs: " + dagger);
            test.getLevel().getServer().getPlayerList().remove(player);
            target.discard();
            checked++;
        }
        test.assertTrue(checked == 45, "Skipped offhand variants");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void axeSlamsApplyAdjustedEffects(GameTestHelper test) throws ReflectiveOperationException {
        Class<?> pending = Class.forName(AxeHeavyNetwork.class.getName() + "$PendingSlam");
        var constructor = pending.getDeclaredConstructor(ServerLevel.class, Item.class, long.class, BlockPos.class, float.class);
        constructor.setAccessible(true);
        var land = AxeHeavyNetwork.class.getDeclaredMethod("landSlam", ServerPlayer.class, pending);
        land.setAccessible(true);
        ServerPlayer player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        BlockPos center = test.absolutePos(new BlockPos(8, 8, 8));
        player.setPos(Vec3.atBottomCenterOf(center.offset(0, 0, -2)));
        int checked = 0;
        for (SwordItemWithEffect axe : weapons()) {
            if (!axe.isAxe()) continue;
            equip(player, axe);
            Cow target = target(test, Vec3.atBottomCenterOf(center));
            axe.applyMaterialEffect(target);
            MobEffectInstance expected = effect(test, target);
            target.removeAllEffects();
            Object slam = constructor.newInstance(test.getLevel(), axe, test.getLevel().getGameTime(), center, 1f);
            land.invoke(null, player, slam);
            MobEffectInstance actual = effect(test, target);
            test.assertTrue(actual.getEffect().equals(expected.getEffect()) && actual.getDuration() == expected.getDuration()
                    && actual.getAmplifier() == expected.getAmplifier(), "Slam material effect differs: " + axe);
            target.discard();
            checked++;
        }
        test.getLevel().getServer().getPlayerList().remove(player);
        test.assertTrue(checked == 45, "Skipped axe slam variants");
        test.succeed();
    }
}
