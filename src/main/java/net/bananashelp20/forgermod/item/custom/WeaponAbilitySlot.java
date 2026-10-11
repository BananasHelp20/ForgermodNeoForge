package net.bananashelp20.forgermod.item.custom;

public enum WeaponAbilitySlot {
    PRIMARY, SECONDARY, TERTIARY;
    public static java.util.List<WeaponAbilitySlot> nativeSlots() {return java.util.List.of(PRIMARY,SECONDARY);}
    public String keyTranslation() {return "key.forgermod.ability_"+name().toLowerCase(java.util.Locale.ROOT);}
}
