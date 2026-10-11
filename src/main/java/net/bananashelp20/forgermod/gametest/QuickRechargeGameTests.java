package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.*;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.Map;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class QuickRechargeGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsShortenOnlyPrimaryOnRealCritical(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(test.absolutePos(new net.minecraft.core.BlockPos(8,5,8))));var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.QUICK_RECHARGE))==eligible,"Wrong Quick Recharge assignment");
                if(!eligible) continue;count++;QuickRechargeEvents.logout(new PlayerEvent.PlayerLoggedOutEvent(player));
                var stack=Augmentations.apply(Augmentations.apply(fresh,AdditionalAbilities.MAGNETIC_THROW),AdditionalAbilities.QUICK_RECHARGE);
                var secondary=Augmentations.pool(stack).stream().filter(a->a.active() && !a.id().equals(AdditionalAbilities.MAGNETIC_THROW)).findFirst().orElse(null);if(secondary!=null) stack=Augmentations.apply(stack,secondary.id());
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int i=0;i<60;i++) player.doTick();
                String item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();String primary=item+":"+AdditionalAbilities.MAGNETIC_THROW;
                String second=secondary!=null && RecommendedAbilities.active(secondary.id())?item+":"+secondary.id():WeaponCooldownKey.of(item,secondary==null?WeaponAbilitySlot.SECONDARY:Augmentations.sourceSlot(stack,WeaponAbilitySlot.SECONDARY));long now=player.level().getGameTime();
                player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of(primary,now+100,second,now+200,"other-variant",now+300));
                player.setOnGround(false);player.fallDistance=1;player.setSprinting(false);mob.setHealth(200);mob.invulnerableTime=0;player.attack(mob);
                var cds=player.getData(WeaponCooldownAttachments.COOLDOWNS);test.assertTrue(cds.get(primary)==now+80,"Critical does not shorten primary by one second: "+item+" scale="+player.getAttackStrengthScale(.5F));test.assertTrue(cds.get(second)==now+200 && cds.get("other-variant")==now+300,"Recharge modifies secondary or another variant");
                for(int i=0;i<60;i++) player.doTick();player.setOnGround(false);player.fallDistance=1;mob.invulnerableTime=0;player.attack(mob);
                test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(primary)==now+80,"Recharge ignores two-second trigger interval");
                QuickRechargeEvents.logout(new PlayerEvent.PlayerLoggedOutEvent(player));for(int i=0;i<60;i++) player.doTick();player.setOnGround(true);player.fallDistance=0;mob.invulnerableTime=0;player.attack(mob);
                test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(primary)==now+80,"Noncritical hit recharges");
                player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of(primary,now+5));test.assertTrue(WeaponAbilityNetwork.reducePrimaryCooldown(player,stack,20),"Short cooldown rejected");test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).get(primary)==now,"Recharge moves readiness before current time");
            }
        } finally {QuickRechargeEvents.logout(new PlayerEvent.PlayerLoggedOutEvent(player));mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Quick Recharge variants");test.succeed();
    }
}
