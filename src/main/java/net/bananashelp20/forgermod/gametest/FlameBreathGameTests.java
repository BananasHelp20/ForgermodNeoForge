package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FlameBreathGameTests {
    @GameTest(template="riftfang_test")
    public static void extendedRangeRespectsRankOneAndRankFourBoundaries(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true);
        player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        player.setYRot(0); player.setXRot(0);
        for(int z=1;z<=12;z++) for(int y=0;y<=3;y++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(0,y,z),Blocks.AIR.defaultBlockState());
        var inside=EntityType.ZOMBIE.create(test.getLevel());
        var outside=EntityType.ZOMBIE.create(test.getLevel());
        for(var zombie:new net.minecraft.world.entity.monster.Zombie[]{inside,outside}) {
            zombie.setNoAi(true); zombie.setNoGravity(true);
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); zombie.setHealth(200);
        }
        inside.setPos(player.position().add(0,0,6.5));
        outside.setPos(player.position().add(0,0,7.5));
        test.getLevel().getChunkAt(inside.blockPosition()); test.getLevel().getChunkAt(outside.blockPosition());
        test.getLevel().addFreshEntity(inside); test.getLevel().addFreshEntity(outside);
        try {
            var rankOne=Augmentations.apply(new ItemStack(ModItems.EMBERFANG_DAGGER.get()),AdditionalAbilities.FLAME_BREATH);
            player.setItemInHand(InteractionHand.MAIN_HAND,rankOne);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,rankOne,AdditionalAbilities.FLAME_BREATH),"Rank I Flame Breath rejected");
            test.assertTrue(inside.getHealth()<200,"Rank I breath missed a target 6.5 blocks ahead");
            test.assertTrue(outside.getHealth()==200,"Rank I breath reached a target 7.5 blocks ahead");

            inside.setPos(player.position().add(0,0,9.5)); inside.setHealth(200); inside.invulnerableTime=0;
            outside.setPos(player.position().add(0,0,10.5));
            ItemStack rankFour=new ItemStack(ModItems.EMBERFANG_DAGGER_RUBY.get());
            for(int rank=0;rank<4;rank++) rankFour=Augmentations.apply(rankFour,AdditionalAbilities.FLAME_BREATH);
            player.setItemInHand(InteractionHand.MAIN_HAND,rankFour);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,rankFour,AdditionalAbilities.FLAME_BREATH),"Rank IV Flame Breath rejected");
            test.assertTrue(inside.getHealth()<200,"Rank IV breath missed a target 9.5 blocks ahead");
            test.assertTrue(outside.getHealth()==200,"Rank IV breath reached a target 10.5 blocks ahead");
        } finally {
            inside.discard(); outside.discard(); player.server.getPlayerList().remove(player);
        }
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void coneAllVariantsFreshRejectionAndWalls(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true);
        var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        player.setPos(origin); player.setYRot(0); player.setXRot(0);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=4;z<=15;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var front=EntityType.ZOMBIE.create(test.getLevel()); front.setNoAi(true);front.setNoGravity(true);
        front.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);front.setHealth(1000);front.setPos(origin.add(0,0,2));test.getLevel().addFreshEntity(front);
        var rear=EntityType.ZOMBIE.create(test.getLevel());rear.setNoAi(true);rear.setNoGravity(true);rear.setPos(origin.add(0,0,-2));test.getLevel().addFreshEntity(rear);
        var neutral=EntityType.COW.create(test.getLevel());neutral.setNoAi(true);neutral.setNoGravity(true);neutral.setPos(origin.add(.5,0,2));test.getLevel().addFreshEntity(neutral);
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                if(!(entry.get() instanceof IgnisiumWeapon)) {
                    test.assertTrue(Augmentations.pool(fresh).stream().noneMatch(a->a.id().equals(AdditionalAbilities.FLAME_BREATH)),"Wrong material learns Flame Breath"); continue;
                }
                count++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.FLAME_BREATH),"Fresh weapon breathes fire");
                var learned=Augmentations.apply(fresh,AdditionalAbilities.FLAME_BREATH);player.setItemInHand(InteractionHand.MAIN_HAND,learned);
                front.invulnerableTime=0;front.setRemainingFireTicks(0);float before=front.getHealth();
                float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,learned,AdditionalAbilities.FLAME_BREATH),"Learned breath rejected");
                test.assertTrue(Math.abs(front.getHealth()-(before-damage))<.001 && front.getRemainingFireTicks()==60,"Cone damage/fire incorrect");
                test.assertTrue(rear.getHealth()==rear.getMaxHealth() && neutral.getHealth()==neutral.getMaxHealth(),"Breath hits behind or neutral targets");
                test.assertTrue(Augmentations.learned(learned).size()==1,"Breath grants unrelated abilities");
            }
            test.setBlock(new BlockPos(8,5,9),Blocks.STONE);test.setBlock(new BlockPos(8,6,9),Blocks.STONE);test.setBlock(new BlockPos(8,7,9),Blocks.STONE);
            front.invulnerableTime=0;front.setRemainingFireTicks(0);float before=front.getHealth();
            test.assertTrue(RecommendedAbilityRuntime.activate(player,player.getMainHandItem(),AdditionalAbilities.FLAME_BREATH),"Blocked breath should still spend use");
            test.assertTrue(front.getHealth()==before && front.getRemainingFireTicks()==0,"Fire breath pierces solid wall");
            test.assertTrue(count==15,"Skipped Ignisium variants");
        } finally {front.discard();rear.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
}
