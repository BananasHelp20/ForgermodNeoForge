package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSpecialSwordItem implements InterpretedItem {
    ArrayList<String> itemProperties;
    ArrayList<String> enchantmentExtras;
    ArrayList<String> specified;
    String rarity = "";
    public InterpretedSpecialSwordItem(String name, String properties, String itemCreationMethod, String modelMethod, ArrayList<String> inSpecifyBrackets, String material, String creativeTab) {
        itemProperties = new ArrayList<>(Arrays.asList(name, (properties.contains("!ULTRA") ? "999999999, 0.1f" : properties), itemCreationMethod, modelMethod, material, creativeTab));
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile, "#"), itemProperties.get(0));
        specified = inSpecifyBrackets;
        if (itemProperties.get(2).contains("WithRarity"))
            this.rarity = specified.getFirst();
    }

    @Override
    public String toString() {
        return "    public static final DeferredItem<SwordItem> " + itemProperties.get(0).toUpperCase() + " = " + itemProperties.get(2) + "(\"" + itemProperties.get(0).toLowerCase() + "\", ModToolTiers." + itemProperties.get(4).toUpperCase() + ", "
                + (rarity.isEmpty() ? "" : "Rarity." + rarity.toUpperCase() + ", ") + itemProperties.get(1) + (itemProperties.get(2).toUpperCase().contains("DESCRIPTION") ? ", \"tooltips.forgermod." + itemProperties.getFirst() + ".tooltip\"" : "") + ");";
    }

    @Override
    public ArrayList<String> getTagsOfItem() {
        return enchantmentExtras;
    }

    public ArrayList<String> getItemEnchantmentTagsList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i < enchantmentExtras.size(); i++) {
            list.add(enchantmentExtras.get(i) + ":                .add(ModItems." + itemProperties.get(0).toUpperCase() + ".get())");
        }
        return list;
    }

    public String getCreativeTab() {
        return "                ModItems." + itemProperties.get(0).toUpperCase() + ".get(),";
    }

    public String getItemModel() {
        if (!this.itemProperties.get(3).contains("!NO_MODEL")) {
            return "        " + itemProperties.get(3) + "(ModItems." + itemProperties.get(0).toUpperCase() + ");";
        } else {
            return "";
        }
    }
}
