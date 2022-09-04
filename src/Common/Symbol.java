/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class represents a symbol in the symbol table
 *******************************************************************************/
package Common;

import Common.SymbolTable.SymbolType;

public class Symbol {
    // The id assigned by the symbol table
    private String ref;
    // The data value of the symbol
    private String val;

    private SymbolType symbolType;

    public Symbol(SymbolType symbolType_, String ref_, String val_) {
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

    public String getVal() {
        return val;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setVal(String val_) {
        val = val_;
    }
}