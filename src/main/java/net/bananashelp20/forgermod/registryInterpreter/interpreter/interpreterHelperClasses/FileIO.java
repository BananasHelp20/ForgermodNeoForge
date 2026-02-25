package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpreterHelperClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreterHelperMethods.*;

public class FileIO {
    public static void rewriteAllAfterError(boolean allowed) {
        try {
            if (!allowed) return; //only for safety reasons
            FileWriter[] writers = new FileWriter[11];
            (writers[0] = new FileWriter(modBlocksFile)).write(unchangedModBlockFileContent);
            (writers[1] = new FileWriter(modRegistry)).write(unchangedModRegistryContent);
            (writers[2] = new FileWriter(modItemTagProviderFile)).write(unchangedModItemTagProviderContent);
            (writers[3] = new FileWriter(modToolTiersFile)).write(unchangedModToolTiersFile);
            (writers[4] = new FileWriter(modBlockStateProviderFile)).write(unchangedModBlockStateProviderFile);
            (writers[5] = new FileWriter(modBlockLootTableProviderFile)).write(unchangedModBlockLootTableProviderFile);
            (writers[6] = new FileWriter(modBlockTagProviderFile)).write(unchangedModBlockTagProviderFile);
            (writers[7] = new FileWriter(modItemModelProviderFile)).write(unchangedModItemModelProviderFile);
            (writers[8] = new FileWriter(modRecipeProviderFile)).write(unchangedModRecipeProviderFile);
            (writers[9] = new FileWriter(modItemsFile)).write(unchangedModItemsFileContent);
            (writers[10] = new FileWriter(modCreativeTabsFile)).write(unchangedModCreativeModeTabsFileContent);
            for (int i = 0; i < writers.length; i++) {
                writers[i].close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static ArrayList<String> getContentInBrackets(int listIndex, int elementIndex, ArrayList<ArrayList<String>> stringObjects) {
        ArrayList<String> objects = new ArrayList<>();
        for (int i = elementIndex+1; i < stringObjects.get(listIndex).size() && !stringObjects.get(listIndex).get(i).contains("]"); i++) {
            objects.add(stringObjects.get(listIndex).get(i));
        }
        return objects;
    }

    public static String getPartWithoutComment(String s) {
        String[] splitText = s.split("#");
        for (int i = 0; i < splitText.length; i++) {
            if (!splitText[i].contains("//")) {
                return splitText[i];
            }
        }
        return "";
    }

    public static ArrayList<String> getContentFromFileAsList(File file, String comment) {
        Scanner reader;
        ArrayList<String> fileContent = new ArrayList<>();
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        while (reader.hasNextLine()) {
            fileContent.add(reader.nextLine() + "\n");
        }

        clearContentFromUnneccesary(fileContent, comment);
        return fileContent;
    }

    public static ArrayList<String> getContentFromFileAsListNonFormated(File file) {
        Scanner reader;
        ArrayList<String> fileContent = new ArrayList<>();
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        while (reader.hasNextLine()) {
            fileContent.add(reader.nextLine());
        }

        return fileContent;
    }

    public static void clearContentFromUnneccesary(ArrayList<String> content, String comment) {
        for (int i = 0; i < content.size(); i++) {
            content.set(i, content.get(i).trim());
            if (!comment.isEmpty()) {
                if (content.get(i).contains(comment)) {
                    content.set(i, getPartWithoutComment(content.get(i)));
                }
            }
            if (content.get(i).trim().equals("")) {
                content.remove(i);
                i--;
            }
        }
    }

    static int getWritablePos(File file, String commentCommand) {
        Scanner reader;
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String searcher;
        for (int i = 1; reader.hasNextLine(); i++) {
            searcher = reader.nextLine();
            if (searcher.trim().equals(commentCommand)) {
                reader.close();
                return i + 1;
            }
        }
        return -1;
    }

    static String getWholeFileContentTillGenerate(File file, String command) {
        String saved = "";
        try {
            Scanner reader = new Scanner(file);
            int startGeneratingAtLine = getWritablePos(file, command);
            for (int i = 1; i < startGeneratingAtLine && reader.hasNextLine(); i++) {
                saved += reader.nextLine() + (i < startGeneratingAtLine-1 ? "\n" : "");
            }
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return saved;
    }

    public static String getContentFromFile(File file) {
        Scanner reader;
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String content = "";
        while (reader.hasNextLine()) {
            content += reader.nextLine() + "\n";
        }
        return content;
    }

    public static void write(String content, File writeTo) {
        try {
            FileWriter modFileWriter = new FileWriter(writeTo);
            modFileWriter.write(content);
            modFileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<String> getArraylistFromBrackets(ArrayList<String> bracketStringList, int indexExpander) {
        if (bracketStringList.get(indexExpander).contains("]")) indexExpander += 2; else if (bracketStringList.get(indexExpander).contains("[")) indexExpander++;
        ArrayList<String> bracketContent = new ArrayList<>();
        for (indexExpander = indexExpander; indexExpander < bracketStringList.size() && !bracketStringList.get(indexExpander).contains("]"); indexExpander++) {
            bracketContent.add(bracketContent.get(indexExpander));
        }
        return bracketContent;
    }
}
