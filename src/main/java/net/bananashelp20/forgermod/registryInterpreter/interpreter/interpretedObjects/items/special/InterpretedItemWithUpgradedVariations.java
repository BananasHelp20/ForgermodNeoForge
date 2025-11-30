package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.io.File;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class InterpretedItemWithUpgradedVariations implements InterpretedItem {
    ArrayList<String> itemProperties;
    ArrayList<String> variants;
    ArrayList<String> enchantmentExtras;
    public InterpretedItemWithUpgradedVariations(String name, String itemClass, String itemCreationMethod, String modelMethod, String material, ArrayList<String> variants, String creativeTab) {
        itemProperties = new ArrayList<>(Arrays.asList(name, itemClass, itemCreationMethod, modelMethod, material, creativeTab));
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile, "#"), itemProperties.get(0));
        boolean hasValue = false;
        for (int i = 0; i < variants.size(); i++) {
            if (variants.get(i).trim().contains("!ALL")) {
                this.variants = RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.upgradeList, "#");
                RegistryInterpreter.clearContentFromUnneccesary(this.variants, "#");
                hasValue = true;
            }
        }
        if (!hasValue) {
            this.variants = variants;
        }
    }

    @Override
    public ArrayList<String> getTagsOfItem() {
        return enchantmentExtras;
    }

    @Override
    public String toString() {
        String s  = "";
        for (int i = 0; i < variants.size(); i++) {
            s += "    public static final DeferredItem<SwordItem> " + itemProperties.get(0).toUpperCase() + (variants.get(i).equals("none") ? "" : "_" + variants.get(i).toUpperCase()) + " = createSpecialSwordItem(\"" + itemProperties.get(0).toLowerCase() + (variants.get(i).equals("none") ? "" : "_" + variants.get(i).toLowerCase()) + "\", () -> new " + itemProperties.get(1) + "(\"" + (variants.get(i).equals("none") ? "no_gemstone" : variants.get(i)) + "\"));\n";
        }
        return s;
    }

    public String getCreativeTab() {
        return "                ModItems." + itemProperties.get(0).toUpperCase() + ".get(),";
    }

    public String getItemModel() {
        String s = "";
        if (!this.itemProperties.get(3).contains("!NO_MODEL")) {
            for (int i = 0; i < variants.size(); i++) {
                s += "        " + itemProperties.get(3) + "(ModItems." + itemProperties.get(0).toUpperCase() + (variants.get(i).equals("none") ? "" : "_" + variants.get(i).toUpperCase()) + ".get());\n";
            }
        }
        return s;
    }

    public ArrayList<String> getItemEnchantmentTagsList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < enchantmentExtras.size(); i++) {
            for (int j = 0; j < variants.size(); j++) {
                list.add(enchantmentExtras.get(i) + ":                .add(ModItems." + itemProperties.get(0).toUpperCase() + (variants.get(i).equals("none") ? "" : "_" + variants.get(i).toUpperCase()) + ".get())");
            }
        }
        return list;
    }
}
