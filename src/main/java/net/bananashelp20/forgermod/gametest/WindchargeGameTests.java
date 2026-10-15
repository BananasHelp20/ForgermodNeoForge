package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.*;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class WindchargeGameTests {
    @GameTest(template="riftfang_test")
    public static void ordinaryLaunchPreventsOneRealFallAndSwitchEndsProtection(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        try {
            for(var item:java.util.List.of(ModItems.DEAD_CALM_DAGGER.get(),ModItems.STORMING_CLAYMORE.get(),ModItems.SKYBREAKER_AXE.get())) {
                var stack=Augmentations.apply(new ItemStack(item),AdditionalAbilities.WINDCHARGE);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setHealth(20);player.invulnerableTime=0;
                test.assertTrue(WindchargeEvents.activate(player,stack),"Ordinary Windcharge launch rejected");
                player.causeFallDamage(8,1,player.damageSources().fall());
                test.assertTrue(player.getHealth()==20,"Windcharge landing dealt fall damage");
                player.causeFallDamage(8,1,player.damageSources().fall());
                test.assertTrue(player.getHealth()<20,"Windcharge protected a second unrelated fall");
                player.setHealth(20);player.invulnerableTime=0;
                test.assertTrue(WindchargeEvents.activate(player,stack),"Ordinary Windcharge could not relaunch");
                player.hurt(player.damageSources().fall(),8);
                test.assertTrue(player.getHealth()==20,"Direct fall damage bypassed Windcharge landing protection");
                player.hurt(player.damageSources().fall(),8);
                test.assertTrue(player.getHealth()<20,"Windcharge protected a second direct fall");
                player.setHealth(20);player.invulnerableTime=0;
                test.assertTrue(WindchargeEvents.activate(player,stack),"Ordinary Windcharge could not relaunch after direct fall");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());
                player.causeFallDamage(8,1,player.damageSources().fall());
                test.assertTrue(player.getHealth()<20,"Changing the held stack kept landing protection");
                WindchargeEvents.cancel(player.getUUID());
            }
        } finally {WindchargeEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
        test.succeed();
    }

    private static final class RecordingConnection extends net.minecraft.network.Connection {
        final java.util.List<net.minecraft.network.protocol.Packet<?>> packets=new java.util.ArrayList<>();
        RecordingConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){packets.add(packet);}
    }
    @GameTest(template="riftfang_test")
    public static void allFiveAxesSmashFiveBlockRadiusWithNativeMaceFeedbackEvenWithoutAHit(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=1;x<=15;x++)for(int z=1;z<=15;z++){test.setBlock(new BlockPos(x,4,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=5;y<=9;y++)test.setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);}
        test.setBlock(new BlockPos(8,6,10),net.minecraft.world.level.block.Blocks.STONE);test.setBlock(new BlockPos(8,7,10),net.minecraft.world.level.block.Blocks.STONE);
        var edge=EntityType.ZOMBIE.create(test.getLevel());var outside=EntityType.ZOMBIE.create(test.getLevel());var hidden=EntityType.ZOMBIE.create(test.getLevel());var neutral=EntityType.COW.create(test.getLevel());
        for(var mob:java.util.List.of(edge,outside,hidden,neutral)){mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);test.getLevel().addFreshEntity(mob);}
        var connection=new RecordingConnection();new io.netty.channel.embedded.EmbeddedChannel(connection);var previous=player.connection;
        new net.minecraft.server.network.ServerGamePacketListenerImpl(test.getLevel().getServer(),connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) if(entry.get() instanceof TaifuniteWeapon weapon && weapon.isAxe()) {
                count++;var stack=new ItemStack(entry.get());
                for(int rank=1;rank<=7;rank++) {
                    stack=Augmentations.apply(stack,AdditionalAbilities.WINDCHARGE);if(rank<4)continue;
                    player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setPos(origin);
                    edge.setPos(origin.add(5,0,0));outside.setPos(origin.add(-5.1,0,0));hidden.setPos(origin.add(0,0,4));neutral.setPos(origin.add(-2,0,0));
                    for(var mob:java.util.List.of(edge,outside,hidden,neutral)){mob.setHealth(1000);mob.invulnerableTime=20;}
                    test.assertTrue(WindchargeEvents.activate(player,stack),"Ground-only flight rejected");connection.packets.clear();
                    var fall=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,30,1);WindchargeEvents.landing(fall);
                    float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)+44*(1+.1f*(rank-4));
                    test.assertTrue(Math.abs(edge.getHealth()-(1000-damage))<.01 && outside.getHealth()==1000 && hidden.getHealth()==1000 && neutral.getHealth()==1000,"Five-block damage, rank scaling, immunity, wall or ally exclusion is wrong");
                    test.assertTrue(fall.getDamageMultiplier()==0 && !WindchargeEvents.active(player),"Landing hurts owner or leaves flight active");
                    test.assertTrue(connection.packets.stream().anyMatch(p->p instanceof net.minecraft.network.protocol.game.ClientboundLevelEventPacket e && e.getType()==2013 && e.getData()==750),"Native mace smash animation not sent");
                    test.assertTrue(connection.packets.stream().anyMatch(p->p instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s && s.getSound().value()==net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND_HEAVY),"Heavy mace sound not sent");
                    float health=edge.getHealth();WindchargeEvents.landing(new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,30,1));test.assertTrue(edge.getHealth()==health,"Landing damage repeats");
                }
            }
            test.assertTrue(count==5,"Skipped Skybreaker axe variants");test.succeed();
        } finally {WindchargeEvents.cancel(player.getUUID());player.connection=previous;for(var mob:java.util.List.of(edge,outside,hidden,neutral))mob.discard();player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void groundedTickStillSmashesAfterMaceHitResetFallDistance(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.setPos(player.position().add(4,0,0));test.getLevel().addFreshEntity(mob);
        try {
            var stack=new ItemStack(ModItems.SKYBREAKER_AXE.get());for(int rank=0;rank<4;rank++)stack=Augmentations.apply(stack,AdditionalAbilities.WINDCHARGE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
            test.assertTrue(WindchargeEvents.activate(player,stack),"Fallback flight rejected");player.setOnGround(false);player.setDeltaMovement(0,-1,0);player.fallDistance=12;
            WindchargeEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            player.resetFallDistance();player.setOnGround(true);player.setDeltaMovement(Vec3.ZERO);
            float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)+WindchargeEvents.fallBonus(12);
            WindchargeEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            test.assertTrue(Math.abs(mob.getHealth()-(1000-damage))<.01 && !WindchargeEvents.active(player),"Ground contact fails when vanilla fall distance is reset");
            player.hurt(player.damageSources().fall(),8);
            test.assertTrue(player.getHealth()==20,"Ground-contact fallback cleared fall protection before impact damage");
            test.succeed();
        } finally {WindchargeEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void skybreakerSuperIVRetainsLaunchAndUpgradesFallStrike(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.setPos(player.position().add(1,0,0));test.getLevel().addFreshEntity(mob);
        try {
            var stack=new ItemStack(ModItems.SKYBREAKER_AXE.get());for(int i=0;i<7;i++)stack=Augmentations.apply(stack,AdditionalAbilities.WINDCHARGE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int tick=0;tick<60;tick++)player.doTick();
            test.assertTrue(WindchargeEvents.activate(player,stack) && WindchargeEvents.active(player),"Super IV flight missing");
            test.assertTrue(Math.abs(player.getDeltaMovement().y-WindchargeEvents.launchVelocity(30))<.001,"Super IV changed launch height");
            player.fallDistance=4;player.setOnGround(false);player.setSprinting(false);player.setDeltaMovement(0,-1,0);float expected=((float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)+14*1.3f)*1.5f;player.attack(mob);
            test.assertTrue(Math.abs(mob.getHealth()-(1000-expected))<.01 && WindchargeEvents.active(player),"Super IV mace critical scaling or pending landing is wrong");
            WindchargeEvents.landing(new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,4,1));test.assertFalse(WindchargeEvents.active(player),"Super IV landing remains active");
            player.setHealth(20);player.invulnerableTime=0;test.assertTrue(WindchargeEvents.activate(player,stack),"Super IV could not relaunch");
            player.setOnGround(false);player.setDeltaMovement(0,-1,0);player.fallDistance=10;
            player.hurt(player.damageSources().fall(),8);
            test.assertTrue(player.getHealth()==20 && !WindchargeEvents.active(player),"Direct Skybreaker impact hurt its owner");
        } finally {WindchargeEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void launchesAllVariantsAndSkybreakerSmashesGroundOnce(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);int count=0;
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.setPos(origin.add(1,0,0));test.getLevel().addFreshEntity(mob);
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.WINDCHARGE))==eligible,"Wrong Windcharge pool");if(!eligible) continue;count++;
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.WINDCHARGE),"Fresh weapon launches");
                ItemStack stack=fresh;boolean axe=((SwordItemWithEffect)fresh.getItem()).isAxe();
                for(int rank=1;rank<=4;rank++) {
                    stack=Augmentations.apply(stack,AdditionalAbilities.WINDCHARGE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int tick=0;tick<60;tick++)player.doTick();player.setPos(origin);player.setDeltaMovement(.23,-.4,.37);player.fallDistance=12;
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.WINDCHARGE),"Learned launch refused");var v=player.getDeltaMovement();double expected=axe?new int[]{10,16,23,30}[rank-1]:5*rank,y=0;
                    for(double speed=v.y;speed>0;speed=(speed-.08)*.98) y+=speed;
                    test.assertTrue(Math.abs(y-expected)<.001 && v.x==.23 && v.z==.37 && player.fallDistance==0,"Launch height/momentum wrong");
                    test.assertTrue(Augmentations.abilityName(stack,AdditionalAbilities.WINDCHARGE,rank).getString().equals(axe && rank==4?"Skybreaker":"Windcharge"),"Rank title wrong");
                    if(axe && rank==4) {
                        test.assertTrue(WindchargeEvents.active(player),"Skybreaker flight absent");mob.setHealth(1000);mob.invulnerableTime=0;
                        var fall=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,4,1);WindchargeEvents.landing(fall);
                        test.assertTrue(fall.getDamageMultiplier()==0 && mob.getHealth()<1000 && !WindchargeEvents.active(player),"Ground smash fails or hurts owner");float health=mob.getHealth();WindchargeEvents.landing(new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,4,1));test.assertTrue(mob.getHealth()==health,"Ground smash repeats");
                        for(int tick=0;tick<60;tick++)player.doTick();player.setPos(origin);mob.setPos(origin.add(1,0,0));WindchargeEvents.activate(player,stack);player.fallDistance=4;player.setOnGround(false);player.setSprinting(false);player.setDeltaMovement(0,-1,0);mob.setHealth(1000);mob.invulnerableTime=0;float expectedStrike=((float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)+14)*1.5f;player.attack(mob);
                        test.assertTrue(Math.abs(mob.getHealth()-(1000-expectedStrike))<.01 && WindchargeEvents.active(player) && player.fallDistance==0,"Real falling strike does not match mace scaling or consumes landing");
                        float beforeLanding=mob.getHealth();
                        var landing=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,0,1);
                        WindchargeEvents.landing(landing);
                        float expectedLanding=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)+WindchargeEvents.fallBonus(4);
                        test.assertTrue(Math.abs(mob.getHealth()-(beforeLanding-expectedLanding))<.01 && landing.getDamageMultiplier()==0
                                && !WindchargeEvents.active(player),"Direct hit prevents the later landing smash or fall protection");
                        WindchargeEvents.activate(player,stack);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(WindchargeEvents.active(player),"Switch keeps smash");
                    } else {
                        var fall=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,12,1);
                        WindchargeEvents.landing(fall);
                        test.assertTrue(fall.getDamageMultiplier()==0,"Ordinary Windcharge leaves fall damage enabled");
                        var second=new net.neoforged.neoforge.event.entity.living.LivingFallEvent(player,12,1);
                        WindchargeEvents.landing(second);
                        test.assertTrue(second.getDamageMultiplier()==1,"Ordinary Windcharge protects more than one landing");
                    }
                }
            }
        } finally {WindchargeEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.assertTrue(WindchargeEvents.fallBonus(3)==12 && WindchargeEvents.fallBonus(8)==22 && WindchargeEvents.fallBonus(30)==44,"Mace damage differs from vanilla");test.succeed();
    }
}
