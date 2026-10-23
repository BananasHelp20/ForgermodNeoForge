package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.abilities.DeathStareAbility;
import net.bananashelp20.forgermod.item.custom.abilities.DistanceTaxEvents;
import net.bananashelp20.forgermod.item.custom.abilities.HeatingUpEvents;
import net.bananashelp20.forgermod.item.custom.abilities.StrongArmAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Read-only comparisons of the offered ranks. Never applies an augment or changes gameplay. */
public final class AugmentUpgradeDescriptions {
    // Readable on both the pale cards and Minecraft's dark tooltip background.
    public static final int CHANGE_COLOR = 0xFFFFFF;
    private static final String PREFIX = "augmentation.forgermod.preview.";

    private record Stat(Object amount, Component value) {}
    public record Change(String property, Object before, Object after, Component value) {
        public Component sentence(boolean highlight) {
            if (property.equals("summons") && before instanceof List<?> old && after instanceof List<?> next)
                return Component.translatable(PREFIX + property, roster(next, old, highlight));
            return Component.translatable(PREFIX + property,
                    highlight ? value.copy().withStyle(style -> style.withColor(CHANGE_COLOR)) : value.copy());
        }
    }

    private AugmentUpgradeDescriptions() {}

    /** Shared rank values keep item text and upgrade comparisons consistent. */
    static Map<String, Component> values(ItemStack stack, String id, int rank) {
        var result = new LinkedHashMap<String, Component>();
        stats(stack, id, rank).forEach((key, stat) -> result.put(key, stat.value().copy()));
        return result;
    }

    public static List<Change> changes(ItemStack stack, String id, int currentRank) {
        if (currentRank < 1 || currentRank >= Augmentations.maxRank(stack, id)) return List.of();
        var before = stats(stack, id, currentRank);
        var after = stats(stack, id, currentRank + 1);
        var result = new ArrayList<Change>();
        after.forEach((key, value) -> {
            Stat previous = before.get(key);
            if (previous == null || !Objects.equals(previous.amount(), value.amount()))
                result.add(new Change(key, previous == null ? null : previous.amount(), value.amount(), value.value()));
        });
        return List.copyOf(result);
    }

    /** Compact cards show only changes; hovering also shows the ability's normal description. */
    public static Component description(ItemStack stack, String id, int currentRank, boolean compact, boolean highlight) {
        if (currentRank == 0) return AbilityDescriptions.description(stack, id, 1, compact);
        MutableComponent text = Component.empty();
        if (!compact) text.append(AbilityDescriptions.description(stack, id, currentRank + 1, false)).append("\n");
        var changes = changes(stack, id, currentRank);
        if (changes.isEmpty()) return text.append(Component.translatable(PREFIX + "unchanged"));
        for (int i = 0; i < changes.size(); i++) {
            if (i > 0) text.append(compact ? " " : "\n");
            text.append(changes.get(i).sentence(highlight));
        }
        return text;
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("\\.?0+$", "");
    }

    private static void numeric(Map<String, Stat> stats, String property, double amount, String unit) {
        double rounded = Math.round(amount * 10000) / 10000.0;
        stats.put(property, new Stat(rounded,
                Component.translatable(PREFIX + "value." + unit, number(amount))));
    }

    private static void text(Map<String, Stat> stats, String property, Object comparison, String valueKey) {
        stats.put(property, new Stat(comparison, Component.translatable(PREFIX + "value." + valueKey)));
    }

    private static void seconds(Map<String, Stat> stats, String property, double ticks) {
        numeric(stats, property, ticks / 20, "seconds");
    }

    private static Component roster(List<?> counts, List<?> before, boolean highlight) {
        String[] types = {"skeleton", "bogged", "stray", "wither_skeleton"};
        MutableComponent text = Component.empty();
        for (int i = 0; i < counts.size(); i++) {
            int count = (Integer)counts.get(i);
            if (count == 0) continue;
            if (!text.getSiblings().isEmpty()) text.append(", ");
            var group = Component.translatable(PREFIX + "value." + types[i] + (count == 1 ? ".one" : ".many"), count);
            if (highlight && !counts.get(i).equals(before.get(i))) group.withStyle(style -> style.withColor(CHANGE_COLOR));
            text.append(group);
        }
        return text;
    }

    private static Map<String, Stat> stats(ItemStack stack, String id, int rank) {
        var stats = new LinkedHashMap<String, Stat>();
        boolean axe = stack.getItem() instanceof SwordItemWithEffect weapon && weapon.isAxe();
        boolean template = Augmentations.isTemplate(stack);
        int jadeTicks = JadeAugments.isJade(stack) ? 20 : 0;
        switch (id) {
            case Augmentations.AREA_DISCHARGE -> {
                int superRank=Math.clamp(rank-4,1,4);
                numeric(stats,"radius",rank<5?5:10*superRank,"blocks");
                numeric(stats,"damage",rank<5?.5:2,"weapon_damage");
                numeric(stats,"waves",rank<5?1:superRank,"waves");
                if(rank>=5)text(stats,"unlocks",true,"anger_of_zeus");
            }
            case "tooltips.forgermod.ability.eye_of_the_storm" -> {
                numeric(stats,"radius",ReviewedRanks.stormRadius(rank),"blocks");
                seconds(stats,"duration",ReviewedRanks.stormTicks(rank)+jadeTicks);
            }
            case AdditionalAbilities.DISPLACE -> {
                if(rank>=4)text(stats,"range","unlimited","visible_range");
                else numeric(stats,"range",10,"blocks");
            }
            case "tooltips.forgermod.ability.charge_attack" -> {
                numeric(stats,"charge_hits",net.bananashelp20.forgermod.item.custom.abilities.ChargeAttackState.chargingHits(rank),"hits");
                numeric(stats,"charge_bonus",net.bananashelp20.forgermod.item.custom.abilities.ChargeAttackState.maximumBonus(rank)*100,"percent");
                numeric(stats,"lightning_damage",net.bananashelp20.forgermod.item.custom.abilities.ChargeAttackState.lightningDamage(rank),"damage");
            }
            case AdditionalAbilities.DREAM_FEAST, AdditionalAbilities.GREEN_FEAST -> numeric(stats,"food",ReviewedRanks.foodPoints(rank),"food_points");
            case RecommendedAbilities.DELUSION -> seconds(stats,"duration",ReviewedRanks.delusionTicks(rank));
            case AdditionalAbilities.BLOOD_SACRAFICE -> seconds(stats,"duration",net.bananashelp20.forgermod.item.custom.abilities.SacrificeCost.durationTicks(rank));
            case RecommendedAbilities.CINDER_DECOY -> {
                numeric(stats, "radius", 1 + Math.clamp(rank, 1, 4), "blocks");
                seconds(stats, "burn", (2 + rank) * 20);
                seconds(stats, "duration", 12000);
            }
            case RecommendedAbilities.COLD_BELLOWS -> seconds(stats, "burn", (2 + rank) * 20);
            case RecommendedAbilities.HEATING_UP -> seconds(stats, "healing_interval", HeatingUpEvents.healingInterval(rank));
            case RecommendedAbilities.FIRE_RESISTANCE -> text(stats, "hands", rank >= 2, rank >= 2 ? "either_hand" : "main_hand");
            case RecommendedAbilities.EXPLOSIVE_HITS -> numeric(stats, "radius", 2 + rank, "blocks");
            case RecommendedAbilities.CRUCIBLE_HOOK -> {
                numeric(stats, "damage", 1 + .25 * rank, "weapon_damage");
                seconds(stats, "burn", (2 + rank) * 20);
                text(stats, "pull", .7 + .1 * rank, "stronger_pull");
            }
            case RecommendedAbilities.STRONG_ARM -> numeric(stats, "chance", StrongArmAbility.chance(rank) * 100, "percent");
            case RecommendedAbilities.DEATH_STARE -> {
                double range = DeathStareAbility.range(rank);
                if (Double.isInfinite(range)) text(stats, "range", "unlimited", "visible_range");
                else numeric(stats, "range", range, "blocks");
            }
            case RecommendedAbilities.ECHOING_SPEED -> numeric(stats, "speed_cap", 10 + 10 * rank, "percent");
            case RecommendedAbilities.DISTANCE_TAX -> numeric(stats, "distance_damage", DistanceTaxEvents.damage(1, rank), "damage_per_block");
            case RecommendedAbilities.FALSE_AWAKENING -> {
                numeric(stats, "bonus_damage", 5 + 5 * rank, "percent");
                seconds(stats, "duration", 600 + 100 * (rank - 1));
            }
            case RecommendedAbilities.GARDENERS -> numeric(stats, "radius", 2 + .5 * (rank - 1), "blocks");
            case RecommendedAbilities.ECHO_HEARING -> {
                numeric(stats, "range", 24 + 8 * Math.clamp(rank, 1, 4), "blocks");
                seconds(stats, "duration", 160 + 40 * Math.clamp(rank, 1, 4));
                seconds(stats, "glow_duration", ReviewedRanks.echoGlowTicks(rank)+jadeTicks);
            }
            case RecommendedAbilities.ECHO_SALVAGE -> numeric(stats, "damage_cap", 2 * rank, "damage");
            case RecommendedAbilities.ENDER_SWING -> {
                numeric(stats,"damage",rank>1?1.2:1,"weapon_damage");
                numeric(stats,"radius",ReviewedRanks.gravitySwingRadius(rank),"blocks");
                numeric(stats,"lift",ReviewedRanks.gravitySwingHeight(rank),"blocks");
                numeric(stats,"gravity",ReviewedRanks.gravitySwingBonus(rank)*100,"percent");
            }
            case RecommendedAbilities.FLAMEBORN -> {
                seconds(stats, "burn", rank > 1 ? 140 : 100);
                numeric(stats,"damage",net.bananashelp20.forgermod.item.custom.abilities.FlamebornAbility.damageMultiplier(rank),"weapon_damage");
            }
            case Augmentations.DOUBLE_JUMP -> {
                numeric(stats, "air_jumps", rank >= 4 ? 2 : 1, "jumps");
                text(stats, "jump_height", .55 + .05 * Math.clamp(rank - 4, 0, 3), "higher_jumps");
            }
            case "tooltips.forgermod.ability.absolute_nightmare" -> seconds(stats, "duration", (rank == 1 ? 100 : 200) + jadeTicks);
            case "tooltips.forgermod.ability.leech" -> numeric(stats, "healing", 10 + 2 * (rank - 1), "damage_healed");
            case AdditionalAbilities.FLAME_BREATH -> {
                numeric(stats, "range", 6 + rank, "blocks");
                seconds(stats, "burn", (2 + rank) * 20);
            }
            case AdditionalAbilities.MAGMA_TOSS -> {
                numeric(stats, "radius", 1.5 + .5 * rank, "blocks");
                seconds(stats, "burn", (2 + rank) * 20);
            }
            case AdditionalAbilities.VOID_PULSE -> {
                numeric(stats, "radius", 3 + rank, "blocks");
                text(stats, "knockback", .4 + .2 * rank, "stronger_knockback");
            }
            case AdditionalAbilities.GRAVITY_WELL -> text(stats, "pull", .5 + .1 * rank, "stronger_pull");
            case AdditionalAbilities.LULLABY -> numeric(stats, "range", 8 + 2 * rank, "blocks");
            case AdditionalAbilities.MAGNETIC_THROW -> text(stats, "throw", .9 + .2 * rank, "stronger_throw");
            case AdditionalAbilities.LIGHTNING_ROD -> seconds(stats, "lightning_interval", (rank == 1 ? 600 : 60) * 20);
            case AdditionalAbilities.DIRECT_CURRENT -> {
                numeric(stats, "targets", Math.min(4, rank + 1), "targets");
                if (rank >= 4) {
                    text(stats, "unlocks", true, "static_field");
                    numeric(stats, "field_damage", 4 + Math.max(0, rank - 4), "field_damage");
                }
            }
            case AdditionalAbilities.STUNNING_PRESENCE -> seconds(stats, "pulse_interval", Math.round(40 - (rank - 1) * 20F / 3));
            case AdditionalAbilities.DOWNDRAFT_BELL -> {
                numeric(stats, "radius", 2 + 2 * rank, "blocks");
                text(stats, "pull", 2 + .2 * (rank - 1), "stronger_pull");
            }
            case AdditionalAbilities.UPWIND -> text(stats, "knockback", .4 + .2 * rank, "stronger_launch");
            case AdditionalAbilities.WINDCHARGE -> {
                if (!axe) numeric(stats, "launch", 5 * Math.clamp(rank, 1, 5), "blocks");
                if (axe || template) numeric(stats, template ? "axe_launch" : "launch", new int[]{10, 16, 23, 30}[Math.clamp(rank, 1, 4) - 1], "blocks");
                if ((axe || template) && rank >= 4) {
                    text(stats, "unlocks", true, template ? "axe_skybreaker" : "skybreaker");
                    numeric(stats, "smash_damage", 100 + 10 * Math.clamp(rank - 4, 0, 3), "percent");
                }
            }
            case AdditionalAbilities.VACUUM_BURST -> numeric(stats, "radius", 4 + 2 * rank, "blocks");
            case AdditionalAbilities.FEATHER_SHELTER -> numeric(stats, "radius", 2 + 2 * rank, "blocks");
            case AdditionalAbilities.POCKET_STORM -> text(stats, "knockback", .8 + .2 * rank, "stronger_launch");
            case AdditionalAbilities.GHOSTWALK -> {
                seconds(stats, "duration", 200 + 40 * Math.max(0, rank - 4) + jadeTicks);
                if (rank >= 4) numeric(stats, "opening_hit", 2, "weapon_damage");
            }
            case AdditionalAbilities.BONE_GUARD -> {
                var counts = switch (Math.clamp(rank, 1, 7)) {
                    case 1 -> List.of(1, 0, 0, 0);
                    case 2 -> List.of(2, 0, 0, 0);
                    case 3 -> List.of(1, 1, 0, 0);
                    case 4 -> List.of(0, 1, 1, 1);
                    case 5 -> List.of(0, 1, 1, 2);
                    case 6 -> List.of(1, 1, 1, 2);
                    default -> List.of(2, 1, 1, 2);
                };
                stats.put("summons", new Stat(counts, roster(counts, counts, false)));
                if (rank >= 4) text(stats, "equipment", rank >= 7 ? 2 : 1, rank >= 7 ? "elite_equipment" : "armor");
                numeric(stats, "ally_range", 6 + 2 * Math.min(4, rank), "blocks");
            }
            case AdditionalAbilities.SKULL_THROW -> numeric(stats, "radius", 1.5 + .5 * rank, "blocks");
            case AdditionalAbilities.CHALLENGE_ROAR -> {
                numeric(stats,"range",ReviewedRanks.roarRange(rank),"blocks");
                seconds(stats,"duration",ReviewedRanks.roarTicks(rank)+jadeTicks);
                if(rank>=5)text(stats,"super_form",true,"intimidating_roar");
            }
            default -> { /* Fixed-effect augments intentionally have no invented rank scaling. */ }
        }
        if (RecommendedAbilities.active(id) || ReviewedRanks.cooldown(id,rank)>=0) {
            int cooldown = RecommendedAbilities.cooldown(id, rank);
            if (cooldown > 0) seconds(stats, "cooldown", cooldown);
        }
        return stats;
    }
}
