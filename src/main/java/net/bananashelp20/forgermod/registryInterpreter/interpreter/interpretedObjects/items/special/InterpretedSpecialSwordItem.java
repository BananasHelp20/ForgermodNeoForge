package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSpecialSwordItem extends InterpretedItem {
    ArrayList<String> itemProperties;
    ArrayList<ArrayList<String>> enchantmentExtras;
    public InterpretedSpecialSwordItem(String name, String properties, String itemCreationMethod, String modelMethod, String material) {
        super(new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material)));
        itemProperties = new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material));
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile), itemProperties.get(0));
    }

    public InterpretedSpecialSwordItem(String name, String properties, String itemCreationMethod, String modelMethod, String material, String rarity) {
        super(new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material)));
        itemProperties = new ArrayList<>(Arrays.asList(name, properties, itemCreationMethod, modelMethod, material, rarity));
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile), itemProperties.get(0));
    }

    @Override
    public String toString() {
        return "    public static final DeferredItem<SwordItem> " + itemProperties.get(0).toUpperCase() + " = " + itemProperties.get(2) + "(\"" + itemProperties.get(0).toLowerCase() + "\", ModToolTiers." + itemProperties.get(4).toUpperCase() + ", " + (itemProperties.size() == 6 ? "Rarity." + itemProperties.get(5).toUpperCase() + ", " : "") + itemProperties.get(1) + ");";
    }

    public ArrayList<String> getItemEnchantmentTagsList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < enchantmentExtras.size(); i++) {
            list.add(enchantmentExtras.get(i) + ":                .add(ModItems." + itemProperties.get(0).toUpperCase() + ".get())");
        }
        return list;
    }

    public String getItemModel() {
        return "        " + itemProperties.get(3) + "(ModItems." + itemProperties.get(0).toUpperCase() + ");";
    }
}
