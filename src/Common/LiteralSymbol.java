/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class represents a literal symbol in the symbol table
 *******************************************************************************/
package Common;

import Common.SymbolTable.SymbolType;

public class LiteralSymbol extends Symbol {
    // Literal values could be strings, integers or floats. Stored as strings for now for ease of
    // comparison
    String val;

    public LiteralSymbol(SymbolType symbolType_, String ref_, String val_) {
        // This kind of symbol behaves the same as a regular symbol but just has a literal value so
        // call the parent constructor
        super(symbolType_, ref_);
        val = val_;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val_) {
        val = val_;
    }
}
