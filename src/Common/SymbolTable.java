/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class manages the symbol table used throughout the compiling process
 *******************************************************************************/
package Common; 

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private HashMap<String, Symbol> table;
    int latestId;

    public SymbolTable() {
        table = new HashMap<String, Symbol>();
        latestId = 1;
    }

    /**
     * Add a symbol to the symbol table
     * @param ref an identifier to be added
     * @param val a value to be added
     * @return the unique id of the location in the symbol table
     */
    public int addSymbol(String ref, String val) {
        // Don't add the symbol if it already exists
        if (table.containsKey(ref)) {
            return table.get(ref).getId();
        }
        // Create a new symbol and increment the id ready for the next symbol
        Symbol s = new Symbol(latestId, val);
        latestId++;
        // Add the symbol to the table and return the id
        table.put(ref, s);
        return s.getId();
    }

    // Getters for the value given either ref or id
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

    /**
     * Debug to string method of the table
     * @return stringified version of the symbol table
     */
    @Override
    public String toString() {
        String out = "";
        for (Map.Entry<String, Symbol> entry : table.entrySet()) {
            out += entry.getValue().getId() + ", " + entry.getKey() + ", " + entry.getValue().getVal() + "\n";
        }
        return out;
    }
}
