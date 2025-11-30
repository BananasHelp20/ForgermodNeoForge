package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items;

import java.util.ArrayList;

public interface InterpretedItem {
    String toString();
    String getCreativeTab();
    String getItemModel();
    ArrayList<String> getItemEnchantmentTagsList();
    ArrayList<String> getTagsOfItem();
}
