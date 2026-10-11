package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.VulnusiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.RevengefulExitEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class RevengefulExitGameTests {
    @GameTest(template="riftfang_test")
    public static void outsideKillerGetsRetributionAndEnvironmentDoesNotTrigger(GameTestHelper test) {
        var player=TestPlayers.survival(test);var p=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(p);
        var killer=EntityType.ZOMBIE.create(test.getLevel());killer.setNoAi(true);killer.getAttribute(Attributes.ARMOR).setBaseValue(0);killer.setPos(p.add(8,0,0));test.getLevel().addFreshEntity(killer);
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setPos(p.add(0,0,2));test.getLevel().addFreshEntity(cow);test.setBlock(new BlockPos(4,5,8),net.minecraft.world.level.block.Blocks.STONE);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(ModItems.CURSEBLOOD_CLAYMORE.get()),AdditionalAbilities.REVENGEFUL_EXIT));
            RevengefulExitEvents.death(new LivingDeathEvent(player,player.damageSources().fall()));test.assertTrue(killer.getHealth()==20 && cow.getHealth()==10,"Environmental death triggers burst");
            float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);player.setHealth(1);player.hurt(player.damageSources().mobAttack(killer),2);
            test.assertTrue(Math.abs(killer.getHealth()-(20-5*damage))<.0001 && Math.abs(cow.getHealth()-(10-damage))<.0001,"Outside killer or area damage wrong");
            test.assertTrue(test.getBlockState(new BlockPos(4,5,8)).is(net.minecraft.world.level.block.Blocks.STONE),"Blood explosion destroys terrain");
        } finally {killer.discard();cow.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=150)
    public static void fifteenVariantsBurstOnRealDeathIncludingNeutralEntities(GameTestHelper test) {
        int count=0;var p=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof VulnusiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.REVENGEFUL_EXIT))==eligible,"Wrong revenge pool");if(!eligible) continue;count++;
            var player=TestPlayers.survival(test);player.setPos(p);var killer=EntityType.ZOMBIE.create(test.getLevel());killer.setNoAi(true);killer.getAttribute(Attributes.ARMOR).setBaseValue(0);killer.setPos(p.add(1,0,0));test.getLevel().addFreshEntity(killer);
            var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setPos(p.add(0,0,2));test.getLevel().addFreshEntity(cow);var far=EntityType.ZOMBIE.create(test.getLevel());far.setNoAi(true);far.getAttribute(Attributes.ARMOR).setBaseValue(0);far.setPos(p.add(8,0,0));test.getLevel().addFreshEntity(far);
            try {
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);RevengefulExitEvents.death(new LivingDeathEvent(player,player.damageSources().mobAttack(killer)));test.assertTrue(killer.getHealth()==20 && cow.getHealth()==10,"Fresh weapon bursts");
                player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(fresh,AdditionalAbilities.REVENGEFUL_EXIT));player.setHealth(1);float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                test.assertTrue(player.hurt(player.damageSources().mobAttack(killer),2),"Fatal attack rejected");test.assertFalse(player.isAlive(),"Attack was not lethal");
                test.assertTrue(Math.abs(killer.getHealth()-(20-6*damage))<.0001,"Killer did not receive area plus retribution damage");test.assertTrue(Math.abs(cow.getHealth()-(10-damage))<.0001 && far.getHealth()==20,"Area excludes neutral entity or exceeds five blocks");
            } finally {killer.discard();cow.discard();far.discard();player.server.getPlayerList().remove(player);}
        }
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
