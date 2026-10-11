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
    public record Definition(String id, boolean active, int maxRank, int cooldown, int cooldownStep, Set<String> weapons) {}
    public static final List<Definition> DEFINITIONS = List.of(
            new Definition(FLAME_BREATH, true, 4, 600, 100, Set.of("Ignisium:dagger", "Ignisium:claymore", "Ignisium:axe")),
            new Definition(CHARRED_EDGE, false, 1, 0, 0, Set.of("Ignisium:dagger")),
            new Definition(MAGMA_TOSS, true, 4, 700, 100, Set.of("Ignisium:axe")),
            new Definition(DISPLACE, true, 4, 800, 100, Set.of("Inanisium:dagger"))
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
