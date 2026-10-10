package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** User-marked recommendations become learnable only after their implementation is complete. */
public final class RecommendedAbilities {
    public static final String QUENCH_POINT="tooltips.forgermod.passive.quench_point";
    public static final String EXPLOSION="tooltips.forgermod.ability.explosion";
    public static final String CINDER_DECOY="tooltips.forgermod.ability.cinder_decoy";
    public static final String COLD_BELLOWS="tooltips.forgermod.passive.cold_bellows";
    public static final String HEATING_UP="tooltips.forgermod.passive.heating_up";
    public static final String FIRE_RESISTANCE="tooltips.forgermod.passive.fire_resistance";
    public static final String EXPLOSIVE_HITS="tooltips.forgermod.ability.explosive_hits";
    public static final String NETHER_BORN="tooltips.forgermod.passive.nether_born";
    public static final String CRUCIBLE_HOOK="tooltips.forgermod.ability.crucible_hook";
    private RecommendedAbilities() {}
    public static List<Augmentations.Ability> pool(ItemStack stack) {
        if(stack.getItem() instanceof IgnisiumWeapon weapon && weapon.isDagger())
            return List.of(new Augmentations.Ability(QUENCH_POINT,false));
        if(stack.getItem() instanceof IgnisiumWeapon weapon && !weapon.isDagger() && !weapon.isAxe())
            return List.of(new Augmentations.Ability(EXPLOSION,true),new Augmentations.Ability(CINDER_DECOY,true),
                    new Augmentations.Ability(EXPLOSIVE_HITS,true),
                    new Augmentations.Ability(COLD_BELLOWS,false),new Augmentations.Ability(HEATING_UP,false),
                    new Augmentations.Ability(FIRE_RESISTANCE,false),new Augmentations.Ability(NETHER_BORN,false));
        if(stack.getItem() instanceof IgnisiumWeapon weapon && weapon.isAxe())
            return List.of(new Augmentations.Ability(FIRE_RESISTANCE,false),new Augmentations.Ability(NETHER_BORN,false),
                    new Augmentations.Ability(CRUCIBLE_HOOK,true));
        return List.of();
    }
    public static boolean active(String id) { return EXPLOSION.equals(id) || CINDER_DECOY.equals(id) || EXPLOSIVE_HITS.equals(id) || CRUCIBLE_HOOK.equals(id); }
    public static int maxRank(String id) { return FIRE_RESISTANCE.equals(id)?2:NETHER_BORN.equals(id)?1:4; }
    public static int cooldown(String id,int rank) {
        if(CRUCIBLE_HOOK.equals(id)) return 800-100*(Math.clamp(rank,1,4)-1);
        return EXPLOSION.equals(id)?1200-100*(Math.clamp(rank,1,4)-1):CINDER_DECOY.equals(id) || EXPLOSIVE_HITS.equals(id)?900:0;
    }
}
