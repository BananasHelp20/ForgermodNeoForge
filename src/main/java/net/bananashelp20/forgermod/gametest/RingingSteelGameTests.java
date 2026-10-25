package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class RingingSteelGameTests {
    @GameTest(template="riftfang_test")
    public static void everyRankWearsBothWeaponsOnceAndDisplaysTheSameDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
        mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));mob.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(mob);
        var old=player.connection;var wire=new SoundWire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof PulsiteWeapon weapon && !weapon.isAxe() && !weapon.isDagger())for(int rank=1;rank<=4;rank++) {
                checked++;int damage=10+rank*5;
                var stack=Augmentations.setLevel(new ItemStack(entry.get()),AdditionalAbilities.RINGING_STEEL,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                var main=new ItemStack(Items.IRON_SWORD);main.setDamageValue(6);var off=new ItemStack(Items.BOW);off.setDamageValue(9);
                mob.setItemSlot(EquipmentSlot.MAINHAND,main);mob.setItemSlot(EquipmentSlot.OFFHAND,off);mob.setHealth(1000);mob.invulnerableTime=0;mob.setPos(player.position().add(0,0,2));mob.setDeltaMovement(0,0,0);
                for(int tick=0;tick<60;tick++)player.doTick();player.setOnGround(false);player.fallDistance=0;player.setSprinting(false);
                wire.clangs.clear();wire.sparks=0;test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL),"Cannot arm rank/variant fixture");player.attack(mob);
                test.assertTrue(main.getDamageValue()==6+damage && off.getDamageValue()==9+damage,"Native hit does not damage both weapons for the approved rank amount");
                test.assertTrue(!RingingSteelEvents.active(player) && wire.clangs.size()==1 && wire.sparks==10,"Both-hand wear repeats consumption or audiovisual impact");
                mob.invulnerableTime=0;player.attack(mob);test.assertTrue(main.getDamageValue()==6+damage && off.getDamageValue()==9+damage && wire.clangs.size()==1,"Spent strike repeats wear/sound");
                var description=AbilityDescriptions.description(stack,AdditionalAbilities.RINGING_STEEL,rank,false);var white=new StringBuilder();
                description.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                test.assertTrue(white.toString().equals(damage+" durability") && description.getString().equals("Your next hit will damage the enemy's held weapon(s) for "+damage+" durability."),"Description differs from runtime damage or colors more than the current durability phrase");
                if(rank<4)test.assertTrue(AugmentUpgradeDescriptions.changes(stack,AdditionalAbilities.RINGING_STEEL,rank).stream().anyMatch(change->change.property().equals("weapon_wear") && ((Number)change.after()).doubleValue()==damage+5),"Upgrade preview omits the increased durability damage");
                test.assertTrue(RecommendedAbilities.cooldown(AdditionalAbilities.RINGING_STEEL,rank)==600-100*(rank-1),"Durability scaling changes cooldown progression");
            }
            test.assertTrue(checked==20,"Skipped a claymore variant/rank");test.succeed();
        }finally{RingingSteelEvents.cancel(player.getUUID());mob.discard();player.connection=old;player.server.getPlayerList().remove(player);}
    }
    private static final class SoundWire extends net.minecraft.network.Connection {
        final java.util.List<net.minecraft.network.protocol.game.ClientboundSoundPacket> clangs=new java.util.ArrayList<>();
        int sparks;
        SoundWire(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound && sound.getSound().value()==net.bananashelp20.forgermod.sound.ModSounds.RINGING_STEEL_HIT.get())clangs.add(sound);
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket particle && particle.getParticle()==net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK)sparks+=particle.getCount();
        }
    }
    @GameTest(template="riftfang_test",batch="ringing_steel_real_reinforcement")
    public static void nativeHitsRingAndWearActualNecromancerBankWeapons(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.setYRot(0);player.setXRot(0);
        var summon=Augmentations.setLevel(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD,5);player.setItemInHand(InteractionHand.MAIN_HAND,summon);
        var bank=NecromancerLoadouts.get(player,summon);bank.types.clear();bank.types.add(0);bank.gear.setItem(0,new ItemStack(Items.IRON_SWORD));
        var dirty=new java.util.concurrent.atomic.AtomicInteger();bank.gear.addListener(ignored->dirty.incrementAndGet());
        test.assertTrue(BoneGuardEvents.activate(player,summon),"Cannot summon the reinforcement fixture");
        var guard=test.getLevel().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(12),mob->mob instanceof BoneGuardEvents.FriendlyGuard friend && friend.owner()==player && mob.isAlive()).getFirst();
        guard.setNoAi(true);guard.setNoGravity(true);guard.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);guard.setHealth(1000);guard.getAttribute(Attributes.ARMOR).setBaseValue(0);
        var old=player.connection;var wire=new SoundWire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));int checked=0;
        try{
            for(var entry:ModItems.ITEMS.getEntries()){
                if(!(entry.get() instanceof PulsiteWeapon weapon) || weapon.isAxe() || weapon.isDagger())continue;checked++;
                var stack=Augmentations.apply(new ItemStack(entry.get()),AdditionalAbilities.RINGING_STEEL);player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int i=0;i<60;i++)player.doTick();player.setData(WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());
                var sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(5);bank.gear.setItem(0,sword);guard.setItemSlot(EquipmentSlot.MAINHAND,bank.gear.getItem(0));guard.setPos(player.position().add(0,0,2));guard.invulnerableTime=0;guard.setHealth(1000);guard.setDeltaMovement(0,0,0);
                var offBow=new ItemStack(Items.BOW);offBow.setDamageValue(2);bank.gear.setItem(5,offBow);guard.setItemSlot(EquipmentSlot.OFFHAND,offBow);
                wire.clangs.clear();wire.sparks=0;AbilityTestPackets.use(player,0);test.assertTrue(RingingSteelEvents.active(player) && wire.clangs.isEmpty(),"Charge is not armed or plays the impact early");
                test.assertTrue(guard.isAlliedTo(player) && guard.getMainHandItem()==bank.gear.getItem(0),"Fixture is not an allied guard with actual bank gear");dirty.set(0);player.setOnGround(false);player.fallDistance=0;player.setSprinting(false);player.attack(guard);
                test.assertTrue(!RingingSteelEvents.active(player) && sword.getDamageValue()==20 && bank.gear.getItem(0).getDamageValue()==20 && dirty.get()>0 && offBow.getDamageValue()==17 && bank.gear.getItem(5).getDamageValue()==17,"Allied reinforcement is excluded or its saved weapon does not wear");
                test.assertTrue(wire.clangs.size()==1 && wire.sparks==10,"Actual reinforcement hit lacks its single ringing metal sound/sparks");
                WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));AbilityTestPackets.use(player,0);test.assertFalse(RingingSteelEvents.active(player),"Successful reinforcement strike bypasses cooldown");
                player.setData(WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());AbilityTestPackets.use(player,0);test.assertTrue(RingingSteelEvents.active(player),"Cannot arm the weapon-break case");sword.setDamageValue(sword.getMaxDamage()-1);offBow.setDamageValue(offBow.getMaxDamage()-1);for(int i=0;i<60;i++)player.doTick();guard.invulnerableTime=0;guard.setPos(player.position().add(0,0,2));guard.setDeltaMovement(0,0,0);dirty.set(0);player.setOnGround(false);player.fallDistance=0;player.attack(guard);
                test.assertTrue(bank.gear.getItem(0).isEmpty() && guard.getMainHandItem().isEmpty() && dirty.get()>0 && wire.clangs.size()==2 && bank.gear.getItem(5).isEmpty() && guard.getOffhandItem().isEmpty(),"Weapon break is not shared/persisted or its impact repeats");
                WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
            }
            var deathSword=new ItemStack(Items.IRON_SWORD);bank.gear.setItem(0,deathSword);guard.setItemSlot(EquipmentSlot.MAINHAND,deathSword);player.setData(WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());for(int i=0;i<60;i++)player.doTick();AbilityTestPackets.use(player,0);
            guard.setHealth(1);guard.invulnerableTime=0;guard.setPos(player.position().add(0,0,2));wire.clangs.clear();dirty.set(0);player.setOnGround(false);player.fallDistance=0;player.attack(guard);
            test.assertTrue(!guard.isAlive() && deathSword.getDamageValue()==15 && dirty.get()>0 && wire.clangs.size()==1,"Lethal reinforcement hit loses its weapon wear or ringing impact");
            NecromancerEquipment.death(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(guard));test.assertTrue(bank.gear.getItem(0).getDamageValue()==15 && guard.getMainHandItem().isEmpty(),"Dead reinforcement cleanup loses bank weapon wear");
            test.assertTrue(checked==5,"Skipped ringing claymore variants");
        }finally{RingingSteelEvents.cancel(player.getUUID());BoneGuardEvents.cancel(player.getUUID());WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.connection=old;player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=650)
    public static void allVariantsStayArmedBeyondThirtySecondsAndCooldownStartsOnlyOnUseOrSwitch(GameTestHelper test) throws Exception {
        var players=new java.util.ArrayList<net.minecraft.server.level.ServerPlayer>();
        for(var entry:ModItems.ITEMS.getEntries()) if(entry.get() instanceof PulsiteWeapon weapon && !weapon.isAxe() && !weapon.isDagger()) {
            for(int outcome=0;outcome<2;outcome++) {
                var player=TestPlayers.survival(test);players.add(player);player.setNoGravity(true);
                player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(entry.get()),AdditionalAbilities.RINGING_STEEL));
                AbilityTestPackets.use(player,0);test.assertTrue(RingingSteelEvents.active(player),"Cannot arm timed Ringing Steel through ability key");
            }
        }
        test.assertTrue(players.size()==10,"Skipped timed Ringing Steel variants/outcomes");
        test.runAfterDelay(601,()->{
            try {
                for(int i=0;i<players.size();i++) {
                    var player=players.get(i);RingingSteelEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
                    WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                    test.assertTrue(RingingSteelEvents.active(player),"Unused Ringing Steel expired after thirty seconds");
                    test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).isEmpty(),"Armed strike starts cooldown without use or switch");
                    var stack=player.getMainHandItem();
                    String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+AdditionalAbilities.RINGING_STEEL;
                    if(i%2==0) {
                        var mob=EntityType.ZOMBIE.create(test.getLevel());var sword=new ItemStack(Items.IRON_SWORD);mob.setItemSlot(EquipmentSlot.MAINHAND,sword);
                        try {mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(sword.getDamageValue()==15,"Long-armed strike loses its durability effect");}
                        finally {mob.discard();}
                    } else player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());
                    WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                    test.assertFalse(RingingSteelEvents.active(player),"Used/switched strike remains armed");
                    test.assertTrue(java.util.Objects.equals(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(key),player.level().getGameTime()+600),"Ending cooldown does not start on actual use/switch");
                }
                test.succeed();
            } finally {for(var player:players) {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));RingingSteelEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}}
        });
    }
    @GameTest(template="riftfang_test")
    public static void fiveClaymoresRespectWeaponEligibilityOnNextMeleeHit(GameTestHelper test) {
        var player=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);int count=0;
        player.setPos(test.getBounds().getCenter());mob.setPos(player.position().add(0,0,2));
        var old=player.connection;var wire=new SoundWire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof PulsiteWeapon w && !w.isAxe() && !w.isDagger();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.RINGING_STEEL))==eligible,"Wrong Ringing Steel pool");if(!eligible)continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.RINGING_STEEL),"Fresh weapon arms strike");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.RINGING_STEEL);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(6);var chest=new ItemStack(Items.IRON_CHESTPLATE);chest.setDamageValue(10);var shield=new ItemStack(Items.SHIELD);shield.setDamageValue(10);
            mob.setItemSlot(EquipmentSlot.MAINHAND,sword);mob.setItemSlot(EquipmentSlot.CHEST,chest);mob.setItemSlot(EquipmentSlot.OFFHAND,shield);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL),"Cannot arm");wire.clangs.clear();wire.sparks=0;
            mob.invulnerableTime=0;mob.hurt(player.damageSources().indirectMagic(player,player),1);test.assertTrue(RingingSteelEvents.active(player) && sword.getDamageValue()==6,"Spell consumes strike");test.assertTrue(wire.clangs.isEmpty() && wire.sparks==0,"Spell plays metallic impact");
            mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);
            test.assertTrue(wire.clangs.size()==1 && wire.clangs.getFirst().getSound().value()==net.bananashelp20.forgermod.sound.ModSounds.RINGING_STEEL_HIT.get() && wire.clangs.stream().allMatch(sound->sound.getSource()==net.minecraft.sounds.SoundSource.PLAYERS) && wire.sparks==10,"Empowered hit lacks its one clang, ringing tail or spark burst");
            test.assertTrue(sword.getDamageValue()==21 && shield.getDamageValue()==10 && chest.getDamageValue()==10,"Wrong durability target or amount");test.assertFalse(RingingSteelEvents.active(player),"Strike not consumed");
            mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(sword.getDamageValue()==21 && wire.clangs.size()==1 && wire.sparks==10,"Strike or metallic feedback repeats");
            var axe=new ItemStack(Items.IRON_AXE);mob.setItemSlot(EquipmentSlot.MAINHAND,shield);mob.setItemSlot(EquipmentSlot.OFFHAND,axe);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(axe.getDamageValue()==15 && shield.getDamageValue()==10,"Offhand fallback damages shield");
            mob.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(shield.getDamageValue()==10 && !RingingSteelEvents.active(player),"No-weapon strike damages shield or fails to consume");
            sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(sword.getMaxDamage()-1);mob.setItemSlot(EquipmentSlot.MAINHAND,sword);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(sword.isEmpty(),"Last durability does not break weapon");
            RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(RingingSteelEvents.active(player),"Switch keeps armed strike");
        }}finally{RingingSteelEvents.cancel(player.getUUID());mob.discard();player.connection=old;player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped variants");test.succeed();
    }
}
