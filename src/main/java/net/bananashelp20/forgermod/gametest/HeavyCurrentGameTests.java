package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HeavyCurrentGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveAxesRequireFourCorrectArmorPiecesForMeleeBonus(GameTestHelper test) {
        var player=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);int count=0;float[] amount={0};
        java.util.function.Consumer<LivingIncomingDamageEvent> probe=event->{if(event.getEntity()==mob) amount[0]=event.getAmount();};NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,probe);
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon weapon && weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.HEAVY_CURRENT))==eligible,"Wrong Heavy Current assignment");
                if(!eligible) continue;count++;mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));mob.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));mob.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.IRON_LEGGINGS));mob.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.IRON_BOOTS));
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(amount[0]==10,"Fresh axe gains armored bonus");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.HEAVY_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(Math.abs(amount[0]-11.5)<.0001,"Full armor melee bonus is not 15 percent before armor");
                for(var slot:java.util.List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)) {
                    var saved=mob.getItemBySlot(slot);mob.setItemSlot(slot,ItemStack.EMPTY);mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(amount[0]==10,"Missing armor slot grants bonus");mob.setItemSlot(slot,saved);
                }
                mob.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.ELYTRA));mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(amount[0]==10,"Elytra counts as full armor");mob.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));
                mob.setHealth(20);mob.invulnerableTime=0;mob.hurt(player.damageSources().indirectMagic(player,player),10);test.assertTrue(amount[0]==10,"Spell receives melee bonus");
            }
        } finally {NeoForge.EVENT_BUS.unregister(probe);mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Heavy Current variants");test.succeed();
    }
}
