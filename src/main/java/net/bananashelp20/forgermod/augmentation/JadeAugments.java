package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/** Jade strengthens status effects supplied by learned augments, including Material Hit. */
public final class JadeAugments {
    private static final Set<String> EFFECT_AUGMENTS = Set.of(
            Augmentations.MATERIAL_HIT,
            "tooltips.forgermod.ability.absolute_nightmare",
            "tooltips.forgermod.ability.lucid_dreaming",
            "tooltips.forgermod.ability.eye_of_the_storm",
            "tooltips.forgermod.ability.deep_wound",
            "tooltips.forgermod.ability.rooting_roots",
            "tooltips.forgermod.ability.poisoned_vein",
            AdditionalAbilities.BLOOD_RUSH,
            AdditionalAbilities.DAYDREAMER,
            AdditionalAbilities.FEATHER_SHELTER,
            AdditionalAbilities.GHOSTWALK,
            AdditionalAbilities.LIGHTNING_ROD,
            AdditionalAbilities.LULLABY,
            AdditionalAbilities.SCAR_COMPASS,
            AdditionalAbilities.SKULL_THROW,
            AdditionalAbilities.CHALLENGE_ROAR,
            RecommendedAbilities.EXPLOSION,
            RecommendedAbilities.FLAMEBORN,
            RecommendedAbilities.FIRE_RESISTANCE,
            RecommendedAbilities.GARDENERS,
            RecommendedAbilities.ECHO_HEARING
    );

    private JadeAugments() {}

    public static boolean isJade(ItemStack stack) {
        return stack.getItem() instanceof SwordItemWithEffect weapon
                && "jade".equals(weapon.gemstoneName());
    }

    public static boolean canInfuse(ItemStack stack) {
        if (!Augmentations.eligible(stack)) return false;
        return Augmentations.level(stack, Augmentations.MATERIAL_HIT) > 0
                || Augmentations.ids(stack, true).stream().anyMatch(EFFECT_AUGMENTS::contains)
                || Augmentations.ids(stack, false).stream().anyMatch(EFFECT_AUGMENTS::contains);
    }

    public static int bonus(ItemStack stack, String augmentId) {
        return isJade(stack) && Augmentations.level(stack, augmentId) > 0 ? 1 : 0;
    }

    public static int duration(ItemStack stack, String augmentId, int ticks) {
        return ticks < 0 ? ticks : ticks + 20 * bonus(stack, augmentId);
    }

    public static int amplifier(ItemStack stack, String augmentId, int amplifier) {
        return amplifier + bonus(stack, augmentId);
    }

    public static MobEffectInstance reinforce(ItemStack stack, String augmentId, MobEffectInstance effect) {
        if (bonus(stack, augmentId) == 0) return effect;
        return new MobEffectInstance(effect.getEffect(), duration(stack, augmentId, effect.getDuration()),
                effect.getAmplifier() + 1, effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }
}
