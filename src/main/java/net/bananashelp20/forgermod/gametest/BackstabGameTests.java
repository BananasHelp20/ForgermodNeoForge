package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
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
public class BackstabGameTests {
    @GameTest(template="riftfang_test")
    public static void fortyMarkedDaggersRewardRearHitsOnly(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=test.getBounds().getCenter();int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                boolean eligible=Augmentations.eligible(fresh) && Augmentations.canonical(fresh).isDagger()
                        && !(entry.get() instanceof net.bananashelp20.forgermod.item.custom.IgnisiumWeapon);
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.BACKSTAB))==eligible,"Incorrect Backstab weapon assignment");
                if(!eligible) continue;checked++;
                var learned=Augmentations.apply(fresh,AdditionalAbilities.BACKSTAB);
                test.assertTrue(Augmentations.apply(learned,AdditionalAbilities.BACKSTAB).isEmpty(),"Backstab offers identical upgrades");
                var target=EntityType.ZOMBIE.create(test.getLevel());target.setPos(origin);target.setYRot(0);target.getAttribute(Attributes.ARMOR).setBaseValue(0);
                for(var offset:new Vec3[]{new Vec3(0,0,-2),new Vec3(0,0,2),new Vec3(2,0,0)}) {
                    player.setPos(origin.add(offset));player.setItemInHand(InteractionHand.MAIN_HAND,learned);target.setHealth(20);target.invulnerableTime=0;
                    target.hurt(player.damageSources().playerAttack(player),10);
                    test.assertTrue(Math.abs(target.getHealth()-(offset.z<0?8:10))<.001,"Rear/front/side damage is incorrect");
                }
                player.setPos(origin.add(0,0,-2));player.setItemInHand(InteractionHand.MAIN_HAND,fresh);target.setHealth(20);target.invulnerableTime=0;
                target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(target.getHealth()==10,"Unlearned Backstab grants bonus");
                player.setItemInHand(InteractionHand.MAIN_HAND,learned);target.setHealth(20);target.invulnerableTime=0;
                target.hurt(player.damageSources().indirectMagic(player,player),10);test.assertTrue(target.getHealth()==10,"Secondary damage gains Backstab");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==40,"Skipped Backstab family or gemstone");test.succeed();
    }
}
