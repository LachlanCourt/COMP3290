/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class represents a symbol in the symbol table
 *******************************************************************************/

public class Symbol {
    // The id assigned by the symbol table
    private int id;
    // The data value of the symbol
    private String val;

    public Symbol(int id_, String val_) {
        id = id_;
        val = val_;
    }

    public int getId() {
        return id;
    }

    public String getVal() {
        return val;
    }
}
