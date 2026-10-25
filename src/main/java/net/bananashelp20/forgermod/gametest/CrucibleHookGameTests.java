package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookEvents;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CrucibleHookGameTests {
    private static final class RopeConnection extends net.minecraft.network.Connection {
        final java.util.List<net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Payload> ropes=new java.util.ArrayList<>();
        final java.util.List<net.minecraft.sounds.SoundEvent> sounds=new java.util.ArrayList<>();
        RopeConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(this).add(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Payload.TYPE.id());}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush) {
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound && java.util.List.of(net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW,net.minecraft.sounds.SoundEvents.CHAIN_STEP,net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND_HEAVY,net.minecraft.sounds.SoundEvents.ANVIL_LAND,net.minecraft.sounds.SoundEvents.FIRECHARGE_USE).contains(sound.getSound().value()))sounds.add(sound.getSound().value());
            if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom && custom.payload() instanceof net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Payload rope)ropes.add(rope);
        }
    }
    private static RopeConnection ropeConnection(net.minecraft.server.level.ServerPlayer player) {
        var connection=new RopeConnection();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));return connection;
    }
    @GameTest(template="riftfang_test")
    public static void ropeSynchronizesOwnerTrackersAndLateViewersWithoutChangingRealLeashes(GameTestHelper test) {
        var owner=TestPlayers.survival(test);var viewer=TestPlayers.survival(test);var late=TestPlayers.survival(test);
        var ownerPrevious=owner.connection;var viewerPrevious=viewer.connection;var latePrevious=late.connection;
        var ownerWire=ropeConnection(owner);var viewerWire=ropeConnection(viewer);var lateWire=ropeConnection(late);
        owner.setNoGravity(true);owner.setPos(test.getBounds().getCenter());owner.setYRot(0);var origin=owner.position();viewer.setPos(origin.add(8,0,0));late.setPos(origin.add(-8,0,0));
        for(int x=-1;x<=1;x++)for(int y=0;y<=4;y++)for(int z=0;z<=12;z++)test.getLevel().setBlockAndUpdate(owner.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(origin.add(0,0,10));test.getLevel().addFreshEntity(cow);cow.setLeashedTo(viewer,false);
        owner.setXRot((float)Math.toDegrees(Math.atan2(owner.getEyeY()-cow.getBoundingBox().getCenter().y,10)));
        var stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.CRUCIBLE_HOOK);owner.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.tracking(new net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking(viewer,owner));viewerWire.ropes.clear();
            test.assertTrue(CrucibleHookEvents.activate(owner,stack),"Cannot start rope visual");
            var packet=net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.snapshot(owner);
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.target(owner)==cow && packet.link().expires()==test.getLevel().getGameTime()+40,"Rope endpoint or duration differs from hook");
            test.assertTrue(ownerWire.ropes.equals(java.util.List.of(packet)) && viewerWire.ropes.equals(java.util.List.of(packet)) && lateWire.ropes.isEmpty(),"Rope start is not sent to owner and tracking viewers only");
            net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.tracking(new net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking(late,owner));
            test.assertTrue(lateWire.ropes.equals(java.util.List.of(packet)),"Late tracker does not receive current rope");
            var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
            try {net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Payload.CODEC.encode(buffer,packet);test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Payload.CODEC.decode(buffer).equals(packet),"Rope packet loses endpoint identity/expiry");}finally{buffer.release();}
            net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.apply(test.getLevel(),new net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Payload(owner.getId(),java.util.UUID.randomUUID(),net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.NONE));
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.target(owner)==cow,"Stale owner id changes rope");
            net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.apply(test.getLevel(),new net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Payload(owner.getId(),owner.getUUID(),new net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Link(cow.getId(),java.util.UUID.randomUUID(),packet.link().expires())));
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.target(owner)==null,"Reused target id renders the wrong entity");
            net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.apply(test.getLevel(),new net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Payload(owner.getId(),owner.getUUID(),new net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.Link(cow.getId(),cow.getUUID(),test.getLevel().getGameTime())));
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.target(owner)==null,"Expired rope is rendered");
            net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.apply(test.getLevel(),packet);
            owner.setPos(origin.add(.2,0,0));cow.setPos(origin.add(0,0,8));CrucibleHookEvents.onTick(new PlayerTickEvent.Post(owner));
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.target(owner)==cow && ownerWire.ropes.size()==1 && viewerWire.ropes.size()==1,"Moving endpoints require repeated rope state packets");
            net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.stoppedTracking(new net.neoforged.neoforge.event.entity.player.PlayerEvent.StopTracking(viewer,owner));
            CrucibleHookEvents.cancel(owner.getUUID());
            test.assertTrue(ownerWire.ropes.size()==2 && lateWire.ropes.size()==2 && viewerWire.ropes.size()==1 && ownerWire.ropes.getLast().link().targetId()<0,"Rope end is not synchronized or still sent to a stopped tracker");
            test.assertTrue(cow.getLeashHolder()==viewer && net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.target(owner)==null,"Visual changes real leash ownership or survives cancellation");
            owner.setPos(origin);cow.setPos(origin.add(0,0,10));CrucibleHookEvents.activate(owner,stack);cow.discard();CrucibleHookEvents.onTick(new PlayerTickEvent.Post(owner));
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.link(owner).targetId()<0,"Removed target leaves a rope");test.succeed();
        } finally {
            CrucibleHookEvents.cancel(owner.getUUID());cow.discard();
            for(var player:java.util.List.of(owner,viewer,late))net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
            owner.connection=ownerPrevious;viewer.connection=viewerPrevious;late.connection=latePrevious;
            for(var player:java.util.List.of(owner,viewer,late))player.server.getPlayerList().remove(player);
        }
    }
    @GameTest(template="riftfang_test")
    public static void ropeCurveHasAnchoredEndsBoundedSagAndNativeLeashVertexBudget(GameTestHelper test) {
        for(var end:java.util.List.of(new net.minecraft.world.phys.Vec3(20,1,0),new net.minecraft.world.phys.Vec3(0,20,0),new net.minecraft.world.phys.Vec3(0,1,0))) {
            var start=new net.minecraft.world.phys.Vec3(0,1,0);
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.point(start,end,0).equals(start) && net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.point(start,end,24).equals(end),"Rope endpoints drift from player/target");
            for(int i=0;i<=24;i++) {
                var point=net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.point(start,end,i);double straight=start.lerp(end,i/24.0).y;
                test.assertTrue(Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z) && point.y<=straight+.0001 && point.y>=straight-.5001,"Rope curve is invalid or sags too far");
            }
        }
        test.assertTrue(4*(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.SEGMENTS+1)==100,"Rope exceeds native leash geometry budget");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void smashWaitsForFixedTwoBlockPointAndActualMeleeReach(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.setYRot(0);player.setXRot(0);
        var start=player.position();var destination=start.add(0,0,2);
        for(int x=-3;x<=10;x++)for(int y=0;y<=4;y++)for(int z=-3;z<=12;z++)test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        var stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.CRUCIBLE_HOOK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setHealth(200);mob.setPos(start.add(0,0,2.8));test.getLevel().addFreshEntity(mob);
        var old=player.connection;var wire=new RopeConnection();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        try {
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.CRUCIBLE_HOOK),"Cannot start close-range hook");
            var castSounds=java.util.List.of(net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW,net.minecraft.sounds.SoundEvents.CHAIN_STEP);
            test.assertTrue(wire.sounds.equals(castSounds),"Hook cast is silent or plays smash before arrival");
            test.assertTrue(CrucibleHookEvents.active(player) && mob.getHealth()==200 && mob.getRemainingFireTicks()<=0 && stack.getDamageValue()==0,"Entering ordinary melee reach smashes before arriving");
            player.setYRot(90);player.setXRot(-85);player.setPos(start.add(.75,0,0));mob.setPos(start.add(0,0,2.6));
            CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
            test.assertTrue(CrucibleHookEvents.active(player) && mob.getHealth()==200 && Math.abs(mob.getDeltaMovement().x)<.0001 && mob.getDeltaMovement().z<0,"Turning/moving shifts the captured point or smashes early");
            player.setPos(start.add(8,0,0));mob.setPos(destination);mob.setDeltaMovement(1,1,1);CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
            test.assertTrue(CrucibleHookEvents.active(player) && mob.getHealth()==200 && mob.getDeltaMovement().lengthSqr()==0,"Arrival smashes outside actual reach or retains incoming pull velocity");
            test.assertTrue(wire.sounds.equals(castSounds),"Pull or out-of-reach arrival plays impact sound");
            player.setPos(start.add(.75,0,0));CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
            float expected=200-(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*1.25F;
            test.assertTrue(!CrucibleHookEvents.active(player) && Math.abs(mob.getHealth()-expected)<.001 && mob.getRemainingFireTicks()==60 && stack.getDamageValue()==1,"Arrived target does not receive exactly one smash");
            var allSounds=java.util.List.of(net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW,net.minecraft.sounds.SoundEvents.CHAIN_STEP,net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND_HEAVY,net.minecraft.sounds.SoundEvents.ANVIL_LAND,net.minecraft.sounds.SoundEvents.FIRECHARGE_USE);
            test.assertTrue(wire.sounds.equals(allSounds),"Arrived smash does not follow grappling with a fiery Vertical Strike");
            CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));test.assertTrue(Math.abs(mob.getHealth()-expected)<.001 && stack.getDamageValue()==1,"Arrival repeats the smash");
            test.assertTrue(wire.sounds.equals(allSounds),"Hook impact audio repeats after smash");
            test.succeed();
        } finally {CrucibleHookEvents.cancel(player.getUUID());mob.discard();player.connection=old;player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void passiveTeamAlliesAndOwnedPetsReceiveTheWholeHookAndSmash(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.setYRot(0);
        for(int x=-2;x<=2;x++)for(int y=0;y<=4;y++)for(int z=0;z<=12;z++)test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        var team=player.getScoreboard().addPlayerTeam("hook_"+player.getUUID().toString().substring(0,8));player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
        var cow=EntityType.COW.create(test.getLevel());var wolf=EntityType.WOLF.create(test.getLevel());wolf.tame(player);
        var stand=EntityType.ARMOR_STAND.create(test.getLevel());stand.setPos(player.position().add(0,0,4));stand.setNoGravity(true);test.getLevel().addFreshEntity(stand);
        player.getScoreboard().addPlayerToTeam(cow.getScoreboardName(),team);
        cow.setPos(player.position().add(8,0,0));wolf.setPos(player.position().add(8,0,2));test.getLevel().addFreshEntity(cow);test.getLevel().addFreshEntity(wolf);
        try {
            test.assertTrue(cow.isAlliedTo(player) && wolf.isAlliedTo(player),"Hook fixtures are not allies");
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof net.bananashelp20.forgermod.item.custom.IgnisiumWeapon weapon) || !weapon.isAxe())continue;
                var stack=Augmentations.setLevel(new ItemStack(entry.get()),RecommendedAbilities.CRUCIBLE_HOOK,4);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
                for(var mob:java.util.List.of(cow,wolf)) {
                    mob.goalSelector.removeAllGoals(goal->true);mob.targetSelector.removeAllGoals(goal->true);mob.setNoGravity(true);
                    mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setHealth(200);mob.invulnerableTime=0;mob.setRemainingFireTicks(0);mob.setDeltaMovement(0,0,0);mob.setPos(player.position().add(0,0,10));
                    double drop=player.getEyeY()-mob.getBoundingBox().getCenter().y;player.setXRot((float)Math.toDegrees(Math.atan2(drop,10)));
                    var before=mob.position();int wear=stack.getDamageValue();
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.CRUCIBLE_HOOK),"Cannot acquire passive/allied mob");
                    test.assertTrue(CrucibleHookEvents.active(player) && mob.position().equals(before) && mob.getDeltaMovement().z<0 && mob.getHealth()==200,"Allied hook cancels, teleports or damages during pull");
                    for(int tick=0;tick<30 && CrucibleHookEvents.active(player);tick++){mob.tick();CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));}
                    test.assertTrue(mob.position().distanceTo(player.position().add(0,0,2))<=.251,"Allied smash occurs before reaching the two-block destination");
                    test.assertTrue(!CrucibleHookEvents.active(player) && Math.abs(mob.getHealth()-(200-player.getAttributeValue(Attributes.ATTACK_DAMAGE)*2))<.001
                        && mob.getRemainingFireTicks()==120 && stack.getDamageValue()==wear+1,"Allied mob does not receive normal smash damage, ignition and weapon wear");
                    mob.setPos(player.position().add(8,0,0));
                }
            }
            test.assertTrue(stand.getHealth()==stand.getMaxHealth() && stand.getDeltaMovement().lengthSqr()==0,"Nonmob armor stand is hooked or damaged");
            test.succeed();
        } finally {CrucibleHookEvents.cancel(player.getUUID());player.getScoreboard().removePlayerTeam(team);cow.discard();wolf.discard();stand.discard();player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsPullWithoutTeleportThenSmashForDoubleDamage(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),ModItems.MOLTEN_AXE_AMBER.get(),
                ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get()}) {
            var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
            ItemStack stack=new ItemStack(item);
            for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.CRUCIBLE_HOOK);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.doTick();
            var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoGravity(true);
            // Disable decisions while retaining ordinary mob travel/velocity physics.
            zombie.goalSelector.removeAllGoals(goal->true); zombie.targetSelector.removeAllGoals(goal->true);
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); zombie.setHealth(200); zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
            zombie.setPos(player.position().add(0,0,10)); test.getLevel().addFreshEntity(zombie);
            var before=zombie.position();
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.CRUCIBLE_HOOK),"Hook cannot acquire aimed hostile");
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.target(player)==zombie,"Hostile pull lacks rope endpoint");
            test.assertTrue(zombie.position().equals(before) && zombie.getDeltaMovement().z<0,"Hook teleports instead of applying pull velocity");
            float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*2;
            for(int tick=0;tick<30 && CrucibleHookEvents.active(player);tick++) {
                zombie.tick(); CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
            }
            test.assertFalse(CrucibleHookEvents.active(player),"Hook never reaches/strikes pulled target");
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope.link(player).targetId()<0,"Smash leaves the rope active");
            test.assertTrue(zombie.position().distanceTo(player.position().add(0,0,2))<=.251,"Hostile smash occurs before reaching the fixed two-block destination");
            test.assertTrue(Math.abs(zombie.getHealth()-(200-damage))<.001,"Rank IV smash does not deal double weapon damage: health="
                    +zombie.getHealth()+" expected="+(200-damage)+" armor="+zombie.getArmorValue());
            test.assertTrue(zombie.getRemainingFireTicks()==120 && stack.getDamageValue()==1,"Smash does not ignite target or wear weapon");
            zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void obstructionAndSwitchPreventHitAndStartCancellationCooldown(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
        var stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.CRUCIBLE_HOOK);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoAi(true); zombie.setNoGravity(true); zombie.setPos(player.position().add(0,0,10));
        test.getLevel().addFreshEntity(zombie); var wall=player.blockPosition().south(2).above();
        test.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
        AbilityTestPackets.use(player,0); test.assertFalse(CrucibleHookEvents.active(player),"Hook targets an enemy through a wall");
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.CRUCIBLE_HOOK;
        test.assertFalse(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Failed hook starts cooldown");
        test.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState()); AbilityTestPackets.use(player,0);
        test.assertTrue(CrucibleHookEvents.active(player),"Unobstructed packet activation failed");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
        test.assertFalse(CrucibleHookEvents.active(player),"Switch leaves hook running");
        test.assertTrue(zombie.getHealth()==20,"Canceled hook still smashes target");
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)==player.level().getGameTime()+800,"Canceled hook lacks full cooldown");
        zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
