package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class VacuumBurstGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsPullDropsWithoutConsumingOrTeleporting(GameTestHelper test) {
        var player=TestPlayers.survival(test);var p=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(p);
        var drop=new ItemEntity(test.getLevel(),p.x+3,p.y,p.z,new ItemStack(Items.DIAMOND,3));drop.setPickUpDelay(100);test.getLevel().addFreshEntity(drop);
        var orb=new ExperienceOrb(test.getLevel(),p.x-3,p.y,p.z,5);test.getLevel().addFreshEntity(orb);var far=new ItemEntity(test.getLevel(),p.x+15,p.y,p.z,new ItemStack(Items.APPLE));test.getLevel().addFreshEntity(far);far.setDeltaMovement(Vec3.ZERO);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.VACUUM_BURST))==eligible,"Wrong Vacuum pool");if(!eligible) continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.VACUUM_BURST),"Fresh weapon pulls");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.VACUUM_BURST);player.setItemInHand(InteractionHand.MAIN_HAND,stack);var original=drop.position();
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.VACUUM_BURST),"Learned gust rejected");
            test.assertTrue(drop.getDeltaMovement().x<0 && orb.getDeltaMovement().x>0 && drop.position().equals(original),"Impulse direction wrong or teleports");
            test.assertTrue(drop.getItem().getCount()==3 && drop.hasPickUpDelay() && orb.getValue()==5 && far.getDeltaMovement().lengthSqr()==0,"Drops consumed or pickup rules changed");
        }} finally {drop.discard();orb.discard();far.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
