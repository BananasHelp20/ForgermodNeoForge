package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSpecialItem implements InterpretedItem {
    String name;
    String modelMethod;
    ArrayList<String> enchantmentExtras;
    String rarity = "";
    String creativeTab;
    public InterpretedSpecialItem(String name, String modelMethod, String creativeTab) {
        this.name = name;
        this.modelMethod = modelMethod;
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile, "#"), name);
        this.creativeTab = creativeTab;
    }

    public InterpretedSpecialItem(String name, String modelMethod, String rarity, String creativeTab) {
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile, "#"), name);
        this.name = name;
        this.modelMethod = modelMethod;
        this.rarity = rarity;
        this.creativeTab = creativeTab;
    }

    @Override
    public ArrayList<String> getTagsOfItem() {
        return enchantmentExtras;
    }

    @Override
    public String getItemTagCode() {
        return "                .add(ModItems." + name.toUpperCase() + ".get())";
    }

    @Override
    public String getCreativeTab() {
        return this.creativeTab;
    }

    @Override
    public String toString() {
        if (rarity.equals(""))
            return "    public static final DeferredItem<Item> " + name.toUpperCase() + " = createItemWithDescription(\"" + name.toLowerCase() + "\", \"tooltips.forgermod." + name.toLowerCase() + ".tooltip\");";
        return "    public static final DeferredItem<Item> " + name.toUpperCase() + " = createItemWithRarityAndDescription(\"" + name.toLowerCase() + "\", \"tooltips.forgermod." + name.toLowerCase() + ".tooltip\", Rarity." + rarity.toUpperCase() + ");";
    }

    public ArrayList<String> getItemEnchantmentTagsList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i < enchantmentExtras.size(); i++) {
            list.add(enchantmentExtras.get(i) + ":                .add(ModItems." + name.toUpperCase() + ".get())");
        }
        return list;
    }

    public String getCreativeTabCode() {
        return "                ModItems." + name.toUpperCase() + ".get(),";
    }

    public String getItemModel() {
        if (!this.modelMethod.contains("!NO_MODEL")) {
            return "        " + modelMethod + "(ModItems." + name.toUpperCase() + ");";
        } else {
            return "";
        }
    }
}
