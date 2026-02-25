package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks;

import java.util.ArrayList;

public interface InterpretedOre {
    String toString();
    String getPlacedFeatureRegistration();
    String getPlacedFeatureInitialisation();
    String getConfigurationList();
    String getConfiguredFeatureRegistration();
    String getConfiguredFeatureInitialisation();
    String getConfiguredFeatureRuleTests();
    String getBiomeModifierInitialisation();
    String getBiomeModifierRegistration();

    default ArrayList<String> getPlacedKeys(ArrayList<String> blockNames) {
        ArrayList<String> toRet = new ArrayList();
        for (int i = 0; i < blockNames.size(); i++) {
            toRet.add(blockNames.get(i).toUpperCase() + "_PLACED_KEY");
        }
        return toRet;
    }

    default ArrayList<String> getPrimaryKeys(ArrayList<String> blockNames) {
        ArrayList<String> toRet = new ArrayList();
        for (int i = 0; i < blockNames.size(); i++) {
            toRet.add(blockNames.get(i).toUpperCase() + "_KEY");
        }
        return toRet;
    }

    default ArrayList<String> getAddOres(ArrayList<String> blockNames) {
        ArrayList<String> toRet = new ArrayList();
        for (int i = 0; i < blockNames.size(); i++) {
            toRet.add("ADD_" + blockNames.get(i).toUpperCase());
        }
        return toRet;
    }
}
