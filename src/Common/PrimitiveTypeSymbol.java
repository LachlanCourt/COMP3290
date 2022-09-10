/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class represents a primitive type symbol in the symbol table
 *******************************************************************************/
package Common;

import Common.SymbolTable.PrimitiveTypes;
import Common.SymbolTable.SymbolType;

public class PrimitiveTypeSymbol extends Symbol {
    private PrimitiveTypes val;

    public PrimitiveTypeSymbol(SymbolType symbolType_, String ref_, PrimitiveTypes val_) {
        // This kind of symbol behaves the same as a regular symbol but just has a type value so
        // call the parent constructor
        super(symbolType_, ref_);
        val = val_;
    }

    public PrimitiveTypes getVal() {
        return val;
    }

    public void setVal(PrimitiveTypes val_) {
        val = val_;
    }
}
