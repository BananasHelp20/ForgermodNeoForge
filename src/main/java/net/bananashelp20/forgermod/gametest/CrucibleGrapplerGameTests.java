package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.*;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
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
public final class CrucibleGrapplerGameTests {
    private static List<Item> variants(){return List.of(ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),ModItems.MOLTEN_AXE_AMBER.get(),ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get());}
    private static ServerPlayer player(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(BlockPos.containing(test.getBounds().getCenter())));player.setXRot(-90);player.setYRot(0);
        for(int y=0;y<=44;y++)test.getLevel().setBlockAndUpdate(player.blockPosition().above(y),Blocks.AIR.defaultBlockState());return player;
    }
    private static void cleanup(ServerPlayer player){CrucibleHookEvents.cancel(player.getUUID());WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
    @GameTest(template="riftfang_test",batch="review_grappler_ranges")
    public static void allVariantsRangesAccelerateWithoutTeleportAndSyncBlockAnchors(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();
        try{for(var item:variants())for(int rank=5;rank<=8;rank++){
            var stack=Augmentations.setLevel(new ItemStack(item),RecommendedAbilities.CRUCIBLE_HOOK,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());player.setDeltaMovement(Vec3.ZERO);
            int range=ReviewedRanks.grappleRange(rank);var inside=BlockPos.containing(player.getEyePosition()).above(range);test.getLevel().setBlockAndUpdate(inside,Blocks.STONE.defaultBlockState());
            AbilityTestPackets.use(player,0);var anchor=CrucibleHookRope.blockAnchor(player);var first=player.getDeltaMovement();
            test.assertTrue(CrucibleHookEvents.active(player) && anchor!=null && anchor.distanceTo(player.getEyePosition())<=range && first.y>=.4 && player.position().equals(origin),"No in-range accelerating grapple at "+rank);
            CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));test.assertTrue(player.getDeltaMovement().length()>first.length() && player.position().equals(origin),"Grapple teleports or has no acceleration");
            // Reach useful traversal speed within a few updates, retaining the existing safe maximum.
            for(int tick=0;tick<4;tick++)CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
            test.assertTrue(player.getDeltaMovement().length()>2.4 && player.getDeltaMovement().length()<=2.50001 && player.position().equals(origin),"Grapple still has a slow wind-up, exceeds its speed cap or teleports");
            var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
            try{var snapshot=CrucibleHookRope.snapshot(player);CrucibleHookRope.Payload.CODEC.encode(buffer,snapshot);test.assertTrue(snapshot.equals(CrucibleHookRope.Payload.CODEC.decode(buffer)),"Block rope codec loses anchor");}finally{buffer.release();}
            var momentum=player.getDeltaMovement();test.getLevel().setBlockAndUpdate(inside,Blocks.AIR.defaultBlockState());
            test.assertFalse(CrucibleHookEvents.active(player),"Destroyed anchor retains grapple");test.assertTrue(player.getDeltaMovement().equals(momentum),"Destroyed anchor clears momentum");WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.CRUCIBLE_HOOK+":grapple";
            test.assertTrue(rank==8?!player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key):player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+500,"Wrong per-mode grapple cooldown at "+rank);
            player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());var outside=inside.above();test.getLevel().setBlockAndUpdate(outside,Blocks.STONE.defaultBlockState());AbilityTestPackets.use(player,0);
            test.assertFalse(CrucibleHookEvents.active(player),"Out-of-range block selected");test.getLevel().setBlockAndUpdate(outside,Blocks.AIR.defaultBlockState());
            test.assertTrue(Augmentations.maxRank(stack,RecommendedAbilities.CRUCIBLE_HOOK)==8 && Augmentations.displayRank(stack,RecommendedAbilities.CRUCIBLE_HOOK,rank)==rank-4,"Wrong Grappler super ranks");
        }
        var base=Augmentations.setLevel(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.CRUCIBLE_HOOK,4);player.setItemInHand(InteractionHand.MAIN_HAND,base);var block=BlockPos.containing(player.getEyePosition()).above(10);test.getLevel().setBlockAndUpdate(block,Blocks.STONE.defaultBlockState());
        test.assertFalse(CrucibleHookEvents.activate(player,base),"Base Hook can grapple blocks");test.getLevel().setBlockAndUpdate(block,Blocks.AIR.defaultBlockState());
        }finally{cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_grappler_release")
    public static void arrivalSettingRightClickAndSwitchPreserveMomentum(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();var block=BlockPos.containing(player.getEyePosition()).above(12);boolean previous=Config.GRAPPLE_AUTO_RELEASE.get();
        var stack=Augmentations.setLevel(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.CRUCIBLE_HOOK,8);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.getLevel().setBlockAndUpdate(block,Blocks.STONE.defaultBlockState());
        try{
            test.assertTrue(Config.GRAPPLE_AUTO_RELEASE.getDefault(),"Auto release default is not enabled");Config.GRAPPLE_AUTO_RELEASE.set(true);AbilityTestPackets.use(player,0,false);
            var destination=CrucibleHookRope.blockAnchor(player).add(0,-.8-player.getBbHeight()*.5,0);player.setPos(destination);player.setDeltaMovement(0,.3,0);CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
            test.assertTrue(CrucibleHookEvents.active(player),"Disabled auto release still disconnects on arrival");var momentum=player.getDeltaMovement();
            var click=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(player,InteractionHand.MAIN_HAND);CrucibleGrapplerEvents.release(click);
            test.assertTrue(click.isCanceled() && !CrucibleHookEvents.active(player) && player.getDeltaMovement().equals(momentum),"Right-click fails to release with momentum");
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));player.setPos(origin);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());Config.GRAPPLE_AUTO_RELEASE.set(true);AbilityTestPackets.use(player,0);
            player.setPos(destination);player.setDeltaMovement(.4,.2,.6);momentum=player.getDeltaMovement();CrucibleHookEvents.onTick(new PlayerTickEvent.Post(player));
            test.assertTrue(!CrucibleHookEvents.active(player) && player.getDeltaMovement().equals(momentum) && CrucibleHookRope.blockAnchor(player)==null,"Auto release clears velocity or keeps rope");
            WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));player.setPos(origin);player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());AbilityTestPackets.use(player,0);momentum=player.getDeltaMovement();player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(!CrucibleHookEvents.active(player) && player.getDeltaMovement().equals(momentum),"Weapon switch leaves grapple or erases velocity");
        }finally{Config.GRAPPLE_AUTO_RELEASE.set(previous);test.getLevel().setBlockAndUpdate(block,Blocks.AIR.defaultBlockState());cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="review_grappler_smash")
    public static void allSuperSmashesKeepMobFunctionalityAndReviewedDamage(GameTestHelper test)throws Exception{
        var player=player(test);player.setXRot(0);var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);zombie.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(zombie);
        try{for(var item:variants())for(int rank=5;rank<=8;rank++){
            var stack=Augmentations.setLevel(new ItemStack(item),RecommendedAbilities.CRUCIBLE_HOOK,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());zombie.setHealth(200);zombie.invulnerableTime=0;zombie.setRemainingFireTicks(0);
            AbilityTestPackets.use(player,0);double expected=200-player.getAttributeValue(Attributes.ATTACK_DAMAGE)*(1+.25*rank);
            test.assertTrue(!CrucibleHookEvents.active(player) && Math.abs(zombie.getHealth()-expected)<.01 && zombie.getRemainingFireTicks()==(2+rank)*20 && stack.getDamageValue()==1,"Wrong super mob smash at "+rank);
            var description=AbilityDescriptions.description(stack,RecommendedAbilities.CRUCIBLE_HOOK,rank,false).getString();test.assertTrue(description.contains(ReviewedRanks.grappleRange(rank)+" blocks") && description.contains((2+rank)+" seconds"),"Missing current grapple description");
        }}finally{zombie.discard();cleanup(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",batch="grappler_rehook_cooldowns")
    public static void maxGrappleRetargetsAtomicallyAndMobHookKeepsIndependentCooldown(GameTestHelper test)throws Exception{
        var player=player(test);var origin=player.position();
        // The template/world can contain reachable blocks outside its bounds: make the invalid ray explicitly empty.
        for(int x=-1;x<=10;x++)for(int z=-41;z<=3;z++)for(int y=0;y<=4;y++)test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        var above=player.blockPosition().above(12);test.getLevel().setBlockAndUpdate(above,Blocks.STONE.defaultBlockState());
        var side=player.blockPosition().east(8).above();test.getLevel().setBlockAndUpdate(side,Blocks.STONE.defaultBlockState());
        try{for(var item:variants()){
            player.setPos(origin);player.setXRot(-90);player.setYRot(0);player.setDeltaMovement(Vec3.ZERO);
            var stack=Augmentations.setLevel(new ItemStack(item),RecommendedAbilities.CRUCIBLE_HOOK,8);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());
            AbilityTestPackets.use(player,0,false);var first=CrucibleHookRope.blockAnchor(player);test.assertTrue(first!=null,"Initial grapple missing");
            player.setXRot(0);player.setYRot(180);var momentum=player.getDeltaMovement();AbilityTestPackets.use(player,0,false);
            test.assertTrue(CrucibleHookEvents.active(player) && first.equals(CrucibleHookRope.blockAnchor(player)) && player.getDeltaMovement().equals(momentum),"Invalid rehook releases previous anchor or changes momentum");
            player.setYRot(-90);AbilityTestPackets.use(player,0,false);var second=CrucibleHookRope.blockAnchor(player);
            test.assertTrue(second!=null && !first.equals(second) && second.distanceToSqr(side.getCenter())<2,"Reachable rehook does not replace old block anchor");
            CrucibleGrapplerEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).isEmpty(),"Max block grapple creates a cooldown");
            String legacyKey=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.CRUCIBLE_HOOK+":grapple";
            player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of(legacyKey,player.level().getGameTime()+500));
            player.setXRot(-90);AbilityTestPackets.use(player,0,false);test.assertTrue(CrucibleHookEvents.active(player),"Max grapple cannot immediately reactivate or inherits old lower-rank cooldown");
            CrucibleGrapplerEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());player.setXRot(0);player.setYRot(0);
            var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);zombie.setHealth(200);zombie.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(zombie);
            try{
                AbilityTestPackets.use(player,0);String attackKey=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.CRUCIBLE_HOOK;
                long deadline=player.level().getGameTime()+500;
                test.assertTrue(zombie.getHealth()<200 && player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(attackKey,0L)==deadline,"Mob hook lost usual cooldown");
                float health=zombie.getHealth();AbilityTestPackets.use(player,0);test.assertTrue(zombie.getHealth()==health,"Mob-hook cooldown is bypassed");
                player.setXRot(-90);AbilityTestPackets.use(player,0,false);
                test.assertTrue(CrucibleHookEvents.active(player) && player.getData(WeaponCooldownAttachments.COOLDOWNS).get(attackKey)==deadline,"Mob cooldown blocks grapple or grapple changes mob deadline");
                CrucibleGrapplerEvents.cancel(player.getUUID());WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            }finally{zombie.discard();}
        }}finally{cleanup(player);}test.succeed();
    }

}
