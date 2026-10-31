package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.custom.WeaponTooltips;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Final wording approved during the one-by-one review. Values describe the supplied rank only. */
public final class FinalReviewedDescriptions {
    private FinalReviewedDescriptions() {}
    private static Component text(String id,String format,Object... args) {
        return AbilityDescriptions.text(id+".final_review",format,args);
    }
    private static Component mixed(String id,String format,Object... args) {
        return Component.translatableWithFallback(id+".final_review",format,args).withStyle(ChatFormatting.GRAY);
    }
    private static Component effect(ItemStack stack,String name,int amplifier) {
        return colored(stack,name+" "+WeaponTooltips.romanNumeral(amplifier+(JadeAugments.isJade(stack)?1:0)));
    }
    private static Component colored(ItemStack stack,String value) {
        return Component.literal(value).withStyle(JadeAugments.isJade(stack)?ChatFormatting.GREEN:ChatFormatting.WHITE);
    }
    /** Short card copy; expanded hover and item descriptions always use the full approved wording. */
    public static Component card(ItemStack stack,String id,int rank) {
        double damage=AbilityDescriptions.weaponDamage(stack);
        String format;Object[] args;
        switch(id) {
            case "tooltips.forgermod.ability.echo_pin" -> {format="For %s, hear noises within %s. Highlights noisy entities only for you.";args=new Object[]{(8+2*Math.clamp(rank,1,4))+" s",(24+8*Math.clamp(rank,1,4))+" blocks"};}
            case "tooltips.forgermod.ability.bone_guard" -> {format=rank<4?"Summon %s to fight for you for 10 minutes.":"Summon configured reinforcements. Customize them with %s.";args=new Object[]{rank<4?NecromancerLoadouts.roster(stack,rank):Component.keybind("key.forgermod.necromancer_customize")};}
            case "tooltips.forgermod.ability.challenge_roar" -> {format="Force hostile mobs within %s to target you. Gain %s for %s.";args=new Object[]{ReviewedRanks.roarRange(rank)+" blocks",effect(stack,"Strength",1),(ReviewedRanks.roarTicks(rank)/20+(JadeAugments.isJade(stack)?1:0))+" s"};}
            case "tooltips.forgermod.ability.charge_attack" -> {format="Build %s over %s. Final hit strikes lightning for %s.";args=new Object[]{"+"+AbilityDescriptions.number(ChargeAttackState.maximumBonus(rank)*100)+"% damage",ChargeAttackState.chargingHits(rank)+" hits",AbilityDescriptions.number(ChargeAttackState.lightningDamage(rank))+" damage"};}
            case "tooltips.forgermod.ability.cinder_decoy" -> {format="Create a decoy. Defeat or expiry: fiery blast within %s; ignites for %s.";args=new Object[]{(1+Math.clamp(rank,1,4))+" blocks",(2+rank)+" s"};}
            case "tooltips.forgermod.ability.crucible_hook" -> {format=rank>=5?"Grapple up to %s, or hook and smash a mob for %s.":"Pull a mob within 20 blocks to you. Smash for %s and ignite for %s.";args=rank>=5?new Object[]{ReviewedRanks.grappleRange(rank)+" blocks",AbilityDescriptions.number(damage*(1+.25*rank))+" damage"}:new Object[]{AbilityDescriptions.number(damage*(1+.25*rank))+" damage",(2+rank)+" s"};}
            case "tooltips.forgermod.ability.direct_current" -> {format=rank<4?"Mark %s. Electrical barriers damage and stun mobs between them.":"Mark %s to form a damaging, stunning electrical field.";args=new Object[]{Math.min(4,rank+1)+" mobs"};}
            case "tooltips.forgermod.ability.ender_swing" -> {format="Lift all mobs within %s by up to %s. Gravity and fall damage %s.";args=new Object[]{AbilityDescriptions.number(ReviewedRanks.gravitySwingRadius(rank))+" blocks",ReviewedRanks.gravitySwingHeight(rank)+" blocks","+"+AbilityDescriptions.number(ReviewedRanks.gravitySwingBonus(rank)*100)+"%"};}
            case "tooltips.forgermod.ability.explosive_hits" -> {format="Next 10 melee hits add %s in a %s blast.";args=new Object[]{"30% area damage",(2+rank)+"-block radius"};}
            case "tooltips.forgermod.ability.eye_of_the_storm" -> {format="Create a storm affecting mobs within %s for %s.";args=new Object[]{AbilityDescriptions.number(ReviewedRanks.stormRadius(rank))+" blocks",(ReviewedRanks.stormTicks(rank)/20+(JadeAugments.isJade(stack)?1:0))+" s"};}
            case "tooltips.forgermod.ability.false_awakening" -> {format="Decoy distracts your aimed enemy. Deal %s to it for %s.";args=new Object[]{(5+5*rank)+"% more damage",(30+5*(rank-1))+" s"};}
            case "tooltips.forgermod.ability.flameborn" -> {format="Fire resistance for %s. Explode for %s within 10 blocks; ignite for %s.";args=new Object[]{(30+(JadeAugments.isJade(stack)?1:0))+" s",AbilityDescriptions.number(damage*FlamebornAbility.damageMultiplier(rank))+" damage",(rank>1?7:5)+" s"};}
            case "tooltips.forgermod.ability.just_a_dream" -> {format="Teleport to your respawn point. Uses world spawn if unavailable.";args=new Object[]{};}
            case "tooltips.forgermod.ability.poisoned_vein" -> {format="Next dagger kill: 5-second cloud within 3 blocks. Poisons others for %s.";args=new Object[]{colored(stack,(30+(JadeAugments.isJade(stack)?1:0))+" s")};}
            case "tooltips.forgermod.ability.rooting_roots" -> {format="Root and poison enemies within %s for %s.";args=new Object[]{RootingRootsState.RANGE+" blocks",colored(stack,(RootingRootsState.durationTicks(JadeAugments.isJade(stack))/20)+" s")};}
            case "tooltips.forgermod.ability.stunning_presence" -> {format="Stun other living entities within %s every %s.";args=new Object[]{"10 blocks",AbilityDescriptions.number(Math.round(40-(rank-1)*20F/3)/20.0)+" s"};}
            case "tooltips.forgermod.ability.void_step" -> {format=rank<4?"Blink %s in your view direction. Both daggers need this ability to double range.":"Blink %s, or sneak to Etherwarp up to %s. Dual wield doubles ranges; Etherwarp cap: 100 blocks.";args=rank<4?new Object[]{VoidStepRanges.blink(rank)+" blocks"}:new Object[]{VoidStepRanges.blink(rank)+" blocks",VoidStepRanges.etherwarp(rank)+" blocks"};}
            case "tooltips.forgermod.passive.echo_appetite" -> {format="Breaking sculk restores hunger. Warden kills restore %s.";args=new Object[]{(EchoAppetiteValues.wardenFood(rank)/2)+" hunger bars"};}
            case "tooltips.forgermod.passive.grave_vault" -> {format="Sneak-right-click: nine-slot flower vault. Enemy kills plant stored flowers.";args=new Object[]{};}
            case "tooltips.forgermod.ability.alternating_current" -> {format="Mark %s. For 1 minute, each jump deals %s and stuns both for 0.5 s.";args=new Object[]{"2 mobs",AlternatingCurrentEvents.damage(rank)+" damage"};}
            case "tooltips.forgermod.ability.always_the_gardeners" -> {format="Next kill creates a %s, %s cloud. Applies %s to others.";args=new Object[]{"5-minute",AbilityDescriptions.number(2+.5*(rank-1))+"-block radius",effect(stack,"Poison",1)};}
            case "tooltips.forgermod.ability.area_discharge" -> {if(rank>=5)return null;format="Strike hostile mobs within %s for half weapon damage.";args=new Object[]{"5 blocks"};}
            case "tooltips.forgermod.ability.storing_anger" -> {format="Store damage for %s, then release it on your next hit.";args=new Object[]{"10 seconds"};}
            case "tooltips.forgermod.ability.strong_arm" -> {format="Next critical hit: %s to break one enemy armor piece.";args=new Object[]{Math.round(StrongArmAbility.chance(rank)*100)+"% chance"};}
            case "tooltips.forgermod.passive.dagger" -> {format="Dual wield matching-material daggers. Only one needs this passive.";args=new Object[]{};}
            case "tooltips.forgermod.passive.echo_salvage" -> {format="Missed Vertical Strikes store half damage. Next melee hit releases echoes. Capacity: %s.";args=new Object[]{EchoSalvageEvents.capacity(rank)+" echoes"};}
            case "tooltips.forgermod.passive.echoing_speed" -> {format="Same target: %s per hit, up to %s. Resets on target change or after %s.";args=new Object[]{"+5% speed","+"+(10+10*rank)+"%","8 s"};}
            case "tooltips.forgermod.passive.final_strike" -> {format="Deal %s to your last target below %s.";args=new Object[]{"50% more damage","10% health"};}
            case "tooltips.forgermod.passive.quick_recharge" -> {format="Charged critical hits: primary cooldown %s. Once every %s.";args=new Object[]{"-1 second","2 seconds"};}
            case "tooltips.forgermod.passive.revengeful_exit" -> {format="On death: %s within %s, plus %s to your killer.";args=new Object[]{AbilityDescriptions.number(damage)+" damage","5 blocks",AbilityDescriptions.number(damage*5)+" damage"};}
            case "tooltips.forgermod.passive.soul_hunger" -> {format="Kills grant %s (cap %s). Lost after %s without this passive.";args=new Object[]{"1 absorption heart","3 hearts","10 seconds"};}
            default -> {return null;}
        }
        // Preserve colored effect arguments while normal variables remain highlighted.
        var values=java.util.Arrays.stream(args).map(a->a instanceof Component?a:AbilityDescriptions.white(a)).toArray();
        return Component.translatableWithFallback(id+(Augmentations.isSuper(stack,id,rank)?".super":"")+".final_card",format,values).withStyle(ChatFormatting.GRAY);
    }
    public static Component description(ItemStack stack,String id,int rank) {
        int jade=JadeAugments.isJade(stack)?1:0;
        double damage=AbilityDescriptions.weaponDamage(stack);
        return switch(id) {
            case "tooltips.forgermod.ability.alternating_current" -> text(id,"Aim at %s within 20 blocks of each other and activate once for each to link them for 1 minute. The current deals %s and stuns both for 0.5 seconds each time it jumps.","2 mobs",AlternatingCurrentEvents.damage(rank)+" damage");
            case "tooltips.forgermod.ability.absolute_nightmare" -> mixed(id,
                    "Your next two dagger hits inflict %s and %s for %s.",effect(stack,"Weakness",4),effect(stack,"Darkness",5),colored(stack,(rank==1?5+jade:10+jade)+" seconds"));
            case "tooltips.forgermod.ability.always_the_gardeners" -> mixed(id,
                    "Your next kill will create a poison cloud that stays for %s. Everyone besides you receives %s while inside the %s.",
                    AbilityDescriptions.white("5 minutes"),effect(stack,"Poison",1),AbilityDescriptions.white(AbilityDescriptions.number(2+.5*(rank-1))+"-block radius"));
            case "tooltips.forgermod.ability.area_discharge" -> rank>=5?null:text(id,
                    "Activating this ability strikes hostile mobs within a %s with lightning, dealing half your weapon damage.","5 block radius");
            case "tooltips.forgermod.ability.storing_anger" -> mixed(id,
                    "In the next %s, you won't deal any damage since it will be stored for the next hit after those %s seconds end.",AbilityDescriptions.white("10 seconds"),Component.literal("10").withStyle(ChatFormatting.GRAY));
            case "tooltips.forgermod.ability.strengthened_bones" -> text(id,"Your next dagger kill grants %s of absolute invulnerability.","10 seconds");
            case "tooltips.forgermod.ability.strong_arm" -> text(id,"Your next critical hit has a %s to break a random armor piece of the enemy.",Math.round(StrongArmAbility.chance(rank)*100)+"% chance");
            case "tooltips.forgermod.ability.swing_attack" -> text(id,"Strike all enemies within melee range for %s.",AbilityDescriptions.number(damage)+" damage");
            case "tooltips.forgermod.ability.vacuum_burst" -> text(id,"Pull items and xp within a %s around you into your inventory.",(4+2*rank)+" blocks radius");
            case "tooltips.forgermod.ability.windcharge" -> rank>=4 && stack.getItem() instanceof net.bananashelp20.forgermod.item.custom.SwordItemWithEffect weapon && weapon.isAxe()?null:
                    Augmentations.isTemplate(stack)?null:text(id,"Launch about %s upward and negate falldamage on your next landing.",WindchargeEvents.launchHeight(stack.getItem() instanceof net.bananashelp20.forgermod.item.custom.SwordItemWithEffect weapon && weapon.isAxe(),rank)+" blocks");
            case "tooltips.forgermod.ability.windy_dash" -> text(id,"Dash into the direction you look.");
            case "tooltips.forgermod.passive.aftershock" -> text(id,"Fully charged hits deal %s after %s.","1 delayed extra damage","1 second");
            case "tooltips.forgermod.passive.chain_lightning" -> text(id,"Hits have a %s to chain to nearby enemies on hit. A chain lightning can travel up to %s.","50% chance","10 mobs");
            case "tooltips.forgermod.passive.charred_edge" -> text(id,"Deal %s more damage to enemies with Fire Resistance.","15%");
            case "tooltips.forgermod.passive.cold_bellows" -> text(id,"Blocking an attack with a shield ignites the attacker for %s.",(2+rank)+" seconds");
            case "tooltips.forgermod.passive.compost" -> text(id,"Eating rotten flesh doesn't give you the Hunger effect.");
            case "tooltips.forgermod.passive.conductive_edge" -> text(id,"Deal %s more damage to enemies exposed to water.","20%");
            case "tooltips.forgermod.passive.daydreamer" -> mixed(id,"Gain %s during the day.",effect(stack,"Jump Boost",2));
            case "tooltips.forgermod.passive.dagger" -> text(id,"Allows you to dual wield daggers of the same material. Only one dagger needs this passive.");
            case "tooltips.forgermod.passive.echo_salvage" -> text(id,"If you miss with a Vertical Strike, it stores half it's damage which is being released on the next successful melee hit with an automatic Vertical Strike. Up to %s stay stored in this weapon.",EchoSalvageEvents.capacity(rank)+" Strike Echoes");
            case "tooltips.forgermod.passive.echoing_speed" -> text(id,"Consecutive hits on the same enemy grant %s per hit, up to %s. Changing targets or not hitting for %s resets the bonus.","5% attack speed",(10+10*rank)+"% more attack speed","8 seconds");
            case "tooltips.forgermod.passive.final_strike" -> text(id,"Deal %s to the last enemy you hit if it has less than %s.","50% more damage","10% health");
            case "tooltips.forgermod.passive.fire_resistance" -> mixed(id,"Gain %s while holding this weapon in %s.",effect(stack,"Fire Resistance",1),AbilityDescriptions.white(rank==1?"your main hand":"either hand"));
            case "tooltips.forgermod.passive.heating_up" -> text(id,"While burning, heal %s every %s instead of taking fire damage. Does not work in lava.","1 HP",AbilityDescriptions.number(HeatingUpEvents.healingInterval(rank)/20.0)+" seconds");
            case "tooltips.forgermod.passive.homesick" -> mixed(id,"Standing on sculk grants %s and %s for %s.",effect(stack,"Speed",2),effect(stack,"Strength",1),colored(stack,(3+jade)+" seconds"));
            case "tooltips.forgermod.ability.leech" -> text(id,"Melee hits heal you for %s.",(10+2*(rank-1))+"% of the damage dealt");
            case "tooltips.forgermod.passive.lonely_dream" -> text(id,"Gain %s for each enemy within %s.","5% attack speed","10 blocks");
            case "tooltips.forgermod.passive.lush_jungle" -> text(id,"Enemy kills bonemeal the block beneath them, if possible.");
            case "tooltips.forgermod.passive.pillow_armor" -> text(id,"Take %s.","25% less fall damage");
            case "tooltips.forgermod.passive.quick_recharge" -> text(id,"Fully charged critical hits reduce your primary ability's cooldown by %s once every %s.","1 second","2 seconds");
            case "tooltips.forgermod.passive.revengeful_exit" -> text(id,"When killed by an enemy, deal %s within %s and an additional %s to the killer.",AbilityDescriptions.number(damage)+" damage","5 blocks",AbilityDescriptions.number(damage*5)+" damage");
            case "tooltips.forgermod.passive.sapling_harvest" -> text(id,"Enemy kills have a %s to drop an extra sapling.","15% chance");
            case "tooltips.forgermod.passive.scar_compass" -> mixed(id,"Your most recently hit enemy receives %s for %s.",effect(stack,"Glowing",1),colored(stack,jade>0?"10 minutes and 1 second":"10 minutes"));
            case "tooltips.forgermod.passive.slipstream" -> text(id,"Sprint behind a moving ally within %s to gain %s.","3 blocks","10% movement speed");
            case "tooltips.forgermod.passive.soul_hunger" -> text(id,"Enemy kills grant %s, up to %s. These disappear after %s without Soul Hunger equipped.","1 absorption heart","3 hearts","10 seconds");
            case "tooltips.forgermod.passive.stone_song" -> text(id,"Gain %s below Y=0.","2 armor");
            case "tooltips.forgermod.passive.sunfed" -> text(id,"Sunlight restores %s and %s every %s.","1 hunger","1 saturation","15 seconds");
            case "tooltips.forgermod.passive.axe" -> text(id,"Every third fully charged attack in a %s triggers an area slam.","10 second combo");
            case "tooltips.forgermod.passive.void_grip" -> text(id,"Your melee hits cause %s.","25% less knockback");
            default -> null;
        };
    }
}
