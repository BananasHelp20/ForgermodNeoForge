package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedItemWithUpgradedVariations extends InterpretedItem {
    ArrayList<String> itemProperties;
    ArrayList<String> variants;
    ArrayList<ArrayList<String>> enchantmentExtras;
    public InterpretedItemWithUpgradedVariations(String name, String itemClass, String itemCreationMethod, String modelMethod, String material, ArrayList<String> variants) {
        super(new ArrayList<>(Arrays.asList(name, itemClass, itemCreationMethod, modelMethod, material)));
        itemProperties = new ArrayList<>(Arrays.asList(name, itemClass, itemCreationMethod, modelMethod, material));
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile));
        this.variants = variants;
    }

    @Override
    public String toString() {
        String s  = "";
        for (int i = 0; i < variants.size(); i++) {
            s += "    public static final DeferredItem<SwordItem> " + itemProperties.get(0).toUpperCase() + "_" + variants.get(i).toUpperCase() + " = createSpecialSwordItem(\"" + itemProperties.get(0).toLowerCase() + "_" + variants.get(i).toLowerCase() + "\", () -> new " + itemProperties.get(1) + "(\"" + variants.get(i) + "\"));\n";
        }
        return s;
    }

    public String getItemModel() {
        String s = "";
        for (int i = 0; i < variants.size(); i++) {
            s += "        " + itemProperties.get(3) + "(ModItems." + itemProperties.get(0).toUpperCase() + "_" + variants.get(i).toUpperCase() + ");\n";
        }
        return s;
    }
}
