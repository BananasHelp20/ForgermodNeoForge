package net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.Exceptions.ListObjectNotFoundException;

import java.util.ArrayList;

public class TitledTable<S> {
    ArrayList<ArrayList<S>> list = new ArrayList<>();
    private ArrayList<String> tableTitles; //Format: "blockName", "blockID". In order to make sort of a map
    private ArrayList<String> actualTypes; //Format: "Integer", "String"

    public TitledTable (ArrayList<String> titles, ArrayList<String> types) {
        tableTitles = titles;
        actualTypes = types;
        lastIndexExpander = 0;
    }

    public TitledTable (ArrayList<String> titles, ArrayList<String> types, int lastIndexExpander) {
        tableTitles = titles;
        actualTypes = types;
        this.lastIndexExpander = lastIndexExpander;
    }

    public void addListToTable(ArrayList<Object> list) {
        this.list.add((ArrayList<S>) list);
    }

    public void addTableTitle(String title) {
        this.tableTitles.add(title);
    }

    public void addToAList(int index, S listObject) {
        this.list.get(index).add(listObject);
    }

    public Object getFromList(int listIndex, String title) {
        String type = actualTypes.get(tableTitles.indexOf(title)).toUpperCase();
        if (type.equals("STRING")) {
            return this.list.get(listIndex).get(tableTitles.indexOf(title));
        } else if (type.contains("BOOL")) {
            return ((String) this.list.get(listIndex).get(tableTitles.indexOf(title))).toUpperCase().contains("T");
        } else if (type.contains("INT")) {
            return Integer.parseInt((String)this.list.get(listIndex).get(tableTitles.indexOf(title)));
        } else if (type.contains("DOUBLE")) {
            return Double.parseDouble((String) this.list.get(listIndex).get(tableTitles.indexOf(title)));
        }
        throw new ListObjectNotFoundException("List Object has invalid Type!");
    }

    public ArrayList<S> getListFromTable(int index) {
        return list.get(index);
    }

    @Override
    public String toString() {
        //titles, then content listed as table
        String s = "";
        for (int i = 0; i < tableTitles.size(); i++) {
            s += tableTitles.get(i) + " [" + actualTypes.get(i) + "] │ ";
        }
        s = s.substring(0, s.length()-2) + "\n";

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                s += list.get(i).get(j) + " │ ";
            }
            s = s.substring(0, s.length()-3) + "\n";
        }
        return s;
    }

    public static ArrayList<String> getArraylistFromBrackets(ArrayList<String> bracketStringList, int indexExpander) {
        if (bracketStringList.get(indexExpander).contains("]")) indexExpander += 2; else if (bracketStringList.get(indexExpander).contains("[")) indexExpander++;
        ArrayList<String> bracketContent = new ArrayList<>();
        for (indexExpander = indexExpander; indexExpander < bracketStringList.size() && !bracketStringList.get(indexExpander).contains("]"); indexExpander++) {
            bracketContent.add(bracketContent.get(indexExpander));
        }
        return bracketContent;
    }

    public ArrayList<ArrayList<S>> getAsList() {
        return list;
    }

    public int length() {
        return list.size();
    }

    public int innerLength(int index) {
        return list.get(index).size();
    }

    public int lastIndexExpander;
    public void parseFromStringList(ArrayList<String> bracketStringList, int indexExpander) {
        int acceptableBrackets = 0;
        ArrayList<String> list = new ArrayList<>();
        for (int i = indexExpander + (bracketStringList.get(indexExpander).contains("[") ? 1 : 0); i < bracketStringList.size() && !(bracketStringList.get(i).contains("]") && (acceptableBrackets == 0 && i > 0)); i++) {
            if (bracketStringList.get(i).contains("[")) acceptableBrackets++;
            if (bracketStringList.get(i).contains("]")) acceptableBrackets--;

            if (acceptableBrackets != 0 && !bracketStringList.contains("]") && !bracketStringList.contains("[")) {
                list.add(bracketStringList.get(i));
            }

            if (acceptableBrackets == 0 && list.size() != 0) {
                this.list.add((ArrayList<S>) list);
                list = new ArrayList<>();
            }
            lastIndexExpander = i;
        }
        lastIndexExpander++;
    }
}
