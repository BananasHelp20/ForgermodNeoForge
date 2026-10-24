package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ThunderclapGameTests {
    private static ServerPlayer player(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setPos(Vec3.atBottomCenterOf(BlockPos.containing(test.getBounds().getCenter())));
        player.setNoGravity(true);player.setXRot(0);player.setYRot(0);
        // Exercise native movement in a real loaded corridor, including travel beyond the first target's radius.
        for(int x=-2;x<=2;x++)for(int z=-2;z<=65;z++)for(int y=0;y<=4;y++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        return player;
    }
    private static Mob mob(GameTestHelper test,EntityType<? extends Mob> type,Vec3 pos){
        var mob=type.create(test.getLevel());mob.setPos(pos);mob.setNoAi(true);mob.setNoGravity(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);mob.setHealth(200);
        if(mob.getAttribute(Attributes.ARMOR)!=null)mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
        mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);test.getLevel().addFreshEntity(mob);return mob;
    }
    private static void advance(ServerPlayer player){
        for(int i=0;i<200 && ThunderclapEvents.active(player);i++){
            var before=player.position();var velocity=player.getDeltaMovement();
            player.move(MoverType.SELF,velocity);
            if(player.position().distanceTo(before)>velocity.length()+.001)throw new AssertionError("Dash teleported");
            ThunderclapEvents.tick(player);
        }
    }
    private static void cleanup(ServerPlayer player){
        ThunderclapEvents.cancel(player.getUUID());
        WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
        player.server.getPlayerList().remove(player);
    }
    private static final class ViewConnection extends net.minecraft.network.Connection {
        int snaps,swings;final List<net.minecraft.core.particles.ParticleOptions> particles=new ArrayList<>();
        ViewConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket)snaps++;
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundAnimatePacket)swings++;
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket particle)particles.add(particle.getParticle());
        }
    }
    @GameTest(template="riftfang_test",batch="thunderclap_view_and_impact")
    public static void impactPrecedesNextTargetAndCameraSnapsOnceWithoutLocking(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();var first=mob(test,EntityType.COW,origin.add(0,0,5));var second=mob(test,EntityType.COW,origin.add(4,0,7));
        var previous=player.connection;var wire=new ViewConnection();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.THUNDERCLAP);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-origin.y-.7,5)));
        try{
            AbilityTestPackets.use(player,0);
            for(int i=0;i<12 && first.getHealth()==200;i++){player.move(MoverType.SELF,player.getDeltaMovement());ThunderclapEvents.tick(player);}
            test.assertTrue(first.getHealth()<200 && second.getHealth()==200 && ThunderclapEvents.bodyDistanceSqr(player.getBoundingBox(),first.getBoundingBox())<=1.00001 && wire.snaps==0 && wire.swings>0,"Chain target selected before a visible hit within one block");
            for(int i=0;i<2;i++){ThunderclapEvents.tick(player);test.assertTrue(wire.snaps==0 && player.getDeltaMovement().equals(Vec3.ZERO),"No visible strike beat before chaining");}
            ThunderclapEvents.tick(player);test.assertTrue(wire.snaps==1 && Math.abs(player.getYRot())>1,"No one-time camera turn to the off-axis next mob");
            player.setYRot(137);player.setXRot(20);ThunderclapEvents.tick(player);test.assertTrue(player.getYRot()==137 && player.getXRot()==20 && wire.snaps==1,"Camera remains forced after snap");
            test.assertTrue(wire.particles.stream().anyMatch(value->value instanceof net.bananashelp20.forgermod.particle.LightningArcParticles.Options options && options.owner()==player.getId()),"Movement trail is not attached to rendered player");
            advance(player);test.assertTrue(second.getHealth()<200 && wire.snaps==1,"Next strike misses or repeatedly controls camera");
        }finally{ThunderclapEvents.cancel(player.getUUID());first.discard();second.discard();player.connection=previous;cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void lightningArcPacketsAndGeometryKeepFiniteAnchoredJaggedStrokes(GameTestHelper test){
        var option=new net.bananashelp20.forgermod.particle.LightningArcParticles.Options(3,4,5,17,99,4);
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
        try{net.bananashelp20.forgermod.particle.LightningArcParticles.Options.STREAM.encode(buffer,option);test.assertTrue(option.equals(net.bananashelp20.forgermod.particle.LightningArcParticles.Options.STREAM.decode(buffer)),"Arc packet loses endpoints/owner/seed/lifetime");}finally{buffer.release();}
        for(var delta:List.of(Vec3.ZERO,new Vec3(10,0,0),new Vec3(0,10,0))){
            test.assertTrue(net.bananashelp20.forgermod.particle.LightningArcParticles.point(delta,0,99,0).equals(Vec3.ZERO) && net.bananashelp20.forgermod.particle.LightningArcParticles.point(delta,16,99,0).equals(delta),"Arc endpoints drift");
            for(int i=0;i<=16;i++){var point=net.bananashelp20.forgermod.particle.LightningArcParticles.point(delta,i,99,0);test.assertTrue(Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z),"Vertical or zero-length arc is invalid");}
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="thunderclap_body")
    public static void wideTallAndSolidMobsAreHitAtTheirBodyWithoutApproachingTheirCenter(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();
        int index=0;try{for(var type:List.of(EntityType.RAVAGER,EntityType.GIANT,EntityType.SHULKER,EntityType.GIANT)){
            player.setPos(origin);player.setYRot(0);player.setXRot(0);player.setDeltaMovement(Vec3.ZERO);
            var target=mob(test,type,origin.add(0,0,index++==3?30:7));if(index==4){target.getAttribute(Attributes.SCALE).setBaseValue(12);target.refreshDimensions();}player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-target.getBoundingBox().getCenter().y,7)));if(index==4)player.setXRot(0);var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.THUNDERCLAP);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
            try{
                AbilityTestPackets.use(player,0);
                for(int tick=0;tick<20 && target.getHealth()==200 && ThunderclapEvents.active(player);tick++){player.move(MoverType.SELF,player.getDeltaMovement());ThunderclapEvents.tick(player);}
                test.assertTrue(target.getHealth()<200 && player.distanceToSqr(target)>1 && ThunderclapEvents.bodyDistanceSqr(player.getBoundingBox(),target.getBoundingBox())<=1.00001,"Thunderclap still requires center proximity for "+type+" case="+index+" active="+ThunderclapEvents.active(player)+" health="+target.getHealth()+" player="+player.position()+" box="+target.getBoundingBox()+" gap="+ThunderclapEvents.bodyDistanceSqr(player.getBoundingBox(),target.getBoundingBox())+" interact="+player.canInteractWithEntity(target,0));
                test.assertFalse(player.getBoundingBox().intersects(target.getBoundingBox()),"Dash enters mob body instead of striking its surface");
            }finally{target.discard();ThunderclapEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));}
        }}finally{cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="thunderclap_heights")
    public static void elevationDifferencesAndRaisedPlatformsReachBodyAndPreserveTenBlockAim(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();
        try{for(int elevation:new int[]{-3,3,6}){
            for(int x=-3;x<=3;x++)for(int z=0;z<=10;z++)for(int y=-4;y<=10;y++)test.getLevel().setBlockAndUpdate(BlockPos.containing(origin).offset(x,y,z),Blocks.AIR.defaultBlockState());
            player.setPos(origin);player.setYRot(0);player.setXRot(0);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
            var target=mob(test,EntityType.COW,origin.add(0,elevation,5));
            if(elevation>0)for(int x=-1;x<=1;x++)for(int z=5;z<=7;z++)for(int y=-1;y<elevation;y++)test.getLevel().setBlockAndUpdate(BlockPos.containing(origin).offset(x,y,z),Blocks.STONE.defaultBlockState());
            var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.THUNDERCLAP);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
            player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-target.getEyeY(),5)));
            try{AbilityTestPackets.use(player,0);advance(player);test.assertTrue(target.getHealth()<200,"Dash fails elevation/platform at "+elevation+" position="+player.position());test.assertTrue(test.getLevel().noCollision(player,player.getBoundingBox().deflate(.0001)),"Height routing clips terrain");}
            finally{target.discard();ThunderclapEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));}
        }
        for(int x=-3;x<=3;x++)for(int z=0;z<=14;z++)for(int y=-4;y<=10;y++)test.getLevel().setBlockAndUpdate(BlockPos.containing(origin).offset(x,y,z),Blocks.AIR.defaultBlockState());
        player.setPos(origin);player.setXRot(0);player.setYRot(0);var distant=mob(test,EntityType.COW,origin.add(0,0,12));
        try{player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());AbilityTestPackets.use(player,0);test.assertFalse(ThunderclapEvents.active(player),"Initial aim still exceeds ten blocks");}
        finally{distant.discard();}
        }finally{cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="thunderclap_confirmed_impact")
    public static void invulnerableTargetsNeverProduceThunderImpactOrAdvanceTheChain(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();var first=mob(test,EntityType.COW,origin.add(0,0,5));var next=mob(test,EntityType.COW,origin.add(3,0,7));first.setInvulnerable(true);
        var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.THUNDERCLAP);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-first.getEyeY(),5)));
        try{AbilityTestPackets.use(player,0);advance(player);
            test.assertTrue(first.getHealth()==200 && next.getHealth()==200 && !ThunderclapEvents.active(player),"Failed melee still chains or stays stuck indefinitely");
            test.assertTrue(player.level().getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class,player.getBoundingBox().inflate(16)).isEmpty(),"Thunder impact appears without an accepted melee hit");
        }finally{first.discard();next.discard();cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="thunderclap_variants")
    public static void allTenVariantsAndRanksChainBeyondFourTargetsWithArrivalSlashesAndViewSnaps(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();int checked=0;
        var method=Class.forName("net.bananashelp20.forgermod.mixin.AttackStrengthAccess");
        test.assertTrue(method.isInstance(player),"Full-strength vanilla attack accessor missing");
        try{
            for(var entry:ModItems.ITEMS.getEntries()){
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon weapon && !weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.THUNDERCLAP))==eligible,"Wrong Thunderclap weapon pool");
                if(!eligible)continue;checked++;
                for(int rank=1;rank<=4;rank++){
                    player.setPos(origin);player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-origin.y-.7,4)));player.setYRot(0);player.setSprinting(false);player.setDeltaMovement(Vec3.ZERO);
                    var stack=Augmentations.setLevel(fresh,AdditionalAbilities.THUNDERCLAP,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
                    player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
                    var targets=new ArrayList<Mob>();
                    var types=List.of(EntityType.COW,EntityType.WOLF,EntityType.BAT,EntityType.ZOMBIE,EntityType.SKELETON,EntityType.CREEPER);
                    var team=player.getScoreboard().addPlayerTeam("thunder_"+UUID.randomUUID().toString().substring(0,8));
                    try{
                        for(int i=0;i<types.size();i++)targets.add(mob(test,types.get(i),origin.add(0,0,4+5*i)));
                        ((net.minecraft.world.entity.animal.Wolf)targets.get(1)).tame(player);targets.get(1).getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);targets.get(1).setHealth(200);
                        player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);player.getScoreboard().addPlayerToTeam(targets.getFirst().getScoreboardName(),team);
                        float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        // An initially empty attack meter must not weaken any automatic melee hit in the chain.
                        player.resetAttackStrengthTicker();AbilityTestPackets.use(player,0);
                        test.assertTrue(ThunderclapEvents.active(player) && player.position().equals(origin) && player.getDeltaMovement().z>0,"Activation teleports or fails to accelerate");
                        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).isEmpty(),"Cooldown starts while dash is still active");
                        advance(player);
                        test.assertFalse(ThunderclapEvents.active(player),"Chain does not end when nearby unvisited mobs run out");
                        for(var target:targets)test.assertTrue(Math.abs(target.getHealth()-(200-1.5F*damage))<.01,"Wrong full melee + half lightning damage or repeated hit at rank "+rank+": "+target.getType()+" health="+target.getHealth()+" damage="+damage);
                        test.assertTrue(stack.getDamageValue()==6,"Each melee hit must use exactly one durability");
                        test.assertTrue(player.getZ()>origin.z+25 && Float.isFinite(player.getYRot()) && Float.isFinite(player.getXRot()),"Chain is capped by rank, anchored to the first target or forces the camera");
                        test.assertTrue(player.isNoGravity() && player.getDeltaMovement().equals(Vec3.ZERO),"Original gravity or arrival precision lost");
                        WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
                        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsValue(player.level().getGameTime()+700-100*rank),"Wrong end-of-chain rank cooldown");
                        AbilityTestPackets.use(player,0);test.assertFalse(ThunderclapEvents.active(player),"Cooldown permits recasting");
                    }finally{ThunderclapEvents.cancel(player.getUUID());targets.forEach(Entity::discard);player.getScoreboard().removePlayerTeam(team);}
                }
            }
            test.assertTrue(checked==10,"Skipped eligible gemstone variants");
            test.assertTrue(Augmentations.maxRank(new ItemStack(ModItems.SUPER_AUGMENT_TEMPLATE.get()),AdditionalAbilities.THUNDERCLAP)==4,"Super template bypasses IV cap");
            test.assertTrue(Augmentations.setLevel(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.THUNDERCLAP,5).isEmpty(),"Stored rank exceeds IV");
        }finally{cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="thunderclap_safety")
    public static void blockedRaysBodyCollisionsSwitchRemovalAndStallsCancelAndRestoreGravity(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();var target=mob(test,EntityType.COW,origin.add(0,0,8));
        player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-origin.y-.7,8)));
        var stack=Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),AdditionalAbilities.THUNDERCLAP);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setNoGravity(false);
        try{
            var wall=player.blockPosition().offset(0,1,4);test.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
            AbilityTestPackets.use(player,0);test.assertTrue(!ThunderclapEvents.active(player) && player.getDeltaMovement().equals(Vec3.ZERO),"Aim pierces solid wall");
            test.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());
            // The eyes can see over this obstacle, but the player's feet cannot pass through it.
            var feet=wall.below();test.getLevel().setBlockAndUpdate(feet,Blocks.STONE.defaultBlockState());
            AbilityTestPackets.use(player,0);advance(player);test.assertFalse(ThunderclapEvents.active(player),"Body collision retains dash");
            test.assertTrue(player.getZ()>feet.getZ() && target.getHealth()<200 && !player.isNoGravity(),"Dash fails to clear a one-block ledge and strike, or leaks gravity");
            target.setHealth(200);target.invulnerableTime=0;
            test.getLevel().setBlockAndUpdate(feet,Blocks.AIR.defaultBlockState());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());player.setPos(origin);
            AbilityTestPackets.use(player,0);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(!ThunderclapEvents.active(player) && !player.isNoGravity() && player.getDeltaMovement().equals(Vec3.ZERO) && target.getHealth()==200,"Switch keeps movement or hits");
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsValue(player.level().getGameTime()+600),"Switch does not start independent cooldown");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());AbilityTestPackets.use(player,0);target.discard();ThunderclapEvents.tick(player);
            test.assertTrue(!ThunderclapEvents.active(player) && !player.isNoGravity(),"Removed target retains dash");
            target=mob(test,EntityType.COW,origin.add(0,0,8));WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());AbilityTestPackets.use(player,0);test.assertTrue(ThunderclapEvents.active(player),"Stall fixture failed to start");for(int i=0;i<12;i++)ThunderclapEvents.tick(player);
            test.assertTrue(!ThunderclapEvents.active(player) && !player.isNoGravity(),"No movement packets cause an endless active dash");
        }finally{target.discard();cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="thunderclap_ledge_clearance")
    public static void slabDetourUsesRealMovementAndCeilingBlocksUnsafeLifts(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();var target=mob(test,EntityType.COW,origin.add(0,0,8));
        var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.THUNDERCLAP);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
        var feet=player.blockPosition().offset(0,0,4);
        try{
            for(boolean ceiling:new boolean[]{false,true}){
                player.setPos(origin);player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-origin.y-.7,8)));player.setYRot(0);
                target.setHealth(200);target.invulnerableTime=0;player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
                test.getLevel().setBlockAndUpdate(feet,Blocks.STONE_SLAB.defaultBlockState());
                // A low roof permits standing, but not the extra half-block clearance required by the ledge.
                for(int z=0;z<=8;z++)test.getLevel().setBlockAndUpdate(BlockPos.containing(origin).offset(0,2,z),ceiling?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
                AbilityTestPackets.use(player,0);advance(player);
                test.assertTrue(target.getHealth()<200,"Dash cannot find a collision-free route past the slab/low roof");
                test.assertTrue(test.getLevel().noCollision(player,player.getBoundingBox().deflate(.0001)),"Dash body clips into ledge/ceiling");
                WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            }
        }finally{target.discard();cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="thunderclap_lethal")
    public static void killingFirstTargetContinuesAndBlockedOrDistantCandidatesAreSkipped(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();var first=mob(test,EntityType.COW,origin.add(0,0,7));first.setHealth(.25F);
        var next=mob(test,EntityType.COW,origin.add(0,0,16));var outside=mob(test,EntityType.COW,origin.add(0,0,27));
        var blocked=mob(test,EntityType.COW,origin.add(9,0,7));
        for(int y=0;y<=3;y++)for(int z=3;z<=11;z++)test.getLevel().setBlockAndUpdate(player.blockPosition().offset(4,y,z),Blocks.STONE.defaultBlockState());
        player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-origin.y-.7,7)));
        var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.THUNDERCLAP);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
        try{AbilityTestPackets.use(player,0);advance(player);
            test.assertTrue(!first.isAlive() && next.getHealth()<200 && outside.getHealth()==200 && blocked.getHealth()==200,"Lethal hit aborts chaining or hop exceeds ten blocks");
            test.assertFalse(ThunderclapEvents.active(player),"Out-of-range target holds ability open");
        }finally{first.discard();next.discard();outside.discard();blocked.discard();cleanup(player);}test.succeed();
    }
}
