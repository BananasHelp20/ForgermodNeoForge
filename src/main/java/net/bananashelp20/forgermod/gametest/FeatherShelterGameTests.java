package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FeatherShelterGameTests {
    @GameTest(template="riftfang_test")
    public static void tenVariantsBuffSelfAndPetNotEnemyOrDistantAlly(GameTestHelper test) {
        var player=TestPlayers.survival(test);var friend=TestPlayers.survival(test);var p=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(p);friend.setPos(p.add(-2,0,0));
        var wolf=EntityType.WOLF.create(test.getLevel());wolf.setTame(true,true);wolf.setOwnerUUID(player.getUUID());wolf.setNoAi(true);wolf.setPos(p.add(2,0,0));test.getLevel().addFreshEntity(wolf);
        var far=EntityType.WOLF.create(test.getLevel());far.setTame(true,true);far.setOwnerUUID(player.getUUID());far.setNoAi(true);far.setPos(p.add(15,0,0));test.getLevel().addFreshEntity(far);
        var enemy=EntityType.ZOMBIE.create(test.getLevel());enemy.setNoAi(true);enemy.setPos(p.add(0,0,2));test.getLevel().addFreshEntity(enemy);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon w && !w.isDagger();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.FEATHER_SHELTER))==eligible,"Wrong Shelter pool");if(!eligible) continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.removeEffect(MobEffects.SLOW_FALLING);wolf.removeEffect(MobEffects.SLOW_FALLING);friend.removeEffect(MobEffects.SLOW_FALLING);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.FEATHER_SHELTER),"Fresh weapon buffs");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.FEATHER_SHELTER);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.FEATHER_SHELTER),"Learned buff refused");
            test.assertTrue(player.getEffect(MobEffects.SLOW_FALLING).getDuration()==200 && wolf.getEffect(MobEffects.SLOW_FALLING).getDuration()==200,"Self/pet duration wrong");if(!player.server.isPvpAllowed()) test.assertTrue(friend.hasEffect(MobEffects.SLOW_FALLING),"Cooperative player ally not buffed");test.assertFalse(enemy.hasEffect(MobEffects.SLOW_FALLING) || far.hasEffect(MobEffects.SLOW_FALLING),"Enemy or far ally buffed");
        }} finally {wolf.discard();far.discard();enemy.discard();player.server.getPlayerList().remove(friend);player.server.getPlayerList().remove(player);}
        test.assertTrue(count==10,"Skipped variants");test.succeed();
    }
}
