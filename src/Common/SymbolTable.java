/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class manages the symbol table used throughout the compiling process
 *******************************************************************************/
package Common;

import Scanner.Token;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private HashMap<String, HashMap<Integer, Symbol>> table;
    int latestId;

    public enum SymbolType {
        PROGRAM_IDEN,
        STRUCT_TYPE,
        ARRAY_TYPE,
        CONSTANT_ARRAY,
        CONSTANT,
        VARIABLE,
        FUNCTION
    }
    public SymbolTable() {
        table = new HashMap<String, HashMap<Integer, Symbol>>();
        latestId = 1;
    }

    //    public int addSymbol(String ref, String val) {
    //        // Don't add the symbol if it already exists
    //        if (table.containsKey(ref)) {
    //            return table.get(ref).getId();
    //        }
    //        // Create a new symbol and increment the id ready for the next symbol
    //        Symbol s = new Symbol(latestId, val);
    //        latestId++;
    //        // Add the symbol to the table and return the id
    //        table.put(ref, s);
    //        return s.getId();
    //    }

    public int addSymbol(SymbolType symbolType_, Token token_) {
        return addSymbol(symbolType_, token_, "global");
    }

    public int addSymbol(SymbolType symbolType, Token token, String scope) {
        if (!table.containsKey(scope)) {
            table.put(scope, new HashMap<Integer, Symbol>());
        }
        Symbol symbol = new Symbol(symbolType, token.getTokenLiteral(), null);
        table.get(scope).put(latestId, symbol);
        return 0;
    }
    public int addSymbol(SymbolType symbolType, Token token, String scope, String value) {
        if (!table.containsKey(scope)) {
            table.put(scope, new HashMap<Integer, Symbol>());
        }
        Symbol symbol = new Symbol(symbolType, token.getTokenLiteral(), value);
        table.get(scope).put(latestId, symbol);
        return latestId++;
    }

    // Getters for the value given either ref or id
//    public String getSymbolValue(String ref) {
//        return table.get(ref).getVal();
//    }

    public Symbol getSymbol(int id) {
        for (Map.Entry<String, HashMap<Integer, Symbol>> scopeEntry : table.entrySet()) {
            for (Map.Entry<Integer, Symbol> entry : table.get(scopeEntry.getKey()).entrySet()) {
                if (id == entry.getKey()) {
                    return entry.getValue();
                }
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
        for (Map.Entry<String, HashMap<Integer, Symbol>> scopeEntry : table.entrySet()) {
            out += scopeEntry.getKey() + "\n";
            for (Map.Entry<Integer, Symbol> entry : table.get(scopeEntry.getKey()).entrySet()) {
                out += entry.getKey() + ", " + entry.getValue().getRef() + ", " + entry.getValue().getVal() + ", "
                        + entry.getValue().getSymbolType() + "\n";
            }
        }
        return out;
    }
}
