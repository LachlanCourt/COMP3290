/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class represents a single node in the parser's syntax tree
 *******************************************************************************/
package Parser;

import Common.LiteralSymbol;
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
        NSTRV,
    }

    private TreeNodes nodeType;
    private TreeNode left;
    private TreeNode mid;
    private TreeNode right;

    int symbolTableId;

    // A reference to the symbol table is kept so that symbols can be stored as references rather than whole symbols
    SymbolTable symbolTable;

    // Default constructor which sets default variables
    public TreeNode() {
        symbolTable = SymbolTable.getSymbolTable();
    }

    // Various constructors for creating nodes at different points through a non-terminal expansion

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

    public void setNextChild(TreeNode child) {
        if (left == null) {
            left = child;
        } else if (mid == null) {
            mid = child;
        } else {
            right = child;
        }
    }

    /**
     * Facilitates iterating over the children of the node by enumerating them
     * @param index of the child 0-2 which matches left-right
     * @return the child node of the specified index
     */
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

    // Getters and Setters

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

    /**
     * Default to string function, returning out the node name and the token literal
      * @return stringified version of the token and its lexeme
     */
    @Override
    public String toString() {
        return nodeType.name() + getTokenString() + " ";
    }

    /**
     * To string function returning either the node name and it's lexeme or just the node name
     * @param includeData flag indicating whether the lexeme should be included
     * @return stringified version of the token, optionally including its lexeme
     */
    public String toString(boolean includeData) {
        if (includeData) {
            return toString();
        }
        return nodeType.name();
    }

    /**
     * Gets the associated lexeme from the symbol table, if the node type has one associated with it
     * @return the associated lexeme if it exists, or an empty string if not
     */
    public String getTokenString() {
        switch (nodeType) {
            // These node types will always have an entry in the table as a LiteralSymble
            case NSTRG:
            case NILIT:
            case NFLIT:
                return " " + ((LiteralSymbol) symbolTable.getSymbol(symbolTableId)).getVal();

            // These ndoe types will always have an entry in the symbol table as a regular symbol with a lexeme. If
            // they don't, null is returned for the purposes of error checking
            case NPROG:
            case NSIMV:
            case NSTRV:
            case NRTYPE:
            case NATYPE:
            case NSDECL:
            case NTDECL:
            case NINIT:
            case NARRD:
                Symbol symbol = symbolTable.getSymbol(symbolTableId);
                if (symbol == null) {
                    return " null";
                }
                return " " + symbol.getRef();
        }
        return "";
    }
}
