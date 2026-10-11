package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.SaplingHarvestEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SaplingHarvestGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsRespectBothRollOutcomesAndPreserveExistingLoot(GameTestHelper test) {
        var player=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setHealth(0);int count=0;long success=0,failure=0;
        while(RandomSource.create(success).nextFloat()>=.15F) success++;while(RandomSource.create(failure).nextFloat()<.15F) failure++;
        boolean oldLoot=test.getLevel().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);test.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(true,player.server);
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.SAPLING_HARVEST))==eligible,"Wrong Sapling pool");if(!eligible) continue;count++;
            var existing=new ItemEntity(test.getLevel(),0,0,0,new ItemStack(Items.DIAMOND,2));var drops=new java.util.ArrayList<ItemEntity>();drops.add(existing);var event=new LivingDropsEvent(mob,player.damageSources().playerAttack(player),drops,true);
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);mob.getRandom().setSeed(success);SaplingHarvestEvents.drops(event);test.assertTrue(drops.size()==1,"Fresh weapon adds sapling");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.SAPLING_HARVEST);player.setItemInHand(InteractionHand.MAIN_HAND,stack);mob.getRandom().setSeed(failure);SaplingHarvestEvents.drops(event);test.assertTrue(drops.size()==1,"Failed roll drops sapling");
            mob.getRandom().setSeed(success);SaplingHarvestEvents.drops(event);test.assertTrue(drops.size()==2 && drops.get(1).getItem().is(ItemTags.SAPLINGS) && drops.get(1).getItem().getCount()==1 && existing.getItem().getCount()==2,"Extra sapling or existing loot wrong");
            test.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(false,player.server);mob.getRandom().setSeed(success);SaplingHarvestEvents.drops(event);test.assertTrue(drops.size()==2,"Disabled mob loot adds drop");test.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(true,player.server);
            event.setCanceled(true);mob.getRandom().setSeed(success);SaplingHarvestEvents.drops(event);test.assertTrue(drops.size()==2,"Canceled drops gain sapling");
        }} finally {test.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(oldLoot,player.server);mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
