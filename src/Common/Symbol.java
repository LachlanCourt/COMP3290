/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class represents a symbol in the symbol table
 *******************************************************************************/
package Common;

import Common.SymbolTable.SymbolType;
import java.util.HashMap;

public class Symbol<T extends Comparable> {
    // The id assigned by the symbol table
    private String ref;
    // The data value of the symbol
    private T val;

    private SymbolType symbolType;

    private HashMap<String, Integer> foreignSymbolTableReferences;

    private Symbol() {
        foreignSymbolTableReferences = new HashMap<String, Integer>();
    }

    public Symbol(SymbolType symbolType_, String ref_, T val_) {
        this();
        symbolType = symbolType_;
        ref = ref_;
        val = val_;
    }

    public Symbol(SymbolType symbolType_, String ref_) {
        this();
        symbolType = symbolType_;
        ref = ref_;
    }

    public String getRef() {
        return ref;
    }

    public T getVal() {
        return val;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setVal(T val_) {
        val = val_;
    }

    public void setForeignSymbolTableReference(String label, int reference_) {
        foreignSymbolTableReferences.put(label, reference_);
    }

    public void setForeignSymbolTableReference(int reference_) {
        foreignSymbolTableReferences.put("default", reference_);
    }

    public Integer getForeignSymbolTableReference(String label) {
        return foreignSymbolTableReferences.get(label);
    }

    public Integer getForeignSymbolTableReference() {
        return getForeignSymbolTableReference("default");
    }
}