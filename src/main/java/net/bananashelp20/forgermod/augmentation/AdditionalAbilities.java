package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Set;

/** Completed user-marked additions; registration is shared by offers, ranks and runtime dispatch. */
public final class AdditionalAbilities {
    public static final String FLAME_BREATH = "tooltips.forgermod.ability.flame_breath";
    public record Definition(String id, boolean active, int maxRank, int cooldown, int cooldownStep, Set<String> weapons) {}
    public static final List<Definition> DEFINITIONS = List.of(
            new Definition(FLAME_BREATH, true, 4, 600, 100, Set.of("Ignisium:dagger", "Ignisium:claymore", "Ignisium:axe"))
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
