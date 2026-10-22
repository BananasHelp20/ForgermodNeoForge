package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.ChargeAttackState;
import net.bananashelp20.forgermod.item.custom.abilities.LightningRodEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class ChargeAttackGameTests {
    @GameTest(template="riftfang_test")
    public static void progressionDischargesOnExpectedHitAndOnlyMaximumTriggersLearnedRod(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var mob=EntityType.ZOMBIE.create(test.getLevel());
        mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(player.position().add(2,0,0));
        mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000);
        test.getLevel().addFreshEntity(mob);
        String id="tooltips.forgermod.ability.charge_attack";
        try {
            for(int rank=1;rank<=4;rank++) for(boolean rod:new boolean[]{false,true}) {
                var stack=Augmentations.setLevel(new ItemStack(ModItems.STATIC_DAGGER.get()),id,rank);
                if(rod)stack=Augmentations.apply(stack,AdditionalAbilities.LIGHTNING_ROD);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                var weapon=(ElectriumWeapon)stack.getItem();
                test.assertTrue(weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY),"Cannot arm ranked charge");
                mob.setHealth(10000);
                int hits=ChargeAttackState.chargingHits(rank);
                for(int hit=1;hit<=hits+1;hit++) {
                    mob.invulnerableTime=0;float before=mob.getHealth();
                    test.assertTrue(mob.hurt(player.damageSources().playerAttack(player),10),"Fixture rejects hit");
                    weapon.onDaggerHit(mob,player);
                    float expected=hit<=hits?10*(1+hit*ChargeAttackState.maximumBonus(rank)/hits):10+ChargeAttackState.lightningDamage(rank);
                    test.assertTrue(Math.abs(before-mob.getHealth()-expected)<.01,"Wrong charge damage on rank "+rank+" hit "+hit);
                    test.assertTrue(weapon.isAbilityActive(player,WeaponAbilitySlot.SECONDARY)==(hit<=hits),"Discharge occurs on wrong hit");
                }
                test.assertTrue(player.getAbsorptionAmount()==(rank==4 && rod?10:0),"Self lightning ignored rank/passive requirement");
                var text=AbilityDescriptions.description(stack,id,rank,false).getString();
                test.assertTrue(text.contains("also striking you")== (rank==4 && rod),"Wrong conditional self-strike description");
                LightningRodEvents.end(player);
            }
        } finally {
            if(player.getMainHandItem().getItem() instanceof ElectriumWeapon weapon)weapon.cancelAbility(player,WeaponAbilitySlot.SECONDARY);
            LightningRodEvents.end(player);mob.discard();player.server.getPlayerList().remove(player);
        }
        test.succeed();
    }
}
