package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.*;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.mixin.AttackStrengthAccess;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class SonicallyChargedCritGameTests {
    private static float strike(ServerPlayer player,LivingEntity target,boolean critical) {
        target.setHealth(target.getMaxHealth());target.invulnerableTime=0;target.setPos(player.position().add(0,0,2));target.setDeltaMovement(0,0,0);
        player.setOnGround(!critical);player.fallDistance=critical?1:0;player.setSprinting(false);
        ((AttackStrengthAccess)player).forgermod$setAttackStrengthTicker(100);
        float before=target.getHealth();player.attack(target);return before-target.getHealth();
    }
    @GameTest(template="riftfang_test")
    public static void nativeCriticalUsesCurrentRankPercentAndConsumesOnlyOnce(GameTestHelper test) {
        String id="tooltips.forgermod.ability.sonic_crit";
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var target=EntityType.COW.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(target);
        int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof PulsiteWeapon weapon && weapon.isDagger())for(int rank=1;rank<=4;rank++) {
                checked++;int percent=5*rank;
                var stack=Augmentations.setLevel(new ItemStack(entry.get()),id,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
                float ordinary=strike(player,target,false),baseline=strike(player,target,true);
                test.assertTrue(weapon.activateAbility(player,stack,WeaponAbilitySlot.PRIMARY),"Cannot arm current-rank charged crit");
                test.assertTrue(Math.abs(strike(player,target,false)-ordinary)<.001 && weapon.isAbilityActive(player,WeaponAbilitySlot.PRIMARY),"Ordinary hit consumes or gains charged critical damage");
                float charged=strike(player,target,true);
                test.assertTrue(Math.abs(charged-baseline-target.getMaxHealth()*percent/100F)<.001 && !weapon.isAbilityActive(player,WeaponAbilitySlot.PRIMARY),"Native critical does not add the approved max-health percentage or consume charge");
                test.assertTrue(Math.abs(strike(player,target,true)-baseline)<.001,"An uncharged critical repeats the percentage bonus");
                for(boolean compact:new boolean[]{false,true}) {
                    var text=AbilityDescriptions.description(stack,id,rank,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    test.assertTrue(text.getString().equals("Your next critical dagger hit adds "+percent+"% of the target's maximum health as damage.") && white.toString().equals(percent+"%"),"Charged crit description has wrong current percent or coloring");
                }
                if(rank<4)test.assertTrue(AugmentUpgradeDescriptions.changes(stack,id,rank).stream().anyMatch(change->change.property().equals("max_health_bonus") && ((Number)change.after()).doubleValue()==percent+5),"Upgrade preview omits the next percentage");
            }
            test.assertTrue(checked==20 && SonicCritState.percentage(5)==20,"Missing variant/rank or exceeded maximum percentage");test.succeed();
        }finally{PulsiteWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));DaggerCriticalEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));target.discard();player.server.getPlayerList().remove(player);}
    }
}
