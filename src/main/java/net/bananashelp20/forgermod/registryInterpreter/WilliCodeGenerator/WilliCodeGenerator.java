package net.bananashelp20.forgermod.registryInterpreter.WilliCodeGenerator;

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

    static boolean running;
    static boolean subRunning;
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
        running = true;
        while (running) {
            System.out.println(ANSI_YELLOW + "What do you want to add?\nOptions are:\n - Blocks\n - Items\n - Creative Tabs\n - Upgrades\n - Recipes\n - Tool Tiers\n" + ANSI_RESET);
            line = userInputWithoutLineBreak(s);
            subRunning = true;
            if (line.contains("!STOP")) running = false;
            if (!line.isEmpty()) {
                if (line.charAt(0) == 'B' || line.charAt(0) == 'b') {
                    while (subRunning) {
                        stringObject = new ArrayList<>();
                        System.out.println(ANSI_YELLOW + "Enter a Block Type (simple, special, complex)" + ANSI_RESET);
                        line = userInputWithoutLineBreak(s, "block").trim();
                        if (line.equalsIgnoreCase("simple")) {
                            stringObject.add("{simple");
                            checkReturnAndInput("Enter a block Name", s, "block[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block Properties (all in one line)", s, "block[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block drop method (dropSelf, dropOther)", s, "block[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block drop (e.g. 'mod item jade_gemstone')", s, "block[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block model method (blockWithItem)", s, "block[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the tool to break the block (e.g. 'pickaxe', or 'axe')", s, "block[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the minimum material of the tool needed (e.g. 'steel')", s, "block[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the creative tab the block belongs to (blocks, items, materials)", s, "block[simple]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        } else if (line.equalsIgnoreCase("special")) {
                            stringObject.add("{special");
                            checkReturnAndInput("Enter a block Name", s, "block[special]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter a block Name", s, "block[special]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block Properties (all in one line, as code segment)", s, "block[special]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block drop method (dropSelf, dropOther)", s, "block[special]", stringObject);
                            if (!subRunning) break;
                            if (stringObject.getLast().equals("dropOther")) {
                                checkReturnAndInput("Enter the block drop (e.g. 'mod item jade_gemstone')", s, "block[special]", stringObject, true);
                                if (!subRunning) break;
                            }
                            checkReturnAndInput("Enter the block model method (blockWithItem)", s, "block[special]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the tool to break the block (e.g. 'pickaxe', or 'axe')", s, "block[special]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the minimum material of the tool needed (e.g. 'steel')", s, "block[special]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the creative tab the block belongs to (blocks, items, materials)", s, "block[special]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        } else if (line.equalsIgnoreCase("complex")) {
                            stringObject.add("{complex");
                            checkReturnAndInput("Enter a block Name", s, "block[complex]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block create method (e.g. createBlockWithDescription)", s, "block[complex]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block Properties (all in one line)", s, "block[complex]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the block drop method (dropSelf, dropOther)", s, "block[complex]", stringObject);
                            if (!subRunning) break;
                            if (stringObject.getLast().equals("dropOther")) {
                                checkReturnAndInput("Enter the block drop (e.g. 'mod item jade_gemstone')", s, "block[special]", stringObject, true);
                                if (!subRunning) break;
                            }
                            checkReturnAndInput("Enter the block model method (blockWithItem)", s, "block[complex]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the tool to break the block (e.g. 'pickaxe', or 'axe')", s, "block[complex]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the minimum material of the tool needed (e.g. 'steel')", s, "block[complex]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the creative tab the block belongs to (blocks, items, materials)", s, "block[complex]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }
                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }
                        stringObjects.add(stringObject);
                    }
                }
                else if (line.trim().charAt(0) == 'I' || line.trim().charAt(0) == 'i') {
                    while (subRunning) {
                        stringObject = new ArrayList<>();
                        System.out.println(ANSI_YELLOW + "Enter an ItemType (simple, special, simple sword, special sword, upgradeable sword, set)" + ANSI_RESET);
                        line = userInputWithoutLineBreak(s, "item").trim();
                        if (line.equalsIgnoreCase("simple")) {
                            stringObject.add("{simpleItem");
                            checkReturnAndInput("Enter an Item name", s, "item[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter a modelingType (basic or handheld)", s, "item[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter a Creative Tab (items, blocks...)", s, "item[simple]", stringObject);
                            if (!subRunning) break;
                            warning("Does the item have Enchants?");
                            if ((line = userInputWithoutLineBreak(s, "Item[simple]")).toLowerCase().contains("y")) {
                                warning("Enter what Enchanting templates you want to use (Axe, Pickaxe, Sword, Hoe, Shovel, head_armor, leg_armor, foot_armor, chest_armor)\nType in \"!STOP\" when your ready");
                                while (!line.contains("!STOP") && !line.trim().contains("!RETURN")) {
                                    line = "Enchantable:" + userInputWithoutLineBreak(s, "Item[simple]$Enchantable");
                                    stringObject.add(line);
                                }
                                stringObject.removeLast();
                            } else {
                                stringObject.add("!NO_ENCHANTS");
                            }
                            if (!subRunning) break;
                            if (line.trim().equals("!RETURN")) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                            success("Successfully registered Object");
                        }
                        else if (line.equalsIgnoreCase("special")) {
                            stringObject.add("{specialItem");
                            checkReturnAndInput("Enter an Item name", s, "item[special]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter a modelingType (basicItem or handheldItem)", s, "item[special]", stringObject);
                            if (!subRunning) break;
                            warning("Does the item have a Rarity?");
                            line = userInputWithoutLineBreak(s, "item[special]");
                            if (line.toUpperCase().contains("y")) {
                                warning("Enter a Rarity for the item");
                                line = "?" + userInputWithoutLineBreak(s, "item[special]$Enchantable");
                                if (!line.contains("!RETURN")) stringObject.add(line);
                            }
                            if (!subRunning) break;
                            checkReturnAndInput("Enter a Creative Tab (items, blocks...)", s, "item[special]", stringObject);
                            if (!subRunning) break;
                            warning("Does the item have Enchants?");
                            if ((line = userInputWithoutLineBreak(s, "Item[special]")).toLowerCase().contains("y")) {
                                stringObject.add("?[E");
                                warning("Enter what Enchanting templates you want to use (Axe, Pickaxe, Sword, Hoe, Shovel, head_armor, leg_armor, foot_armor, chest_armor)\nType in \"!STOP\" when your ready");
                                while (!line.equals("!STOP") && !line.trim().equals("!RETURN")) {
                                    line = "Enchantable:" + userInputWithoutLineBreak(s, "Item[special]");
                                    stringObject.add(line);
                                }
                                stringObject.removeLast();
                                stringObject.add("?]");
                            }  else {
                                stringObject.add("!NO_ENCHANTS");
                            }
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                            success("Successfully registered Object");
                        }
                        else if (line.replace(" ", "").equalsIgnoreCase("simpleSword")) {
                            stringObject.add("{simpleSword");
                            checkReturnAndInput("Enter an Item Sword Name", s, "item[simpleSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the Sword Properties (Format: [damage], [speed]f) [note: the lower the speed the faster for attack speed]", s, "item[simpleSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an item creation method", s, "item[simpleSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter a modelingType (basicItem, handheldItem)", s, "item[simpleSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an item material", s, "item[simpleSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter a creative tab", s, "item[simpleSword]", stringObject);
                            if (!subRunning) break;
                            warning("Does the item have Enchants?");
                            if ((line = userInputWithoutLineBreak(s, "Item[simpleSword]")).toLowerCase().contains("y")) {
                                stringObject.add("?[E");
                                warning("Enter what Enchanting templates you want to use (Axe, Pickaxe, Sword, Hoe, Shovel, head_armor, leg_armor, foot_armor, chest_armor)\nType in \"!STOP\" when your ready");
                                while (!line.equals("!STOP") && !line.trim().equals("!RETURN")) {
                                    line = "Enchantable:" + userInputWithoutLineBreak(s, "Item[simpleSword]$Enchantable");
                                    stringObject.add(line);
                                }
                                stringObject.removeLast();
                                stringObject.add("?]");
                            } else {
                                stringObject.add("!NO_ENCHANTS");
                            }
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                            success("Successfully registered Object");
                        }
                        else if (line.replace(" ", "").equalsIgnoreCase("specialSword")) {
                            stringObject.add("{specialSword");
                            checkReturnAndInput("Enter an Item name", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the Item Properties (format: [damage, speed])", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an item creation method", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an item modeling method", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an item material", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter a creative Tab", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Specify what you want to add [e.g. enter rarity -> input is e.g. 'uncommon']", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            warning("Does the item have Enchants?");
                            if ((line = userInputWithoutLineBreak(s, "Item[specialSword]")).toLowerCase().contains("y")) {
                                stringObject.add("?[E");
                                warning("Enter what Enchanting templates you want to use (Axe, Pickaxe, Sword, Hoe, Shovel, head_armor, leg_armor, foot_armor, chest_armor)\nType in \"!STOP\" when your ready");
                                while (!line.equals("!STOP") && !line.trim().equals("!RETURN")) {
                                    line = "Enchantable:" + userInputWithoutLineBreak(s, "Item[specialSword]$Enchantable");
                                    stringObject.add(line);
                                }
                                stringObject.removeLast();
                                stringObject.add("?]");
                            } else {
                                stringObject.add("!NO_ENCHANTS");
                            }
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                            success("Successfully registered Object");
                        }
                        else if (line.equalsIgnoreCase("upgradeable") || line.equalsIgnoreCase("upgradable") || line.replace(" ", "").equalsIgnoreCase("upgradeableSword") || line.replace(" ", "").equalsIgnoreCase("upgradableSword")) {
                            stringObject.add("{upgradableSword");
                            checkReturnAndInput("Enter an Item name", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the Weaponclass", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the item creation method", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the item modelling method", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the item material", s, "item[specialSword]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Now enter what variants of the weapon exist", s, "item[specialSword]", stringObject);
                            boolean gotvariants = false;
                            stringObject.add("[");
                            while (!gotvariants) {
                                if ((line = userInputWithoutLineBreak(s, "item[specialSword]$variants").trim()).equals("!ALL")) {
                                    gotvariants = true;
                                    stringObject.add("!ALL");
                                } else {
                                    stringObject.add(line);
                                }
                            }
                            stringObject.add("]");
                            if (!subRunning) break;
                            stringObject.add("?[E");
                            warning("Do the items have Enchants?");
                            if ((line = userInputWithoutLineBreak(s, "Item[upgradeableItem]")).toLowerCase().contains("y")) {
                                warning("Enter what Enchanting templates you want to use (Axe, Pickaxe, Sword, Hoe, Shovel, head_armor, leg_armor, foot_armor, chest_armor)\nType in \"!STOP\" when your ready");
                                while (!line.equals("!STOP") && !line.trim().equals("!RETURN")) {
                                    line = "Enchantable:" + userInputWithoutLineBreak(s, "Item[upgradeableItem]$Enchantable");
                                    stringObject.add(line);
                                }
                                stringObject.removeLast();
                                stringObject.add("?]");
                            }
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                            success("Successfully registered Object");
                        } /*else if (line.equalsIgnoreCase("set")) {
                            warning("Do the items have Enchants?");
                            if ((line = userInputWithoutLineBreak(s, "Item[set]")).toLowerCase().contains("y")) {
                                warning("Enter what Enchanting templates you want to use (Axe, Pickaxe, Sword, Hoe, Shovel, head_armor, leg_armor, foot_armor, chest_armor)\nType in \"!STOP\" when your ready");
                                while (!line.equals("!STOP") && !line.trim().equals("!RETURN")) {
                                    line = "Enchantable:" + userInputWithoutLineBreak(s, "Item[set]$Enchantable");
                                    stringObject.add(line);
                                }
                                stringObject.removeLast();
                            }
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }*/

                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }
                    }
                }
                else if (line.charAt(0) == 'C' || line.charAt(0) == 'c') {
                    while (subRunning) {
                        stringObject = new ArrayList<>();
                        System.out.println(ANSI_YELLOW + "Enter a Creative Tab Type (currently only 'simple' exists)" + ANSI_RESET);
                        line = userInputWithoutLineBreak(s, "creative_tab").trim();
                        if (line.equalsIgnoreCase("simple")) {
                            stringObject.add("{simpleTab");
                            checkReturnAndInput("Enter a creative Tab Name like this: 'forger_test_tab'", s, "creative_tab[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Reenter Name in camelCase ('forgerTestTab')", s, "creative_tab[simple]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter display item and specify type(format: 'moditem jade_gemstone')", s, "creative_tab[simple]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }
                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }
                    }
                }
                else if (line.charAt(0) == 'U' || line.charAt(0) == 'u') {
                    System.out.println(ANSI_YELLOW + "Enter an Item Upgrade" + ANSI_RESET);
                    upgrades.add(userInputWithoutLineBreak(s, "upgrade").trim().replace(" ", "_").toLowerCase());
                }
                else if (line.charAt(0) == 'R' || line.charAt(0) == 'r') {
                    while (subRunning) {
                        stringObject = new ArrayList<>();
                        System.out.println(ANSI_YELLOW + "Enter an Recipe Type (smelting, blasting, both (smelting and blasting), shaped, shapeless, custom)" + ANSI_RESET);
                        line = userInputWithoutLineBreak(s, "recipe").trim().replace(" ", "");
                        if (line.equalsIgnoreCase("smelting")) {
                            stringObject.add("{smelting");
                            checkReturnAndInput("Enter a recipe Category", s, "recipe[smelting]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an output item (format: e.g. modItem sapphire_gemstone)", s, "recipe[smelting]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the smelting properties", s, "recipe[smelting]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("[");
                            while (running && subRunning) {
                                checkReturnAndInput("Enter an item you could use as input here", s, "recipe[smelting]$input", stringObject);
                            }
                            subRunning = true;
                            stringObject.add("]");
                            running = true;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }
                        else if (line.equalsIgnoreCase("blasting")) {
                            stringObject.add("{blasting");
                            checkReturnAndInput("Enter a recipe Category", s, "recipe[blasting]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an output item (format: e.g. modItem sapphire_gemstone)", s, "recipe[blasting]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the blasting properties", s, "recipe[blasting]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("[");
                            while (running && subRunning) {
                                checkReturnAndInput("Enter an item you could use as input here", s, "recipe[smelting]$input", stringObject);
                                if (!subRunning) break;
                            }
                            subRunning = true;
                            stringObject.add("]");
                            running = true;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }
                        else if (line.equalsIgnoreCase("both") || line.equalsIgnoreCase("smeltingandblasting") || line.equalsIgnoreCase("smelting&blasting") || line.equalsIgnoreCase("smelting+blasting")) {
                            stringObject.add("{both");
                            checkReturnAndInput("Enter a recipe Category", s, "recipe[smeltingandblasting]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an output item (format: e.g. modItem sapphire_gemstone)", s, "recipe[smeltingandblasting]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the blasting properties", s, "recipe[smeltingandblasting]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the smelting properties", s, "recipe[smeltingandblasting]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("[");
                            while (running && subRunning) {
                                checkReturnAndInput("Enter an item you could use as input here", s, "recipe[smelting]$input", stringObject);
                                if (!subRunning) break;
                            }
                            subRunning = true;
                            stringObject.add("]");
                            running = true;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }
                        else if (line.equalsIgnoreCase("shaped")) {
                            stringObject.add("{shaped");
                            checkReturnAndInput("Enter a recipe Category", s, "recipe[shaped]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the first row of the shape (format: AAA) X=NoItem", s, "recipe[shaped]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the second row of the shape (format: AAA) X=NoItem", s, "recipe[shaped]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the third row of the shape (format: AAA) X=NoItem", s, "recipe[shaped]", stringObject);
                            if (!subRunning) break;
                            String differentChars = getDifferentChars(stringObject.get(stringObject.size()-1), stringObject.get(stringObject.size()-2), stringObject.get(stringObject.size()-3));
                            for (int i = 0; i < differentChars.length(); i++) {
                                checkReturnAndInput("what does " + differentChars.charAt(i) + " mean? (format: A -> moditem sapphire_gemstone)", s, "recipe[shaped]$meanings", stringObject);
                                if (!subRunning) break;
                            }
                            if (!subRunning) break;
                            warning("Enter the Item/Block which unlocks the recipe (format: moditem sapphire_gemstone)");
                            line = userInputWithoutLineBreak(s, "recipe[shaped]");
                            if (line.trim().equals("!RETURN")) break;
                            stringObject.add(line);
                            stringObject.add(line); //jo 2 moi passt so, is für des unlockedBy
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }
                        else if (line.equalsIgnoreCase("shapeless")) {
                            stringObject.add("{shapeless");
                            checkReturnAndInput("Enter a recipe Category", s, "recipe[shapeless]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an output item (format: modItem sapphire_gemstone)", s, "recipe[shapeless]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the Item/Block which unlocks the recipe (format: moditem sapphire_gemstone)", s, "recipe[shapeless]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter an output count", s, "recipe[shapeless]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("[");
                            while (running && subRunning) {
                                checkReturnAndInput("Enter an item you could use as input here", s, "recipe[shapeless]$input", stringObject);
                                if (!subRunning) break;
                            }
                            subRunning = true;
                            stringObject.add("]");
                            running = true;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }
                        else if (line.equalsIgnoreCase("custom")) {
                            stringObject.add("{custom");
                            checkReturnAndInput("Enter target BlockEntity class (the BlockEntity class of the workstation, e.g. ForgeBlockEntity) in CAMELCASE!", s, "recipe[custom]", stringObject, "ls");
                            if (!subRunning) break;
                            stringObject.add("[");
                            while (running && subRunning) {
                                checkReturnAndInput("Enter all items needed as input here", s, "recipe[custom]$input", stringObject);
                                if (!subRunning) break;
                            }
                            subRunning = true;
                            stringObject.add("]");
                            running = true;
                            if (!subRunning) break;
                            stringObject.add("[");
                            while (running && subRunning) {
                                checkReturnAndInput("Enter all items you get as output here", s, "recipe[custom]$output", stringObject);
                                if (!subRunning) break;
                            }
                            subRunning = true;
                            stringObject.add("]");
                            running = true;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
                        }
                        if (line.equals("!STOP")) subRunning = false;
                        if (line.contains("!STOP -")) {
                            subRunning = false;
                            running = false;
                        }
                    }
                }
                else if (line.charAt(0) == 'T' || line.charAt(0) == 't') {
                    while (subRunning) {
                        stringObject = new ArrayList<>();
                        warning("Enter an Tool Tier Type (currently only 'normal' exists)");
                        line = userInputWithoutLineBreak(s).trim();
                        if (line.equalsIgnoreCase("normal")) {
                            stringObject.add("{normal");
                            checkReturnAndInput("Enter tool Material", s, "toolTier[normal]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter ingredient fitting the material", s, "toolTier[normal]", stringObject);
                            if (!subRunning) break;
                            checkReturnAndInput("Enter the tier Properties (as single String seperated with ',')", s, "toolTier[normal]", stringObject);
                            if (!subRunning) break;
                            stringObject.add("}");
                            stringObjects.add(stringObject);
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

    public static String getDifferentChars(String row3, String row2, String row1) {
        String all = (row1 + row2 + row3);
        String allNoDuplicates = "";
        for (int i = 0; i < all.length(); i++) {
            allNoDuplicates += all.charAt(0);
            all = all.replaceAll(all.charAt(0) + "", "");
        }
        return allNoDuplicates.replace("X", "");
    }

    public static void checkReturnAndInput(String msg, Scanner s, String addOn, ArrayList<String> stringObject) {
        warning(msg);
        String line = userInputWithoutLineBreak(s, addOn).trim();
        if (!line.equals("!RETURN") && !line.equals("!STOP")) {
            stringObject.add("    " + line);
        } else {
            subRunning = false;
        }
        if (line.contains("!STOP -")) {
            running = false;
        }
    }

    public static void checkReturnAndInput(String msg, Scanner s, String addOn, ArrayList<String> stringObject, String ls) {
        warning(msg);
        String line = userInputWithoutLineBreak(s, addOn).trim();
        if (!line.equals("!RETURN") && !line.equals("!STOP") && !line.equals("!LS")) {
            stringObject.add("    " + line);
        } else if (!line.equals("!LS")) {
            subRunning = false;
        } else {
            //TODO: list files in block/entity/custom
            FileSystem fs = new FileSystem("./src/main/java/net/bananashelp20/forgermod/block/entity/custom", ".java");
            warning(fs.toString());
        }
        if (line.contains("!STOP -")) {
            running = false;
        }
    }

    public static void checkReturnAndInput(String msg, Scanner s, String addOn, ArrayList<String> stringObject, boolean optionalParameter) {
        warning(msg);
        String line = userInputWithoutLineBreak(s, addOn).trim();
        if (!line.equals("!RETURN") && !line.equals("!STOP")) {
            stringObject.add("    " + (optionalParameter ? "?" : "") + line);
        } else {
            subRunning = false;
        }
        if (line.contains("!STOP -")) {
            running = false;
        }
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
