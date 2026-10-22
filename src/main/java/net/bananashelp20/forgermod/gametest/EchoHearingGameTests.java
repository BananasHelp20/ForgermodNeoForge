package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.EchoHearingAbility;
import net.bananashelp20.forgermod.item.custom.abilities.EchoHearingHighlights;
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
    @GameTest(template="riftfang_test")
    public static void rankProgressionControlsPrivateGlowAndReviewedDescription(GameTestHelper test) {
        var wielder=player(test);
        var mob=EntityType.ZOMBIE.create(test.getLevel());
        mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(wielder.position().add(4,0,0));test.getLevel().addFreshEntity(mob);
        try {
            for(var item:new net.minecraft.world.item.Item[]{ModItems.SHRIEKING_CLAYMORE.get(),ModItems.SHRIEKING_CLAYMORE_JADE.get()}) {
                for(int rank=1;rank<=4;rank++) {
                    var stack=Augmentations.setLevel(new ItemStack(item),RecommendedAbilities.ECHO_HEARING,rank);
                    wielder.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    test.assertTrue(EchoHearingAbility.activate(wielder,stack),"Cannot start rank "+rank);
                    noise(test,GameEvent.STEP,mob);
                    int ticks=net.bananashelp20.forgermod.augmentation.ReviewedRanks.echoGlowTicks(rank)+(JadeAugments.isJade(stack)?20:0);
                    test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==ticks,"Wrong glow duration at rank "+rank);
                    var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(stack,RecommendedAbilities.ECHO_HEARING,rank,false).getString();
                    test.assertTrue(text.contains((24+8*rank)+" block radius") && text.contains(net.bananashelp20.forgermod.augmentation.AbilityDescriptions.number(ticks/20.0)+" seconds"),"Description differs from runtime: "+text);
                    EchoHearingAbility.cancel(wielder.getUUID());
                }
            }
        } finally {
            EchoHearingAbility.cancel(wielder.getUUID());mob.discard();wielder.server.getPlayerList().remove(wielder);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void privateOutlinesKeepExternalGlowingAndCodecTargetsOnlyItsViewer(GameTestHelper test) {
        var wielder=player(test);var observer=player(test);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(wielder.position().add(4,0,0));test.getLevel().addFreshEntity(mob);
        var drop=new ItemEntity(test.getLevel(),wielder.getX()+3,wielder.getY(),wielder.getZ(),new ItemStack(Items.DIAMOND));
        drop.setNoGravity(true);drop.setPickUpDelay(32767);drop.setGlowingTag(true);test.getLevel().addFreshEntity(drop);
        try {
            mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.GLOWING,600,0));
            var potion=mob.getEffect(MobEffects.GLOWING);
            var stack=Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),RecommendedAbilities.ECHO_HEARING);
            wielder.setItemInHand(InteractionHand.MAIN_HAND,stack);EchoHearingAbility.activate(wielder,stack);
            noise(test,GameEvent.STEP,mob);noise(test,GameEvent.ENTITY_ACTION,drop);
            test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==20 && EchoHearingHighlights.remainingTicks(observer,mob)==0,"Private state is shared across viewers or rank-I glow is wrong");
            var packet=new EchoHearingHighlights.Payload(mob.getUUID(),wielder.level().getGameTime()+40,false);
            var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
            try {
                EchoHearingHighlights.Payload.CODEC.encode(buffer,packet);
                var copy=EchoHearingHighlights.Payload.CODEC.decode(buffer);
                test.assertTrue(copy.equals(packet),"Private highlight packet loses target/deadline");
                EchoHearingHighlights.apply(wielder,copy);
                test.assertTrue(EchoHearingHighlights.remainingTicks(observer,mob)==0,"Receiving a packet changes other viewers");
                var stale=new EchoHearingHighlights.Payload(mob.getUUID(),wielder.level().getGameTime()-1,false);
                EchoHearingHighlights.apply(wielder,stale);
                test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==40,"Old packet shortens a refreshed outline");
                var clear=new EchoHearingHighlights.Payload(new java.util.UUID(0,0),0,true);
                EchoHearingHighlights.Payload.CODEC.encode(buffer,clear);
                EchoHearingHighlights.apply(wielder,EchoHearingHighlights.Payload.CODEC.decode(buffer));
                test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==0,"Clear packet leaves private outlines");
                EchoHearingHighlights.apply(wielder,copy);
            } finally {buffer.release();}
            EchoHearingAbility.cancel(wielder.getUUID());
            test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==0 && EchoHearingHighlights.remainingTicks(wielder,drop)==0,"Cancel does not clear private outlines");
            test.assertTrue(mob.getEffect(MobEffects.GLOWING)==potion && potion.getDuration()==600 && drop.isCurrentlyGlowing(),"Echo Hearing alters another source's glowing");
        } finally {
            EchoHearingAbility.cancel(wielder.getUUID());EchoHearingHighlights.clear(wielder);
            mob.discard();drop.discard();wielder.server.getPlayerList().remove(wielder);observer.server.getPlayerList().remove(observer);
        }
        test.succeed();
    }
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
                test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob) == 20+(JadeAugments.isJade(learned)?20:0), "Noisy mob received the wrong private outline duration");
                test.assertTrue(EchoHearingHighlights.remainingTicks(otherPlayer,mob)==0 && !mob.hasEffect(MobEffects.GLOWING), "Outline leaks to another viewer or shared effects");
                noise(test, GameEvent.STEP, otherPlayer);
                test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,otherPlayer)>0 && !otherPlayer.hasEffect(MobEffects.GLOWING), "Noisy other player was excluded or publicly revealed");
                noise(test, GameEvent.STEP, wielder);
                test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,wielder)==0 && !wielder.hasEffect(MobEffects.GLOWING), "Listening player revealed themselves");
                noise(test, GameEvent.ENTITY_ACTION, drop);
                test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,drop)>0 && EchoHearingHighlights.remainingTicks(otherPlayer,drop)==0 && !drop.isCurrentlyGlowing(), "Noisy nonliving entity was excluded or publicly revealed");
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
        test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==0, "Mob outside original radius was revealed");
        wielder.setPos(wielder.getX() + 8, wielder.getY(), wielder.getZ());
        noise(test, GameEvent.STEP, mob);
        test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)>0, "Moving radius did not follow wielder");
        wielder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        test.runAfterDelay(2, () -> {
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(wielder));
            noise(test, GameEvent.STEP, mob);
            test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==0 && !mob.hasEffect(MobEffects.GLOWING), "Switch left listener or private outlines active");
            String key = BuiltInRegistries.ITEM.getKey(learned.getItem()) + ":" + RecommendedAbilities.ECHO_HEARING;
            long ready = wielder.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key, 0L);
            test.assertTrue(ready > wielder.level().getGameTime() + 590, "Switch did not begin 30-second cooldown");
            mob.discard();
            test.getLevel().getServer().getPlayerList().remove(wielder);
            test.succeed();
        });
    }

    @GameTest(template = "riftfang_test", batch = "echo_hearing_expiry", timeoutTicks = 230)
    public static void glowExpiresAfterOneSecondAndListeningEndsAfterTen(GameTestHelper test) throws Exception {
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
        test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)>0, "First noise was not revealed");
        var originalPosition = wielder.position();
        wielder.setPos(originalPosition.add(40, 0, 0));
        test.runAfterDelay(21, () -> {
            test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==0 && !mob.hasEffect(MobEffects.GLOWING), "Private outline outlasted one second");
            wielder.setPos(originalPosition);
            noise(test, GameEvent.STEP, mob);
            test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)>0, "Later noise did not reveal mob again");
        });
        test.runAfterDelay(205, () -> {
            test.assertFalse(RecommendedAbilityRuntime.active(wielder, RecommendedAbilities.ECHO_HEARING), "Listening did not end");
            noise(test, GameEvent.STEP, mob);
            test.assertTrue(EchoHearingHighlights.remainingTicks(wielder,mob)==0 && !mob.hasEffect(MobEffects.GLOWING), "Expired listener reacted to noise");
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
