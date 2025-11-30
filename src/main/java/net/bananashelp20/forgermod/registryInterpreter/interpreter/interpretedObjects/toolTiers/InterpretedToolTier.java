package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.toolTiers;

public class InterpretedToolTier {
    String tierCode;
    String tagCode;
    public InterpretedToolTier(String ingredientName, String material, String tierProperties) {
        this.tierCode = "    public static final Tier " + material.toUpperCase() + " = new SimpleTier(\n" +
                "            ModTags.Blocks.INCORRECT_FOR_" + material.toUpperCase() + "_TOOL,\n" +
                "            " + tierProperties + ",\n" +
                "            () -> Ingredient.of(ModItems." + ingredientName.toUpperCase() + ".get())\n" +
                "    );";
        this.tagCode = "        public static final TagKey<Block> NEEDS_" + material.toUpperCase() + "_TOOL = createTag(\"needs_" + material.toLowerCase() + "_tool\");\n" +
                "        public static final TagKey<Block> INCORRECT_FOR_" + material.toUpperCase() + "_TOOL = createTag(\"incorrect_for_" + material.toLowerCase() + "_tool\");";
    }

    @Override
    public String toString() {
        return tierCode + "\n" + tagCode + "\n";
    }

    public String getTierCode() {
        return tierCode + "\n";
    }

    public String getTags() {
        return tagCode + "\n";
    }
}
