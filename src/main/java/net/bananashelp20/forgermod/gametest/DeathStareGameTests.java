package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.DeathStareAbility;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DeathStareGameTests {
    private static ServerPlayer player(GameTestHelper test,int length) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
        // Keep the 150-block sight line above neighboring test arenas and their mobs.
        if(length>120) player.setPos(player.getX(),160,player.getZ());
        for(int x=-2;x<=2;x++) for(int z=-2;z<=length+2;z++) for(int y=0;y<=8;y++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        return player;
    }
    @GameTest(template="riftfang_test")
    public static void upwardViewTargetsAirborneMobAndMountedUseIsRejected(GameTestHelper test) {
        var player=player(test,12); var stack=Augmentations.apply(new ItemStack(ModItems.RIFTFANG_DAGGER.get()),RecommendedAbilities.DEATH_STARE);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setNoGravity(true); mob.setPos(player.position().add(0,4,10)); test.getLevel().addFreshEntity(mob);
        player.setXRot((float)-Math.toDegrees(Math.atan2(4,10)));
        double before=player.getY();
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.DEATH_STARE),"Upward targeting ignores view pitch");
        test.assertTrue(player.distanceToSqr(mob)<4 && player.getY()>before+3,"Airborne target cannot be reached safely");
        test.assertTrue(player.startRiding(mob,true),"Mounted fixture cannot ride");
        test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.DEATH_STARE),"Mounted activation allowed");
        player.stopRiding(); mob.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allDaggerVariantsTeleportBesideCrosshairMob(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.RIFTFANG_DAGGER.get(),ModItems.RIFTFANG_DAGGER_RUBY.get(),ModItems.RIFTFANG_DAGGER_AMBER.get(),
                ModItems.RIFTFANG_DAGGER_AMETHYST.get(),ModItems.RIFTFANG_DAGGER_JADE.get()}) {
            var player=player(test,12); var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,RecommendedAbilities.DEATH_STARE),"Fresh dagger teleports");
            var stack=Augmentations.apply(fresh,RecommendedAbilities.DEATH_STARE); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setNoGravity(true); mob.setPos(player.position().add(0,0,10)); test.getLevel().addFreshEntity(mob);
            player.fallDistance=15; float yaw=player.getYRot();
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.DEATH_STARE),"Death Stare does not activate");
            test.assertTrue(player.distanceToSqr(mob)<4 && player.getYRot()==yaw && player.fallDistance==0,"Wrong destination/view/fall reset");
            test.assertTrue(player.level().noCollision(player,player.getBoundingBox()),"Teleported into collision");
            mob.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=600)
    public static void unlimitedRankUsesLoadedMobButRejectsWallsAndStartsInstantCooldown(GameTestHelper test) throws Exception {
        var player=player(test,160); ItemStack initial=Augmentations.apply(new ItemStack(ModItems.RIFTFANG_DAGGER.get()),RecommendedAbilities.DEATH_STARE);
        player.setItemInHand(InteractionHand.MAIN_HAND,initial);
        var destination=player.position().add(0,0,150);
        var forced=new java.util.HashMap<net.minecraft.world.level.ChunkPos,Boolean>();
        for(int x=(player.blockPosition().getX()-2)>>4;x<=((player.blockPosition().getX()+2)>>4);x++)
            for(int z=(player.blockPosition().getZ()-2)>>4;z<=((player.blockPosition().getZ()+162)>>4);z++) {
                var chunk=new net.minecraft.world.level.ChunkPos(x,z); forced.put(chunk,test.getLevel().getForcedChunks().contains(chunk.toLong()));
                test.getLevel().setChunkForced(x,z,true);
            }
        test.startSequence().thenWaitUntil(()->test.assertTrue(forced.keySet().stream().allMatch(chunk->test.getLevel().isPositionEntityTicking(chunk.getWorldPosition())),"Waiting for fixture chunks to become entity-ticking")).thenExecute(()-> { try {
        ItemStack stack=player.getMainHandItem();
        var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setNoGravity(true); mob.setPos(player.position().add(0,0,150)); test.getLevel().addFreshEntity(mob);
        test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.DEATH_STARE),"Rank I exceeds 30 blocks");
        for(int rank=1;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.DEATH_STARE);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var wall=player.blockPosition().south(2).above(); test.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
        AbilityTestPackets.use(player,0);
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.DEATH_STARE;
        test.assertFalse(player.getData(WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Blocked targeting starts cooldown");
        test.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());
        test.assertTrue(DeathStareAbility.target(player,Double.POSITIVE_INFINITY)==mob,"Far fixture target unavailable: entity="+test.getLevel().getEntity(mob.getId())+", alive="+mob.isAlive()+", loaded="+test.getLevel().getChunkSource().hasChunk(mob.blockPosition().getX()>>4,mob.blockPosition().getZ()>>4));
        AbilityTestPackets.use(player,0);
        test.assertTrue(player.distanceToSqr(mob)<4,"Rank IV fails beyond rank III range");
        test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+600,"Instant rank IV cooldown wrong");
        test.assertTrue(DeathStareAbility.range(2)==60 && DeathStareAbility.range(3)==120 && Double.isInfinite(DeathStareAbility.range(4)),"Upgrade range wrong");
        mob.discard(); test.succeed();
        } catch(Exception error) { throw new RuntimeException(error); } finally {
            forced.forEach((chunk,wasForced)->test.getLevel().setChunkForced(chunk.x,chunk.z,wasForced));
            test.getLevel().getServer().getPlayerList().remove(player);
        } });
    }
}
