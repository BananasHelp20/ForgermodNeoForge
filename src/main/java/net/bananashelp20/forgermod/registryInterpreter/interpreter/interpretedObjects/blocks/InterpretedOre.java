package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks;

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
}
