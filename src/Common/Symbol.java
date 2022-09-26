/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class represents a symbol in the symbol table
 *******************************************************************************/
package Common;

import Common.SymbolTable.SymbolType;
import Parser.TreeNode;

import java.util.HashMap;

public class Symbol {
    // The variable name identifier associated with the symbol
    protected String ref;

    protected SymbolType symbolType;

    // Most symbols will only have a single default foreign ID, either related to type or in the
    // case of constants the value; however symbols such as array types also need to keep track of
    // size and therefore a map is used
    protected final HashMap<String, Integer> foreignSymbolTableIds;

    private TreeNode foreignTreeNode;

    // Private constructor called by the others to set default values
    private Symbol() {
        foreignSymbolTableIds = new HashMap<>();
    }

    public Symbol(SymbolType symbolType_, String ref_) {
        this();
        symbolType = symbolType_;
        ref = ref_;
    }

    // Getters
    public String getRef() {
        return ref;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    // Setters

    public void setForeignSymbolTableId(String label, int reference_) {
        foreignSymbolTableIds.put(label, reference_);
    }

    /**
     * Sets the default foreign symbol table ID
     * @param reference_ the ID to save into the default position in the foreign ID map
     */
    public void setForeignSymbolTableId(int reference_) {
        // If no label is given, assume it is for the default value
        foreignSymbolTableIds.put("default", reference_);
    }

    /**
     * Gets a foreign symbol table ID based off the given label
     * @param label indicating the type of foreign ID required
     * @return the ID specified by the label, or -1 if it does not exist
     */
    public int getForeignSymbolTableId(String label) {
        // Only return the value if it exists in the map, else return -1 as an error code
        if (foreignSymbolTableIds.containsKey(label))
            return foreignSymbolTableIds.get(label);
        return -1;
    }

    /**
     * Gets the default foreign symbol table ID
     * @return the default foreign ID, or -1 if it does not exist
     */
    public int getForeignSymbolTableId() {
        return getForeignSymbolTableId("default");
    }

    /**
     * Set the symbol type to constant array
     */
    public void makeConstArray() {
        // This is the only way a symbol type can be modified after creation, and is required here
        // as a byproduct of how constant array params are parsed
        symbolType = SymbolType.CONSTANT_ARRAY;
    }

    public TreeNode getForeignTreeNode() {
        return foreignTreeNode;
    }

    public void setForeignTreeNode(TreeNode node) {
        foreignTreeNode = node;
    }
}