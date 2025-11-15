package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSpecialItem extends InterpretedItem {
    String name;
    String modelMethod;
    String rarity = "";
    boolean hasRarity = false;
    public InterpretedSpecialItem(String name, String modelMethod) {
        super(new ArrayList<>(Arrays.asList(name, modelMethod)));
        this.name = name;
        this.modelMethod = modelMethod;
    }

    public InterpretedSpecialItem(String name, String modelMethod, String rarity) {
        super(new ArrayList<>(Arrays.asList(name, modelMethod)));
        this.name = name.trim();
        this.modelMethod = modelMethod.trim();
        hasRarity = true;
        this.rarity = (rarity.trim().charAt(0) == '?' ? rarity.trim().substring(1) : rarity.trim());
    }

    @Override
    public String toString() {
        if (!hasRarity)
            return "    public static final DeferredItem<Item> " + name.trim().toUpperCase() + " = createItemWithDescription(\"" + name.trim().toLowerCase() + "\", \"tooltips.forgermod." + name.trim().toLowerCase() + ".tooltip\");";
        return "    public static final DeferredItem<Item> " + name.trim().toUpperCase() + " = createItemWithRarityAndDescription(\"" + name.trim().toLowerCase() + "\", \"tooltips.forgermod." + name.trim().toLowerCase() + ".tooltip\", Rarity." + rarity.trim().toUpperCase() + ");";
    }

    public String getItemModel() {
        return "        " + modelMethod + "(ModItems." + name.toUpperCase() + ");";
    }
}
