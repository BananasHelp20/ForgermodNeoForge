package net.bananashelp20.forgermod.registryInterpreter.interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class WilliCodeGenerator {
    public static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/blocks.willi");
    public static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/items.willi");
    public static File creativeTabFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/creativeTabs.willi");
    public static File upgradeList = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/itemUpgradeList.txt");
    public static File recipeFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/recipes.willi");
    public static File toolTiersFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/toolTiers.willi");

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) throws FileNotFoundException {
        try {
            if (!generateCode()) {
                throw new FileNotFoundException(ANSI_RED + "Code could not be generated, an Error occurred" + ANSI_RESET);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static boolean generateCode() {
        if (!(recipeFile.exists() && recipeFile.canWrite() && recipeFile.canRead()
                && toolTiersFile.exists() && toolTiersFile.canWrite() && toolTiersFile.canRead()
                && upgradeList.exists() && upgradeList.canWrite() && upgradeList.canRead()
                && blockFile.exists() && blockFile.canRead()
                && itemFile.exists() && itemFile.canRead()
                && creativeTabFile.exists() && creativeTabFile.canRead()
        )) {
            return false;
        }
        Scanner s = new Scanner(System.in);
        String line = "";
        boolean running = true;
        while (running) {
            System.out.println(ANSI_YELLOW + "What do you want to add?\nOptions are:\n - Blocks\n - Items\n - Creative Tabs\n - Upgrades\n - Recipes\n - Tool Tiers" + ANSI_RESET);
            System.out.println();
            System.out.print(ANSI_CYAN + "#USER>" + ANSI_RESET);
            line = s.nextLine();
            System.out.println();
            if (line.charAt(0) == 'B' || line.charAt(0) == 'b') {
                //blocks
            } else if (line.trim().charAt(0) == 'I' || line.trim().charAt(0) == 'i') {
                System.out.println(ANSI_YELLOW + "Enter an ItemType (simple, special, simple sword, special sword, upgradeable sword, set)\n" + ANSI_RESET);
                System.out.print(ANSI_CYAN + "#USER>" + ANSI_RESET);
                System.out.println();
            } else if (line.charAt(0) == 'C' || line.charAt(0) == 'c') {

            } else if (line.charAt(0) == 'U' || line.charAt(0) == 'u') {

            } else if (line.charAt(0) == 'R' || line.charAt(0) == 'r') {

            } else if (line.charAt(0) == 'T' || line.charAt(0) == 't') {

            }
            if (line.equals("!STOP"))
                running = false;
        }
//        generateToolTiers();
//        generateAndWriteItemCode();
//        generateAndWriteBlockCode();
//        generateAndWriteCreativeTabs();
//        generateAndWriteRecipes();

        return true;
    }
}
