package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class VoidPulseGameTests {
    @GameTest(template="riftfang_test")
    public static void approvedDescriptionHasOnlyCurrentRadiusWhite(GameTestHelper test) {
        int count=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            if(!(entry.get() instanceof InanisiumWeapon)) continue;
            count++;
            for(int rank=1;rank<=4;rank++) for(boolean compact:new boolean[]{false,true}) {
                var text=AbilityDescriptions.description(new ItemStack(entry.get()),AdditionalAbilities.VOID_PULSE,rank,compact);var white=new StringBuilder();
                text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                test.assertTrue(text.getString().equals("Push living entities in a "+(3+rank)+" block radius away from you."),"Wrong current pulse description");
                test.assertTrue(white.toString().equals((3+rank)+" block radius"),"Wrong pulse highlighting");
            }
        }
        test.assertTrue(count==15,"Skipped pulse variants");test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_void_pulse_all_living")
    public static void pulsePushesNonhostilePlayersButNotCasterOrItems(GameTestHelper test) throws Exception {
        var owner=TestPlayers.survival(test);var other=TestPlayers.survival(test);
        var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        for(int x=6;x<=12;x++) for(int y=5;y<=8;y++) for(int z=6;z<=10;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        owner.setPos(origin);other.setPos(origin.add(2,0,0));
        var item=new net.minecraft.world.entity.item.ItemEntity(test.getLevel(),origin.x,origin.y,origin.z+2,new ItemStack(Items.STONE));item.setNoGravity(true);test.getLevel().addFreshEntity(item);
        boolean pvp=owner.server.isPvpAllowed();owner.server.setPvpAllowed(false);
        var release=VoidPulseAbility.class.getDeclaredMethod("release",net.minecraft.server.level.ServerPlayer.class,int.class);release.setAccessible(true);
        try {
            test.assertFalse(WeaponAbilityTargets.enemy(owner,other),"Fixture player is hostile");
            for(int rank=1;rank<=4;rank++) {
                owner.setDeltaMovement(Vec3.ZERO);other.setDeltaMovement(Vec3.ZERO);item.setDeltaMovement(Vec3.ZERO);float health=other.getHealth();
                release.invoke(null,owner,rank);
                test.assertTrue(Math.abs(other.getDeltaMovement().x-(.4+.2*rank))<.0001,"Nonhostile player is excluded or has wrong rank knockback");
                test.assertTrue(owner.getDeltaMovement().equals(Vec3.ZERO) && item.getDeltaMovement().equals(Vec3.ZERO),"Pulse affects caster or nonliving item");
                test.assertTrue(other.getHealth()==health,"Pulse deals damage");
            }
        } finally {owner.server.setPvpAllowed(pvp);item.discard();owner.server.getPlayerList().remove(other);owner.server.getPlayerList().remove(owner);}
        test.succeed();
    }
    private static final class SoundWire extends net.minecraft.network.Connection {
        final java.util.List<net.minecraft.network.protocol.game.ClientboundSoundPacket> sounds=new java.util.ArrayList<>();
        SoundWire(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound)sounds.add(sound);
        }
    }
    @GameTest(template="riftfang_test",timeoutTicks=50)
    public static void reversedPortalChargePrecedesForwardPortalReleaseAndCancellationDoesNotBurst(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var old=player.connection;var wire=new SoundWire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        Runnable cleanup=()->{VoidPulseAbility.cancel(player.getUUID());WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.connection=old;player.server.getPlayerList().remove(player);};
        try{
            var stack=Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()),AdditionalAbilities.VOID_PULSE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            AbilityTestPackets.use(player,0);AbilityTestPackets.use(player,0);
            test.assertTrue(wire.sounds.size()==1 && wire.sounds.getFirst().getSound().value()==net.bananashelp20.forgermod.sound.ModSounds.VOID_PULSE_IMPLODE.get(),"Charge is silent, duplicated or plays the forward portal sound");
            test.runAfterDelay(9,()->{
                test.assertTrue(VoidPulseAbility.active(player) && wire.sounds.size()==1,"Forward portal sound starts before the inward charge finishes");
            });
            test.runAfterDelay(11,()->{try{
                VoidPulseAbility.active(player);
                test.assertTrue(wire.sounds.size()==2 && wire.sounds.getLast().getSound().value()==net.bananashelp20.forgermod.sound.ModSounds.VOID_PULSE_RELEASE.get(),"Outward phase does not play the matching forward portal sound");
                VoidPulseAbility.active(player);test.assertTrue(wire.sounds.size()==2,"Completed pulse repeats release audio");
                test.assertTrue(VoidPulseAbility.activate(player,stack),"Cancellation fixture cannot charge");player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));VoidPulseAbility.active(player);
                test.runAfterDelay(11,()->{try{VoidPulseAbility.active(player);test.assertTrue(wire.sounds.size()==3 && wire.sounds.getLast().getSound().value()==net.bananashelp20.forgermod.sound.ModSounds.VOID_PULSE_IMPLODE.get(),"Canceled charge plays an outward sound");test.succeed();}finally{cleanup.run();}});
            }catch(Exception|Error failure){cleanup.run();throw failure;}});
        }catch(Exception|Error failure){cleanup.run();throw failure;}
    }
    @GameTest(template="riftfang_test")
    public static void allFifteenVariantsActivateThroughKeysAndKeepIndependentCooldowns(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof InanisiumWeapon))continue;count++;
                var stack=Augmentations.apply(new ItemStack(entry.get()),AdditionalAbilities.VOID_PULSE);test.assertFalse(stack.isEmpty(),"Dagger/axe refuses Void Pulse augmentation");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                AbilityTestPackets.use(player,0);test.assertTrue(VoidPulseAbility.active(player),"Configured ability slot cannot dispatch Void Pulse");
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
                String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+AdditionalAbilities.VOID_PULSE;
                test.assertTrue(!VoidPulseAbility.active(player) && player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)==player.level().getGameTime()+600,"Variant cooldown/cancellation wrong");
            }
            test.assertTrue(count==15,"Skipped Inanisium variants");
        } finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void particleMotionContractsThenBurstsAndPacketKeepsItsPhase(GameTestHelper test) {
        var offset=new Vec3(7,2,-3);double distance=offset.length();
        for(int tick=1;tick<=10;tick++){var next=net.bananashelp20.forgermod.particle.VoidPulseMotion.inward(offset,tick/10.0);test.assertTrue(next.length()<distance,"Contraction moves away from player");distance=next.length();}
        test.assertTrue(distance==0,"Contraction fails to meet player");
        for(int i=0;i<96;i++)test.assertTrue(net.bananashelp20.forgermod.particle.VoidPulseMotion.outward(i,7).length()>1.4,"Outward burst is not fast");
        for(boolean outward:new boolean[]{false,true}) {
            var packet=new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(new net.bananashelp20.forgermod.particle.VoidPulseParticles.Options(outward,123),false,2,3,4,1.5f,.2f,-1.5f,1,0);
            var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
            try {
                net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket.STREAM_CODEC.encode(buffer,packet);var copy=net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket.STREAM_CODEC.decode(buffer);
                test.assertTrue(copy.getParticle() instanceof net.bananashelp20.forgermod.particle.VoidPulseParticles.Options options && options.outward()==outward && options.ownerId()==123 && copy.getCount()==0 && copy.getXDist()==1.5f && copy.getZDist()==-1.5f,"Particle packet loses phase/owner/directional velocity");
            } finally {buffer.release();}
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=80)
    public static void maxRankPulseRespectsRadiusAndStartsCooldownOnRelease(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        for(int x=1;x<=18;x++)for(int y=5;y<=11;y++)for(int z=1;z<=18;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var near=EntityType.COW.create(test.getLevel());var far=EntityType.COW.create(test.getLevel());for(var mob:java.util.List.of(near,far)){mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);}near.setPos(point.add(6.5,0,0));far.setPos(point.add(7.5,0,0));
        ItemStack stack=new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get());for(int i=0;i<4;i++)stack=Augmentations.apply(stack,AdditionalAbilities.VOID_PULSE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);AbilityTestPackets.use(player,0);
        test.runAfterDelay(11,()->{try {
            test.assertFalse(VoidPulseAbility.active(player),"Pulse never finishes");WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(near.getDeltaMovement().x>0 && far.getDeltaMovement().equals(Vec3.ZERO),"Rank IV radius changed");
            String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem())+":"+AdditionalAbilities.VOID_PULSE;
            long now=player.level().getGameTime(),ready=player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key);test.assertTrue(ready>=now+298 && ready<=now+300,"Release cooldown wrong");test.succeed();
        } finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));near.discard();far.discard();player.server.getPlayerList().remove(player);}});
    }
    @GameTest(template="riftfang_test",timeoutTicks=240)
    public static void allVariantsChargeThenPushHostilePassiveAndResistantMobsWithoutDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        for(int x=1;x<=18;x++)for(int y=5;y<=11;y++)for(int z=1;z<=18;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var front=EntityType.ZOMBIE.create(test.getLevel());var rear=EntityType.ZOMBIE.create(test.getLevel());var neutral=EntityType.COW.create(test.getLevel());var resistant=EntityType.IRON_GOLEM.create(test.getLevel());
        front.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));rear.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));resistant.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        var mobs=java.util.List.<Mob>of(front,rear,neutral,resistant);for(var mob:mobs){mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);}
        var variants=new java.util.ArrayList<Item>();
        for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof InanisiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.VOID_PULSE))==eligible,"Wrong Void Pulse assignment");if(eligible)variants.add(entry.get());
        }
        Runnable cleanup=()->{VoidPulseAbility.cancel(player.getUUID());for(var mob:mobs)mob.discard();player.server.getPlayerList().remove(player);};
        class Cases {
            void run(int index) {
                front.setPos(point.add(0,0,3));rear.setPos(point.add(0,0,-3));neutral.setPos(point.add(2,0,0));resistant.setPos(point.add(-2,0,0));for(var mob:mobs)mob.setDeltaMovement(Vec3.ZERO);
                var fresh=new ItemStack(variants.get(index));player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.VOID_PULSE),"Fresh blade casts");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.VOID_PULSE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.VOID_PULSE),"Learned pulse rejected");
                test.assertTrue(VoidPulseAbility.active(player) && mobs.stream().allMatch(m->m.getDeltaMovement().equals(Vec3.ZERO)),"Push happens before particle contraction finishes");
                test.runAfterDelay(11,()->{
                    test.assertFalse(VoidPulseAbility.active(player),"Charge never releases");
                    test.assertTrue(front.getDeltaMovement().z>0 && rear.getDeltaMovement().z<0 && neutral.getDeltaMovement().x>0 && resistant.getDeltaMovement().x<0,"Hostile/passive/resistant mob not pushed outward");
                    test.assertTrue(front.getHealth()==20 && rear.getHealth()==20 && neutral.getHealth()==10 && resistant.getHealth()==100,"Pulse deals damage");
                    if(index+1<variants.size())run(index+1);
                    else {
                        test.assertTrue(variants.size()==15,"Skipped variants");front.setPos(point.add(0,0,3));front.setDeltaMovement(Vec3.ZERO);test.setBlock(new BlockPos(8,6,10),Blocks.STONE);VoidPulseAbility.activate(player,stack);
                        test.runAfterDelay(11,()->{try {VoidPulseAbility.active(player);test.assertTrue(front.getDeltaMovement().equals(Vec3.ZERO),"Push passes through wall");test.succeed();}finally {cleanup.run();}});
                    }
                });
            }
        }
        new Cases().run(0);
    }
    @GameTest(template="riftfang_test",timeoutTicks=80)
    public static void switchingCancelsWindupAndStartsExistingCooldown(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());var stack=Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()),AdditionalAbilities.VOID_PULSE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(player.position().add(2,0,0));test.getLevel().addFreshEntity(cow);
        AbilityTestPackets.use(player,0);test.assertTrue(VoidPulseAbility.active(player),"Windup is not ongoing");player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+AdditionalAbilities.VOID_PULSE;
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key)==player.level().getGameTime()+600,"Switch fails to start cooldown");
        test.runAfterDelay(11,()->{try {test.assertFalse(VoidPulseAbility.active(player),"Canceled pulse returns");test.assertTrue(cow.getDeltaMovement().equals(Vec3.ZERO),"Canceled charge pushes mobs");test.succeed();}finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));cow.discard();player.server.getPlayerList().remove(player);}});
    }
}
