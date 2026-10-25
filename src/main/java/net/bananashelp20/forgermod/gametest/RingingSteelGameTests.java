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
    private static final class SoundWire extends net.minecraft.network.Connection {
        final java.util.List<net.minecraft.network.protocol.game.ClientboundSoundPacket> clangs=new java.util.ArrayList<>();
        int sparks;
        SoundWire(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound && (sound.getSound().value()==net.minecraft.sounds.SoundEvents.ANVIL_HIT || sound.getSound().value()==net.minecraft.sounds.SoundEvents.BELL_BLOCK))clangs.add(sound);
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket particle && particle.getParticle()==net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK)sparks+=particle.getCount();
        }
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
    public static void fiveClaymoresDamageOnlyOneHeldWeaponOnNextMeleeHit(GameTestHelper test) {
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
            test.assertTrue(wire.clangs.size()==2 && wire.clangs.get(0).getSound().value()==net.minecraft.sounds.SoundEvents.ANVIL_HIT && wire.clangs.get(1).getSound().value()==net.minecraft.sounds.SoundEvents.BELL_BLOCK && wire.clangs.stream().allMatch(sound->sound.getSource()==net.minecraft.sounds.SoundSource.PLAYERS) && wire.sparks==10,"Empowered hit lacks its one clang, ringing tail or spark burst");
            test.assertTrue(sword.getDamageValue()==21 && shield.getDamageValue()==10 && chest.getDamageValue()==10,"Wrong durability target or amount");test.assertFalse(RingingSteelEvents.active(player),"Strike not consumed");
            mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(sword.getDamageValue()==21 && wire.clangs.size()==2 && wire.sparks==10,"Strike or metallic feedback repeats");
            var axe=new ItemStack(Items.IRON_AXE);mob.setItemSlot(EquipmentSlot.MAINHAND,shield);mob.setItemSlot(EquipmentSlot.OFFHAND,axe);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(axe.getDamageValue()==15 && shield.getDamageValue()==10,"Offhand fallback damages shield");
            mob.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(shield.getDamageValue()==10 && !RingingSteelEvents.active(player),"No-weapon strike damages shield or fails to consume");
            sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(sword.getMaxDamage()-1);mob.setItemSlot(EquipmentSlot.MAINHAND,sword);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(sword.isEmpty(),"Last durability does not break weapon");
            RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.RINGING_STEEL);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(RingingSteelEvents.active(player),"Switch keeps armed strike");
        }}finally{RingingSteelEvents.cancel(player.getUUID());mob.discard();player.connection=old;player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped variants");test.succeed();
    }
}
