package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.creativeTabs;

public class InterpretedCreativeTab {
    String name; //forger_ingredients_tab
    String camelCaseName; //ForgerIngredientsTab
    String displayItemName; //jade_gemstone
    String displayItemType; //Items., Blocks., ModBlocks., ModItems.

    public InterpretedCreativeTab(String name, String camelCaseName, String displayItemName) {
        this.camelCaseName = camelCaseName; //BeginsBig
        this.name = name;
        String[] temp = displayItemName.split(" ");
        this.displayItemName = temp[0];
        if (temp[1].toUpperCase().contains("MODITEM")) {
            this.displayItemType = "ModItems.";
        } else if (temp[1].toUpperCase().contains("MODBLOCK")) {
            this.displayItemType = "ModBlocks.";
        } else if (temp[1].toUpperCase().contains("ITEM")) {
            this.displayItemType = "Items.";
        } else {
            this.displayItemType = "Blocks.";
        }
    }

    @Override
    public String toString() {
        return "    public static final Supplier<CreativeModeTab> " + name.toUpperCase() + " = CREATIVE_MODE_TABS.register(\"" + name.toLowerCase() + "\",\n" +
                "            () -> CreativeModeTab.builder()\n" +
                "                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemFor" + camelCaseName + "()))\n" +
                "                    .title(Component.translatable(\"creativetab.forgermod." + name.toLowerCase() + "\"))\n" +
                "                    .displayItems((itemDisplayParameters, output) -> {\n" +
                "                        ItemLike[] register = RegistryClass.get" + camelCaseName + "Register();\n" +
                "                        for (int i = 0; i < register.length; i++) {\n" +
                "                            output.accept(register[i]);\n" +
                "                        }\n" +
                "                    }).build()\n" +
                "    );\n";
    }

    public String getRegistryMethods() {
        return "public static ItemLike getDisplayItemFor" + camelCaseName + "() {\n" +
                "    return " + displayItemType + displayItemName.toUpperCase() + (displayItemType.equalsIgnoreCase("Items.") || displayItemType.equalsIgnoreCase("Blocks.") ? "" : ".get()") + ";\n" +
                "}\n" +
                "\n" +
                "public static ItemLike[] get" + camelCaseName + "Register() {\n" +
                "    return new ItemLike[] {\n" +
                "\n" +
                "    };\n" +
                "}\n";
    }
}