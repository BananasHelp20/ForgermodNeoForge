package net.bananashelp20.forgermod.augmentation;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Describes only the supplied rank, never a future upgrade or super form. */
public final class AbilityDescriptions {
    private AbilityDescriptions() {}

    private static final java.util.Map<String,String> ENGLISH = loadEnglish();
    private static java.util.Map<String,String> loadEnglish() {
        try (var input=AbilityDescriptions.class.getResourceAsStream("/assets/forgermod/lang/en_us.json")) {
            if(input==null) throw new IllegalStateException("Missing language resource");
            return new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8),
                    new com.google.gson.reflect.TypeToken<java.util.Map<String,String>>(){}.getType());
        } catch(java.io.IOException exception) { throw new IllegalStateException(exception); }
    }
    public static Component white(Object value) {
        return (value instanceof Component component?component.copy():Component.literal(String.valueOf(value))).withStyle(ChatFormatting.WHITE);
    }
    public static Component text(String key,String fallback,Object... values) {
        return Component.translatableWithFallback(key,fallback,java.util.Arrays.stream(values).map(AbilityDescriptions::white).toArray()).withStyle(ChatFormatting.GRAY);
    }
    public static Component description(ItemStack stack,String id,int rank,boolean compact) {
        Component reviewed=reviewed(stack,id,Math.max(1,rank));
        if(reviewed!=null) return reviewed;
        Component current=currentDescription(stack,id,rank,compact);
        if(current.getContents() instanceof TranslatableContents translated) {
            String key=translated.getKey();
            String english=ENGLISH.get(key);
            if(translated.getArgs().length>0)
                return text(key,english==null?key:english,translated.getArgs());
            if(english!=null) {
                var resolved=RankedDescription.resolve(english,Math.max(1,rank));
                return text(key+".highlighted",resolved.format(),resolved.values().toArray());
            }
        }
        return current.copy().withStyle(ChatFormatting.GRAY);
    }
    private static Component reviewed(ItemStack stack,String id,int rank) {
        int jade=JadeAugments.isJade(stack)?1:0;
        return switch(id) {
            case AdditionalAbilities.LULLABY -> text(id+".reviewed","Aim at a mob within %s to put it to sleep for %s. Any form of damage wakes it.",(8+2*rank)+" blocks",(3+jade)+" seconds");
            case AdditionalAbilities.LIVE_WIRE -> text(id+".reviewed","In the next %s, enemies that hit you take %s in return.","8 seconds","1 hearts damage");
            case AdditionalAbilities.FLAME_BREATH -> text(id+".reviewed","Incinerate the area in front of you with a range of %s, igninting enemies for %s.",(6+rank)+" blocks",(2+rank)+" seconds");
            case AdditionalAbilities.FEATHER_SHELTER -> text(id+".reviewed","Give yourself and allies within %s slow falling for %s.",(2+2*rank)+" blocks",(10+jade)+" seconds");
            case RecommendedAbilities.EXPLOSIVE_HITS -> text(id+".reviewed","Your next 10 melee hits create small blasts that deal an additional %s. The areal blast has a radius of %s around the hit enemy.","30% areal damage",(2+rank)+" blocks");
            case AdditionalAbilities.DOWNDRAFT_BELL -> text(id+".reviewed","Force all flying mobs within a %s to the ground. (phantom\'s worst nightmare)",(2+2*rank)+" block radius");
            case RecommendedAbilities.DEATH_STARE -> rank>=4 ? text(id+".reviewed.max","Teleport beside the mob you aim at when activating the ability.") : text(id+".reviewed","Teleport beside the mob you aim at when activating the ability. The mob has to be within a distance of %s.",((int)net.bananashelp20.forgermod.item.custom.abilities.DeathStareAbility.range(rank))+" blocks");
            case AdditionalAbilities.CROSSWIND -> text(id+".reviewed","Deflect incoming projectiles away from you for 2 minutes. Your own shots are unaffected. Cooldown: %s.",(RecommendedAbilities.cooldown(id,rank)/20)+" seconds");
            case AdditionalAbilities.BLOOD_SACRAFICE -> text(id+".reviewed","Sacrafice half of your total HP to deal double weapon damage for %s.",
                    (net.bananashelp20.forgermod.item.custom.abilities.SacrificeCost.durationTicks(rank)/20)+" seconds");
            case AdditionalAbilities.BLOOD_RUSH -> text(id+".current","On activation pays %s to gain %s for %s.",
                    "1 heart","speed "+net.bananashelp20.forgermod.item.custom.WeaponTooltips.romanNumeral(2+jade),(10+jade)+" seconds");
            default -> null;
        };
    }


    private static final java.util.Set<String> JADE_KEYS = java.util.Set.of(
            "tooltips.forgermod.ability.absolute_nightmare.current.jade",
            "tooltips.forgermod.ability.always_the_gardeners.current.jade",
            "tooltips.forgermod.ability.lullaby.current.jade",
            "tooltips.forgermod.ability.flameborn.current.jade",
            "tooltips.forgermod.ability.feather_shelter.current.jade",
            "tooltips.forgermod.ability.skull_throw.current.jade",
            "tooltips.forgermod.ability.challenge_roar.current.jade",
            "tooltips.forgermod.passive.lightning_rod.current.jade",
            "tooltips.forgermod.ability.explosion.jade",
            "tooltips.forgermod.ability.lucid_dreaming.jade",
            "tooltips.forgermod.ability.deep_wound.jade",
            "tooltips.forgermod.ability.rooting_roots.jade",
            "tooltips.forgermod.ability.poisoned_vein.jade",
            "tooltips.forgermod.ability.blood_rush.jade",
            "tooltips.forgermod.passive.daydreamer.jade",
            "tooltips.forgermod.passive.homesick.jade",
            "tooltips.forgermod.passive.scar_compass.jade",
            "tooltips.forgermod.ability.always_the_gardeners.current.card.jade",
            "tooltips.forgermod.ability.flameborn.current.card.jade",
            "tooltips.forgermod.ability.absolute_nightmare.current.card.jade",
            "tooltips.forgermod.ability.lullaby.current.card.jade",
            "tooltips.forgermod.passive.lightning_rod.current.card.jade",
            "tooltips.forgermod.ability.feather_shelter.current.card.jade",
            "tooltips.forgermod.ability.skull_throw.current.card.jade",
            "tooltips.forgermod.ability.challenge_roar.current.card.jade"
    );

    private static Component currentDescription(ItemStack stack, String id, int rank, boolean compact) {
        rank = Math.max(1, rank);
        var values = AugmentUpgradeDescriptions.values(stack, id, rank);
        if (id.equals(Augmentations.DOUBLE_JUMP) && rank < 4)
            values.put("air_jumps", Component.translatable("augmentation.forgermod.preview.value.one_air_jump"));
        if (id.equals(AdditionalAbilities.BONE_GUARD) && rank >= 4)
            values.put("equipment", Component.translatable(rank >= 7
                    ? "tooltips.forgermod.ability.bone_guard.elite_equipment"
                    : "tooltips.forgermod.ability.bone_guard.armor"));
        String variant = id;
        if (rank >= 4 && Augmentations.hasSuperForm(stack, id)
                && !id.equals(Augmentations.DOUBLE_JUMP)) variant += ".super";
        if (Augmentations.isTemplate(stack) && id.equals(AdditionalAbilities.WINDCHARGE)) {
            // A template has no weapon type yet; describe both current possibilities.
            return Component.translatable(rank >= 4 ? "tooltips.forgermod.ability.windcharge.template.super.current"
                            : "tooltips.forgermod.ability.windcharge.template.current",
                    values.get("launch"), values.get("axe_launch"));
        }
        Component result = switch (variant) {
            case "tooltips.forgermod.ability.cinder_decoy" -> Component.translatable("tooltips.forgermod.ability.cinder_decoy.current", values.get("duration"), values.get("radius"), values.get("burn"));
            case "tooltips.forgermod.passive.cold_bellows" -> Component.translatable("tooltips.forgermod.passive.cold_bellows.current", values.get("burn"));
            case "tooltips.forgermod.passive.heating_up" -> Component.translatable("tooltips.forgermod.passive.heating_up.current", values.get("healing_interval"));
            case "tooltips.forgermod.passive.fire_resistance" -> Component.translatable("tooltips.forgermod.passive.fire_resistance.current", values.get("hands"));
            case "tooltips.forgermod.ability.explosive_hits" -> Component.translatable("tooltips.forgermod.ability.explosive_hits.current", values.get("radius"));
            case "tooltips.forgermod.ability.crucible_hook" -> Component.translatable("tooltips.forgermod.ability.crucible_hook.current", values.get("damage"), values.get("burn"));
            case "tooltips.forgermod.ability.strong_arm" -> Component.translatable("tooltips.forgermod.ability.strong_arm.current", values.get("chance"));
            case "tooltips.forgermod.ability.death_stare" -> Component.translatable("tooltips.forgermod.ability.death_stare.current", values.get("range"));
            case "tooltips.forgermod.passive.echoing_speed" -> Component.translatable("tooltips.forgermod.passive.echoing_speed.current", values.get("speed_cap"));
            case "tooltips.forgermod.passive.distance_tax" -> Component.translatable("tooltips.forgermod.passive.distance_tax.current", values.get("distance_damage"));
            case "tooltips.forgermod.ability.false_awakening" -> Component.translatable("tooltips.forgermod.ability.false_awakening.current", values.get("duration"), values.get("bonus_damage"));
            case "tooltips.forgermod.ability.always_the_gardeners" -> Component.translatable("tooltips.forgermod.ability.always_the_gardeners.current", values.get("radius"));
            case "tooltips.forgermod.ability.echo_pin" -> Component.translatable("tooltips.forgermod.ability.echo_pin.current", values.get("range"), values.get("duration"));
            case "tooltips.forgermod.passive.echo_salvage" -> Component.translatable("tooltips.forgermod.passive.echo_salvage.current", values.get("damage_cap"));
            case "tooltips.forgermod.ability.ender_swing" -> Component.translatable("tooltips.forgermod.ability.ender_swing.current", values.get("damage"));
            case "tooltips.forgermod.ability.flameborn" -> Component.translatable("tooltips.forgermod.ability.flameborn.current", values.get("burn"));
            case "tooltips.forgermod.passive.double_jump" -> Component.translatable("tooltips.forgermod.passive.double_jump.current", values.get("air_jumps"));
            case "tooltips.forgermod.ability.absolute_nightmare" -> Component.translatable("tooltips.forgermod.ability.absolute_nightmare.current", values.get("duration"));
            case "tooltips.forgermod.ability.leech" -> Component.translatable("tooltips.forgermod.ability.leech.current", values.get("healing"));
            case "tooltips.forgermod.ability.flame_breath" -> Component.translatable("tooltips.forgermod.ability.flame_breath.current", values.get("range"), values.get("burn"));
            case "tooltips.forgermod.ability.magma_toss" -> Component.translatable("tooltips.forgermod.ability.magma_toss.current", values.get("radius"), values.get("burn"));
            case "tooltips.forgermod.ability.void_pulse" -> Component.translatable("tooltips.forgermod.ability.void_pulse.current", values.get("radius"));
            case "tooltips.forgermod.ability.lullaby" -> Component.translatable("tooltips.forgermod.ability.lullaby.current", values.get("range"));
            case "tooltips.forgermod.passive.lightning_rod" -> Component.translatable("tooltips.forgermod.passive.lightning_rod.current", values.get("lightning_interval"));
            case "tooltips.forgermod.ability.direct_current" -> Component.translatable("tooltips.forgermod.ability.direct_current.current", values.get("targets"));
            case "tooltips.forgermod.ability.direct_current.super" -> Component.translatable("tooltips.forgermod.ability.direct_current.super.current", values.get("targets"), values.get("field_damage"));
            case "tooltips.forgermod.ability.stunning_presence" -> Component.translatable("tooltips.forgermod.ability.stunning_presence.current", values.get("pulse_interval"));
            case "tooltips.forgermod.ability.downdraft_bell" -> Component.translatable("tooltips.forgermod.ability.downdraft_bell.current", values.get("radius"));
            case "tooltips.forgermod.ability.windcharge" -> Component.translatable("tooltips.forgermod.ability.windcharge.current", values.get("launch"));
            case "tooltips.forgermod.ability.windcharge.super" -> Component.translatable("tooltips.forgermod.ability.windcharge.super.current", values.get("launch"), values.get("smash_damage"));
            case "tooltips.forgermod.ability.vacuum_burst" -> Component.translatable("tooltips.forgermod.ability.vacuum_burst.current", values.get("radius"));
            case "tooltips.forgermod.ability.feather_shelter" -> Component.translatable("tooltips.forgermod.ability.feather_shelter.current", values.get("radius"));
            case "tooltips.forgermod.ability.ghostwalk" -> Component.translatable("tooltips.forgermod.ability.ghostwalk.current", values.get("duration"));
            case "tooltips.forgermod.ability.ghostwalk.super" -> Component.translatable("tooltips.forgermod.ability.ghostwalk.super.current", values.get("duration"));
            case "tooltips.forgermod.ability.bone_guard" -> Component.translatable("tooltips.forgermod.ability.bone_guard.current", values.get("summons"), values.get("ally_range"));
            case "tooltips.forgermod.ability.bone_guard.super" -> Component.translatable("tooltips.forgermod.ability.bone_guard.super.current", values.get("summons"), values.get("ally_range"), values.get("equipment"));
            case "tooltips.forgermod.ability.skull_throw" -> Component.translatable("tooltips.forgermod.ability.skull_throw.current", values.get("radius"));
            case "tooltips.forgermod.ability.challenge_roar" -> Component.translatable("tooltips.forgermod.ability.challenge_roar.current", values.get("range"));
            default -> Component.translatable(compact && Language.getInstance().has(id + ".card") ? id + ".card" : id);
        };
        if (compact && result.getContents() instanceof TranslatableContents translated
                && Language.getInstance().has(translated.getKey() + ".card"))
            result = Component.translatable(translated.getKey() + ".card", translated.getArgs());
        if (JadeAugments.isJade(stack) && result.getContents() instanceof TranslatableContents translated) {
            // These keys describe only Jade's presently reinforced effects, not future upgrades.
            String jadeKey = translated.getKey() + ".jade";
            if (JADE_KEYS.contains(jadeKey)) return Component.translatable(jadeKey, translated.getArgs());
        }
        return result;
    }

    public static boolean canBecomeSuper(ItemStack stack, String id, int rank) {
        return rank > 0 && rank < 4 && Augmentations.hasSuperForm(stack, id);
    }

    /** Call only in expanded tooltips, after the cooldown (or passive description). */
    public static void appendSuperHint(List<Component> tooltip, ItemStack stack, String id, int rank) {
        if (canBecomeSuper(stack, id, rank))
            tooltip.add(Component.translatable("augmentation.forgermod.super_available").withStyle(ChatFormatting.GRAY));
    }
}
