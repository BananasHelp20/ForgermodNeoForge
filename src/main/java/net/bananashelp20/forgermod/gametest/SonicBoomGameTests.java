package net.bananashelp20.forgermod.gametest;

import com.mojang.authlib.GameProfile;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Proxy;
import java.util.UUID;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SonicBoomGameTests {
    private static boolean hasTranslation(Component message, String key) {
        if (message.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text && text.getKey().equals(key)) return true;
        return message.getSiblings().stream().anyMatch(child -> hasTranslation(child, key));
    }
    private static ServerPlayer player(GameTestHelper test, Item item) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "sonic-test");
        ServerPlayer player = new ServerPlayer(test.getLevel().getServer(), test.getLevel(), profile, ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(test.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND),
                player, CommonListenerCookie.createInitial(profile, false));
        player.setNoGravity(true);
        player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8, 16, 8))));
        player.setYRot(0);
        player.setXRot(0);
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(item));
        player.tick(); // Apply the equipped weapon's actual damage attributes.
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos pos = BlockPos.containing(player.position()).offset(x * 16, 0, z * 16);
                test.getLevel().getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            }
        }
        return player;
    }

    private static Zombie enemy(GameTestHelper test, Vec3 position) {
        Zombie zombie = EntityType.ZOMBIE.create(test.getLevel());
        zombie.setNoAi(true);
        zombie.setNoGravity(true);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        zombie.setHealth(100);
        zombie.setPos(position);
        test.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    private static PulsiteWeapon weapon(ServerPlayer player) {
        return (PulsiteWeapon)player.getMainHandItem().getItem();
    }

    private static void fire(GameTestHelper test, ServerPlayer player) {
        test.assertTrue(weapon(player).activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.SECONDARY), "Blast activation failed");
        test.assertFalse(weapon(player).isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Blast retained an ongoing charge");
    }

    private static void damaged(GameTestHelper test, ServerPlayer player, Zombie target) {
        float expected = 100 - (float)(2 * player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        test.assertTrue(Math.abs(target.getHealth() - expected) < .0001,
                "Wrong sonic damage: " + target.getHealth() + " expected " + expected);
    }

    @GameTest(template = "sonic_boom_test")
    public static void piercesEveryEnemyAlongRay(GameTestHelper test) {
        ServerPlayer player = player(test, ModItems.WARDENS_NEEDLE.get());
        Zombie near = enemy(test, player.position().add(0, 0, 5));
        Zombie middle = enemy(test, player.position().add(0, 0, 25));
        Zombie far = enemy(test, player.position().add(0, 0, 49.9));
        Zombie beyond = enemy(test, player.position().add(0, 0, 50.5));
        Zombie offRay = enemy(test, player.position().add(3, 0, 15));
        Zombie wide = enemy(test, player.position().add(1.4, 0, 15));
        Zombie behind = enemy(test, player.position().add(0, 0, -4));
        Cow cow = EntityType.COW.create(test.getLevel());
        cow.setPos(player.position().add(0, .5, 10));
        cow.setNoGravity(true);
        test.getLevel().addFreshEntity(cow);
        float cowHealth = cow.getHealth();
        fire(test, player);
        damaged(test, player, near);
        damaged(test, player, middle);
        damaged(test, player, wide);
        damaged(test, player, far);
        test.assertTrue(beyond.getHealth() == 100 && offRay.getHealth() == 100 && behind.getHealth() == 100, "Blast hit outside its straight 50-block ray");
        test.assertTrue(cow.getHealth() < cowHealth, "Blast missed a passive animal");
        test.assertTrue(near.getDeltaMovement().z > 0, "Missing Warden-style knockback");
        test.succeed();
    }

    @GameTest(template = "sonic_boom_test")
    public static void blocksStopPiercing(GameTestHelper test) {
        ServerPlayer player = player(test, ModItems.WARDENS_NEEDLE.get());
        Zombie before = enemy(test, player.position().add(0, 0, 5));
        Zombie after = enemy(test, player.position().add(0, 0, 25));
        BlockPos wall = new BlockPos(8, 17, 20);
        for (var block : new net.minecraft.world.level.block.Block[]{Blocks.STONE, Blocks.GLASS}) {
            test.setBlock(wall, block);
            before.setHealth(100);
            before.invulnerableTime = 0;
            fire(test, player);
            damaged(test, player, before);
            test.assertTrue(after.getHealth() == 100, "Blast pierced a blocking " + block);
        }
        test.succeed();
    }

    @GameTest(template = "sonic_boom_test")
    public static void centerHolePassesBeamAndItemsAreExcluded(GameTestHelper test) {
        ServerPlayer player = player(test, ModItems.WARDENS_NEEDLE.get());
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x != 0 || y != 0) test.setBlock(new BlockPos(8 + x, 17 + y, 20), Blocks.STONE);
            }
        }
        Zombie target = enemy(test, player.position().add(1.4, 0, 25));
        var item = new net.minecraft.world.entity.item.ItemEntity(test.getLevel(),
                player.getX(), player.getEyeY(), player.getZ() + 10,
                new ItemStack(net.minecraft.world.item.Items.DIAMOND));
        test.getLevel().addFreshEntity(item);
        fire(test, player);
        damaged(test, player, target);
        test.assertTrue(item.isAlive() && item.getItem().getCount() == 1, "Blast damaged item drops");
        target.setHealth(100);
        target.invulnerableTime = 0;
        test.setBlock(new BlockPos(8, 17, 20), Blocks.STONE);
        fire(test, player);
        test.assertTrue(target.getHealth() == 100, "Center wall failed to stop wide beam");
        test.succeed();
    }

    @GameTest(template = "sonic_boom_test")
    public static void partialShapesAndFluids(GameTestHelper test) {
        ServerPlayer player = player(test, ModItems.WARDENS_NEEDLE.get());
        Zombie target = enemy(test, player.position().add(0, 0, 25));
        BlockPos obstacle = new BlockPos(8, 17, 20);
        test.setBlock(obstacle, Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        fire(test, player);
        test.assertTrue(target.getHealth() == 100, "Blast pierced the top slab at eye height");
        test.setBlock(obstacle, Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        fire(test, player);
        damaged(test, player, target);
        target.setHealth(100);
        target.invulnerableTime = 0;
        test.setBlock(obstacle, Blocks.WATER);
        fire(test, player);
        damaged(test, player, target);
        test.succeed();
    }

    @GameTest(template = "sonic_boom_test")
    public static void followsPitchAndYaw(GameTestHelper test) {
        ServerPlayer player = player(test, ModItems.WARDENS_NEEDLE.get());
        for (float[] aim : new float[][]{{0, -90}, {0, 90}, {-90, 0}, {45, -45}}) {
            player.setYRot(aim[0]);
            player.setXRot(aim[1]);
            Zombie target = enemy(test, player.getEyePosition().add(player.getLookAngle().scale(10)).add(0, -.9, 0));
            fire(test, player);
            damaged(test, player, target);
            target.discard();
        }
        test.succeed();
    }

    @GameTest(template = "sonic_boom_test")
    public static void variantsAndPrimaryRemainIndependent(GameTestHelper test) {
        for (Item item : new Item[]{ModItems.WARDENS_NEEDLE.get(), ModItems.WARDENS_NEEDLE_RUBY.get(),
                ModItems.WARDENS_NEEDLE_AMBER.get(), ModItems.WARDENS_NEEDLE_AMETHYST.get(), ModItems.WARDENS_NEEDLE_JADE.get()}) {
            ServerPlayer player = player(test, item);
            Zombie target = enemy(test, player.position().add(0, 0, 10));
            double reach = player.entityInteractionRange();
            weapon(player).activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.PRIMARY);
            fire(test, player);
            damaged(test, player, target);
            test.assertTrue(weapon(player).isAbilityActive(player, WeaponAbilitySlot.PRIMARY), "Blast consumed the separate critical-hit charge");
            test.assertTrue(player.entityInteractionRange() == reach, "Blast modified melee reach");
            test.assertTrue(weapon(player).abilityCooldownTicks(WeaponAbilitySlot.SECONDARY) == 900, "Secondary cooldown changed");
            weapon(player).cancelAbility(player, WeaponAbilitySlot.PRIMARY);
            target.discard();
        }
        test.succeed();
    }

    @GameTest(template = "sonic_boom_test")
    public static void cooldownCommandPersistsAndRestores(GameTestHelper test) throws Exception {
        ServerPlayer player = player(test, ModItems.WARDENS_NEEDLE.get());
        var server = test.getLevel().getServer();
        // Named targets are online players; register the test player so selectors can resolve it.
        var source = server.createCommandSourceStack().withEntity(player);
        try {
            String command = "no-ability-cooldown @s ";
            var messages = new java.util.ArrayList<Component>();
            var restricted = source.withPermission(0).withSource(new net.minecraft.commands.CommandSource() {
                @Override public void sendSystemMessage(Component message) { messages.add(message); }
                @Override public boolean acceptsSuccess() { return true; }
                @Override public boolean acceptsFailure() { return true; }
                @Override public boolean shouldInformAdmins() { return false; }
            });
            var dispatcher = server.getCommands().getDispatcher();
            test.assertTrue(dispatcher.getRoot().getChild("no-ability-cooldown").canUse(restricted), "Command hidden from chat without operator permission");
            test.assertTrue(dispatcher.execute(command + "true", restricted) == 0
                    && !player.getData(WeaponCooldownAttachments.DISABLED), "Non-operator changed cooldowns");
            test.assertTrue(messages.stream().anyMatch(message -> hasTranslation(message, "commands.forgermod.cooldown.permission")), "Missing explicit permission error");
            dispatcher.execute("no-ability-cooldown", restricted);
            test.assertTrue(messages.stream().anyMatch(message -> hasTranslation(message, "commands.forgermod.cooldown.usage")), "Bare command has no usage help");
            server.getCommands().getDispatcher().execute(command + "true", source);
            test.assertTrue(player.getData(WeaponCooldownAttachments.DISABLED), "Command did not enable bypass");
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).isEmpty(), "Existing deadlines not cleared");
            var save = new net.minecraft.nbt.CompoundTag();
            player.saveWithoutId(save);
            ServerPlayer loaded = player(test, ModItems.WARDENS_NEEDLE.get());
            loaded.load(save);
            test.assertTrue(loaded.getData(WeaponCooldownAttachments.DISABLED), "Bypass was not saved");
            server.getCommands().getDispatcher().execute(command + "false", source);
            test.assertFalse(player.getData(WeaponCooldownAttachments.DISABLED), "Command did not restore cooldowns");
        } finally {
            player.setData(WeaponCooldownAttachments.DISABLED, false);
        }
        test.succeed();
    }

    @GameTest(template = "sonic_boom_test")
    public static void networkStartsCooldownImmediately(GameTestHelper test) throws ReflectiveOperationException {
        ServerPlayer player = player(test, ModItems.WARDENS_NEEDLE.get());
        String id = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString();
        var payload = new WeaponAbilityNetwork.UseAbilityPayload(1, player.getInventory().selected, id);
        IPayloadContext context = (IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(),
                new Class<?>[]{IPayloadContext.class}, (proxy, method, args) -> {
                    if (method.getName().equals("player")) return player;
                    throw new UnsupportedOperationException(method.getName());
                });
        var handle = WeaponAbilityNetwork.class.getDeclaredMethod("handleAbility", WeaponAbilityNetwork.UseAbilityPayload.class, IPayloadContext.class);
        handle.setAccessible(true);
        long now = test.getLevel().getGameTime();
        handle.invoke(null, payload, context); // A miss is still a fired blast and starts cooldown.
        String key = WeaponCooldownKey.of(id, WeaponAbilitySlot.SECONDARY);
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key, 0L) == now + 900, "Instant blast did not start its cooldown");
        Zombie target = enemy(test, player.position().add(0, 0, 10));
        handle.invoke(null, payload, context);
        test.assertTrue(target.getHealth() == 100, "Cooldown allowed another blast");
        var primary = new WeaponAbilityNetwork.UseAbilityPayload(0, player.getInventory().selected, id);
        handle.invoke(null, primary, context);
        test.assertTrue(weapon(player).isAbilityActive(player, WeaponAbilitySlot.PRIMARY), "Secondary cooldown blocked primary");
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(ModItems.WARDENS_NEEDLE_RUBY.get()));
        player.tick();
        String rubyId = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString();
        handle.invoke(null, new WeaponAbilityNetwork.UseAbilityPayload(1, player.getInventory().selected, rubyId), context);
        damaged(test, player, target);
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(
                WeaponCooldownKey.of(rubyId, WeaponAbilitySlot.SECONDARY), 0L) == now + 900, "Gemstone variant did not get its own cooldown");
        test.assertFalse(weapon(player).isAbilityActive(player, WeaponAbilitySlot.PRIMARY), "Switching retained the original primary charge");
        test.succeed();
    }
}
