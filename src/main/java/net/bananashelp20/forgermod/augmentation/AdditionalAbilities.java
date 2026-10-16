package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Set;

/** Completed user-marked additions; registration is shared by offers, ranks and runtime dispatch. */
public final class AdditionalAbilities {
    public static final String FLAME_BREATH = "tooltips.forgermod.ability.flame_breath";
    public static final String CHARRED_EDGE = "tooltips.forgermod.passive.charred_edge";
    public static final String MAGMA_TOSS = "tooltips.forgermod.ability.magma_toss";
    public static final String DISPLACE = "tooltips.forgermod.ability.displace";
    public static final String BACKSTAB = "tooltips.forgermod.passive.backstab";
    public static final String VOID_GRIP = "tooltips.forgermod.passive.void_grip";
    public static final String VOID_PULSE = "tooltips.forgermod.ability.void_pulse";
    public static final String GRAVITY_WELL = "tooltips.forgermod.ability.gravity_well";
    public static final String LONELY_DREAM = "tooltips.forgermod.passive.lonely_dream";
    public static final String UNFINISHED_DREAM = "tooltips.forgermod.passive.unfinished_dream";
    public static final String JUST_A_DREAM = "tooltips.forgermod.ability.just_a_dream";
    public static final String SLEEPWALKER = "tooltips.forgermod.passive.sleepwalker";
    public static final String LULLABY = "tooltips.forgermod.ability.lullaby";
    public static final String DREAM_FEAST = "tooltips.forgermod.ability.dream_feast";
    public static final String DAYDREAMER = "tooltips.forgermod.passive.daydreamer";
    public static final String PILLOW_ARMOR = "tooltips.forgermod.passive.pillow_armor";
    public static final String MAGNETIC_THROW = "tooltips.forgermod.ability.magnetic_throw";
    public static final String LIVE_WIRE = "tooltips.forgermod.ability.live_wire";
    public static final String QUICK_RECHARGE = "tooltips.forgermod.passive.quick_recharge";
    public static final String CONDUCTIVE_EDGE = "tooltips.forgermod.passive.conductive_edge";
    public static final String STATIC_BLADE = "tooltips.forgermod.ability.static_blade";
    public static final String LIGHTNING_ROD = "tooltips.forgermod.passive.lightning_rod";
    public static final String HOMESICK = "tooltips.forgermod.passive.homesick";
    public static final String ALTERNATING_CURRENT = "tooltips.forgermod.ability.alternating_current";
    public static final String DIRECT_CURRENT = "tooltips.forgermod.ability.direct_current";
    public static final String STUNNING_PRESENCE = "tooltips.forgermod.ability.stunning_presence";
    public static final String HEAVY_CURRENT = "tooltips.forgermod.passive.heavy_current";
    public static final String DOWNDRAFT_BELL = "tooltips.forgermod.ability.downdraft_bell";
    public static final String CROSSWIND = "tooltips.forgermod.ability.crosswind";
    public static final String SLIPSTREAM = "tooltips.forgermod.passive.slipstream";
    public static final String UPWIND = "tooltips.forgermod.ability.upwind";
    public static final String WINDCHARGE = "tooltips.forgermod.ability.windcharge";
    public static final String VACUUM_BURST = "tooltips.forgermod.ability.vacuum_burst";
    public static final String HEADWIND = "tooltips.forgermod.passive.headwind";
    public static final String FEATHER_SHELTER = "tooltips.forgermod.ability.feather_shelter";
    public static final String POCKET_STORM = "tooltips.forgermod.ability.pocket_storm";
    public static final String BLOOD_RUSH = "tooltips.forgermod.ability.blood_rush";
    public static final String FINAL_STRIKE = "tooltips.forgermod.passive.final_strike";
    public static final String BLOOD_SACRAFICE = "tooltips.forgermod.ability.blood_sacrafice";
    public static final String SCAR_COMPASS = "tooltips.forgermod.passive.scar_compass";
    public static final String REVENGEFUL_EXIT = "tooltips.forgermod.passive.revengeful_exit";
    public static final String SUNFED = "tooltips.forgermod.passive.sunfed";
    public static final String LUSH_JUNGLE = "tooltips.forgermod.passive.lush_jungle";
    public static final String PHOTOSYNTHESIS = "tooltips.forgermod.passive.photosynthesis";
    public static final String GREEN_FEAST = "tooltips.forgermod.ability.green_feast";
    public static final String SAPLING_HARVEST = "tooltips.forgermod.passive.sapling_harvest";
    public static final String COMPOST = "tooltips.forgermod.passive.compost";
    public static final String LUMBERJACK = "tooltips.forgermod.passive.lumberjack";
    public static final String GHOSTWALK = "tooltips.forgermod.ability.ghostwalk";
    public static final String SOUL_HUNGER = "tooltips.forgermod.passive.soul_hunger";
    public static final String LAST_STAND = "tooltips.forgermod.passive.last_stand";
    public static final String BONE_GUARD = "tooltips.forgermod.ability.bone_guard";
    public static final String SKULL_THROW = "tooltips.forgermod.ability.skull_throw";
    public static final String DEATHS_DUE = "tooltips.forgermod.passive.deaths_due";
    public static final String ECHO_APPETITE = "tooltips.forgermod.passive.echo_appetite";
    public static final String RESONANCE_CRIT = "tooltips.forgermod.passive.resonance_crit";
    public static final String AFTERSHOCK = "tooltips.forgermod.passive.aftershock";
    public static final String RINGING_STEEL = "tooltips.forgermod.ability.ringing_steel";
    public static final String STONE_SONG = "tooltips.forgermod.passive.stone_song";
    public static final String CHALLENGE_ROAR = "tooltips.forgermod.ability.challenge_roar";
    public record Definition(String id, boolean active, int maxRank, int cooldown, int cooldownStep, Set<String> weapons) {}
    public static final List<Definition> DEFINITIONS = List.of(
            new Definition(FLAME_BREATH, true, 4, 600, 100, Set.of("Ignisium:dagger", "Ignisium:claymore", "Ignisium:axe")),
            new Definition(CHARRED_EDGE, false, 1, 0, 0, Set.of("Ignisium:dagger")),
            new Definition(MAGMA_TOSS, true, 4, 700, 100, Set.of("Ignisium:axe")),
            new Definition(DISPLACE, true, 4, 800, 100, Set.of("Inanisium:dagger")),
            new Definition(BACKSTAB, false, 1, 0, 0, Set.of("Inanisium:dagger", "Somnium:dagger", "Electrium:dagger", "Taifunite:dagger", "Vulnusium:dagger", "Lush:dagger", "Morsium:dagger", "Pulsite:dagger")),
            new Definition(VOID_GRIP, false, 1, 0, 0, Set.of("Inanisium:dagger")),
            new Definition(VOID_PULSE, true, 4, 600, 100, Set.of("Inanisium:dagger", "Inanisium:claymore", "Inanisium:axe")),
            new Definition(GRAVITY_WELL, true, 4, 800, 100, Set.of("Inanisium:axe")),
            new Definition(LONELY_DREAM, false, 1, 0, 0, Set.of("Somnium:dagger")),
            new Definition(UNFINISHED_DREAM, false, 1, 0, 0, Set.of("Somnium:dagger", "Somnium:claymore", "Somnium:axe")),
            new Definition(JUST_A_DREAM, true, 4, 1200, 200, Set.of("Somnium:dagger", "Somnium:claymore", "Somnium:axe")),
            new Definition(SLEEPWALKER, false, 1, 0, 0, Set.of("Somnium:dagger")),
            new Definition(LULLABY, true, 4, 800, 100, Set.of("Somnium:claymore")),
            new Definition(DREAM_FEAST, true, 4, 1200, 200, Set.of("Somnium:claymore")),
            new Definition(DAYDREAMER, false, 1, 0, 0, Set.of("Somnium:axe")),
            new Definition(PILLOW_ARMOR, false, 1, 0, 0, Set.of("Somnium:axe")),
            new Definition(MAGNETIC_THROW, true, 4, 500, 100, Set.of("Electrium:dagger", "Electrium:claymore", "Electrium:axe")),
            new Definition(LIVE_WIRE, true, 4, 800, 100, Set.of("Electrium:dagger")),
            new Definition(QUICK_RECHARGE, false, 1, 0, 0, Set.of("Electrium:dagger", "Electrium:claymore", "Electrium:axe")),
            new Definition(CONDUCTIVE_EDGE, false, 1, 0, 0, Set.of("Electrium:dagger", "Electrium:claymore", "Electrium:axe")),
            new Definition(STATIC_BLADE, true, 4, 800, 100, Set.of("Electrium:dagger")),
            new Definition(LIGHTNING_ROD, false, 1, 0, 0, Set.of("Electrium:dagger", "Electrium:claymore", "Electrium:axe")),
            new Definition(HOMESICK, false, 1, 0, 0, Set.of("Electrium:dagger", "Electrium:claymore", "Electrium:axe")),
            new Definition(ALTERNATING_CURRENT, true, 4, 2400, 400, Set.of("Electrium:dagger", "Electrium:claymore", "Electrium:axe")),
            new Definition(DIRECT_CURRENT, true, 4, 2400, 400, Set.of("Electrium:dagger", "Electrium:claymore", "Electrium:axe")),
            new Definition(STUNNING_PRESENCE, true, 4, 1200, 200, Set.of("Electrium:claymore")),
            new Definition(HEAVY_CURRENT, false, 1, 0, 0, Set.of("Electrium:axe")),
            new Definition(DOWNDRAFT_BELL, true, 4, 600, 100, Set.of("Taifunite:dagger", "Taifunite:claymore", "Taifunite:axe")),
            new Definition(CROSSWIND, true, 4, 3600, 400, Set.of("Taifunite:dagger", "Taifunite:claymore", "Taifunite:axe")),
            new Definition(SLIPSTREAM, false, 1, 0, 0, Set.of("Taifunite:dagger")),
            new Definition(UPWIND, true, 4, 600, 100, Set.of("Taifunite:dagger", "Taifunite:claymore", "Taifunite:axe")),
            new Definition(WINDCHARGE, true, 4, 600, 100, Set.of("Taifunite:dagger", "Taifunite:claymore", "Taifunite:axe")),
            new Definition(VACUUM_BURST, true, 4, 600, 100, Set.of("Taifunite:dagger", "Taifunite:claymore", "Taifunite:axe")),
            new Definition(HEADWIND, false, 1, 0, 0, Set.of("Taifunite:dagger", "Taifunite:claymore", "Taifunite:axe")),
            new Definition(FEATHER_SHELTER, true, 4, 1200, 200, Set.of("Taifunite:claymore", "Taifunite:axe")),
            new Definition(POCKET_STORM, true, 4, 800, 100, Set.of("Taifunite:axe")),
            new Definition(BLOOD_RUSH, true, 4, 600, 100, Set.of("Vulnusium:dagger", "Vulnusium:claymore")),
            new Definition(FINAL_STRIKE, false, 1, 0, 0, Set.of("Vulnusium:dagger", "Vulnusium:claymore", "Vulnusium:axe")),
            new Definition(BLOOD_SACRAFICE, true, 4, 1200, 200, Set.of("Vulnusium:dagger", "Vulnusium:claymore", "Vulnusium:axe")),
            new Definition(SCAR_COMPASS, false, 1, 0, 0, Set.of("Vulnusium:dagger", "Vulnusium:claymore", "Vulnusium:axe")),
            new Definition(REVENGEFUL_EXIT, false, 1, 0, 0, Set.of("Vulnusium:dagger", "Vulnusium:claymore", "Vulnusium:axe")),
            new Definition(SUNFED, false, 1, 0, 0, Set.of("Lush:dagger", "Lush:claymore", "Lush:axe")),
            new Definition(LUSH_JUNGLE, false, 1, 0, 0, Set.of("Lush:dagger", "Lush:claymore", "Lush:axe")),
            new Definition(PHOTOSYNTHESIS, false, 1, 0, 0, Set.of("Lush:dagger", "Lush:claymore", "Lush:axe")),
            new Definition(GREEN_FEAST, true, 4, 400, 100, Set.of("Lush:dagger", "Lush:claymore", "Lush:axe")),
            new Definition(SAPLING_HARVEST, false, 1, 0, 0, Set.of("Lush:dagger", "Lush:claymore", "Lush:axe")),
            new Definition(COMPOST, false, 1, 0, 0, Set.of("Lush:dagger", "Lush:claymore", "Lush:axe")),
            new Definition(LUMBERJACK, false, 1, 0, 0, Set.of("Lush:axe")),
            new Definition(GHOSTWALK, true, 4, 600, 100, Set.of("Morsium:dagger", "Morsium:claymore", "Morsium:axe")),
            new Definition(SOUL_HUNGER, false, 1, 0, 0, Set.of("Morsium:dagger", "Morsium:claymore", "Morsium:axe")),
            new Definition(LAST_STAND, false, 1, 0, 0, Set.of("Morsium:dagger", "Morsium:claymore", "Morsium:axe")),
            new Definition(BONE_GUARD, true, 4, 1200, 200, Set.of("Morsium:claymore", "Morsium:axe")),
            new Definition(SKULL_THROW, true, 4, 1000, 200, Set.of("Morsium:axe")),
            new Definition(DEATHS_DUE, false, 1, 0, 0, Set.of("Morsium:dagger", "Morsium:claymore", "Morsium:axe")),
            new Definition(ECHO_APPETITE, false, 1, 0, 0, Set.of("Pulsite:dagger", "Pulsite:claymore", "Pulsite:axe")),
            new Definition(RESONANCE_CRIT, false, 1, 0, 0, Set.of("Pulsite:dagger", "Pulsite:claymore")),
            new Definition(AFTERSHOCK, false, 1, 0, 0, Set.of("Pulsite:dagger", "Pulsite:claymore", "Pulsite:axe")),
            new Definition(RINGING_STEEL, true, 4, 600, 100, Set.of("Pulsite:claymore")),
            new Definition(STONE_SONG, false, 1, 0, 0, Set.of("Pulsite:claymore")),
            new Definition(CHALLENGE_ROAR, true, 4, 600, 100, Set.of("Pulsite:dagger", "Pulsite:claymore", "Pulsite:axe"))
    );
    private AdditionalAbilities() {}
    public static Definition definition(String id) {
        return DEFINITIONS.stream().filter(d -> d.id().equals(id)).findFirst().orElse(null);
    }
    public static List<Augmentations.Ability> pool(ItemStack stack) {
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon) || !weapon.hasMaterialEffect()) return List.of();
        String family = weapon.getClass().getSimpleName().replace("Weapon", "");
        String type = weapon.isDagger() ? "dagger" : weapon.isAxe() ? "axe" : "claymore";
        return DEFINITIONS.stream().filter(d -> d.weapons().contains(family + ":" + type))
                .map(d -> new Augmentations.Ability(d.id(), d.active())).toList();
    }
}
