package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks;

import java.util.ArrayList;

public interface InterpretedBlock {
    String getTag();
    String getLoottable();
    String getBlockState();
    String getCreativeTab();
    String getTagTool();
    String getTagType();
}
