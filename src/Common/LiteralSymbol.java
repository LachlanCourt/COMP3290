package Common;

import Common.SymbolTable.SymbolType;

public class LiteralSymbol extends Symbol {
    String val;

    public LiteralSymbol(SymbolType symbolType_, String ref_, String val_) {
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
