package Parser;

import Common.Symbol;
import Common.SymbolTable;

public class TreeNode {
    enum TreeNodes {
        NPROG,
        NGLOB,
        NILIST,
        NINIT,
        NILIT,
        NFLIT,
        NTYPEL,
        NATYPE,
        NRTYPE,
        NFLIST,
        NSDECL,
        NTDECL,
        NALIST,
        NARRD,
        NADD,
        NSUB,
        NMUL,
        NDIV,
        NMOD,
        NPOW,
        NTRUE,
        NFALS,
        NSIMV,
        NARRV,
        NAELT,
        NFCALL,
        NBOOL,
        NAND,
        NOR,
        NXOR,
        NNOT,
        NEQL,
        NNEQ,
        NGRT,
        NLSS,
        NLEQ,
        NGEQ,
        NEXPL,
        NMAIN,
        NSDLST,
        NSTATS,
        NREPT,
        NASGNS,
        NASGN,
        NPLEQ,
        NMNEQ,
        NSTEA,
        NDVEQ,
        NFORL,
        NIFTH,
        NIFTE,
        NIFEF,
        NINPUT,
        NPRINT,
        NPRLN,
        NVLIST,
        NPRLST,
        NSTRG,
        NCALL,
        NRETN,
        NFUNCS,
        NFUND,
        NPLIST,
        NSIMP,
        NARRP,
        NARRC,
        NDLIST,

    }

    private TreeNodes nodeType;
    private TreeNode left;
    private TreeNode mid;
    private TreeNode right;

    int symbolTableId;
    SymbolTable symbolTable;

    public TreeNode() {
        symbolTable = SymbolTable.getSymbolTable();
    }

    public TreeNode(TreeNodes type_) {
        this();
        nodeType = type_;
        left = null;
        mid = null;
        right = null;
    }

    public TreeNode(TreeNodes type_, int symbolTableReference_) {
        this();
        nodeType = type_;
        symbolTableId = symbolTableReference_;
        left = null;
        mid = null;
        right = null;
    }

    public TreeNode(TreeNodes type_, int symbolTableReference_, TreeNode left_) {
        this();
        nodeType = type_;
        symbolTableId = symbolTableReference_;
        left = left_;
        mid = null;
        right = null;
    }

    public TreeNode(TreeNodes type_, TreeNode left_) {
        this();
        nodeType = type_;
        left = left_;
        mid = null;
        right = null;
    }

    public TreeNode(TreeNodes type_, TreeNode left_, TreeNode mid_) {
        this();
        nodeType = type_;
        left = left_;
        mid = mid_;
        right = null;
    }

    public TreeNode(TreeNodes type_, TreeNode left_, TreeNode mid_, TreeNode right_) {
        this();
        nodeType = type_;
        left = left_;
        mid = mid_;
        right = right_;
    }

    public void setNextChild(TreeNode child) {
        if (left == null) {
            left = child;
        } else if (mid == null) {
            mid = child;
        } else {
            right = child;
        }
    }

    public TreeNode getChildByIndex(int index) {
        switch (index) {
            case 0:
                return left;
            case 1:
                return mid;
            case 2:
                return right;
        }
        return null;
    }

    public TreeNode getLeft() {
        return left;
    }

    public TreeNode getMid() {
        return mid;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setNodeType(TreeNodes type_) {
        nodeType = type_;
    }

    public TreeNodes getNodeType() {
        return nodeType;
    }

    public void setSymbolTableId(int reference) {
        symbolTableId = reference;
    }

    public int getSymbolTableId() {
        return symbolTableId;
    }

    @Override
    public String toString() {
        return nodeType.name() + getTokenString() + " ";
    }

    public String toString(boolean includeData) {
        if (includeData) {
            return toString();
        }
        return nodeType.name();
    }

    public String getTokenString() {
        switch (nodeType) {
            case NSTRG:
            case NILIT:
            case NFLIT:
                return " " + symbolTable.getSymbol(symbolTableId).getVal();

            case NPROG:
            case NSIMV:
            case NRTYPE:
            case NATYPE:
            case NSDECL:
            case NTDECL:
                Symbol symbol = symbolTable.getSymbol(symbolTableId);
                if (symbol == null) {
                    return " null";
                }
                return " " + symbol.getRef();
        }
        return "";
    }
}
