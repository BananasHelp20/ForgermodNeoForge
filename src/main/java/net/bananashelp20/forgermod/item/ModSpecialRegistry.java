package net.bananashelp20.forgermod.item;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

import java.io.FileWriter;

public class ModSpecialRegistry {

    //weapon type standards
    public static final int DEFAULT_CLAYMORE_DAMAGE = 3;
    public static final float DEFAULT_CLAYMORE_SPEED = 2.8f;
    public static final int DEFAULT_RUSTY_CLAYMORE_DAMAGE = -2;
    public static final float DEFAULT_RUSTY_CLAYMORE_SPEED = 3.2f;

    public static final int DEFAULT_AXE_DAMAGE = 6;
    public static final float DEFAULT_AXE_SPEED = 4f;
    public static final int DEFAULT_RUSTY_AXE_DAMAGE = 1;
    public static final float DEFAULT_RUSTY_AXE_SPEED = 6f;

    public static final int DEFAULT_KNIFE_DAMAGE = 1;
    public static final float DEFAULT_KNIFE_SPEED = 1.4f;
    public static final int DEFAULT_RUSTY_KNIFE_DAMAGE = -4;
    public static final float DEFAULT_RUSTY_KNIFE_SPEED = 2.6f;

    //knifes
    public static final int KNIFE_DAMASK_DAMAGE = 0;
    public static final float KNIFE_DAMASK_SPEED = 1f;

    //swords
    public static final int SCRAP_IRON_SWORD_DAMAGE = 3;
    public static final float SCRAP_IRON_SWORD_SPEED = 2.6f;
    public static final int REINFORCED_IRON_SWORD_DAMAGE = 3;
    public static final float REINFOCED_IRON_SWORD_SPEED = 2.8f;
    public static final int SCRAP_SWORD_DAMAGE = 3;
    public static final float SCRAP_SWORD_SPEED = 2.6f;
    public static final int DAMASK_SWORD_DAMAGE = 3;
    public static final float DAMASK_SWORD_SPEED = 2.4f;
    public static final int STEEL_SWORD_DAMAGE = 3;
    public static final float STEEL_SWORD_SPEED = 2.4f;
    
    public static Properties getCorrectAttributes(String gemstone, String type, Properties pProperties, String variety) {
        if (gemstone.equals("amber")) {
            return pProperties.fireResistant();
        }

        int varietyBaseDamage = 0;
        float varietyBaseSpeed = 0;
        Tier tier = ModToolTiers.DEVELOPIUM;

        final int AXE_DAMAGE_AMPLIFIER = 3;
        final int KNIFE_DAMAGE_AMPLIFIER = -2;
        final int CLAYMORE_DAMAGE_AMPLIFIER = 0;

        final float AXE_SPEED_AMPLIFIER = -1.2f;
        final float KNIFE_SPEED_AMPLIFIER = 1.4f;
        final float CLAYMORE_SPEED_AMPLIFIER = 0f;

        final int RUBY_DAMAGE_AMPLIFIER = 1;
        final float AMETHYST_SPEED_AMPLIFIER = 0.4f;

        switch (variety.toLowerCase()) {
            case "lush":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.LUSH;
                break;

            case "vulnusium":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.VULNUSIUM;
                break;

            case "taifunite":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.TAIFUNITE;
                break;

            case "somnium":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.SOMNIUM;
                break;

            case "pulsite":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.PULSITE;
                break;

            case "morsium":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.MORSIUM;
                break;

            case "inanisium":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.INANISIUM;
                break;

            case "ignisium":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.IGNISIUM;
                break;

            case "electrium":
                varietyBaseDamage = 4;
                varietyBaseSpeed = -2.4f;
                tier = ModToolTiers.ELECTRIUM;
                break;
        }

        switch (type.toLowerCase()) {
            case "knife":
                varietyBaseDamage += KNIFE_DAMAGE_AMPLIFIER;
                varietyBaseSpeed -= KNIFE_SPEED_AMPLIFIER;
                break;

            case "axe":
                varietyBaseDamage += AXE_DAMAGE_AMPLIFIER;
                varietyBaseSpeed -= AXE_SPEED_AMPLIFIER;
                break;

            case "claymore":
                varietyBaseDamage += CLAYMORE_DAMAGE_AMPLIFIER;
                varietyBaseSpeed -= CLAYMORE_SPEED_AMPLIFIER;
                break;
        }

        switch (gemstone.toLowerCase()) {
            case "ruby": varietyBaseDamage += RUBY_DAMAGE_AMPLIFIER;
                break;
            case "amethyst": varietyBaseSpeed += AMETHYST_SPEED_AMPLIFIER;
                break;
        }

        return pProperties.attributes(SwordItem.createAttributes(tier, varietyBaseDamage, varietyBaseSpeed));
    }
}
