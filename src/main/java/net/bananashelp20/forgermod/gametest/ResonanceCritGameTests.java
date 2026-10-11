package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.ResonanceCritEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ResonanceCritGameTests {
    @GameTest(template="riftfang_test")
    public static void tenVariantsCritJumpingEnemiesFromGroundOnActualChargedAttacks(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new net.minecraft.core.BlockPos(8,5,8))));
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof PulsiteWeapon weapon && !weapon.isAxe();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.RESONANCE_CRIT))==eligible,"Wrong crit pool");if(!eligible)continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);for(int i=0;i<60;i++)player.doTick();player.setOnGround(true);player.fallDistance=0;zombie.setOnGround(false);zombie.setDeltaMovement(0,.4,0);
            var event=new CriticalHitEvent(player,zombie,1,false);ResonanceCritEvents.critical(event);test.assertFalse(event.isCriticalHit(),"Fresh weapon gains crit");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.RESONANCE_CRIT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int i=0;i<60;i++)player.doTick();player.setOnGround(true);player.fallDistance=0;player.setSprinting(false);zombie.setHealth(200);zombie.invulnerableTime=0;zombie.setDeltaMovement(0,.4,0);
            float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);player.attack(zombie);test.assertTrue(Math.abs(200-zombie.getHealth()-damage*1.5F)<.001,"Actual grounded attack is not critical");
            event=new CriticalHitEvent(player,zombie,1,false);ResonanceCritEvents.critical(event);test.assertFalse(event.isCriticalHit(),"Uncharged hit crits");
            for(int i=0;i<60;i++)player.doTick();zombie.setOnGround(true);event=new CriticalHitEvent(player,zombie,1,false);ResonanceCritEvents.critical(event);test.assertFalse(event.isCriticalHit(),"Grounded enemy crits");
            zombie.setOnGround(false);zombie.setDeltaMovement(0,-.4,0);event=new CriticalHitEvent(player,zombie,1,false);ResonanceCritEvents.critical(event);test.assertFalse(event.isCriticalHit(),"Falling enemy counts as jumping");
            zombie.setDeltaMovement(0,.4,0);event=new CriticalHitEvent(player,zombie,2,true);ResonanceCritEvents.critical(event);test.assertTrue(event.getDamageMultiplier()==2,"Existing stronger crit downgraded");
        }}finally {zombie.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==10,"Skipped variants");test.succeed();
    }
}
