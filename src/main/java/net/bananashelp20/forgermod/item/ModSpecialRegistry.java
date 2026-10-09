package net.bananashelp20.forgermod.item;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class ModSpecialRegistry {

    // Speeds are final attacks per second, not penalties or signed attribute modifiers.
    public static final int DEFAULT_CLAYMORE_DAMAGE = 3;
    public static final float DEFAULT_CLAYMORE_SPEED = 1.2f;
    public static final int DEFAULT_RUSTY_CLAYMORE_DAMAGE = -2;
    public static final float DEFAULT_RUSTY_CLAYMORE_SPEED = 0.8f;

    public static final int DEFAULT_AXE_DAMAGE = 6;
    public static final float DEFAULT_AXE_SPEED = 0.9f;
    public static final int DEFAULT_RUSTY_AXE_DAMAGE = 1;
    public static final float DEFAULT_RUSTY_AXE_SPEED = 0.6f;

    public static final int DEFAULT_DAGGER_DAMAGE = 1;
    public static final float DEFAULT_DAGGER_SPEED = 2.6f;
    public static final int DEFAULT_RUSTY_DAGGER_DAMAGE = -4;
    public static final float DEFAULT_RUSTY_DAGGER_SPEED = 1.4f;

    //knifes
    public static final int KNIFE_DAMASK_DAMAGE = 0;
    public static final float KNIFE_DAMASK_SPEED = 3f;

    //swords
    public static final int SCRAP_IRON_SWORD_DAMAGE = 3;
    public static final float SCRAP_IRON_SWORD_SPEED = 1.4f;
    public static final int REINFORCED_IRON_SWORD_DAMAGE = 3;
    public static final float REINFOCED_IRON_SWORD_SPEED = 1.2f;
    public static final int SCRAP_SWORD_DAMAGE = 3;
    public static final float SCRAP_SWORD_SPEED = 1.4f;
    public static final int DAMASK_SWORD_DAMAGE = 3;
    public static final float DAMASK_SWORD_SPEED = 1.6f;
    public static final int STEEL_SWORD_DAMAGE = 3;
    public static final float STEEL_SWORD_SPEED = 1.6f;

    public static final float SPECIAL_AXE_SPEED = 0.9f;
    public static final float SPECIAL_CLAYMORE_SPEED = 1.6f;
    public static final float SPECIAL_DAGGER_SPEED = 3f;
    public static final float AMETHYST_SPEED_BONUS = 0.4f;

    /** Minecraft adds the item's modifier to the player's base attack speed of 4. */
    public static float attackSpeedModifier(float attacksPerSecond) {
        if (!Float.isFinite(attacksPerSecond) || attacksPerSecond <= 0) {
            throw new IllegalArgumentException("Weapon attack speed must be finite and positive: " + attacksPerSecond);
        }
        return attacksPerSecond - 4f;
    }

    public static float specialAttackSpeed(String gemstone, String type) {
        float speed = switch (type.toLowerCase()) {
            case "axe" -> SPECIAL_AXE_SPEED;
            case "claymore" -> SPECIAL_CLAYMORE_SPEED;
            case "dagger", "knife" -> SPECIAL_DAGGER_SPEED;
            default -> throw new IllegalArgumentException("Unknown special weapon type: " + type);
        };
        return speed + ("amethyst".equalsIgnoreCase(gemstone) ? AMETHYST_SPEED_BONUS : 0);
    }

    /** Keep each material's claymore duration as the baseline, including Jade's bonus. */
    public static int materialEffectDuration(int claymoreDuration, String gemstone, String type) {
        if (claymoreDuration <= 0 || !("axe".equals(type) || "dagger".equals(type))) return claymoreDuration;
        double ratio = (double)specialAttackSpeed(gemstone, "claymore") / specialAttackSpeed(gemstone, type);
        return Math.max(1, (int)Math.round(claymoreDuration * ratio));
    }
    
    public static Properties getCorrectAttributes(String gemstone, String type, Properties pProperties, String variety) {
        if (gemstone.equals("amber")) pProperties.fireResistant();

        int varietyBaseDamage = 0;
        float attacksPerSecond = specialAttackSpeed(gemstone, type);
        Tier tier = ModToolTiers.DEVELOPIUM;

        final int AXE_DAMAGE_AMPLIFIER = 3;
        final int KNIFE_DAMAGE_AMPLIFIER = -2;
        final int CLAYMORE_DAMAGE_AMPLIFIER = 0;

        final int RUBY_DAMAGE_AMPLIFIER = 1;

        switch (variety.toLowerCase()) {
            case "lush":
                varietyBaseDamage = 4;
                tier = ModToolTiers.LUSH;
                break;

            case "vulnusium":
                varietyBaseDamage = 4;
                tier = ModToolTiers.VULNUSIUM;
                break;

            case "taifunite":
                varietyBaseDamage = 4;
                tier = ModToolTiers.TAIFUNITE;
                break;

            case "somnium":
                varietyBaseDamage = 4;
                tier = ModToolTiers.SOMNIUM;
                break;

            case "pulsite":
                varietyBaseDamage = 4;
                tier = ModToolTiers.PULSITE;
                break;

            case "morsium":
                varietyBaseDamage = 4;
                tier = ModToolTiers.MORSIUM;
                break;

            case "inanisium":
                varietyBaseDamage = 4;
                tier = ModToolTiers.INANISIUM;
                break;

            case "ignisium":
                varietyBaseDamage = 4;
                tier = ModToolTiers.IGNISIUM;
                break;

            case "electrium":
                varietyBaseDamage = 4;
                tier = ModToolTiers.ELECTRIUM;
                break;
        }

        switch (type.toLowerCase()) {
            case "knife":
                varietyBaseDamage += KNIFE_DAMAGE_AMPLIFIER;
                break;

            case "axe":
                varietyBaseDamage += AXE_DAMAGE_AMPLIFIER;
                break;

            case "claymore":
                varietyBaseDamage += CLAYMORE_DAMAGE_AMPLIFIER;
                break;
        }

        switch (gemstone.toLowerCase()) {
            case "ruby": varietyBaseDamage += RUBY_DAMAGE_AMPLIFIER;
                break;
        }

        return pProperties.attributes(SwordItem.createAttributes(tier, varietyBaseDamage, attackSpeedModifier(attacksPerSecond)));
    }
}
