package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSimpleItem extends InterpretedItem {
    String name;
    String modelMethod;
    public InterpretedSimpleItem(String name, String modelMethod) {
        super(new ArrayList<>(Arrays.asList(name.trim(), modelMethod.trim())));
        this.name = name;
        this.modelMethod = modelMethod;
    }

    @Override
    public String toString() {
        return "    public static final DeferredItem<Item> " + name.toUpperCase() + " = createItem(\"" + name.toLowerCase() + "\");";
    }

    public String getItemModel() {
        return "        " + modelMethod + "(ModItems." + name.toUpperCase() + ");";
    }
}
