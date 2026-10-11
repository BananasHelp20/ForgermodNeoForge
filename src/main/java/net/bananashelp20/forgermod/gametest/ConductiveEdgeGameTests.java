package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ConductiveEdgeGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsGainTwentyPercentOnlyForWetMelee(GameTestHelper test) {
        float rain=test.getLevel().getRainLevel(1F);test.getLevel().setRainLevel(0);var player=TestPlayers.survival(test);var position=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);int count=0;
        for(int x=7;x<=9;x++) for(int y=5;y<=7;y++) for(int z=7;z<=9;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.CONDUCTIVE_EDGE))==eligible,"Wrong Conductive Edge assignment");
                if(!eligible) continue;count++;test.setBlock(new BlockPos(8,5,8),Blocks.WATER);mob.setPos(position);mob.tick();test.assertTrue(mob.isInWater(),"Wet test target does not touch water");
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(mob.getHealth()==10,"Fresh weapon gains wet damage");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.CONDUCTIVE_EDGE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);
                test.assertTrue(Math.abs(mob.getHealth()-8)<.0001,"Wet melee damage is not increased by 20 percent");
                mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().indirectMagic(player,player),10);test.assertTrue(mob.getHealth()==10,"Wet spell gets melee bonus");
                test.setBlock(new BlockPos(8,5,8),Blocks.AIR);mob.setPos(position);mob.setDeltaMovement(Vec3.ZERO);mob.tick();test.assertFalse(mob.isInWaterOrRain(),"Dry test target remains wet");
                mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(mob.getHealth()==10,"Dry melee gets wet bonus");
            }
            for(int y=8;y<test.getLevel().getMaxBuildHeight()-test.absolutePos(BlockPos.ZERO).getY();y++) test.setBlock(new BlockPos(8,y,8),Blocks.AIR);
            var center=net.minecraft.core.BlockPos.containing(position);for(int x=-4;x<=4;x+=4) for(int z=-4;z<=4;z+=4) test.getLevel().getChunkAt(center.offset(x,0,z));
            var fill=net.minecraft.server.commands.FillBiomeCommand.fill(test.getLevel(),center.offset(-4,-4,-4),center.offset(4,4,4),test.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS));test.assertTrue(fill.left().isPresent(),"Cannot create rain biome fixture: "+fill);mob.setPos(position);test.getLevel().setRainLevel(1);test.assertTrue(mob.isInWaterOrRain(),"Exposed target does not detect rain: sky="+test.getLevel().canSeeSky(mob.blockPosition())+" height="+test.getLevel().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,mob.blockPosition())+" pos="+mob.blockPosition()+" precipitation="+test.getLevel().getBiome(mob.blockPosition()).value().getPrecipitationAt(mob.blockPosition()));
            mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(Math.abs(mob.getHealth()-8)<.0001,"Rain does not grant wet bonus");
            test.setBlock(new BlockPos(8,8,8),Blocks.STONE);test.assertFalse(mob.isInWaterOrRain(),"Roof does not shelter target from rain");
            mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(mob.getHealth()==10,"Sheltered target gains rain bonus");
        } finally {test.getLevel().setRainLevel(rain);mob.discard();player.server.getPlayerList().remove(player);test.setBlock(new BlockPos(8,5,8),Blocks.AIR);}
        test.assertTrue(count==15,"Skipped Conductive Edge variants");test.succeed();
    }
}
