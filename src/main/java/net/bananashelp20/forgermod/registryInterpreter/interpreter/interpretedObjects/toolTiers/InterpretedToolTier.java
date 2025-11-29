package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.toolTiers;

public class InterpretedToolTier {
    String tierCode;
    public InterpretedToolTier(String ingredientName, String material, String tierProperties) {
        this.tierCode = "    public static final Tier " + material.toUpperCase() + " = new SimpleTier(\n" +
                "            ModTags.Blocks.INCORRECT_FOR_" + material.toUpperCase() + "_TOOL,\n" +
                "            " + tierProperties + ",\n" +
                "            () -> Ingredient.of(ModItems." + ingredientName.toUpperCase() + ".get())\n" +
                "    );";
    }

    @Override
    public String toString() {
        return tierCode + "\n";
    }

    public String getTierCode() {
        return tierCode + "\n";
    }
}
