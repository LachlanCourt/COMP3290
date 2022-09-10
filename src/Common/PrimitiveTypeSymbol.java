package Common;

import Common.SymbolTable.PrimitiveTypes;
import Common.SymbolTable.SymbolType;

public class PrimitiveTypeSymbol extends Symbol {
    private PrimitiveTypes val;

    public PrimitiveTypeSymbol(SymbolType symbolType_, String ref_, PrimitiveTypes val_) {
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
