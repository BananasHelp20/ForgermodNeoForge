package net.bananashelp20.forgermod.registryInterpreter.registries.interpretedObjects.creativeTabs;

import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.world.level.ItemLike;

public class InterpretedCreativeTab {
    public String name; //forger_ingredients_tab
    public String camelCaseName; //ForgerIngredientsTab
    public String displayItemName; //jade_gemstone
    public String displayItemType; //Items., Blocks., ModBlocks., ModItems.

    public InterpretedCreativeTab(String name, String camelCaseName, String displayItemName, String displayItemType) {
        this.camelCaseName = camelCaseName; //BeginsBig
        this.name = name;
        this.displayItemName = displayItemName;
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
                "                    }).build());";
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