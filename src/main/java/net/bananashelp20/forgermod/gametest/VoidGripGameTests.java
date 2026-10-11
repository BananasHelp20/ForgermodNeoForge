package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.VoidGripEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class VoidGripGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveDaggersReduceActualMeleeImpulseByOneQuarter(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                boolean eligible=entry.get() instanceof InanisiumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.VOID_GRIP))==eligible,"Wrong Void Grip assignment");
                if(!eligible) continue;count++;
                var target=EntityType.ZOMBIE.create(test.getLevel());target.getAttribute(Attributes.ARMOR).setBaseValue(0);target.setPos(player.position().add(0,0,2));
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);target.hurt(player.damageSources().playerAttack(player),5);
                double baseline=target.getDeltaMovement().horizontalDistance();test.assertTrue(baseline>0,"Test attack produces no knockback");
                var learned=Augmentations.apply(fresh,AdditionalAbilities.VOID_GRIP);player.setItemInHand(InteractionHand.MAIN_HAND,learned);
                target.setHealth(20);target.invulnerableTime=0;target.setDeltaMovement(Vec3.ZERO);
                target.hurt(player.damageSources().playerAttack(player),5);
                test.assertTrue(Math.abs(target.getDeltaMovement().horizontalDistance()-baseline*.75)<.0001,"Melee knockback not reduced by 25%");
                test.assertTrue(target.getHealth()==15,"Void Grip changes damage");
                target.setHealth(20);target.invulnerableTime=0;target.setDeltaMovement(Vec3.ZERO);
                target.hurt(player.damageSources().indirectMagic(player,player),5);
                test.assertTrue(Math.abs(target.getDeltaMovement().horizontalDistance()-baseline)<.0001,"Spell knockback is reduced");
                VoidGripEvents.endTick(new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(()->true,player.server));
                target.setDeltaMovement(Vec3.ZERO);target.knockback(1,0,-1);
                test.assertTrue(Math.abs(target.getDeltaMovement().horizontalDistance()-1)<.0001,"Previous melee hit reduces later unrelated knockback");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Void Grip variants");test.succeed();
    }
}
