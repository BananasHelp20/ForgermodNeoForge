package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.SkullThrowEvents;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SkullThrowGameTests {
    @GameTest(template="empty")
    public static void reviewedSkullDescriptionAndPreviewsMatchConditionalCurrentValues(GameTestHelper test) {
        int checked=0;
        for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof MorsiumWeapon weapon && weapon.isAxe())for(int rank=1;rank<=4;rank++) {
            checked++;var stack=Augmentations.setLevel(new ItemStack(entry.get()),AdditionalAbilities.SKULL_THROW,rank);
            boolean jade=JadeAugments.isJade(stack);int seconds=8+2*rank+(jade?1:0);
            String radius=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.number(1.5+.5*rank)+" blocks";
            for(boolean compact:new boolean[]{false,true}) {
                var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(stack,AdditionalAbilities.SKULL_THROW,rank,compact);
                var white=new StringBuilder();var green=new StringBuilder();var gray=new StringBuilder();
                text.visit((style,part)->{int color=style.getColor()==null?-1:style.getColor().getValue();
                    if(color==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);
                    if(color==net.minecraft.ChatFormatting.GREEN.getColor())green.append(part);
                    if(color==net.minecraft.ChatFormatting.GRAY.getColor())gray.append(part);
                    return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                test.assertTrue(text.getString().equals("Fire a fast skull projectile that bursts within "+radius+" and applies Wither "+(jade?"II":"I")+" for "+seconds+" seconds."),"Skull description uses wrong current values or stale wording");
                test.assertTrue(white.toString().equals(radius+(jade?"":seconds+" seconds")) && green.toString().equals(jade?"Wither II"+seconds+" seconds":""),"Skull radius/effect/duration conditional styles are wrong");
                test.assertTrue(gray.toString().contains(" and applies ") && gray.toString().contains(" for ") && (jade || gray.toString().contains("Wither I")),"Unannotated Skull Throw prose is not gray");
            }
            if(rank<4)test.assertTrue(net.bananashelp20.forgermod.augmentation.AugmentUpgradeDescriptions.changes(stack,AdditionalAbilities.SKULL_THROW,rank).stream().anyMatch(change->change.property().equals("duration") && ((Number)change.after()).doubleValue()==seconds+2),"Upgrade preview omits the longer Wither duration");
        }
        test.assertTrue(checked==20,"Skipped Skull Throw variant/rank descriptions");test.succeed();
    }
    private static WitherSkull skull(net.minecraft.server.level.ServerPlayer player) {
        return player.level().getEntitiesOfClass(WitherSkull.class,player.getBoundingBox().inflate(5),e->e.getOwner()==player).getFirst();
    }
    private static int ticketCount(WitherSkull skull) throws Exception {
        var field=skull.getClass().getDeclaredField("tickets");field.setAccessible(true);
        return ((java.util.Set<?>)field.get(skull)).size();
    }
    @SuppressWarnings("unchecked")
    private static void retainFlightTicketsDuringAcceleratedTestClock(WitherSkull skull)throws Exception{
        var field=skull.getClass().getDeclaredField("tickets");field.setAccessible(true);
        var typeField=SkullThrowEvents.class.getDeclaredField("FLIGHT_TICKET");typeField.setAccessible(true);
        var type=(net.minecraft.server.level.TicketType<java.util.UUID>)typeField.get(null);
        for(var chunk:java.util.Set.copyOf((java.util.Set<net.minecraft.world.level.ChunkPos>)field.get(skull)))
            ((net.minecraft.server.level.ServerLevel)skull.level()).getChunkSource().addRegionTicket(type,chunk,3,skull.getUUID());
    }
    private static final class RecordingConnection extends net.minecraft.network.Connection {
        final java.util.List<net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket> particles=new java.util.ArrayList<>();
        final java.util.List<net.minecraft.sounds.SoundEvent> sounds=new java.util.ArrayList<>();
        RecordingConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush) {
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket particle) particles.add(particle);
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound)sounds.add(sound.getSound().value());
        }
        boolean explosionAndDeathImpact(){return sounds.size()==4
            && java.util.Collections.frequency(sounds,net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value())==1
            && java.util.Collections.frequency(sounds,net.minecraft.sounds.SoundEvents.SKELETON_DEATH)==1
            && !sounds.contains(net.minecraft.sounds.SoundEvents.BONE_BLOCK_BREAK)
            && !sounds.contains(net.minecraft.sounds.SoundEvents.SOUL_ESCAPE.value());}
        boolean burst(){return particles.stream().anyMatch(p->p.getParticle() instanceof net.minecraft.core.particles.BlockParticleOption block && block.getState().is(Blocks.BONE_BLOCK))
            && particles.stream().anyMatch(p->p.getParticle()==net.minecraft.core.particles.ParticleTypes.EXPLOSION);}
    }
    @GameTest(template="riftfang_test",timeoutTicks=100)
    public static void everyVariantAndRankStartsCooldownAtLaunchWithoutRestartOnImpactOrSwitch(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setXRot(-90);
        player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,8,8))));
        int checked=0;
        for(var entry:ModItems.ITEMS.getEntries()){
            if(!(entry.get() instanceof MorsiumWeapon weapon) || !weapon.isAxe())continue;
            for(int rank=1;rank<=4;rank++){
                var stack=Augmentations.setLevel(new ItemStack(entry.get()),AdditionalAbilities.SKULL_THROW,rank);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());
                AbilityTestPackets.use(player,0);var rock=skull(player);String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(entry.get())+":"+AdditionalAbilities.SKULL_THROW;
                long ready=player.level().getGameTime()+Augmentations.cooldown(stack,net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot.PRIMARY,weapon);
                test.assertTrue(java.util.Objects.equals(player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS).get(key),ready),"Skull cooldown missing at launch: "+key+" rank "+rank);
                AbilityTestPackets.use(player,0);test.assertTrue(rock.isAlive() && SkullThrowEvents.active(player),"Cooldown recast replaces active skull");
                SkullThrowEvents.cancel(player.getUUID());net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));checked++;
            }
        }
        test.assertTrue(checked==20,"Skipped Skull Throw variants/ranks");
        var stack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.SKULL_THROW);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
        player.setData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());AbilityTestPackets.use(player,0);
        var deadlines=player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS);var rock=skull(player);
        test.runAfterDelay(5,()->{
            var point=rock.position();SkullThrowEvents.impact(new ProjectileImpactEvent(rock,new net.minecraft.world.phys.BlockHitResult(point,net.minecraft.core.Direction.UP,BlockPos.containing(point),false)));
            net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
            test.assertTrue(!rock.isAlive() && player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS).equals(deadlines),"Impact restarts launch cooldown");
            player.setData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());
            try{AbilityTestPackets.use(player,0);}catch(Exception failure){throw new RuntimeException(failure);}
            var switchedDeadline=player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS);
            test.runAfterDelay(5,()->{
                try{
                    player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                    net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                    test.assertTrue(!SkullThrowEvents.active(player) && player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS).equals(switchedDeadline),"Weapon switch restarts launch cooldown");test.succeed();
                }finally{SkullThrowEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
            });
        });
    }
    @GameTest(template="riftfang_test",timeoutTicks=830)
    public static void fixedFiveHundredBlockRangeAtEveryRankAndBeyondPlayerChunks(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,8,8))).with(net.minecraft.core.Direction.Axis.Y,test.getLevel().getMaxBuildHeight()+20);
        player.setPos(start);player.setXRot(-90);
        for(int rank=1;rank<=4;rank++) {
            var stack=Augmentations.setLevel(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.SKULL_THROW,rank);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);SkullThrowEvents.activate(player,stack);
            var skull=skull(player);var launch=skull.position();int ticks=0;
            test.assertTrue(!skull.shouldBeSaved() && !skull.canUsePortal(true) && !skull.canChangeDimensions(test.getLevel(),test.getLevel()),"Temporary safe skull can be saved or cloned into an unsafe vanilla skull");
            while(skull.isAlive() && ticks++<400) {skull.tick();test.assertTrue(ticketCount(skull)<=2,"Flight retains traversed chunk tickets");}
            test.assertTrue(!skull.isAlive() && Math.abs(skull.position().distanceTo(launch)-500)<.001,"Range is not exactly 500 blocks at rank "+rank);
            test.assertTrue(ticks>100 && ticks<300 && ticketCount(skull)==0 && !SkullThrowEvents.active(player),"Flight expires too early, moves too slowly or leaks tickets");
        }
        var previewStack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.SKULL_THROW);player.setItemInHand(InteractionHand.MAIN_HAND,previewStack);
        SkullThrowEvents.activate(player,previewStack);var preview=skull(player);preview.tick();
        test.assertTrue(ticketCount(preview)>0,"Travel does not acquire a temporary chunk ticket");
        SkullThrowEvents.cancel(player.getUUID());test.assertTrue(!preview.isAlive() && ticketCount(preview)==0,"Cancellation retains flight tickets");
        player.setXRot(0);player.setYRot(-90);
        var stack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.SKULL_THROW);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        SkullThrowEvents.activate(player,stack);var skull=skull(player);var launch=skull.position();
        // Chunk activation can delay real entity ticks relative to the GameTest clock.
        boolean[] checkedFlight={false};
        test.onEachTick(()->{
            // GameTest advances hundreds of game ticks while async chunk work is still pending.
            // Retain only tickets the real projectile already issued; do not drive its physics manually.
            try{if(skull.isAlive())retainFlightTicketsDuringAcceleratedTestClock(skull);}catch(Exception failure){SkullThrowEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);throw new RuntimeException(failure);}
            if(!checkedFlight[0] && skull.tickCount>=101) {
                test.assertTrue(SkullThrowEvents.active(player) && skull.position().distanceTo(launch)>200,"Flight still has a five-second expiry or fails to advance outside player chunks");
                checkedFlight[0]=true;
            }
            if(!skull.isAlive()){
                try{test.assertTrue(checkedFlight[0] && !SkullThrowEvents.active(player) && Math.abs(skull.position().distanceTo(launch)-500)<.001 && ticketCount(skull)==0,"Natural flight ends early or leaks tickets");test.succeed();}
                catch(Exception failure){throw new RuntimeException(failure);}
                finally{SkullThrowEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
            }
        });
        test.runAfterDelay(800,()->{
            try {test.assertTrue(checkedFlight[0] && !skull.isAlive() && !SkullThrowEvents.active(player) && Math.abs(skull.position().distanceTo(launch)-500)<.001 && ticketCount(skull)==0,"Natural flight does not reach its fixed range and clean up: alive="+skull.isAlive()+" distance="+skull.position().distanceTo(launch)+" tickets="+ticketCount(skull)+" removed="+skull.getRemovalReason());test.succeed();}
            catch(Exception exception){throw new RuntimeException(exception);}
            finally {SkullThrowEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
        });
    }
    @GameTest(template="riftfang_test")
    public static void fastSkullBurstsOnRealGroundAndPassiveEntityImpacts(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var previous=player.connection;
        var connection=new RecordingConnection();new net.minecraft.server.network.ServerGamePacketListenerImpl(test.getLevel().getServer(),connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        for(int x=5;x<=11;x++)for(int z=5;z<=13;z++){test.setBlock(new BlockPos(x,4,z),Blocks.STONE);for(int y=5;y<=12;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);}
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(start.add(0,0,3));test.getLevel().addFreshEntity(cow);
        var stack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.SKULL_THROW);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            player.setPos(start.add(0,5,0));player.setXRot(90);SkullThrowEvents.activate(player,stack);var skull=skull(player);
            test.assertTrue(connection.sounds.contains(net.minecraft.sounds.SoundEvents.WITHER_SHOOT) && connection.sounds.contains(net.minecraft.sounds.SoundEvents.TRIDENT_THROW.value()),"Skull launch lacks spectral projectile whoosh");
            for(int i=0;i<5 && skull.isAlive();i++)skull.tick();
            test.assertTrue(!skull.isAlive() && connection.burst() && ticketCount(skull)==0 && test.getBlockState(new BlockPos(8,4,8)).is(Blocks.STONE),"Ground impact does not burst into bone fragments or damages terrain");
            test.assertTrue(connection.explosionAndDeathImpact(),"Ground impact does not play exactly one explosion plus one skeleton death");
            player.setPos(start);player.setXRot(0);player.setYRot(0);connection.particles.clear();connection.sounds.clear();SkullThrowEvents.activate(player,stack);skull=skull(player);
            for(int i=0;i<3 && skull.isAlive();i++)skull.tick();
            test.assertTrue(!skull.isAlive() && connection.burst() && cow.getHealth()==cow.getMaxHealth() && !cow.hasEffect(net.minecraft.world.effect.MobEffects.WITHER),"Passive entity does not burst the skull safely");
            test.assertTrue(connection.explosionAndDeathImpact(),"Entity impact duplicates or omits explosion/skeleton death layers");
            test.succeed();
        } finally {SkullThrowEvents.cancel(player.getUUID());cow.discard();player.connection=previous;player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test", timeoutTicks=100)
    public static void realProjectileHitsWallAndLeavesTerrainAndHiddenEnemyUntouched(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setYRot(0);player.setXRot(0);
        for(int x=6;x<=10;x++) for(int y=5;y<=8;y++) for(int z=7;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        for(int y=5;y<=7;y++) test.setBlock(new BlockPos(8,y,10),Blocks.STONE);
        var hidden=EntityType.ZOMBIE.create(test.getLevel());hidden.setNoAi(true);hidden.setNoGravity(true);
        hidden.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));
        hidden.setPos(origin.add(0,0,4));test.getLevel().addFreshEntity(hidden);
        var stack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.SKULL_THROW);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.SKULL_THROW),"Cannot launch real rock");
        test.runAfterDelay(10,()->{
            try {
                test.assertFalse(SkullThrowEvents.active(player),"Real projectile ignores stone wall");
                test.assertTrue(hidden.getHealth()==hidden.getMaxHealth() && !hidden.hasEffect(net.minecraft.world.effect.MobEffects.WITHER),"Blast damages hidden enemy");
                test.assertTrue(test.getBlockState(new BlockPos(8,6,10)).is(Blocks.STONE),"Magma blast changes terrain");
            } finally {SkullThrowEvents.cancel(player.getUUID());hidden.discard();player.server.getPlayerList().remove(player);}
            test.succeed();
        });
    }

    @GameTest(template="riftfang_test")
    public static void impactUsesReviewedWitherAtEveryRankAndVariant(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);player.setYRot(0);player.setXRot(0);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=6;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(point.add(0,0,3));
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);test.getLevel().addFreshEntity(target);
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                if(!(entry.get() instanceof MorsiumWeapon weapon) || !weapon.isAxe()) continue;
                for(int rank=1;rank<=4;rank++) {
                    count++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                    test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.SKULL_THROW),"Fresh axe throws a skull");
                    var stack=Augmentations.setLevel(fresh,AdditionalAbilities.SKULL_THROW,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.SKULL_THROW),"Skull Throw cannot activate");
                    var rock=player.level().getEntitiesOfClass(WitherSkull.class,player.getBoundingBox().inflate(3),e->e.getOwner()==player).getFirst();
                    test.assertTrue(Math.abs(rock.getDeltaMovement().length()-3.5)<.0001,"Skull does not launch at its faster speed");
                    target.invulnerableTime=0;target.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);float before=target.getHealth(),owner=player.getHealth();
                    var event=new ProjectileImpactEvent(rock,new EntityHitResult(target,target.getBoundingBox().getCenter()));SkullThrowEvents.impact(event);
                    test.assertTrue(event.isCanceled() && !rock.isAlive() && !SkullThrowEvents.active(player),"Impact does not end rock");
                    test.assertTrue(Math.abs(before-target.getHealth()-player.getAttributeValue(Attributes.ATTACK_DAMAGE))<.001 && target.hasEffect(net.minecraft.world.effect.MobEffects.WITHER) && target.getEffect(net.minecraft.world.effect.MobEffects.WITHER).getDuration()==(8+2*rank)*20+(JadeAugments.isJade(stack)?20:0) && target.getEffect(net.minecraft.world.effect.MobEffects.WITHER).getAmplifier()==(JadeAugments.isJade(stack)?1:0),"Impact damage or Wither duration wrong");
                    test.assertTrue(player.getHealth()==owner,"Skull blast damages caster");
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.SKULL_THROW),"Cannot start next direct-runtime test");
                    player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(SkullThrowEvents.active(player),"Switch does not cancel rock");
                }
            }
            test.assertTrue(count==20,"Skipped Skull Throw variant/rank");
        } finally {SkullThrowEvents.cancel(player.getUUID());target.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
