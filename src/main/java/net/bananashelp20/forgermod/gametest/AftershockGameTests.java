package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.AftershockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AftershockGameTests {
    private record Pair(ServerPlayer player,Zombie target,float health) {}
    private static Zombie target(GameTestHelper test,Vec3 pos) {
        var z=EntityType.ZOMBIE.create(test.getLevel());z.setNoAi(true);z.setNoGravity(true);z.setPos(pos);z.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));
        z.getAttribute(Attributes.ARMOR).setBaseValue(0);z.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);z.setHealth(1000);test.getLevel().addFreshEntity(z);return z;
    }
    @GameTest(template="riftfang_test",timeoutTicks=90)
    public static void fifteenVariantsEchoExactlyOnceAfterTwentyTicksEvenDuringImmunity(GameTestHelper test) {
        var pairs=new ArrayList<Pair>();var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        for(int x=5;x<=11;x++)for(int y=5;y<=9;y++)for(int z=5;z<=12;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof PulsiteWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.AFTERSHOCK))==eligible,"Wrong echo pool");if(!eligible)continue;
            var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(point);var mob=target(test,point.add(0,0,2));
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(fresh,AdditionalAbilities.AFTERSHOCK));for(int i=0;i<60;i++)player.doTick();player.setOnGround(false);player.fallDistance=0;
            player.attack(mob);AxeAnimationTestDriver.finish(player);mob.setDeltaMovement(Vec3.ZERO);pairs.add(new Pair(player,mob,mob.getHealth()));
        }}catch(Throwable failure){cleanup(pairs);throw failure;}
        test.assertTrue(pairs.size()==15,"Skipped variants");
        test.runAfterDelay(10,()->{for(var pair:pairs)test.assertTrue(pair.target().getHealth()==pair.health(),"Echo arrives before one second");});
        test.runAfterDelay(19,()->{for(var pair:pairs)pair.target().invulnerableTime=20;});
        test.runAfterDelay(21,()->{for(var pair:pairs)pair.player().doTick();});
        test.runAfterDelay(22,()->{for(var pair:pairs)test.assertTrue(pair.target().getHealth()==pair.health()-1,"Echo suppressed by immunity or wrong damage: "+pair.target().getHealth()+" expected "+(pair.health()-1));});
        test.runAfterDelay(45,()->{try {for(var pair:pairs)pair.player().doTick();for(var pair:pairs)test.assertTrue(pair.target().getHealth()==pair.health()-1,"Echo repeats or recurses");}finally{cleanup(pairs);}test.succeed();});
    }
    private static void cleanup(List<Pair> pairs) {for(var pair:pairs){AftershockEvents.cancel(pair.player().getUUID());pair.target().discard();pair.player().server.getPlayerList().remove(pair.player());}}
    @GameTest(template="riftfang_test",timeoutTicks=60)
    public static void freshUnchargedAndSwitchedHitsDoNotEcho(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        var freshTarget=target(test,point.add(0,0,2));var uncharged=target(test,point.add(1,0,2));var switched=target(test,point.add(-1,0,2));
        var fresh=new ItemStack(ModItems.SHRIEKING_CLAYMORE.get());player.setItemInHand(InteractionHand.MAIN_HAND,fresh);for(int i=0;i<60;i++)player.doTick();player.setOnGround(false);player.fallDistance=0;player.attack(freshTarget);AxeAnimationTestDriver.finish(player);
        var stack=Augmentations.apply(fresh,AdditionalAbilities.AFTERSHOCK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.attack(uncharged);AxeAnimationTestDriver.finish(player);
        for(int i=0;i<60;i++)player.doTick();player.setOnGround(false);player.fallDistance=0;player.attack(switched);AxeAnimationTestDriver.finish(player);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());
        float f=freshTarget.getHealth(),u=uncharged.getHealth(),s=switched.getHealth();
        test.runAfterDelay(25,()->{try{player.doTick();test.assertTrue(freshTarget.getHealth()==f && uncharged.getHealth()==u && switched.getHealth()==s,"Invalid hit or switched stack still echoes");}finally{AftershockEvents.cancel(player.getUUID());freshTarget.discard();uncharged.discard();switched.discard();player.server.getPlayerList().remove(player);}test.succeed();});
    }
}
