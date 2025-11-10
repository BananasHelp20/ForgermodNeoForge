package net.bananashelp20.forgermod.registryInterpreter.registries.interpretedObjects.items;

import net.bananashelp20.forgermod.registries.RegistryInterpreterHelper;
import net.bananashelp20.forgermod.registryInterpreter.registries.interpretedObjects.toolTiers.InterpretedToolTier;

import java.util.ArrayList;

public class InterpretedItem {
    ArrayList<String> itemProperties;

    public InterpretedItem(ArrayList<String> itemProperties) {
        this.itemProperties = itemProperties;
    }
}
