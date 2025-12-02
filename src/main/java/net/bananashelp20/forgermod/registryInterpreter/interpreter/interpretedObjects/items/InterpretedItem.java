package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items;

import java.util.ArrayList;

public interface InterpretedItem {
    String toString();
    String getCreativeTabCode();
    String getItemModel();
    ArrayList<String> getItemEnchantmentTagsList();
    ArrayList<String> getTagsOfItem();
    String getItemTagCode();
    String getCreativeTab();
}
