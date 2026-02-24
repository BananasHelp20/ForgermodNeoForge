package net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.Exceptions.ListObjectNotFoundException;

import java.util.ArrayList;

public class TitledList<S> {
    ArrayList<S> list = new ArrayList<>();
    private String title; //Format: "blockName", "blockID". In order to make sort of a map
    private String type; //Format: "Integer", "String"

    public TitledList(String titles, String type) {
        this.title = titles;
        this.type = type;
        lastIndexExpander = 0;
    }

    public TitledList(String titles, String type, int lastIndexExpander) {
        this.title = titles;
        this.type = type;
        this.lastIndexExpander = lastIndexExpander;
    }

    public void addToList(int index, S listObject) {
        this.list.add(index, listObject);
    }
    
    public void addToList(S listObject) {
        this.list.add(listObject);
    }

    public Object get(int listIndex) {
        if (type.equals("STRING")) {
            return this.list.get(listIndex);
        } else if (type.contains("BOOL")) {
            return ((String) this.list.get(listIndex)).toUpperCase().contains("T");
        } else if (type.contains("INT")) {
            return Integer.parseInt((String) this.list.get(listIndex));
        } else if (type.contains("DOUBLE")) {
            return Double.parseDouble((String) this.list.get(listIndex));
        }
        throw new ListObjectNotFoundException("List Object has invalid Type!");
    }

    public int lastIndexExpander;
    public void parseFromStringList(ArrayList<String> bracketStringList, int indexExpander) {
        for (int i = indexExpander + (bracketStringList.get(indexExpander).contains("[") ? 1 : 0); i < bracketStringList.size() && !bracketStringList.get(i).contains("]"); i++) {
            this.list.add((S) bracketStringList.get(i));
            lastIndexExpander = i;
        }
        lastIndexExpander++;
    }
}