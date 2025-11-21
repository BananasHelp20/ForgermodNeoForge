package net.bananashelp20.forgermod.registryInterpreter.WilliCodeGenerator;

import net.minecraft.world.level.block.FenceGateBlock;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
        ArrayList<ArrayList<String>> stringObjects = new ArrayList<>();
        ArrayList<String> stringObject;
        ArrayList<String> upgrades = new ArrayList<>();
        boolean running = true;
        boolean subRunning;
        System.out.println(ANSI_YELLOW + "What do you want to add?\nOptions are:\n - Blocks\n - Items\n - Creative Tabs\n - Upgrades\n - Recipes\n - Tool Tiers\n" + ANSI_RESET);
        while (running) {
            line = userInputWithoutLineBreak(s);
            subRunning = true;
            if (line.contains("!STOP")) running = false;
            if (!line.isEmpty()) {
                if (line.charAt(0) == 'B' || line.charAt(0) == 'b') {
                    stringObject = new ArrayList<>();
                    System.out.println(ANSI_YELLOW + "Enter a Block Type (simple, special, complex)" + ANSI_RESET);
                    while (subRunning) {
                        line = userInputWithoutLineBreak(s, "block").trim();
                        if (line.equalsIgnoreCase("simple")) {
                            stringObject.add("{simple");
                            warning("Enter a block Name");
                            line = userInputWithoutLineBreak(s, "block[simple]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the block Properties (all in one line)");
                            line = userInputWithoutLineBreak(s, "block[simple]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the block drop method (dropSelf, dropOther)");
                            line = userInputWithoutLineBreak(s, "block[simple]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            if (line.equals("dropOther")) {
                                warning("Enter the block drop (e.g. 'mod item jade_gemstone')");
                                line = userInputWithoutLineBreak(s, "block[simple]");
                                if (!line.equals("!RETURN")) {
                                    stringObject.add("?" + line);
                                } else {
                                    break;
                                }
                            }
                            warning("Enter the block model method (blockWithItem)");
                            line = userInputWithoutLineBreak(s, "block[simple]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the tool to break the block (e.g. 'pickaxe', or 'axe')");
                            line = userInputWithoutLineBreak(s, "block[simple]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the minimum material of the tool needed (e.g. 'steel')");
                            line = userInputWithoutLineBreak(s, "block[simple]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the creative tab the block belongs to (blocks, items, materials)");
                            line = userInputWithoutLineBreak(s, "block[simple]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            stringObject.add("}");
                        } else if (line.equalsIgnoreCase("special")) {
                            stringObject.add("{special");
                            warning("Enter a block Name");
                            line = userInputWithoutLineBreak(s, "block[special]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the block Properties (all in one line, as code segment)");
                            line = userInputWithoutLineBreak(s, "block[special]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the block drop method (dropSelf, dropOther)");
                            line = userInputWithoutLineBreak(s, "block[special]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            if (line.equals("dropOther")) {
                                warning("Enter the block drop (e.g. 'mod item jade_gemstone')");
                                line = userInputWithoutLineBreak(s, "block[special]");
                                if (!line.equals("!RETURN")) {
                                    stringObject.add("?" + line);
                                } else {
                                    break;
                                }
                            }
                            warning("Enter the block model method (blockWithItem)");
                            line = userInputWithoutLineBreak(s, "block[special]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the tool to break the block (e.g. 'pickaxe', or 'axe')");
                            line = userInputWithoutLineBreak(s, "block[special]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the minimum material of the tool needed (e.g. 'steel')");
                            line = userInputWithoutLineBreak(s, "block[special]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the creative tab the block belongs to (blocks, items, materials)");
                            line = userInputWithoutLineBreak(s, "block[special]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            stringObject.add("}");
                        } else if (line.equalsIgnoreCase("complex")) {
                            stringObject.add("{complex");
                            warning("Enter a block Name");
                            line = userInputWithoutLineBreak(s, "block[complex]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the block create method (e.g. createBlockWithDescription)");
                            line = userInputWithoutLineBreak(s, "block[complex]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the block Properties (all in one line)");
                            line = userInputWithoutLineBreak(s, "block[complex]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the block drop method (dropSelf, dropOther)");
                            line = userInputWithoutLineBreak(s, "block[complex]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            if (line.equals("dropOther")) {
                                warning("Enter the block drop (e.g. 'mod item jade_gemstone')");
                                line = userInputWithoutLineBreak(s, "block[complex]");
                                if (!line.equals("!RETURN")) {
                                    stringObject.add("?" + line);
                                } else {
                                    break;
                                }
                            }
                            warning("Enter the block model method (blockWithItem)");
                            line = userInputWithoutLineBreak(s, "block[complex]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the tool to break the block (e.g. 'pickaxe', or 'axe')");
                            line = userInputWithoutLineBreak(s, "block[complex]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the minimum material of the tool needed (e.g. 'steel')");
                            line = userInputWithoutLineBreak(s, "block[complex]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            warning("Enter the creative tab the block belongs to (blocks, items, materials)");
                            line = userInputWithoutLineBreak(s, "block[complex]");
                            if (!line.equals("!RETURN")) {
                                stringObject.add(line);
                            } else {
                                break;
                            }
                            stringObject.add("}");
                        }
                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }

                    }
                } else if (line.trim().charAt(0) == 'I' || line.trim().charAt(0) == 'i') {
                    stringObject = new ArrayList<>();
                    System.out.println(ANSI_YELLOW + "Enter an ItemType (simple, special, simple sword, special sword, upgradeable sword, set)" + ANSI_RESET);
                    line = userInputWithoutLineBreak(s, "item").trim();
                    while (subRunning) {
                        if (line.equalsIgnoreCase("simple")) {
                        } else if (line.equalsIgnoreCase("special")) {
                        } else if (line.replace(" ", "").equalsIgnoreCase("simpleSword")) {
                        } else if (line.replace(" ", "").equalsIgnoreCase("specialSword")) {
                        } else if (line.equalsIgnoreCase("upgradeable") || line.equalsIgnoreCase("upgradable") || line.replace(" ", "").equalsIgnoreCase("upgradeableSword") || line.replace(" ", "").equalsIgnoreCase("upgradableSword")) {
                        } else if (line.equalsIgnoreCase("set")) {
                        }

                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }
                    }
                } else if (line.charAt(0) == 'C' || line.charAt(0) == 'c') {
                    stringObject = new ArrayList<>();
                    System.out.println(ANSI_YELLOW + "Enter a Creative Tab Type (currently only 'simple' exists)" + ANSI_RESET);
                    while (subRunning) {
                        line = userInputWithoutLineBreak(s, "creative_tab").trim();
                        if (line.equalsIgnoreCase("simple")) {
                            subRunning = false;
                        }
                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }
                    }
                } else if (line.charAt(0) == 'U' || line.charAt(0) == 'u') {
                    System.out.println(ANSI_YELLOW + "Enter an Item Upgrade" + ANSI_RESET);
                    upgrades.add(userInputWithoutLineBreak(s, "upgrade").trim().replace(" ", "_").toLowerCase());
                } else if (line.charAt(0) == 'R' || line.charAt(0) == 'r') {
                    stringObject = new ArrayList<>();
                    System.out.println(ANSI_YELLOW + "Enter an Recipe Type (smelting, blasting, both (smelting and blasting), shaped, shapeless, custom)" + ANSI_RESET);
                    while (subRunning) {
                        line = userInputWithoutLineBreak(s, "recipe").trim().replace(" ", "");
                        if (line.equalsIgnoreCase("smelting")) {
                            subRunning = false;
                        } else if (line.equalsIgnoreCase("blasting")) {
                            subRunning = false;
                        } else if (line.equalsIgnoreCase("both") || line.equalsIgnoreCase("smeltingandblasting") || line.equalsIgnoreCase("smelting&blasting") || line.equalsIgnoreCase("smelting+blasting")) {
                            subRunning = false;
                        } else if (line.equalsIgnoreCase("shaped")) {
                            subRunning = false;
                        } else if (line.equalsIgnoreCase("shapeless")) {
                            subRunning = false;
                        } else if (line.equalsIgnoreCase("custom")) {

                        }
                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }
                    }
                } else if (line.charAt(0) == 'T' || line.charAt(0) == 't') {
                    stringObject = new ArrayList<>();
                    System.out.println(ANSI_YELLOW + "Enter an Tool Tier Type (currently only 'normal' exists)" + ANSI_RESET);
                    while (subRunning) {
                        line = userInputWithoutLineBreak(s).trim();
                        if (line.equalsIgnoreCase("normal")) {
                            subRunning = false;
                        }
                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }
                    }
                }
            }
        }

//        generateToolTier();
//        generateItem();
//        generateBlock();
//        generateCreativeTab();
//        generateRecipe();

        return true;
    }

    public static String userInput(Scanner s) {
        System.out.println();
        System.out.print(ANSI_CYAN + "#USER> " + ANSI_RESET);
        String line = s.nextLine();
        System.out.println();
        return line;
    }

    public static String userInputWithoutLineBreak(Scanner s) {
        System.out.print(ANSI_CYAN + "#USER> " + ANSI_RESET);
        String line = s.nextLine();
        return line;
    }

    public static String userInputWithoutLineBreak(Scanner s, String addOn) {
        System.out.print(ANSI_CYAN + "#USER@" + addOn.toUpperCase() + "> " + ANSI_RESET);
        String line = s.nextLine();
        return line;
    }

    public static void success(String msg) {
        System.out.println(ANSI_GREEN + msg + ANSI_RESET);
    }

    public static void error(String msg) {
        System.out.println(ANSI_RED + msg + ANSI_RESET);
    }

    public static void warning(String msg) {
        System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
    }
}
