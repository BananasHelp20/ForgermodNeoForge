package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSpecialItem extends InterpretedItem {
    String name;
    String modelMethod;
    ArrayList<String> enchantmentExtras;
    String rarity = "";
    public InterpretedSpecialItem(String name, String modelMethod) {
        super(new ArrayList<>(Arrays.asList(name, modelMethod)));
        this.name = name;
        this.modelMethod = modelMethod;
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile), name);
    }

    public InterpretedSpecialItem(String name, String modelMethod, String rarity) {
        super(new ArrayList<>(Arrays.asList(name, modelMethod)));
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile), name);
        this.name = name;
        this.modelMethod = modelMethod;
        this.rarity = rarity;
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

    public String getItemModel() {
        return "        " + modelMethod + "(ModItems." + name.toUpperCase() + ");";
    }
}
