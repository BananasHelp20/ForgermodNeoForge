package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.LightningRodEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LightningRodGameTests {
    @GameTest(template="riftfang_test")
    public static void levelOneUsesTenMinuteAverageAndHigherRanksKeepTheirRate(GameTestHelper test) {
        var player=TestPlayers.survival(test);
        player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        for(int y=5;y<test.getLevel().getMaxBuildHeight()-test.absolutePos(BlockPos.ZERO).getY();y++) test.setBlock(new BlockPos(8,y,8),Blocks.AIR);
        long successful=0,oldRateOnly=0;
        for(long seed=1;successful==0 || oldRateOnly==0;seed++) {
            if(net.minecraft.util.RandomSource.create(seed).nextInt(600)==0) successful=seed;
            else if(net.minecraft.util.RandomSource.create(seed).nextInt(60)==0) oldRateOnly=seed;
        }
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof ElectriumWeapon)) continue;
                count++;
                var stack=new ItemStack(entry.get());
                for(int rank=1;rank<=2;rank++) {
                    if(rank==1) stack=Augmentations.apply(stack,AdditionalAbilities.LIGHTNING_ROD);
                    else {
                        var template=new ItemStack(ModItems.SUPER_AUGMENT_TEMPLATE.get());
                        for(int i=0;i<2;i++) template=Augmentations.apply(template,AdditionalAbilities.LIGHTNING_ROD);
                        stack=Augmentations.merge(stack,template);
                    }
                    test.assertTrue(Augmentations.level(stack,AdditionalAbilities.LIGHTNING_ROD)==rank,"Strike-rate fixture failed to learn rank "+rank);
                    player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    for(long seed:new long[]{oldRateOnly,successful}) {
                        LightningRodEvents.end(player);
                        player.tickCount=20;
                        player.getRandom().setSeed(seed);
                        LightningRodEvents.tick(new PlayerTickEvent.Post(player));
                        var bolts=test.getLevel().getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class,player.getBoundingBox().inflate(2));
                        boolean expected=net.minecraft.util.RandomSource.create(seed).nextInt(rank==1?600:60)==0;
                        test.assertTrue(bolts.size()==(expected?1:0),"Wrong extra-strike probability at Lightning Rod rank "+rank);
                        bolts.forEach(net.minecraft.world.entity.Entity::discard);
                    }
                }
            }
            test.assertTrue(count==15,"Skipped Lightning Rod strike-rate variants");test.succeed();
        } finally {LightningRodEvents.end(player);player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsGrantExactlyFiveHeartsAndPreserveOtherBuffs(GameTestHelper test) {
        var player=TestPlayers.survival(test);var bolt=EntityType.LIGHTNING_BOLT.create(test.getLevel());int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.LIGHTNING_ROD))==eligible,"Wrong Lightning Rod assignment");
                if(!eligible) continue;count++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.setHealth(20);player.invulnerableTime=0;
                var natural=new EntityStruckByLightningEvent(player,bolt);LightningRodEvents.struck(natural);test.assertFalse(natural.isCanceled(),"Fresh weapon prevents lightning");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.LIGHTNING_ROD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,200,0));player.setAbsorptionAmount(4);player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,2));
                natural=new EntityStruckByLightningEvent(player,bolt);LightningRodEvents.struck(natural);test.assertTrue(natural.isCanceled(),"Learned rod permits lightning injury/fire");
                test.assertTrue(player.getAbsorptionAmount()==14,"Rod does not add exactly five absorption hearts");LightningRodEvents.grant(player);test.assertTrue(player.getAbsorptionAmount()==14,"Repeated flash stacks absorption");
                test.assertTrue(player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier()==2,"Rod overwrites stronger Strength potion");
                player.invulnerableTime=0;player.hurt(player.damageSources().lightningBolt(),5);test.assertTrue(player.getHealth()==20 && player.getAbsorptionAmount()==14,"Lightning harms rod wielder");
                player.invulnerableTime=0;player.hurt(player.damageSources().generic(),2);test.assertTrue(player.getAbsorptionAmount()==12,"Ordinary damage does not consume absorption");
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);LightningRodEvents.tick(new PlayerTickEvent.Post(player));
                test.assertTrue(player.getAbsorptionAmount()==4,"Putting rod away destroys unrelated absorption or leaves owned hearts");
                test.assertTrue(player.getEffect(MobEffects.DAMAGE_BOOST)!=null && player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier()==2,"Putting rod away destroys unrelated Strength");
                player.removeEffect(MobEffects.ABSORPTION);player.removeEffect(MobEffects.DAMAGE_BOOST);player.setAbsorptionAmount(0);
            }
            player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));for(int y=5;y<test.getLevel().getMaxBuildHeight()-test.absolutePos(BlockPos.ZERO).getY();y++) test.setBlock(new BlockPos(8,y,8),Blocks.AIR);
            var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.LIGHTNING_ROD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.setBlock(new BlockPos(8,4,8),Blocks.OXIDIZED_COPPER);
            test.assertTrue(LightningRodEvents.extraStrike(player),"Extra strike requires a thunderstorm");test.assertTrue(player.getAbsorptionAmount()==10 && player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier()==0,"Extra strike does not grant five hearts and Strength I");
            var cosmetic=test.getLevel().getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class,player.getBoundingBox().inflate(2)).getFirst();cosmetic.tick();
            test.assertTrue(test.getBlockState(new BlockPos(8,4,8)).is(Blocks.OXIDIZED_COPPER) && test.getBlockState(new BlockPos(8,5,8)).isAir(),"Cosmetic strike cleans copper or ignites terrain");
            test.setBlock(new BlockPos(8,4,8),Blocks.LIGHTNING_ROD);cosmetic.tick();test.assertFalse(test.getBlockState(new BlockPos(8,4,8)).getValue(net.minecraft.world.level.block.LightningRodBlock.POWERED),"Cosmetic strike powers a lightning rod");
            cosmetic.discard();
        } finally {LightningRodEvents.end(player);bolt.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Lightning Rod variants");test.succeed();
    }
}
