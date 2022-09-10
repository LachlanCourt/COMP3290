/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class manages the symbol table used throughout the compiling process
 *******************************************************************************/
package Common;

import Scanner.Token;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final HashMap<String, HashMap<Integer, Symbol>> table;
    // Ensure every symbol has a unique ID
    int latestId;

    // The symbol table is a singleton
    private static SymbolTable self;

    // The various different kinds of symbols that can be stored in the table
    public enum SymbolType {
        PROGRAM_IDEN,
        STRUCT_TYPE,
        ARRAY_TYPE,
        CONSTANT_ARRAY,
        CONSTANT,
        VARIABLE,
        FUNCTION,
        LITERAL,
        UNKNOWN
    }

    // Primitive types for variables and function returns
    public enum PrimitiveTypes { INTEGER, FLOAT, BOOLEAN, VOID, UNKNOWN }

    /**
     * Private constructor for singleton
     */
    private SymbolTable() {
        table = new HashMap<>();
        latestId = 1;

        // The result of an invalid call to getSymbol returns -1. This ensures that there is always
        // an object returned that can have symbol functions called on it. This is still an error
        // state, although it will help prevent unexpected program crashes without constant
        // verification of the return value
        table.put("@error", new HashMap<>());
        table.get("@error").put(-1, new Symbol(SymbolType.UNKNOWN, ""));
    }

    /**
     * Getter for symbol table
     * @return the current instance of the symbol table
     */
    public static SymbolTable getSymbolTable() {
        if (self == null) {
            self = new SymbolTable();
        }
        return self;
    }

    /**
     * Add a symbol to the table
     * @param symbolType_ the type of the symbol to be added
     * @param token_ the primary token that the symbol is associated with
     * @return the ID of the symbol just added to the table
     */
    public int addSymbol(SymbolType symbolType_, Token token_) {
        // Literal values get added to a different section of the table. Literals, globals, and
        // error symbol table scopes are all prefixed by @ which is an invalid token in CD22 and
        // thus guarantees there won't be a clash between these keywords and a function name or
        // struct type which is also used as scope
        if (symbolType_ == SymbolType.LITERAL) {
            return addSymbol(symbolType_, token_, "@literals");
        }
        // If no scope is specified and the symbol type is not a literal, add it in at the global
        // scope. This could be global variables, constants, types, array types, or function names
        return addSymbol(symbolType_, token_, "@global");
    }

    public int addSymbol(SymbolType symbolType, Token token, String scope) {
        return addSymbol(symbolType, token, scope, false);
    }

    /**
     * Add a symbol to the table under a specified scope
     * @param symbolType the type of the symbol to be added
     * @param token the primary token that the symbol is associated with
     * @param scope the scope of the table to add the symbol to
     * @return the ID of the symbol just added to the table
     */
    public int addSymbol(SymbolType symbolType, Token token, String scope, boolean primitiveType) {
        // If the scope does not already exist, create a new map with that scope
        if (!table.containsKey(scope)) {
            table.put(scope, new HashMap<>());
        }
        Symbol symbol;
        // If the symbol is a literal value, there is no reference but there is a value
        if (symbolType == SymbolType.LITERAL) {
            symbol = new LiteralSymbol(symbolType, null, token.getTokenLiteral());

        } else if (primitiveType) {
            symbol = new PrimitiveTypeSymbol(
                symbolType, token.getTokenLiteral(), PrimitiveTypes.UNKNOWN);
        } else {
            // If the symbol is anything other than a literal, there is a reference but there is no
            // value
            symbol = new Symbol(symbolType, token.getTokenLiteral());
        }
        // Check if the symbol already exists in the table
        int symbolTableId = containsEntry(table.get(scope), token);
        // If the symbol does not already exist, add the symbol and increment the running ID for the
        // next symbol
        if (symbolTableId == -1) {
            table.get(scope).put(latestId, symbol);
            return latestId++;
        }
        // If the symbol already exists, return the ID of that symbol. Consider adding an error here
        return symbolTableId;
    }

    /**
     * Check if a symbol already exists in the table given a particular scope map and a token
     * @param map a sub-scoped map from the symbol table
     * @param token a token to be checked to see if it already exists
     * @return the ID of the symbol if it exists, or -1 if it does not
     */
    private int containsEntry(HashMap<Integer, Symbol> map, Token token) {
        // Loop through the map entries and return the ID if it exists
        for (Map.Entry<Integer, Symbol> entry : map.entrySet()) {
            // If the token is a literal, compare the value
            if (entry.getValue().getSymbolType() == SymbolType.LITERAL) {
                if (((LiteralSymbol) entry.getValue()).getVal().compareTo(token.getTokenLiteral())
                    == 0)
                    return entry.getKey();
            } else {
                // If the token is not a literal, compare the reference
                if (entry.getValue().getRef().compareTo(token.getTokenLiteral()) == 0)
                    return entry.getKey();
            }
        }
        // Return -1 the loop finishes without a successful match
        return -1;
    }

    /**
     * Get a symbol from the table given an ID
     * @param id of the symbol to be retrieved
     * @return the symbol from the table, or null if it does not exist
     */
    public Symbol getSymbol(int id) {
        // Loop through the main symbol table map
        for (Map.Entry<String, HashMap<Integer, Symbol>> scopeEntry : table.entrySet()) {
            // Loop through the map for each scope
            for (Map.Entry<Integer, Symbol> entry : table.get(scopeEntry.getKey()).entrySet()) {
                // Return the symbol if the key matches
                if (id == entry.getKey()) {
                    return entry.getValue();
                }
            }
        }
        // Return null if no symbol exists with the given ID
        return null;
    }

    /**
     * Gets a symbol given a reference and a scope. Will check the scope first, and then check
     * global if not found in the given scope
     * @param reference the reference of the required symbol
     * @param scope the scope of the table to look in
     * @return the ID of the symbol, or -1 if it does not exist
     */
    public int getSymbolIdFromReference(String reference, String scope) {
        return getSymbolIdFromReference(reference, scope, true);
    }

    /**
     * Gets a symbol given a reference and a scope. Will check the scope first, and then optionally
     * check global if not found in the given scope
     * @param reference the reference of the required symbol
     * @param scope the scope of the table to look in
     * @param fallbackToGlobal whether to check the global scope if the reference is not found in
     *     the given scope
     * @return the ID of the symbol, or -1 if it does not exist
     */
    public int getSymbolIdFromReference(String reference, String scope, boolean fallbackToGlobal) {
        // Loop through the given scope
        if (table.containsKey(scope)) {
            for (Map.Entry<Integer, Symbol> entry : table.get(scope).entrySet()) {
                // Return the symbol table ID if the reference matches
                if (reference.compareTo(entry.getValue().getRef()) == 0) {
                    return entry.getKey();
                }
            }
        }

        // If requested, also check the global scope by looping through that
        if (fallbackToGlobal) {
            for (Map.Entry<Integer, Symbol> entry : table.get("@global").entrySet()) {
                // Return the symbol table ID if the reference matches
                if (reference.compareTo(entry.getValue().getRef()) == 0) {
                    return entry.getKey();
                }
            }
        }
        // If the symbol has not been found return an error code
        return -1;
    }

    /**
     * Debug to string method of the table
     * @return stringified version of the symbol table
     */
    @Override
    public String toString() {
        StringBuilder out =
            new StringBuilder("SymbolId, Reference, Val, ForeignSymbolTableReference, Type\n");
        for (Map.Entry<String, HashMap<Integer, Symbol>> scopeEntry : table.entrySet()) {
            out.append("\n").append(scopeEntry.getKey()).append("\n");
            for (Map.Entry<Integer, Symbol> entry : table.get(scopeEntry.getKey()).entrySet()) {
                out.append(entry.getKey())
                    .append(", ")
                    .append(entry.getValue().getRef())
                    .append(", ");

                // Only output the value if it is not a standard symbol
                if (entry.getValue() instanceof PrimitiveTypeSymbol)
                    out.append(((PrimitiveTypeSymbol) entry.getValue()).getVal()).append(", ");
                if (entry.getValue() instanceof LiteralSymbol)
                    out.append(((LiteralSymbol) entry.getValue()).getVal()).append(", ");

                out.append(entry.getValue().getForeignSymbolTableId())
                    .append(", ")
                    .append(entry.getValue().getSymbolType())
                    .append("\n");
            }
        }
        return out.toString();
    }
}
