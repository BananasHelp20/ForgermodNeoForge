package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSpecialSwordItem extends InterpretedItem {
    ArrayList<String> itemProperties;
    public InterpretedSpecialSwordItem(String name, String properties, String itemCreationMethod, String modelMethod, String material) {
        super(new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material)));
        itemProperties = new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material));
    }

    public InterpretedSpecialSwordItem(String name, String properties, String itemCreationMethod, String modelMethod, String material, String rarity) {
        super(new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material)));
        itemProperties = new ArrayList<>(Arrays.asList(name.trim(), properties.trim(), itemCreationMethod.trim(), modelMethod.trim(), material.trim(), (rarity.trim().charAt(0) == '?' ? rarity.trim().substring(1) : rarity.trim())));
    }

    @Override
    public String toString() {
        return "    public static final DeferredItem<SwordItem> " + itemProperties.get(0).toUpperCase() + " = " + itemProperties.get(2) + "(\"" + itemProperties.get(0).toLowerCase() + "\", ModToolTiers." + itemProperties.get(4).toUpperCase() + ", " + (itemProperties.size() == 6 ? "Rarity." + itemProperties.get(5).toUpperCase() + ", " : "") + itemProperties.get(1) + ");";
    }

    public String getItemModel() {
        return "        " + itemProperties.get(3) + "(ModItems." + itemProperties.get(0).toUpperCase() + ");";
    }
}
