package net.bananashelp20.forgermod.registryInterpreter.registries.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.registries.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSpecialItem extends InterpretedItem {
    String name;
    String modelMethod;
    public InterpretedSpecialItem(String name, String modelMethod) {
        super(new ArrayList<>(Arrays.asList(name, modelMethod)));
        this.name = name;
        this.modelMethod = modelMethod;
    }

    @Override
    public String toString() {
        return "    public static final DeferredItem<Item> " + name.toUpperCase() + " = createItemWithDescription(\"" + name.toLowerCase() + "\", \"tooltips.forgermod." + name.toLowerCase() + ".tooltip\");";
    }

    public String getItemModel() {
        return "        " + modelMethod + "(ModItems." + name.toUpperCase() + ");";
    }
}
