package net.bananashelp20.forgermod.registryInterpreter.WilliCodeGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FileSystem {
    public static ArrayList<File> files;
    public static File topFile;
    public static String filter;

    public FileSystem(String topPath, String filter) {
        topFile = new File(topPath);
        this.filter = filter;
    }

    @Override
    public String toString() {
        String fs = "";
        if (topFile.exists()) {
            fs += topFile.getName() + "\n";
            fs += contentListToString(1, topFile.listFiles(), filter);
        } else {
            System.err.println("File/Directory konnte nicht gefunden werden");
        }
        return fs;
    }

    public String contentListToString(int level, File[] list, String justThis) {
        if (list == null) {
            return "";
        }
        String fs = "";

        for (int i = 0; i < list.length; i++) {
            if (justThis.equals("all") || justThis.equals("") || justThis.equals("none") || list[i].isDirectory() || (justThis.equalsIgnoreCase(list[i].getName().substring(list[i].getName().length()-justThis.length())))) {
                for (int j = 1; j < level; j++) {
                    fs += "┃  ";
                }
                fs += ((i + 1 == list.length) ? "┗ " : "┣ ") + (list[i].isFile() ? "\uD83D\uDCC4 " : "\uD83D\uDCC1 ") + list[i].getName() + " ";
                if (list[i].length() > 1000) {
                    fs += list[i].length() / 1000 + "KB";
                } else if (list[i].length() > 1000000) {
                    fs += list[i].length() / 1000000 + "MB";
                } else if (list[i].length() > 1000000000) {
                    fs += list[i].length() / 1000000000 + "GB";
                } else {
                    fs += list[i].length() + "B";
                }

                if (list[i].isDirectory()) {
                    fs += " Files: " + list[i].listFiles().length + "\n";
                    fs += contentListToString(level + 1, list[i].listFiles(), justThis);
                } else {
                    fs += "\n";
                }
            }
        }
        return fs;
    }
}
