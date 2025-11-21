package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedComplexBlock extends InterpretedBlock {
    ArrayList<String> blockProperties;
    public InterpretedComplexBlock(String name, String properties, String dropMethod, String dropsItem, String modelMethod, String toolTag, String typeTag, String creativeTab) {
        super(new ArrayList<>(Arrays.asList(name, properties, dropMethod, dropsItem, modelMethod, toolTag, typeTag, creativeTab)));
        blockProperties = new ArrayList<>(Arrays.asList(name, (properties.contains("!INDESTRUCTABLE") ? "-1.0f, 3600000.0f" : properties), dropMethod, dropsItem, modelMethod, toolTag, typeTag, creativeTab));
    }

    @Override
    public String toString() {
        return "    public static final DeferredBlock<Block> "
                + blockProperties.get(0).toUpperCase()
                + " = registerBlock(\"" + blockProperties.get(0).toLowerCase()
                + "\",\n"
                + "            " + blockProperties.get(1)
                + "\n    );\n";
    }

    public String getTag() {
        return "                .add(ModBlocks." + blockProperties.get(0).toUpperCase() + ".get())";
    }

    public String getBlockState() {
        return (blockProperties.get(4).contains("!NO_MODEL")) ? "" : "        " + blockProperties.get(4) + "(ModBlocks." + blockProperties.get(0).toUpperCase() + ");";
    }

    public String getLoottable() {
        if (blockProperties.get(3).isEmpty()) {
            return "        " + blockProperties.get(2) + "(ModBlocks." + blockProperties.get(0).toUpperCase() + ".get());";
        }
        String drops = "        " + blockProperties.get(2) + "(ModBlocks." + blockProperties.get(0).toUpperCase() + ".get(), ";
        String[] dropsItem = blockProperties.get(3).split(" ");
        if (dropsItem[0].contains("mod")) {
            drops += "Mod";
        }
        if (dropsItem[1].contains("block")) {
            drops += "Blocks.";
        } else {
            drops += "Items.";
        }
        drops += dropsItem[2].toUpperCase().trim() + (dropsItem[0].contains("mod") ? ".get());" : ");");
        return drops;
    }

    public String getCreativeTab() {
        return "                ModBlocks." + blockProperties.get(0).toUpperCase() + ".get(),";
    }
}
