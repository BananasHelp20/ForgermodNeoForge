package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSimpleItem extends InterpretedItem {
    String name;
    String modelMethod;
    String creativeTab;
    ArrayList<String> enchantmentExtras;
    public InterpretedSimpleItem(String name, String modelMethod, String creativeTab) {
        super(new ArrayList<>(Arrays.asList(name, modelMethod, creativeTab)));
        this.enchantmentExtras = RegistryInterpreter.getEnchantmentablesFromOptionalParameter(RegistryInterpreter.getContentFromFileAsList(RegistryInterpreter.itemFile, "#"), name);
        this.name = name;
        this.modelMethod = modelMethod;
        this.creativeTab = creativeTab;
    }

    @Override
    public String toString() {
        return "    public static final DeferredItem<Item> " + name.toUpperCase() + " = createItem(\"" + name.toLowerCase() + "\");";
    }

    public ArrayList<String> getItemEnchantmentTagsList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i < enchantmentExtras.size(); i++) {
            list.add(enchantmentExtras.get(i) + ":                .add(ModItems." + name.toUpperCase() + ".get())");
        }
        return list;
    }

    public String getCreativeTab() {
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
