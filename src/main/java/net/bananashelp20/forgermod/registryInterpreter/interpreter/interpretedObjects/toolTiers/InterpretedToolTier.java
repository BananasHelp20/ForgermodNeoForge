package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.toolTiers;

public class InterpretedToolTier {
    String tierCode;
    String tagCode;
    public InterpretedToolTier(String ingredientName, String material, String tierProperties) {
        this.tierCode = "    public static final Tier " + material.toUpperCase().trim() + " = new SimpleTier(\n" +
                "            ModTags.Blocks.INCORRECT_FOR_" + material.toUpperCase().trim() + "_TOOL,\n" +
                "            " + tierProperties.trim() + ",\n" +
                "            () -> Ingredient.of(ModItems." + ingredientName.toUpperCase().trim() + ".get())\n" +
                "    );";
        this.tagCode = "        public static final TagKey<Block> NEEDS_" + material.toUpperCase().trim() + "_TOOL = createTag(\"needs_" + material.toLowerCase().trim() + "_tool\");\n" +
                "        public static final TagKey<Block> INCORRECT_FOR_" + material.toUpperCase().trim() + "_TOOL = createTag(\"incorrect_for_" + material.toLowerCase().trim() + "_tool\");";
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
