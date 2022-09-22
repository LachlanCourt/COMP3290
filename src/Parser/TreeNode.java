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
import Common.Utils;
import Scanner.Token;

public class TreeNode {
    @SuppressWarnings("SpellCheckingInspection")
    public enum TreeNodes {
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

    public enum VariableTypes {
        INTEGER,
        FLOAT,
        BOOLEAN,
        COMPLEX,
        UNKNOWN
    }

    private TreeNodes nodeType;
    private TreeNode left;
    private TreeNode mid;
    private TreeNode right;

    int symbolTableId;

    // A reference to the symbol table is kept so that symbols can be stored as references rather
    // than whole symbols
    SymbolTable symbolTable;
    Utils utils;
    // Used for variables and expressions for type checking
    private VariableTypes expectedType;
    private int typeIdentifier;
    private VariableTypes nodeDataType;

    // Default constructor which sets default variables
    public TreeNode() {
        symbolTable = SymbolTable.getSymbolTable();
        utils = Utils.getUtils();
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

    public TreeNode(TreeNode left_) {
        this();
        nodeType = null;
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
     *
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

    public void setExpectedType(VariableTypes expectedType_) {
        expectedType = expectedType_;
        nodeDataType = expectedType_;
    }

    public void setExpectedType(VariableTypes expectedType_, int typeIdentifier_) {
        setExpectedType(expectedType_);
        typeIdentifier = typeIdentifier_;
    }

    public void setNodeDataType(VariableTypes type_) {
        nodeDataType = type_;
    }
    public VariableTypes getExpectedType() {
        return expectedType;
    }

    public int getTypeIdentifier() {
        return typeIdentifier;
    }

    public VariableTypes getNodeDataType() {
        return nodeDataType;
    }

    public int calculateNodeVariableTypeAndValue() {
        // Calculate the type first. If the types don't match, we can't calculate the value
        TreeNode leftChildNode, rightChildNode, operationNode;
        if (nodeType == TreeNodes.NBOOL) {
            leftChildNode = left;
            operationNode = mid;
            rightChildNode = right;
        } else {
            leftChildNode = left;
            operationNode = this;
            rightChildNode = mid;
        }

        if (leftChildNode.getNodeDataType() == VariableTypes.COMPLEX || rightChildNode.getNodeDataType() == VariableTypes.COMPLEX) {
            return -1;
        }

        // Primitive types don't match or if they are a complex type
        if (leftChildNode.getNodeDataType() != rightChildNode.getNodeDataType()) {
            // If the types don't match but are combined floats and integers, this is fine as the result will just become a float
            if ((leftChildNode.getNodeDataType() != VariableTypes.INTEGER && leftChildNode.getNodeDataType() != VariableTypes.FLOAT) || (rightChildNode.getNodeDataType() != VariableTypes.INTEGER && rightChildNode.getNodeDataType() != VariableTypes.FLOAT)) {
                return -1;
            } else {
                nodeDataType = VariableTypes.FLOAT;
            }
        } else {
            nodeDataType = leftChildNode.getNodeDataType();
        }

        // If the type has been successfully determined, we can now try to calculate the value and assign it to the
        // operation node
        utils.calculateValue(leftChildNode, rightChildNode, operationNode);

        // If it is possible to calculate the value at compile time, we can drop the children of this node and change
        // it to a literal value
        if (operationNode.getSymbolTableId() != -1 && symbolTable.getSymbol(operationNode.getSymbolTableId()) instanceof LiteralSymbol) {
            // Node value was able to be calculated at compile time
            String operationNodeValue = ((LiteralSymbol) symbolTable.getSymbol(operationNode.getSymbolTableId())).getVal();
            this.setSymbolTableId(-1);

            if (leftChildNode.getNodeDataType() == VariableTypes.BOOLEAN && rightChildNode.getNodeDataType() == VariableTypes.BOOLEAN) {
                if (Double.parseDouble(operationNodeValue) == 1) {
                    this.nodeType = TreeNodes.NTRUE;
                    this.nodeDataType = VariableTypes.BOOLEAN;
                    this.setSymbolTableId(symbolTable.addSymbol(SymbolTable.SymbolType.LITERAL, new Token(Token.Tokens.TTRUE, "1.0", 0, 0)));
                } else {
                    this.nodeType = TreeNodes.NFALS;
                    this.nodeDataType = VariableTypes.BOOLEAN;
                    this.setSymbolTableId(symbolTable.addSymbol(SymbolTable.SymbolType.LITERAL, new Token(Token.Tokens.TFALS, "0.0", 0, 0)));
                }
            } else if (leftChildNode.getNodeDataType() == VariableTypes.INTEGER && rightChildNode.getNodeDataType() == VariableTypes.INTEGER) {
                this.nodeType = TreeNodes.NILIT;
                this.nodeDataType = VariableTypes.INTEGER;
                // TODO should this be operationNode.getSymbolTableId()
                this.setSymbolTableId(symbolTable.addSymbol(SymbolTable.SymbolType.LITERAL, new Token(Token.Tokens.TILIT, String.valueOf(Double.valueOf(operationNodeValue).intValue()), 0, 0)));
            }else {
                this.nodeType = TreeNodes.NFLIT;
                this.nodeDataType = VariableTypes.FLOAT;
                this.setSymbolTableId(symbolTable.addSymbol(SymbolTable.SymbolType.LITERAL, new Token(Token.Tokens.TFLOT, operationNodeValue, 0, 0)));
            }
            this.left = this.mid = this.right = null;
        }

        return 0;
    }

    /**
     * Default to string function, returning out the node name and the token literal
     *
     * @return stringified version of the token and its lexeme
     */
    @Override
    public String toString() {
        return nodeType.name() + getTokenString() + " ";
    }

    /**
     * To string function returning either the node name and it's lexeme or just the node name
     *
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
     *
     * @return the associated lexeme if it exists, or an empty string if not
     */
    public String getTokenString() {
        switch (nodeType) {
            // These node types will always have an entry in the table as a LiteralSymbol
            case NSTRG:
            case NILIT:
            case NFLIT:
                Symbol literalSymbol = symbolTable.getSymbol(symbolTableId);
                return " "
                        + (literalSymbol instanceof LiteralSymbol
                        ? ((LiteralSymbol) symbolTable.getSymbol(symbolTableId)).getVal()
                        : "null");

            // These node types will always have an entry in the symbol table as a regular symbol
            // with a lexeme. If they don't, null is returned for the purposes of error checking
            case NPROG:
            case NSIMV:
            case NSTRV:
            case NRTYPE:
            case NATYPE:
            case NSDECL:
            case NTDECL:
            case NINIT:
            case NARRD:
                Symbol idenSymbol = symbolTable.getSymbol(symbolTableId);
                if (idenSymbol == null) {
                    return " null";
                }
                return " " + idenSymbol.getRef();
        }
        return "";
    }
}
