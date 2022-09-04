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
        FUNCTION,
        LITERAL
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
        Symbol symbol;
        if (symbolType == SymbolType.LITERAL) {
            symbol = new Symbol(symbolType, null, token.getTokenLiteral());

        } else {
            symbol = new Symbol(symbolType, token.getTokenLiteral(), null);
        }
        int symbolTableReference = containsEntry(table.get(scope), token);
        if (symbolTableReference == -1) {
            table.get(scope).put(latestId, symbol);
            return latestId++;
        }
        return symbolTableReference;
    }

    private int containsEntry(HashMap<Integer, Symbol> map, Token token) {
        for (Map.Entry<Integer, Symbol> entry : map.entrySet()) {
            if (entry.getValue().getSymbolType() == SymbolType.LITERAL) {
                if (entry.getValue().getVal().compareTo(token.getTokenLiteral()) == 0) return entry.getKey();
            }
            else {
                if (entry.getValue().getRef().compareTo(token.getTokenLiteral()) == 0) return entry.getKey();
            }
        }
        return -1;
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
        String out = "SymbolId, Reference, Val, Type\n";
        for (Map.Entry<String, HashMap<Integer, Symbol>> scopeEntry : table.entrySet()) {
            out += "\n" + scopeEntry.getKey() + "\n";
            for (Map.Entry<Integer, Symbol> entry : table.get(scopeEntry.getKey()).entrySet()) {
                out += entry.getKey() + ", " + entry.getValue().getRef() + ", " + entry.getValue().getVal()  + ", "
                        + entry.getValue().getSymbolType() + "\n";
            }
        }
        return out;
    }
}
