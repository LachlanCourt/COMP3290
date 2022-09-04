/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class represents a symbol in the symbol table
 *******************************************************************************/
package Common;

import Common.SymbolTable.SymbolType;

public class Symbol<T extends Comparable> {
    // The id assigned by the symbol table
    private final String ref;
    // The data value of the symbol
    private T val;

    private final SymbolType symbolType;

    private Integer foreignSymbolTableReference;

    public Symbol(SymbolType symbolType_, String ref_, T val_) {
        symbolType = symbolType_;
        ref = ref_;
        val = val_;
    }

    public Symbol(SymbolType symbolType_, String ref_) {
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

    public void setForeignSymbolTableReference(int reference_) {
        foreignSymbolTableReference = reference_;
    }

    public Integer getForeignSymbolTableReference() {
        return foreignSymbolTableReference;
    }
}