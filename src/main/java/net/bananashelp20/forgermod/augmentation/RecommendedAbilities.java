package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.minecraft.world.item.ItemStack;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import java.util.ArrayList;
import java.util.List;

/** User-marked recommendations become learnable only after their implementation is complete. */
public final class RecommendedAbilities {
    public static final String EXPLOSION="tooltips.forgermod.ability.explosion";
    public static final String CINDER_DECOY="tooltips.forgermod.ability.cinder_decoy";
    public static final String COLD_BELLOWS="tooltips.forgermod.passive.cold_bellows";
    public static final String HEATING_UP="tooltips.forgermod.passive.heating_up";
    public static final String FIRE_RESISTANCE="tooltips.forgermod.passive.fire_resistance";
    public static final String EXPLOSIVE_HITS="tooltips.forgermod.ability.explosive_hits";
    public static final String NETHER_BORN="tooltips.forgermod.passive.nether_born";
    public static final String CRUCIBLE_HOOK="tooltips.forgermod.ability.crucible_hook";
    public static final String STRONG_ARM="tooltips.forgermod.ability.strong_arm";
    public static final String SWING_ATTACK="tooltips.forgermod.ability.swing_attack";
    public static final String DEATH_STARE="tooltips.forgermod.ability.death_stare";
    public static final String ECHOING_SPEED="tooltips.forgermod.passive.echoing_speed";
    public static final String DISTANCE_TAX="tooltips.forgermod.passive.distance_tax";
    public static final String NULL_RECEIPT="tooltips.forgermod.ability.null_receipt";
    public static final String OVERWHELMING_SMASH="tooltips.forgermod.passive.overwhelming_smash";
    public static final String FALSE_AWAKENING="tooltips.forgermod.ability.false_awakening";
    public static final String DELUSION="tooltips.forgermod.ability.delusion";
    public static final String GARDENERS="tooltips.forgermod.ability.always_the_gardeners";
    public static final String GRAVE_VAULT="tooltips.forgermod.passive.grave_vault";
    // Keep the original translation/augmentation ID so existing weapon stacks and cooldowns remain compatible.
    public static final String ECHO_HEARING="tooltips.forgermod.ability.echo_pin";
    public static final String WARDEN_TONE="tooltips.forgermod.passive.warden_tone";
    public static final String ECHO_SALVAGE="tooltips.forgermod.passive.echo_salvage";
    public static final String FLAMING_STOMP="tooltips.forgermod.ability.flaming_stomp";
    public static final String FLAMEBORN="tooltips.forgermod.ability.flameborn";
    public static final String ENDER_SWING="tooltips.forgermod.ability.ender_swing";
    private RecommendedAbilities() {}
    public static List<Augmentations.Ability> pool(ItemStack stack) {
        var result=new ArrayList<>(materialPool(stack));
        result.addAll(AdditionalAbilities.pool(stack));
        if(stack.getItem() instanceof IgnisiumWeapon)
            result.add(new Augmentations.Ability(FLAMEBORN,true));
        if(stack.getItem() instanceof SwordItemWithEffect weapon && weapon.hasMaterialEffect() && weapon.isAxe()) {
            result.add(new Augmentations.Ability(SWING_ATTACK,true));
            if(!(weapon instanceof IgnisiumWeapon)) result.add(new Augmentations.Ability(OVERWHELMING_SMASH,false));
        }
        return List.copyOf(result);
    }
    private static List<Augmentations.Ability> materialPool(ItemStack stack) {
        if(stack.getItem() instanceof PulsiteWeapon weapon) return weapon.isAxe()
                ?List.of(new Augmentations.Ability(ECHO_HEARING,true),new Augmentations.Ability(WARDEN_TONE,false),new Augmentations.Ability(ECHO_SALVAGE,false))
                :List.of(new Augmentations.Ability(ECHO_HEARING,true),new Augmentations.Ability(WARDEN_TONE,false));
        if(stack.getItem() instanceof LushWeapon) return List.of(new Augmentations.Ability(GARDENERS,true),new Augmentations.Ability(GRAVE_VAULT,false));
        if(stack.getItem() instanceof SomniumWeapon weapon)
            return weapon.isDagger()?List.of(new Augmentations.Ability(FALSE_AWAKENING,true),new Augmentations.Ability(DELUSION,true))
                    :List.of(new Augmentations.Ability(DELUSION,true));
        if(stack.getItem() instanceof InanisiumWeapon weapon && weapon.isDagger())
            return List.of(new Augmentations.Ability(DEATH_STARE,true),new Augmentations.Ability(ECHOING_SPEED,false));
        if(stack.getItem() instanceof InanisiumWeapon weapon && !weapon.isDagger() && !weapon.isAxe())
            return List.of(new Augmentations.Ability(DISTANCE_TAX,true));
        if(stack.getItem() instanceof InanisiumWeapon weapon && weapon.isAxe())
            return List.of(new Augmentations.Ability(NULL_RECEIPT,true),new Augmentations.Ability(ENDER_SWING,true));
        if(stack.getItem() instanceof IgnisiumWeapon weapon && !weapon.isDagger() && !weapon.isAxe())
            return List.of(new Augmentations.Ability(EXPLOSION,true),new Augmentations.Ability(CINDER_DECOY,true),
                    new Augmentations.Ability(EXPLOSIVE_HITS,true),
                    new Augmentations.Ability(COLD_BELLOWS,false),new Augmentations.Ability(HEATING_UP,false),
                    new Augmentations.Ability(FIRE_RESISTANCE,false),new Augmentations.Ability(NETHER_BORN,false));
        if(stack.getItem() instanceof IgnisiumWeapon weapon && weapon.isAxe())
            return List.of(new Augmentations.Ability(FIRE_RESISTANCE,false),new Augmentations.Ability(NETHER_BORN,false),
                    new Augmentations.Ability(CRUCIBLE_HOOK,true),new Augmentations.Ability(STRONG_ARM,true),
                    new Augmentations.Ability(FLAMING_STOMP,true));
        return List.of();
    }
    public static boolean active(String id) { var added=AdditionalAbilities.definition(id); if(added!=null) return added.active(); return DISTANCE_TAX.equals(id) || ENDER_SWING.equals(id) || FLAMEBORN.equals(id) || FLAMING_STOMP.equals(id) || ECHO_HEARING.equals(id) || GARDENERS.equals(id) || EXPLOSION.equals(id) || CINDER_DECOY.equals(id) || EXPLOSIVE_HITS.equals(id) || CRUCIBLE_HOOK.equals(id) || STRONG_ARM.equals(id) || SWING_ATTACK.equals(id) || DEATH_STARE.equals(id) || NULL_RECEIPT.equals(id) || FALSE_AWAKENING.equals(id) || DELUSION.equals(id); }
    public static int maxRank(String id) { if(NETHER_BORN.equals(id) || "tooltips.forgermod.passive.revenge".equals(id)) return 5; if(Augmentations.DOUBLE_JUMP.equals(id)) return 2; if("tooltips.forgermod.passive.death_march".equals(id)) return 5; if("tooltips.forgermod.ability.sudden_presence".equals(id)) return 7; var added=AdditionalAbilities.definition(id); if(added!=null) return added.maxRank(); return Augmentations.DUAL_WIELD.equals(id) || Augmentations.VERTICAL_STRIKE.equals(id) || FLAMING_STOMP.equals(id) || FLAMEBORN.equals(id) || ENDER_SWING.equals(id)?1:FIRE_RESISTANCE.equals(id)?2:OVERWHELMING_SMASH.equals(id) || GRAVE_VAULT.equals(id) || WARDEN_TONE.equals(id)?1:4; }
    public static int cooldown(String id,int rank) {
        int reviewed=ReviewedRanks.cooldown(id,rank);if(reviewed>=0)return reviewed;
        var added=AdditionalAbilities.definition(id); if(added!=null) return added.cooldown()-added.cooldownStep()*(Math.clamp(rank,1,added.maxRank())-1);
        if(DISTANCE_TAX.equals(id))return 0; // Its ten-second mark resolves before it can be armed again.
        if(ECHO_HEARING.equals(id)) return 600-100*(Math.clamp(rank,1,4)-1);
        if(GARDENERS.equals(id)) return 1200-200*(Math.clamp(rank,1,4)-1);
        if(DELUSION.equals(id)) return 800-100*(Math.clamp(rank,1,4)-1);
        if(FALSE_AWAKENING.equals(id)) return 1200-200*(Math.clamp(rank,1,4)-1);
        if(NULL_RECEIPT.equals(id)) return 800-100*(Math.clamp(rank,1,4)-1);
        if(DEATH_STARE.equals(id)) return 900-100*(Math.clamp(rank,1,4)-1);
        if(SWING_ATTACK.equals(id)) return 600-100*(Math.clamp(rank,1,4)-1);
        if(FLAMING_STOMP.equals(id)) return 2400;
        if(FLAMEBORN.equals(id)) return 6000;
        if(ENDER_SWING.equals(id)) return 1200;
        if(STRONG_ARM.equals(id)) return 1200-200*(Math.clamp(rank,1,4)-1);
        if(CRUCIBLE_HOOK.equals(id)) return 800-100*(Math.clamp(rank,1,4)-1);
        return EXPLOSION.equals(id)?1200-100*(Math.clamp(rank,1,4)-1):CINDER_DECOY.equals(id) || EXPLOSIVE_HITS.equals(id)?900:0;
    }
}
