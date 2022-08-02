/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class manages the symbol table used throughout the compiling process
 *******************************************************************************/

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private HashMap<String, Symbol> table;
    int latestId;

    public SymbolTable() {
        table = new HashMap<String, Symbol>();
        latestId = 1;
    }

    public int addSymbol(String ref, String val) {
        if (table.containsKey(ref)) {
            return table.get(ref).getId();
        }
        Symbol s = new Symbol(latestId, val);
        latestId++;
        table.put(ref, s);
        return s.getId();
    }

    public String getSymbolValue(String ref) {
        return table.get(ref).getVal();
    }

    public String getSymbolValue(int id) {
        for (Map.Entry<String, Symbol> entry : table.entrySet()) {
            if (id == entry.getValue().getId()) {
                return entry.getValue().getVal();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String out = "";
        for (Map.Entry<String, Symbol> entry : table.entrySet()) {
            out += entry.getValue().getId() + ", " + entry.getKey() + ", " + entry.getValue().getVal() + "\n";
        }
        return out;
    }
}
