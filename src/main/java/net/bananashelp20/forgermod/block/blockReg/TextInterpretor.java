package net.bananashelp20.forgermod.block.blockReg;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class TextInterpretor {
    static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/block/blockreg/blocks.txt");
    static File modBlocksFile = new File("./src/main/bananashelp20/forgermod/block/ModBlocks.java");
    static File itemFile = new File("./items.txt");
    static File modItemsFile = new File("../../item/ModItems.java");
    static File creativeTabFile = new File("./creativeTabs.txt");
    static File modCreativeModeTabs = new File("../../CreativeModeTabs/ModCreativeModeTabs.java");
    static File modRegistry = new File("../../RegistryClass.java");

    public static void main(String[] args) throws Exception {
        if (!generateCode()) {
            throw new Exception("Code could not be generated, an Error occurred");
        }

        System.out.println(blockFile.exists());
        System.out.println(blockFile.canRead());
        System.out.println(blockFile.list());
        System.out.println();
//        System.out.println(getWritablePos(modItemsFile.list()));
    }

    static int getWritablePos(String[] ar) {
        ArrayList<String> arList = new ArrayList<String>(Arrays.asList(ar));
        for (int i = 0; i < arList.size(); i++) {
            if (arList.get(i).substring(0, 17).equals("//STARTGENERATING")) {
                System.err.println(getWritablePos(modItemsFile.list()));
                return i+1;
            }
        }
        return 0;
    }

    public static boolean generateCode() {
        return true;
    }

    public static String generateSimpleBlocks() {
        return "";
    }

    public static String generateComplexBlocks() {
        return "";
    }

    public static String generateCreativeModeTabs() {
        return "";
    }

    public static String generateSimpleItems() {
        return "";
    }

    public static String generateComplexItems() {
        return "";
    }

    public static String generateGemstoneItems() {
        return "";
    }

}
