package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;

import java.util.ArrayList;
import java.util.Arrays;

public class InterpretedSpecialBlock implements InterpretedBlock {
    ArrayList<String> blockProperties;
    public InterpretedSpecialBlock(String name, String creationMethod, String properties, String dropMethod, String dropsItem, String modelMethod, String toolTag, String typeTag, String creativeTab) {
        blockProperties = new ArrayList<>(Arrays.asList(name, creationMethod, (properties.contains("!INDESTRUCTABLE") ? "-1.0f, 3600000.0f" : properties), dropMethod, dropsItem, modelMethod, toolTag, typeTag, creativeTab));
    }

    @Override
    public String toString() {
        return "    public static final DeferredBlock<Block> "
                + blockProperties.get(0).toUpperCase()
                + " = " + blockProperties.get(1) + "(\""
                + blockProperties.get(0).toUpperCase().toLowerCase()
                + "\", " + blockProperties.get(2) + ");";
    }

    public String getTag() {
        return "                .add(ModBlocks." + blockProperties.get(0).toUpperCase() + ".get())";
    }

    public String getTagTool() {
        return blockProperties.get(6);
    }

    public String getTagType() {
        return blockProperties.get(7);
    }

    public String getBlockState() {
        return (blockProperties.get(5).contains("!NO_MODEL")) ? "" : "        " + blockProperties.get(5) + "(ModBlocks." + blockProperties.get(0).toUpperCase() + ");";
    }

    public String getLoottable() {
        if (blockProperties.get(4).isEmpty()) {
            return "        " + blockProperties.get(3) + "(ModBlocks." + blockProperties.get(0).toUpperCase() + ".get());";
        }
        String drops = "        " + blockProperties.get(3) + "(ModBlocks." + blockProperties.get(0).toUpperCase() + ".get(), ";
        String[] dropsItem = blockProperties.get(4).split(" ");
        if (dropsItem[0].contains("mod")) {
            drops += "Mod";
        }
        if (dropsItem[0].contains("block")) {
            drops += "Blocks.";
        } else {
            drops += "Items.";
        }
        drops += dropsItem[1].toUpperCase().trim() + (dropsItem[0].contains("mod") ? ".get());" : ");");
        return drops;
    }

    public String getCreativeTab() {
        return "                ModBlocks." + blockProperties.get(0).toUpperCase() + ".get(),";
    }
}
