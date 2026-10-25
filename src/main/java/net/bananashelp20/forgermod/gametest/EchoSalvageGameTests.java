package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeHeavyNetwork;
import net.bananashelp20.forgermod.mixin.AttackStrengthAccess;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
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
public class EchoSalvageGameTests {
 private static final class SoundWire extends net.minecraft.network.Connection {
  final java.util.List<net.minecraft.network.protocol.game.ClientboundSoundPacket> echoes=new ArrayList<>();
  SoundWire(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
  @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){
   if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound && sound.getSound().value()==net.bananashelp20.forgermod.sound.ModSounds.ECHO_SALVAGE_RELEASE.get())echoes.add(sound);
  }
 }
 private static SoundWire listen(ServerPlayer player){var wire=new SoundWire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));return wire;}

 private static void slam(ServerPlayer player,BlockPos center,float damage)throws Exception{
  var pending=Class.forName(AxeHeavyNetwork.class.getName()+"$PendingSlam");var ctor=pending.getDeclaredConstructor(ServerLevel.class,Item.class,long.class,BlockPos.class,float.class);ctor.setAccessible(true);
  var land=AxeHeavyNetwork.class.getDeclaredMethod("landSlam",ServerPlayer.class,pending);land.setAccessible(true);
  land.invoke(null,player,ctor.newInstance(player.serverLevel(),player.getMainHandItem().getItem(),player.level().getGameTime(),center,damage));
 }
 private static ServerPlayer player(GameTestHelper test,BlockPos center){
  var player=TestPlayers.survival(test);player.setInvulnerable(true);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(center.north(2)));player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
  for(int x=-3;x<=3;x++)for(int y=0;y<=3;y++)for(int z=-3;z<=3;z++)test.getLevel().setBlockAndUpdate(center.offset(x,y,z),Blocks.AIR.defaultBlockState());
  test.getLevel().setBlockAndUpdate(center.below(),Blocks.STONE.defaultBlockState());return player;
 }
 private static ItemStack weapon(Item item,int rank){return Augmentations.setLevel(Augmentations.apply(new ItemStack(item),Augmentations.VERTICAL_STRIKE),RecommendedAbilities.ECHO_SALVAGE,rank);}
 private static net.minecraft.world.entity.monster.Zombie enemy(GameTestHelper test,BlockPos center){
  var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000);mob.setHealth(10000);mob.setPos(Vec3.atBottomCenterOf(center));test.getLevel().addFreshEntity(mob);return mob;
 }
 private static void attack(ServerPlayer player,LivingEntity target){
  ((AttackStrengthAccess)player).forgermod$setAttackStrengthTicker(100);player.setOnGround(true);player.fallDistance=0;player.setSprinting(false);target.invulnerableTime=0;player.attack(target);
 }
 private static void cleanup(ServerPlayer player){EchoSalvageEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
 @GameTest(template="riftfang_test")
 public static void everyVariantAndRankBanksTwoThroughFiveUncappedStrikesAndReleasesOnce(GameTestHelper test)throws Exception{
  var center=test.absolutePos(new BlockPos(8,5,8));var player=player(test,center);var old=player.connection;var wire=listen(player);
  try{for(var item:List.of(ModItems.ECHOING_AXE.get(),ModItems.ECHOING_AXE_RUBY.get(),ModItems.ECHOING_AXE_AMBER.get(),ModItems.ECHOING_AXE_AMETHYST.get(),ModItems.ECHOING_AXE_JADE.get()))for(int rank=1;rank<=4;rank++){
   var stack=weapon(item,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();wire.echoes.clear();
   test.getLevel().setBlockAndUpdate(center.below(),Blocks.AIR.defaultBlockState());slam(player,center,40);test.assertTrue(EchoSalvageEvents.storedCount(stack)==0,"Air impact stores echo");test.getLevel().setBlockAndUpdate(center.below(),Blocks.STONE.defaultBlockState());
   int capacity=rank+1;for(int i=0;i<capacity+2;i++)slam(player,center,40);
   test.assertTrue(EchoSalvageEvents.storedCount(stack)==capacity && EchoSalvageEvents.bonus(player)==20*capacity,"Wrong strike capacity or old damage cap at "+rank);
   test.assertTrue(wire.echoes.isEmpty(),"Stored misses play the discharge sound");
   var mob=enemy(test,center);
   try{mob.setInvulnerable(true);slam(player,center,40);mob.setInvulnerable(false);
    test.assertTrue(EchoSalvageEvents.storedCount(stack)==capacity,"Rejected hit spends bank");test.assertTrue(wire.echoes.isEmpty(),"Rejected impact plays a discharge");
    float before=mob.getHealth();slam(player,center,40);test.assertTrue(mob.getHealth()==before-40-20*capacity && EchoSalvageEvents.storedCount(stack)==0,"Native Vertical Strike duplicates or loses bank damage");
    test.assertTrue(wire.echoes.size()==1 && wire.echoes.getFirst().getVolume()==1F && Math.abs(wire.echoes.getFirst().getPitch()-(1.12F-.04F*capacity))<.001 && wire.echoes.getFirst().getSource()==net.minecraft.sounds.SoundSource.PLAYERS,"Charged Vertical Strike lacks its single distinct full-volume echo or bank-size pitch");
    before=mob.getHealth();slam(player,center,40);test.assertTrue(mob.getHealth()==before-40 && EchoSalvageEvents.storedCount(stack)==0,"Released bank repeats");test.assertTrue(wire.echoes.size()==1,"Ordinary Vertical Strike repeats the discharge audio");
   }finally{mob.discard();}
  }test.succeed();}finally{player.connection=old;cleanup(player);}
 }
 @GameTest(template="riftfang_test")
 public static void nextSuccessfulMeleeConvertsToAreaVerticalStrikeWithoutDoubleHittingPrimary(GameTestHelper test)throws Exception{
  var center=test.absolutePos(new BlockPos(8,5,8));var player=player(test,center);var stack=weapon(ModItems.ECHOING_AXE.get(),1);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
  var old=player.connection;var wire=listen(player);
  try{slam(player,center,40);var rejected=enemy(test,center);rejected.setInvulnerable(true);
   try{slam(player,center,40);test.assertTrue(EchoSalvageEvents.storedCount(stack)==1,"Rejected entity strike incorrectly charges another stored miss");}finally{rejected.discard();}
   slam(player,center,40);var primary=enemy(test,center);var neighbor=enemy(test,center.east());
   try{primary.setInvulnerable(true);attack(player,primary);primary.setInvulnerable(false);test.assertTrue(EchoSalvageEvents.bonus(player)==40,"Rejected melee consumes bank");
    // Ability/indirect damage cannot take the next native melee trigger.
    primary.hurt(player.damageSources().indirectMagic(player,player),1);test.assertTrue(EchoSalvageEvents.bonus(player)==40,"Indirect hit releases bank");test.assertTrue(wire.echoes.isEmpty(),"Rejected or indirect hit plays echo discharge");
    float before=primary.getHealth(),other=neighbor.getHealth(),base=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    attack(player,primary);
    test.assertTrue(Math.abs(primary.getHealth()-(before-base-40))<.01 && Math.abs(neighbor.getHealth()-(other-base-40))<.01,"Converted hit duplicates primary damage or lacks area strike");
    test.assertTrue(EchoSalvageEvents.storedCount(stack)==0 && player.getPersistentData().contains(AxeHeavyNetwork.ANIMATION_TAG),"Bank did not spend or native Vertical Strike animation absent");test.assertTrue(wire.echoes.size()==1,"Converted area strike duplicates or misses its echo sound");
   }finally{primary.discard();neighbor.discard();}
   slam(player,center,100);test.assertTrue(EchoSalvageEvents.bonus(player)==50,"Per-strike damage is still capped");
   var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);cow.setHealth(1000);cow.setPos(Vec3.atBottomCenterOf(center));test.getLevel().addFreshEntity(cow);
   try{attack(player,cow);test.assertTrue(EchoSalvageEvents.storedCount(stack)==0 && cow.getHealth()<1000,"Successful neutral melee fails to release bank");test.assertTrue(wire.echoes.size()==2,"Second bank release lacks a fresh echo sound");}finally{cow.discard();}
   test.succeed();
  }finally{player.connection=old;cleanup(player);}
 }
 @GameTest(template="riftfang_test",timeoutTicks=650)
 public static void storedWeaponSurvivesSwitchThirtySecondsAndSaveReload(GameTestHelper test)throws Exception{
  var center=test.absolutePos(new BlockPos(8,5,8));var player=player(test,center);var stack=weapon(ModItems.ECHOING_AXE.get(),4);player.setItemInHand(InteractionHand.MAIN_HAND,stack);slam(player,center,100);slam(player,center,120);
  player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);EchoSalvageEvents.onTick(new PlayerTickEvent.Post(player));
  test.assertTrue(EchoSalvageEvents.storedCount(stack)==2 && EchoSalvageEvents.bonus(player)==0,"Switch destroys bank or applies unequipped bonus");
  test.runAfterDelay(605,()->{try{
   var loaded=ItemStack.parseOptional(test.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)stack.save(test.getLevel().registryAccess()));player.setItemInHand(InteractionHand.MAIN_HAND,loaded);
   test.assertTrue(EchoSalvageEvents.storedCount(loaded)==2 && EchoSalvageEvents.bonus(player)==110,"Bank expires after 30s or is absent after item reload");
   var other=weapon(ModItems.ECHOING_AXE.get(),4);player.setItemInHand(InteractionHand.MAIN_HAND,other);test.assertTrue(EchoSalvageEvents.bonus(player)==0,"Another weapon borrows stored strikes");test.succeed();
  }finally{cleanup(player);}});
 }
 @GameTest(template="riftfang_test")
 public static void reinforcementEchoConvertsMeleeAndKeepsOwnerAndAlliesSafe(GameTestHelper test){
  var center=test.absolutePos(new BlockPos(8,5,8));var player=player(test,center);var summon=Augmentations.setLevel(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD,5);player.setItemInHand(InteractionHand.MAIN_HAND,summon);var bank=NecromancerLoadouts.get(player,summon);bank.types.clear();bank.types.add(0);var stack=weapon(ModItems.ECHOING_AXE.get(),4);bank.gear.setItem(0,stack);
  var enemy=enemy(test,center);var other=enemy(test,center.east());var old=player.connection;var wire=listen(player);
  try{test.assertTrue(BoneGuardEvents.activate(player,summon),"Guard fixture cannot summon");var guard=test.getLevel().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(12),m->m instanceof BoneGuardEvents.FriendlyGuard f && f.owner()==player && m.isAlive()).getFirst();guard.setNoAi(true);guard.setNoGravity(true);guard.setPos(Vec3.atBottomCenterOf(center.north()));player.setInvulnerable(false);player.setPos(Vec3.atBottomCenterOf(center.west()));
   EchoSalvageEvents.onImpact(guard,false,false,true,100);EchoSalvageEvents.onImpact(guard,false,false,true,100);float health=player.getHealth();test.assertTrue(wire.echoes.isEmpty(),"Guard storing echoes plays discharge");
   test.assertTrue(NecromancerPassiveCombat.attack(guard,enemy),"Guard melee failed");
   test.assertTrue(EchoSalvageEvents.storedCount(stack)==0 && other.getHealth()<10000 && player.getHealth()==health,"Guard echo fails to release area strike or harms owner");test.assertTrue(wire.echoes.size()==1 && wire.echoes.getFirst().getSource()==guard.getSoundSource(),"Guard discharge is silent or duplicated across victims");test.succeed();
  }finally{enemy.discard();other.discard();BoneGuardEvents.cancel(player.getUUID());player.connection=old;cleanup(player);}
 }
}
