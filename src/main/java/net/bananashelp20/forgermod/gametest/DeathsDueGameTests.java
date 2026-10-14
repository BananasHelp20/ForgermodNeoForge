package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.DeathsDueEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class DeathsDueGameTests {
    @GameTest(template="riftfang_test")
    public static void allFifteenVariantsBoostOnlyEnemyExperience(GameTestHelper test) {
        var player=TestPlayers.survival(test);var zombie=EntityType.ZOMBIE.create(test.getLevel());var cow=EntityType.COW.create(test.getLevel());int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.DEATHS_DUE))==eligible,"Wrong XP pool");if(!eligible)continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);var event=new LivingExperienceDropEvent(zombie,player,20);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20,"Fresh weapon grants XP");
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(fresh,AdditionalAbilities.DEATHS_DUE));
            event=new LivingExperienceDropEvent(zombie,player,20);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==25,"Wrong XP multiplier");
            event=new LivingExperienceDropEvent(zombie,player,5);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==6,"Wrong whole-point rounding");
            event=new LivingExperienceDropEvent(cow,player,20);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20,"Neutral kill boosted");
            event=new LivingExperienceDropEvent(zombie,player,20);event.setCanceled(true);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20,"Canceled XP changed");
            event=new LivingExperienceDropEvent(zombie,null,20);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==20,"Uncredited XP boosted");
            event=new LivingExperienceDropEvent(zombie,player,Integer.MAX_VALUE);DeathsDueEvents.experience(event);test.assertTrue(event.getDroppedExperience()==Integer.MAX_VALUE,"XP overflow");
        }}finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void realKillDropsSixExperienceInsteadOfFive(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=net.minecraft.world.phys.Vec3.atBottomCenterOf(test.absolutePos(new net.minecraft.core.BlockPos(8,5,8)));player.setPos(point);
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.setPos(point.add(0,0,2));test.getLevel().addFreshEntity(zombie);
        try {player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.DEATHS_DUE));
            zombie.hurt(player.damageSources().playerAttack(player),100);
            int value=player.level().getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,zombie.getBoundingBox().inflate(2)).stream().mapToInt(orb -> orb.getValue()*orb.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getInt("Count")).sum();
            test.assertTrue(value==6,"Actual XP award not boosted: "+value);
        }finally {zombie.discard();player.server.getPlayerList().remove(player);}test.succeed();
    }
}
