package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class DirectCurrentGameTests {
    private static final String ID=AdditionalAbilities.DIRECT_CURRENT;
    private static ServerPlayer player(GameTestHelper test){var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());return player;}
    private static Zombie zombie(GameTestHelper test,Vec3 position){
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoGravity(true);mob.setNoAi(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);mob.setHealth(200);mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Blocks.CARVED_PUMPKIN));mob.setPos(position);test.getLevel().addFreshEntity(mob);return mob;
    }
    private static void aim(ServerPlayer player,LivingEntity target){var delta=target.getBoundingBox().getCenter().subtract(player.getEyePosition());player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))));}
    private static void mark(ServerPlayer player,LivingEntity target){
        player.setPos(target.position().add(0,6,0));aim(player,target);
        try{AbilityTestPackets.use(player,0);}catch(Exception failure){throw new RuntimeException(failure);}
    }
    private static ItemStack equip(ServerPlayer player,int rank){
        DirectCurrentEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
        var stack=Augmentations.setLevel(new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),ID,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());return stack;
    }
    private static void cleanup(ServerPlayer player,Collection<? extends Entity> entities){DirectCurrentEvents.cancel(player.getUUID());WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));entities.forEach(Entity::discard);player.server.getPlayerList().remove(player);}
    private static void later(GameTestHelper test,int ticks,ServerPlayer player,Collection<? extends Entity> entities,Runnable action){test.runAfterDelay(ticks,()->{try{action.run();}catch(RuntimeException|Error failure){cleanup(player,entities);throw failure;}});}
    private static void fieldsLater(GameTestHelper test,int ticks,List<ServerPlayer> players,List<List<Zombie>> groups,Runnable action){
        test.runAfterDelay(ticks,()->{try{action.run();}catch(RuntimeException|Error failure){for(int i=0;i<players.size();i++)cleanup(players.get(i),groups.get(i));throw failure;}});
    }

    @GameTest(template="riftfang_test")
    public static void allFifteenVariantsMarkWithKeysNotMeleeAndPreserveRankCaps(GameTestHelper test){
        var player=player(test);var origin=player.position();var actors=List.of(zombie(test,origin),zombie(test,origin.add(6,0,0)),zombie(test,origin.add(6,0,6)),zombie(test,origin.add(0,0,6)),zombie(test,origin.add(3,0,-4)));int count=0;
        try{for(var entry:ModItems.ITEMS.getEntries()){
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon;test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(ID))==eligible,"Wrong Direct Current pool");if(!eligible)continue;count++;
            for(int rank=1;rank<=3;rank++){
                DirectCurrentEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));var stack=Augmentations.setLevel(fresh.copy(),ID,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
                actors.forEach(mob->{mob.setHealth(200);mob.invulnerableTime=0;});mark(player,actors.getFirst());
                test.assertTrue(DirectCurrentEvents.marked(player)==1 && !WeaponControlLocks.frozen(actors.getFirst()),"First mark missing or freezes before a connection");mark(player,actors.getFirst());test.assertTrue(DirectCurrentEvents.marked(player)==1,"Duplicate anchor accepted");
                actors.get(1).hurt(player.damageSources().playerAttack(player),1);test.assertTrue(DirectCurrentEvents.marked(player)==1,"Melee still marks anchors");
                for(int i=1;i<=rank;i++)mark(player,actors.get(i));mark(player,actors.get(4));
                test.assertTrue(DirectCurrentEvents.marked(player)==rank+1 && DirectCurrentEvents.remainingTicks(player)==400,"Wrong mark cap or initial lifetime");
                var health=actors.stream().map(Zombie::getHealth).toList();DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));
                for(int i=0;i<=rank;i++)test.assertTrue(actors.get(i).getHealth()==health.get(i) && WeaponControlLocks.frozen(actors.get(i)),"Marked anchor damaged or not held");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(DirectCurrentEvents.active(player),"Switch leaves current");
                for(var actor:actors)test.assertFalse(WeaponControlLocks.frozen(actor),"Switch leaves anchors locked");
            }
        }}finally{cleanup(player,actors);}test.assertTrue(count==15,"Missing Electrium variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=50)
    public static void stationaryContactRepeatsAtHalfSecondsAndWallsStopIt(GameTestHelper test){
        var player=player(test);var origin=player.position();var a=zombie(test,origin);var b=zombie(test,origin.add(0,0,6));var standing=zombie(test,origin.add(.4,0,3));var outside=zombie(test,origin.add(1.2,0,3));var above=zombie(test,origin.add(0,4,3));var actors=List.of(a,b,standing,outside,above);equip(player,1);mark(player,a);mark(player,b);
        DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(standing.getHealth()==196 && outside.getHealth()==200 && above.getHealth()==200 && a.getHealth()==200 && b.getHealth()==200,"Wrong body contact or anchor immunity");
        DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(standing.getHealth()==196,"Same-tick repeat damage");
        later(test,9,player,actors,()->{DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(standing.getHealth()==196,"Early half-second pulse");});
        later(test,10,player,actors,()->{DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(standing.getHealth()==192 && standing.getDeltaMovement().equals(Vec3.ZERO),"Missing repeated contact or added knockback");});
        later(test,20,player,actors,()->{var wall=net.minecraft.core.BlockPos.containing(origin.add(0,1,3));test.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));
            test.assertTrue(standing.getHealth()==192,"Current passes through wall");test.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());cleanup(player,actors);test.succeed();});
    }
    @GameTest(template="riftfang_test")
    public static void baseUsesConsecutiveEdgesNotDiagonalsOrClosingEdges(GameTestHelper test){
        var player=player(test);var origin=player.position();var actors=new ArrayList<Zombie>();for(var offset:List.of(Vec3.ZERO,new Vec3(6,0,0),new Vec3(6,0,6),new Vec3(0,0,6),new Vec3(3,0,3),new Vec3(0,0,3),new Vec3(3,0,0)))actors.add(zombie(test,origin.add(offset)));
        try{equip(player,3);for(int i=0;i<4;i++)mark(player,actors.get(i));player.setPos(origin.add(3,0,3));DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));
            test.assertTrue(actors.get(4).getHealth()==200 && actors.get(5).getHealth()==200 && actors.get(6).getHealth()==196 && player.getHealth()==20,"Base current adds diagonals, closes its chain or hurts the caster");
        }finally{cleanup(player,actors);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_static_field_pulses",timeoutTicks=60)
    public static void allFourSuperRanksFillEveryAnchorAndPulseWithHalfSecondStuns(GameTestHelper test){
        var origin=test.getBounds().getCenter();var players=new ArrayList<ServerPlayer>();var groups=new ArrayList<List<Zombie>>();
        for(int rank=4;rank<=7;rank++){
            var player=player(test);players.add(player);var base=origin.add(0,(rank-4)*10,0);var actors=new ArrayList<Zombie>();
            for(int x=-1;x<=7;x++)for(int y=-1;y<=8;y++)for(int z=-1;z<=7;z++)test.getLevel().setBlockAndUpdate(net.minecraft.core.BlockPos.containing(base).offset(x,y,z),Blocks.AIR.defaultBlockState());
            // Deliberately unordered marks; the field must fill the whole enclosing square.
            for(var offset:List.of(Vec3.ZERO,new Vec3(6,0,0),new Vec3(0,0,6),new Vec3(6,0,6),new Vec3(1,0,5)))actors.add(zombie(test,base.add(offset)));groups.add(actors);equip(player,rank);for(int i=0;i<4;i++)mark(player,actors.get(i));
            DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(DirectCurrentEvents.marked(player)==4 && actors.get(4).getHealth()==200 && !WeaponControlLocks.frozen(actors.get(4)),"Missing four anchors or field pulses before its interval at "+rank);
        }
        fieldsLater(test,20,players,groups,()->{for(int i=0;i<4;i++){var player=players.get(i);var actors=groups.get(i);DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));
            test.assertTrue(Math.abs(actors.get(4).getHealth()-(199-.25*i))<.01 && WeaponControlLocks.frozen(actors.get(4)),"Wrong full-field damage/stun at super"+(i+1)+": hp="+actors.get(4).getHealth()+" frozen="+WeaponControlLocks.frozen(actors.get(4))+" marks="+DirectCurrentEvents.marked(player));for(int anchor=0;anchor<4;anchor++)test.assertTrue(actors.get(anchor).getHealth()==200,"Own anchor takes field damage");DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(Math.abs(actors.get(4).getHealth()-(199-.25*i))<.01,"Same-tick field repeats");}});
        fieldsLater(test,29,players,groups,()->{for(var actors:groups)test.assertTrue(WeaponControlLocks.frozen(actors.get(4)),"Stun shorter than0.5s");});
        fieldsLater(test,31,players,groups,()->{for(var actors:groups)test.assertFalse(WeaponControlLocks.frozen(actors.get(4)),"Field stun never releases between pulses");});
        test.runAfterDelay(40,()->{try{for(int i=0;i<4;i++){DirectCurrentEvents.tick(new PlayerTickEvent.Post(players.get(i)));test.assertTrue(Math.abs(groups.get(i).get(4).getHealth()-(198-.5*i))<.01,"One-second pulse does not repeat");}test.succeed();}
            finally{for(int i=0;i<players.size();i++)cleanup(players.get(i),groups.get(i));}});
    }
    @GameTest(template="riftfang_test",batch="review_static_field_timer",timeoutTicks=40)
    public static void laterMarksDoNotRefreshFirstConnectionLifetime(GameTestHelper test){
        var player=player(test);var origin=player.position();var actors=List.of(zombie(test,origin),zombie(test,origin.add(6,0,0)),zombie(test,origin.add(0,0,6)),zombie(test,origin.add(6,0,6)));equip(player,7);mark(player,actors.get(0));mark(player,actors.get(1));
        test.assertTrue(DirectCurrentEvents.remainingTicks(player)==400,"Lifetime does not start at first connection");
        later(test,10,player,actors,()->{mark(player,actors.get(2));mark(player,actors.get(3));test.assertTrue(DirectCurrentEvents.marked(player)==4 && DirectCurrentEvents.remainingTicks(player)==390,"Later marks refresh field lifetime");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));test.assertFalse(DirectCurrentEvents.active(player),"Switch keeps field");for(var actor:actors)test.assertFalse(WeaponControlLocks.frozen(actor),"Cancel keeps anchor lock");cleanup(player,actors);test.succeed();});
    }
    @GameTest(template="riftfang_test")
    public static void passiveAlliedAndDistantTargetsAreNotMarked(GameTestHelper test){
        var player=player(test);var origin=player.position();var cow=EntityType.COW.create(test.getLevel());var wolf=EntityType.WOLF.create(test.getLevel());wolf.tame(player);
        for(var mob:List.of(cow,wolf)){mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(origin);test.getLevel().addFreshEntity(mob);}var near=zombie(test,origin.add(6,0,0));var far=zombie(test,origin.add(0,60,0));var actors=List.of(cow,wolf,near,far);
        try{equip(player,1);mark(player,cow);mark(player,wolf);test.assertFalse(DirectCurrentEvents.active(player),"Passive/allied target starts circuit");mark(player,near);player.setPos(origin);aim(player,far);
            try{AbilityTestPackets.use(player,0);}catch(Exception e){throw new RuntimeException(e);}test.assertTrue(DirectCurrentEvents.marked(player)==1,"Distant target outside50 blocks is marked");
        }finally{cleanup(player,actors);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void geometryRejectsWrongHeightAndFillsUnorderedMarksAtLargeCoordinates(GameTestHelper test){
        Vec3 a=new Vec3(0,0,0),b=new Vec3(0,0,6),c=new Vec3(6,0,0);
        test.assertTrue(DirectCurrentEvents.crosses(new Vec3(-1,0,3),new Vec3(1,0,3),a,b),"Real crossing rejected");test.assertFalse(DirectCurrentEvents.crosses(new Vec3(-1,4,3),new Vec3(1,4,3),a,b),"Barrier ignores height");
        test.assertTrue(DirectCurrentEvents.inside(new Vec3(1,0,1),a,b,c),"Triangle rejected");test.assertFalse(DirectCurrentEvents.inside(new Vec3(0,0,1),a,b,new Vec3(0,0,3)),"Degenerate field accepted");
        var offset=new Vec3(14000000,0,-14000000);var polygon=DirectCurrentEvents.fieldPolygon(List.of(a.add(offset),b.add(offset),c.add(offset),new Vec3(6,0,6).add(offset)));
        test.assertTrue(DirectCurrentEvents.inside(new Vec3(5,0,5).add(offset),polygon),"All-anchor field missing opposite corner");test.assertFalse(DirectCurrentEvents.inside(new Vec3(5,4,5).add(offset),polygon) || DirectCurrentEvents.inside(new Vec3(7,0,5).add(offset),polygon),"Polygon exceeds height or boundary");test.succeed();
    }
}
