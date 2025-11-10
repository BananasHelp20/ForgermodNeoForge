package net.bananashelp20.forgermod.registryInterpreter.registries.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.registries.interpretedObjects.items.InterpretedItem;
import net.bananashelp20.forgermod.registryInterpreter.registries.interpretedObjects.toolTiers.InterpretedToolTier;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSwordItem extends InterpretedItem {
    ArrayList<String> itemProperties;
    public InterpretedSwordItem(String name, String properties, String itemCreationMethod, String modelMethod, String material) {
        super(new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material)));
        itemProperties = new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material));
    }

    @Override
    public String toString() {
        return "    public static final DeferredItem<SwordItem> " + itemProperties.get(0).toUpperCase() + " = createSwordItem(\"" + itemProperties.get(0).toLowerCase() + "\", ModToolTiers." + itemProperties.get(4).toUpperCase() + ", " + itemProperties.get(1) + ");";
    }

    public String getItemModel() {
        return "        " + itemProperties.get(3) + "(ModItems." + itemProperties.get(0).toUpperCase() + ");";
    }
}
