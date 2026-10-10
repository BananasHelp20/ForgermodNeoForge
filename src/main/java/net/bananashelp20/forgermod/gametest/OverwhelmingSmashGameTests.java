package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class OverwhelmingSmashGameTests {
    @GameTest(template="riftfang_test",timeoutTicks=200)
    public static void allFortyMarkedAxesBreakRealShieldsAndAddExactlyTwentyPercentWear(GameTestHelper test) {
        var server=test.getLevel().getServer(); boolean pvp=server.isPvpAllowed(); server.setPvpAllowed(true);
        try {
        var attacker=TestPlayers.survival(test); var defender=TestPlayers.survival(test);
        attacker.setNoGravity(true); defender.setNoGravity(true); var origin=test.getBounds().getCenter();
        for(int x=-3;x<=3;x++) for(int y=0;y<=3;y++) for(int z=-3;z<=3;z++)
            test.getLevel().setBlockAndUpdate(net.minecraft.core.BlockPos.containing(origin).offset(x,y,z),Blocks.AIR.defaultBlockState());
        int count=0;
        for(var item:BuiltInRegistries.ITEM) {
            if(!(item instanceof SwordItemWithEffect weapon) || !weapon.hasMaterialEffect() || !weapon.isAxe() || weapon instanceof IgnisiumWeapon) continue;
            count++; attacker.setPos(origin.add(0,0,2)); attacker.setDeltaMovement(Vec3.ZERO); defender.setPos(origin); defender.setDeltaMovement(Vec3.ZERO);
            defender.setYRot(0); defender.setYHeadRot(0); defender.setHealth(20);
            for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) defender.setItemSlot(slot,ItemStack.EMPTY);
            var fresh=new ItemStack(item); attacker.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            defender.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD)); defender.startUsingItem(InteractionHand.OFF_HAND);
            for(int tick=0;tick<6;tick++) defender.doTick(); defender.invulnerableTime=0;
            defender.hurt(defender.damageSources().playerAttack(attacker),4);
            test.assertTrue(!defender.getOffhandItem().isEmpty() && defender.getOffhandItem().getDamageValue()>0 && defender.getHealth()==20,"Fresh axe breaks shield or shield fixture fails");
            var stack=Augmentations.apply(fresh,RecommendedAbilities.OVERWHELMING_SMASH); attacker.setItemInHand(InteractionHand.MAIN_HAND,stack); defender.invulnerableTime=0;
            defender.hurt(defender.damageSources().playerAttack(attacker),4);
            test.assertTrue(defender.getOffhandItem().isEmpty() && defender.getHealth()==20,"Learned axe does not break actual shield safely");
            defender.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET)); defender.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));
            defender.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS)); defender.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS)); defender.doTick();
            for(int hit=0;hit<5;hit++) { defender.invulnerableTime=0; defender.hurt(defender.damageSources().playerAttack(attacker),4); }
            for(var armor:defender.getArmorSlots()) test.assertTrue(armor.getDamageValue()==6,"Five normal wear points should become six, not ten or five: "+armor.getDamageValue());
            test.assertTrue(Math.abs(defender.getHealth()-15.2F)<.001,"Armor-wear bonus changes normal mitigated health damage: "+defender.getHealth());
            test.assertTrue(Augmentations.apply(stack,RecommendedAbilities.OVERWHELMING_SMASH).isEmpty(),"Fixed passive allows redundant upgrade");
        }
        test.assertTrue(count==40,"Wrong number of marked axes");
        test.assertFalse(Augmentations.pool(new ItemStack(ModItems.MOLTEN_AXE.get())).stream().anyMatch(a->a.id().equals(RecommendedAbilities.OVERWHELMING_SMASH)),"Unmarked Molten Axe gets passive");
        test.getLevel().getServer().getPlayerList().remove(attacker); test.getLevel().getServer().getPlayerList().remove(defender); test.succeed();
        } finally { server.setPvpAllowed(pvp); }
    }
    @GameTest(template="riftfang_test")
    public static void mobsReceiveBonusWearAndRejectedDamageDoesNotAccumulate(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true);
        var stack=Augmentations.apply(new ItemStack(ModItems.NULLIFIED_AXE.get()),RecommendedAbilities.OVERWHELMING_SMASH); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); zombie.setHealth(1000);
        var armor=new ItemStack(Items.DIAMOND_CHESTPLATE); zombie.setItemSlot(EquipmentSlot.CHEST,armor);
        zombie.setInvulnerable(true); for(int hit=0;hit<5;hit++) zombie.hurt(player.damageSources().playerAttack(player),4);
        zombie.setInvulnerable(false);
        for(int hit=0;hit<4;hit++) { zombie.invulnerableTime=0; zombie.hurt(player.damageSources().playerAttack(player),4); }
        test.assertTrue(armor.getDamageValue()==0,"Rejected hits accumulate extra armor wear");
        zombie.invulnerableTime=0; zombie.hurt(player.damageSources().playerAttack(player),4);
        test.assertTrue(armor.getDamageValue()==1,"Mob armor does not receive fractional twenty-percent bonus");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.NULLIFIED_AXE.get()));
        for(int hit=0;hit<5;hit++) { zombie.invulnerableTime=0; zombie.hurt(player.damageSources().playerAttack(player),4); }
        test.assertTrue(armor.getDamageValue()==1,"Fresh weapon keeps learned mob armor-wear bonus");
        zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
