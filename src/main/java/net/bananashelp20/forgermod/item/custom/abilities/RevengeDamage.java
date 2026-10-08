package net.bananashelp20.forgermod.item.custom.abilities;

/** Flat twenty-percent dagger damage bonus against undead. */
public final class RevengeDamage {
    private RevengeDamage() {}

    public static float multiplier(boolean undead) {
        return undead ? 1.2F : 1.0F;
    }
}
