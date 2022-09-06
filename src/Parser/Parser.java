/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class is a placeholder for a Parser for the CD22 programming language.
 ****    in this project it runs a debug routine to output tokens found by the scanner
 ****    until EOF
 *******************************************************************************/
package Parser;

import Common.Symbol;
import Common.SymbolTable;
import Common.SymbolTable.PrimitiveTypes;
import Common.SymbolTable.SymbolType;
import Parser.TreeNode.TreeNodes;
import Scanner.Scanner;
import Scanner.Token;
import Scanner.Token.Tokens;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokenStream;
    private int tokenStreamIndex;
    private Scanner scanner;
    private Token lookahead;
    private Token previousLookahead;

    private TreeNode syntaxTree;

    private SymbolTable symbolTable;

    private String currentScope;

    public Parser(Scanner s_, SymbolTable symbolTable_) {
        scanner = s_;
        symbolTable = symbolTable_;
        tokenStreamIndex = 0;
    }

    /**
     * Initialisation function
     */
    public void initialise() {
        tokenStream = scanner.getTokenStream();
        currentScope = "@global";
    }

    public Token getToken() {
        if (tokenStreamIndex < tokenStream.size()) {
            return tokenStream.get(tokenStreamIndex++);
        } else {
            return tokenStream.get(tokenStream.size() - 1);
        }
    }

    private void error(String message, Token token) {
        System.err.println("ERROR");
        System.err.println(message);
        System.exit(1);
    }

    private void error(String message) {
        System.err.println("ERROR");
        System.err.println(message);
        System.exit(1);
    }

    private void match(Tokens token) {
        if (token == lookahead.getToken()) {
            previousLookahead = lookahead;
            lookahead = getToken();
        } else
            error("Failed to match " + token + " around " + previousLookahead.getRow() + ":"
                + previousLookahead.getCol());
    }

    /**
     * Main run method of the parser
     */
    public void run() {
        lookahead = getToken();
        syntaxTree = program();
        if (!lookahead.isEof()) {
            error("Not at eof");
        }
    }

    public ArrayList<Token> parseIdentifierFollowedByColon() {
        ArrayList<Token> list = new ArrayList<Token>();
        if (lookahead.getToken() == Tokens.TIDEN) {
            list.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error("Expected Identifier");
        }
        match(Tokens.TCOLN);
        return list;
    }

    public ArrayList<Token> parseColonSeparatedIdentifiers() {
        ArrayList<Token> list = new ArrayList<Token>();
        list.add(parseIdentifierFollowedByColon().get(0));
        if (lookahead.getToken() == Tokens.TIDEN) {
            list.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error("Expected Identifier");
        }
        return list;
    }

    public ArrayList<Token> parseColonSeparatedIdentifiers(Token first) {
        ArrayList<Token> list = new ArrayList<Token>();
        list.add(first);
        match(Tokens.TCOLN);
        if (lookahead.getToken() == Tokens.TIDEN) {
            list.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error("Expected Identifier");
        }
        return list;
    }

    private TreeNode program() {
        TreeNode t = new TreeNode(TreeNodes.NPROG);
        match(Tokens.TCD22);
        if (lookahead.getToken() == Tokens.TIDEN) {
            t.setSymbolTableId(
                symbolTable.addSymbol(SymbolTable.SymbolType.PROGRAM_IDEN, lookahead));
            match(Tokens.TIDEN);
        } else {
            error("Program name is missing");
        }
        t.setNextChild(globals());
        t.setNextChild(funcs());
        t.setNextChild(mainbody());
        match(Tokens.TTEOF);
        return t;
    }

    private TreeNode globals() {
        TreeNode t = new TreeNode(TreeNodes.NGLOB);
        t.setNextChild(consts());
        t.setNextChild(types());
        t.setNextChild(arrays());

        return t;
    }

    private TreeNode consts() {
        if (lookahead.getToken() == Tokens.TCONS) {
            match(Tokens.TCONS);
            return initlist();
        }
        return null;
    }

    private TreeNode initlist() {
        TreeNode t1 = init(), t2 = null;
        if (lookahead.getToken() == Tokens.TTYPS || lookahead.getToken() == Tokens.TARRS
            || lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = initlist();
        }
        return new TreeNode(TreeNodes.NILIST, t1, t2);
    }

    private TreeNode init() {
        TreeNode t = new TreeNode(TreeNodes.NINIT);
        int symbolTableId = 0;
        if (lookahead.getToken() == Tokens.TIDEN) {
            symbolTableId = symbolTable.addSymbol(SymbolType.CONSTANT, lookahead);
            t.setSymbolTableId(symbolTableId);
            match(Tokens.TIDEN);
        } else {
            error("Missing identifier");
        }
        match(Tokens.TEQUL);
        TreeNode exprNode = expr();
        symbolTable.getSymbol(symbolTableId)
            .setForeignSymbolTableId(exprNode.getSymbolTableId());
        t.setNextChild(exprNode);
        return t;
    }

    private TreeNode types() {
        if (lookahead.getToken() == Tokens.TTYPS) {
            match(Tokens.TTYPS);
            return typelist();
        }
        return null;
    }

    private TreeNode typelist() {
        TreeNode t1 = type(), t2 = null;
        if (lookahead.getToken() == Tokens.TARRS || lookahead.getToken() == Tokens.TFUNC
            || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        t2 = typelist();
        return new TreeNode(TreeNodes.NTYPEL, t1, t2);
    }

    private TreeNode type() {
        TreeNode t = new TreeNode();
        Token typeNameToken = null;
        if (lookahead.getToken() == Tokens.TIDEN) {
            typeNameToken = lookahead;
            match(Tokens.TIDEN);
        } else {
            error("Missing identifier");
        }
        match(Tokens.TTDEF);
        if (lookahead.getToken() == Tokens.TARAY) {
            // Array
            int symbolTableId = symbolTable.addSymbol(SymbolType.ARRAY_TYPE, typeNameToken);
            t.setSymbolTableId(symbolTableId);

            match(Tokens.TARAY);
            match(Tokens.TLBRK);
            TreeNode exprNode = expr();
            t.setNextChild(exprNode);
            match(Tokens.TRBRK);
            match(Tokens.TTTOF);
            int typeReference = 0;
            if (lookahead.getToken() == Tokens.TIDEN) {
                typeReference =
                    symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
                match(Tokens.TIDEN);
            } else {
                error("Missing identifier");
            }
            t.setNodeType(TreeNodes.NATYPE);

            Symbol newSymbol =
                symbolTable.getSymbol(symbolTable.addSymbol(SymbolType.ARRAY_TYPE, typeNameToken));
            newSymbol.setForeignSymbolTableId(typeReference);
            newSymbol.setForeignSymbolTableId("size", exprNode.getSymbolTableId());

        } else if (lookahead.getToken() == Tokens.TIDEN) {
            // Struct
            int symbolTableId = symbolTable.addSymbol(SymbolType.STRUCT_TYPE, typeNameToken);
            t.setSymbolTableId(symbolTableId);

            t.setNextChild(fields());
            t.setNodeType(TreeNodes.NRTYPE);
        }
        match(Tokens.TTEND);
        return t;
    }

    private TreeNode fields() {
        TreeNode t1 = sdecl(), t2 = null;
        if (lookahead.getToken() == Tokens.TTEND) {
            return t1;
        } else if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = fields();
        }
        return new TreeNode(TreeNodes.NFLIST, t1, t2);
    }

    private TreeNode sdecl() {
        return sdecl(parseIdentifierFollowedByColon().get(0));
    }

    private TreeNode sdecl(Token nameIdenToken) {
        int symbolTableId = symbolTable.addSymbol(SymbolType.VARIABLE, nameIdenToken, currentScope);
        // Grab the symbol table ID for this node either from the NTDECL parent node or from the
        // variable name referencing it
        TreeNode t = new TreeNode(TreeNodes.NTDECL, symbolTableId);
        if (lookahead.getToken() == Tokens.TIDEN) {
            // structid
            int typeReference =
                symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
            symbolTable.getSymbol(symbolTableId).setForeignSymbolTableId(typeReference);
            match(Tokens.TIDEN);
        } else {
            // stype
            symbolTable.getSymbol(symbolTableId).setVal(stype());
            t.setNodeType(TreeNodes.NSDECL);
        }
        return t;
    }

    private PrimitiveTypes stype() {
        switch (lookahead.getToken()) {
            case TINTG:
                match(Tokens.TINTG);
                return PrimitiveTypes.INTEGER;
            case TFLOT:
                match(Tokens.TFLOT);
                return PrimitiveTypes.FLOAT;
            case TBOOL:
                match(Tokens.TBOOL);
                return PrimitiveTypes.BOOLEAN;
            default:
                return PrimitiveTypes.UNKNOWN;
        }
    }

    private TreeNode arrays() {
        if (lookahead.getToken() == Tokens.TARRS) {
            match(Tokens.TARRS);
            return arrdecls();
        }
        return null;
    }

    private TreeNode arrdecls() {
        TreeNode t1 = arrdecl(), t2 = null;
        if (lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = arrdecls();
        }
        return new TreeNode(TreeNodes.NALIST, t1, t2);
    }

    private TreeNode arrdecl() {
        ArrayList<Token> idenList = parseColonSeparatedIdentifiers();
        return arrdecl(idenList);
    }

    /**
     * Requires colon to have already been parsed. Used to look ahead at the next identifier and
     * then call arrdecl once it is known that it is an array type
     * @param nameIdenToken
     * @return
     */
    private TreeNode arrdecl(Token nameIdenToken) {
        ArrayList<Token> idenList = new ArrayList<Token>();
        idenList.add(nameIdenToken);
        if (lookahead.getToken() == Tokens.TIDEN) {
            idenList.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error("Expected Identifier");
        }
        return arrdecl(idenList);
    }

    private TreeNode arrdecl(ArrayList<Token> idenList) {
        TreeNode t = new TreeNode(TreeNodes.NARRD);

        int symbolTableId =
            symbolTable.addSymbol(SymbolType.VARIABLE, idenList.get(0), currentScope);
        t.setSymbolTableId(symbolTableId);

        int typeId =
            symbolTable.getSymbolIdFromReference(idenList.get(1).getTokenLiteral(), currentScope);
        symbolTable.getSymbol(symbolTableId).setForeignSymbolTableId(typeId);

        return t;
    }

    private TreeNode expr() {
        TreeNode t = new TreeNode();
        t.setNextChild(term());
        // Sets node type, if it is not epsilon
        t.setNextChild(exprr(t));
        if (t.getNodeType() == null) {
            // Epsilon path of recursive exprr rule. If no following token was found, just return
            // the term as its own node
            return t.getLeft();
        }
        return t;
    }

    private TreeNode exprr(TreeNode t) {
        if (lookahead.getToken() == Tokens.TPLUS) {
            match(Tokens.TPLUS);
            t.setNodeType(TreeNodes.NADD);
            return term();
        } else if (lookahead.getToken() == Tokens.TMINS) {
            match(Tokens.TMINS);
            t.setNodeType(TreeNodes.NSUB);
            return term();
        }
        return t;
    }

    private TreeNode term() {
        TreeNode t = new TreeNode();
        t.setNextChild(fact());
        // Sets node type, if it is not epsilon
        t.setNextChild(termr(t));
        if (t.getNodeType() == null) {
            // Epsilon path of recursive termr rule. If no following token was found, just return
            // the fact as its own node
            return t.getLeft();
        }
        return t;
    }

    private TreeNode termr(TreeNode t) {
        if (lookahead.getToken() == Tokens.TSTAR) {
            match(Tokens.TSTAR);
            t.setNodeType(TreeNodes.NMUL);
            return fact();
        } else if (lookahead.getToken() == Tokens.TDIVD) {
            match(Tokens.TDIVD);
            t.setNodeType(TreeNodes.NDIV);
            return fact();

        } else if (lookahead.getToken() == Tokens.TPERC) {
            match(Tokens.TPERC);
            t.setNodeType(TreeNodes.NMOD);
            return fact();
        }
        return t;
    }

    private TreeNode fact() {
        TreeNode t = new TreeNode();
        t.setNextChild(exponent());
        // Sets node type, if it is not epsilon
        t.setNextChild(factr(t));
        if (t.getNodeType() == null) {
            // Epsilon path of recursive factr rule. If no following token was found, just return
            // the exponent as its own node
            return t.getLeft();
        }
        return t;
    }

    private TreeNode factr(TreeNode t) {
        if (lookahead.getToken() == Tokens.TCART) {
            match(Tokens.TCART);
            t.setNodeType(TreeNodes.NPOW);
            return exponent();
        }
        return t;
    }

    private TreeNode exponent() {
        if (lookahead.getToken() == Tokens.TILIT) {
            TreeNode t =
                new TreeNode(TreeNodes.NILIT, symbolTable.addSymbol(SymbolType.LITERAL, lookahead));
            match(Tokens.TILIT);
            return t;
        } else if (lookahead.getToken() == Tokens.TFLIT) {
            TreeNode t =
                new TreeNode(TreeNodes.NFLIT, symbolTable.addSymbol(SymbolType.LITERAL, lookahead));
            match(Tokens.TFLIT);
            return t;
        } else if (lookahead.getToken() == Tokens.TTRUE) {
            TreeNode t =
                new TreeNode(TreeNodes.NTRUE, symbolTable.addSymbol(SymbolType.LITERAL, lookahead));
            match(Tokens.TTRUE);
            return t;
        } else if (lookahead.getToken() == Tokens.TFALS) {
            TreeNode t =
                new TreeNode(TreeNodes.NFALS, symbolTable.addSymbol(SymbolType.LITERAL, lookahead));
            match(Tokens.TFALS);
            return t;
        } else if (lookahead.getToken() == Tokens.TLPAR) {
            match(Tokens.TLPAR);
            TreeNode t = bool();
            match(Tokens.TRPAR);
            return t;
        } else if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            if (lookahead.getToken() == Tokens.TLPAR) {
                return fncall(token);
            }
            // If the node is not a function call, it's either an ID or an array accessor. Either
            // way start by passing the id token straight to the overridden function
            return var(token);
        }
        error("Not a number");
        return null;
    }

    private TreeNode bool() {
        TreeNode t = new TreeNode(TreeNodes.NBOOL);
        t.setNextChild(rel());
        boolr(t);
        return t;
    }

    private TreeNode boolr(TreeNode t) {
        t.setNextChild(logop());
        if (t.getMid() != null) {
            t.setNextChild(rel());
        }
        return t;
    }

    private TreeNode logop() {
        if (lookahead.getToken() == Tokens.TTAND) {
            match(Tokens.TTAND);
            return new TreeNode(TreeNodes.NAND);
        } else if (lookahead.getToken() == Tokens.TTTOR) {
            match(Tokens.TTTOR);
            return new TreeNode(TreeNodes.NOR);
        } else if (lookahead.getToken() == Tokens.TTXOR) {
            match(Tokens.TTXOR);
            return new TreeNode(TreeNodes.NXOR);
        }
        return null;
    }

    private TreeNode rel() {
        boolean not = false;
        if (lookahead.getToken() == Tokens.TNOTT) {
            match(Tokens.TNOTT);
            not = true;
            //            return new TreeNode(TreeNodes.NNOT, expr(), relop(), expr());
        }
        TreeNode exprNode = expr();
        TreeNode relopNode = relop();
        if (relopNode == null) {
            if (not) {
                return new TreeNode(TreeNodes.NNOT, exprNode);
            }
            return exprNode;
        }
        relopNode.setNextChild(exprNode);
        relopNode.setNextChild(expr());
        if (not) {
            return new TreeNode(TreeNodes.NNOT, relopNode);
        }
        return relopNode;
    }

    private TreeNode relop() {
        if (lookahead.getToken() == Tokens.TEQEQ) {
            match(Tokens.TEQEQ);
            return new TreeNode(TreeNodes.NEQL);
        } else if (lookahead.getToken() == Tokens.TNEQL) {
            match(Tokens.TNEQL);
            return new TreeNode(TreeNodes.NNEQ);
        } else if (lookahead.getToken() == Tokens.TGRTR) {
            match(Tokens.TGRTR);
            return new TreeNode(TreeNodes.NGRT);
        } else if (lookahead.getToken() == Tokens.TLESS) {
            match(Tokens.TLESS);
            return new TreeNode(TreeNodes.NLSS);
        } else if (lookahead.getToken() == Tokens.TLEQL) {
            match(Tokens.TLEQL);
            return new TreeNode(TreeNodes.NLEQ);
        } else if (lookahead.getToken() == Tokens.TGEQL) {
            match(Tokens.TGEQL);
            return new TreeNode(TreeNodes.NGEQ);
        }
        return null;
    }

    private TreeNode fncall() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return fncall(token);
        }
        error("Expected identifier");
        return null;
    }

    private TreeNode fncall(Token nameIdenToken) {
        TreeNode t = new TreeNode(TreeNodes.NFCALL,
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), "@global"));
        match(Tokens.TLPAR);
        if (lookahead.getToken() != Tokens.TRPAR) {
            t.setNextChild(elist());
        }
        match(Tokens.TRPAR);
        return t;
    }

    private TreeNode elist() {
        TreeNode t1 = bool(), t2 = null;
        if (lookahead.getToken() == Tokens.TRPAR) {
            return t1;
        }
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = elist();
        }
        return new TreeNode(TreeNodes.NEXPL, t1, t2);
    }

    private TreeNode var() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return var(token);
        }
        error("Expected identifier");
        return null;
    }

    private TreeNode var(Token nameIdenToken) {
        int symbolTableId =
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), currentScope);
        if (lookahead.getToken() == Tokens.TLBRK) {
            TreeNode t = new TreeNode();
            match(Tokens.TLBRK);
            t.setSymbolTableId(symbolTableId);
            t.setNextChild(expr());
            match(Tokens.TRBRK);
            // Access struct field
            if (lookahead.getToken() == Tokens.TDOTT) {
                // Change the node type as we know now that it is a field or a struct
                t.setNodeType(TreeNodes.NARRV);
                match(Tokens.TDOTT);
                if (lookahead.getToken() == Tokens.TIDEN) {
                    int fieldTypeReference = symbolTable.getSymbolIdFromReference(
                        lookahead.getTokenLiteral(), currentScope);
                    match(Tokens.TIDEN);
                    t.setNextChild(new TreeNode(TreeNodes.NSIMV, fieldTypeReference));
                }
            } else {
                // If the if didn't run it is not accessing a struct field, but is instead the
                // entire struct
                t.setNodeType(TreeNodes.NAELT);
            }
            // Either return with whole struct or individual field if the above if ran
            return t;
        }
        // Simple variable
        return new TreeNode(TreeNodes.NSIMV, symbolTableId);
    }

    private TreeNode mainbody() {
        TreeNode t = new TreeNode(TreeNodes.NMAIN);
        match(Tokens.TMAIN);
        t.setNextChild(slist());
        match(Tokens.TBEGN);
        t.setNextChild(stats());
        match(Tokens.TTEND);
        match(Tokens.TCD22);
        if (lookahead.getToken() == Tokens.TIDEN) {
            int symbolTableId =
                symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
            match(Tokens.TIDEN);
            t.setNextChild(new TreeNode(TreeNodes.NSIMV, symbolTableId));
        } else {
            error("Missing identifier");
        }
        return t;
    }

    private TreeNode slist() {
        TreeNode t1 = sdecl(), t2 = null;
        if (lookahead.getToken() == Tokens.TBEGN) {
            return t1;
        }
        match(Tokens.TCOMA);
        t2 = slist();
        return new TreeNode(TreeNodes.NSDLST, t1, t2);
    }

    private TreeNode stats() {
        TreeNode t = null;
        // Assign t1
        if (lookahead.getToken() == Tokens.TTFOR || lookahead.getToken() == Tokens.TIFTH) {
            t = strstat();
        } else if (lookahead.getToken() == Tokens.TREPT || lookahead.getToken() == Tokens.TIDEN
            || lookahead.getToken() == Tokens.TINPT || lookahead.getToken() == Tokens.TPRNT
            || lookahead.getToken() == Tokens.TPRLN || lookahead.getToken() == Tokens.TRETN) {
            t = stat();
            match(Tokens.TSEMI);
        }
        if (t != null) {
            // Epsilon path
            if (lookahead.getToken() != Tokens.TTFOR && lookahead.getToken() != Tokens.TIFTH
                && lookahead.getToken() != Tokens.TREPT && lookahead.getToken() != Tokens.TIDEN
                && lookahead.getToken() != Tokens.TINPT && lookahead.getToken() != Tokens.TPRNT
                && lookahead.getToken() != Tokens.TPRLN && lookahead.getToken() != Tokens.TRETN) {
                return t;
            }
            return new TreeNode(TreeNodes.NSTATS, t, stats());
        }
        error("At least one statement is required");
        return null;
    }

    private TreeNode stat() {
        if (lookahead.getToken() == Tokens.TREPT) {
            return reptstat();
        } else if (lookahead.getToken() == Tokens.TINPT || lookahead.getToken() == Tokens.TPRNT
            || lookahead.getToken() == Tokens.TPRLN) {
            return iostat();
        } else if (lookahead.getToken() == Tokens.TRETN) {
            return returnstat();
        }
        Token token = lookahead;
        match(Tokens.TIDEN);
        if (lookahead.getToken() == Tokens.TLPAR) {
            return callstat(token);
        } else {
            return asgnstat(token);
        }
    }

    private TreeNode reptstat() {
        TreeNode t = new TreeNode(TreeNodes.NREPT);
        match(Tokens.TREPT);
        match(Tokens.TLPAR);
        TreeNode asgnlistNode = asgnlist();
        if (asgnlistNode != null)
            t.setNextChild(asgnlistNode);
        match(Tokens.TRPAR);
        t.setNextChild(stats());
        match(Tokens.TUNTL);
        t.setNextChild(bool());
        return t;
    }

    private TreeNode iostat() {
        if (lookahead.getToken() == Tokens.TINPT) {
            match(Tokens.TINPT);
            return new TreeNode(TreeNodes.NINPUT, vlist());
        } else if (lookahead.getToken() == Tokens.TPRNT) {
            match(Tokens.TPRNT);
            return new TreeNode(TreeNodes.NPRINT, prlist());
        } else {
            match(Tokens.TPRLN);
            return new TreeNode(TreeNodes.NPRLN, prlist());
        }
    }

    private TreeNode returnstat() {
        TreeNode t = new TreeNode(TreeNodes.NRETN);
        match(Tokens.TRETN);
        if (lookahead.getToken() == Tokens.TVOID) {
            match(Tokens.TVOID);
        } else {
            t.setNextChild(expr());
        }
        return t;
    }

    private TreeNode strstat() {
        if (lookahead.getToken() == Tokens.TTFOR) {
            return forstat();
        }
        return ifstat();
    }

    private TreeNode callstat() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return callstat(token);
        }
        error("Expected identifier");
        return null;
    }

    private TreeNode callstat(Token nameIdenToken) {
        TreeNode t = new TreeNode(TreeNodes.NCALL,
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), currentScope));
        match(Tokens.TLPAR);
        if (lookahead.getToken() != Tokens.TRPAR) {
            t.setNextChild(elist());
        }
        match(Tokens.TRPAR);
        return t;
    }

    private TreeNode asgnstat() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return asgnstat(token);
        }
        error("Expected identifier");
        return null;
    }

    private TreeNode asgnstat(Token nameIdenToken) {
        TreeNode varNode = var(nameIdenToken);
        TreeNode t = asgnop();
        t.setSymbolTableId(varNode.getSymbolTableId());
        t.setNextChild(varNode);
        t.setNextChild(bool());
        return t;
    }

    private TreeNode forstat() {
        TreeNode t = new TreeNode(TreeNodes.NFORL);
        match(Tokens.TTFOR);
        match(Tokens.TLPAR);
        TreeNode asgnlistNode = asgnlist();
        if (asgnlistNode != null)
            t.setNextChild(asgnlistNode);
        match(Tokens.TSEMI);
        t.setNextChild(bool());
        match(Tokens.TRPAR);
        t.setNextChild(stats());
        match(Tokens.TTEND);
        return t;
    }

    private TreeNode ifstat() {
        TreeNode t = new TreeNode();
        match(Tokens.TIFTH);
        match(Tokens.TLPAR);
        t.setNextChild(bool());
        match(Tokens.TRPAR);
        t.setNextChild(stats());
        if (lookahead.getToken() == Tokens.TTEND) {
            t.setNodeType(TreeNodes.NIFTH);
            match(Tokens.TTEND);
        } else if (lookahead.getToken() == Tokens.TELSE) {
            t.setNodeType(TreeNodes.NIFTE);
            match(Tokens.TELSE);
            t.setNextChild(stats());
            match(Tokens.TTEND);
        } else {
            t.setNodeType(TreeNodes.NIFEF);
            match(Tokens.TELIF);
            match(Tokens.TLPAR);
            TreeNode childNode = new TreeNode(TreeNodes.NIFTH);
            childNode.setNextChild(bool());
            match(Tokens.TRPAR);
            childNode.setNextChild(stats());
            match(Tokens.TTEND);
            t.setNextChild(childNode);
        }
        return t;
    }

    private TreeNode asgnlist() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            return alist();
        }
        return null;
    }

    private TreeNode alist() {
        TreeNode t1 = asgnstat(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = alist();
        }
        return new TreeNode(TreeNodes.NASGNS, t1, t2);
    }

    private TreeNode asgnop() {
        if (lookahead.getToken() == Tokens.TEQUL) {
            match(Tokens.TEQUL);
            return new TreeNode(TreeNodes.NASGN);
        } else if (lookahead.getToken() == Tokens.TPLEQ) {
            match(Tokens.TPLEQ);
            return new TreeNode(TreeNodes.NPLEQ);
        } else if (lookahead.getToken() == Tokens.TMNEQ) {
            match(Tokens.TMNEQ);
            return new TreeNode(TreeNodes.NMNEQ);
        } else if (lookahead.getToken() == Tokens.TSTEQ) {
            match(Tokens.TSTEQ);
            return new TreeNode(TreeNodes.NSTEA);
        } else if (lookahead.getToken() == Tokens.TDVEQ) {
            match(Tokens.TDVEQ);
            return new TreeNode(TreeNodes.NDVEQ);
        }
        error("Expected assignment operator");
        return null;
    }

    private TreeNode vlist() {
        TreeNode t1 = var(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = vlist();
        }
        return new TreeNode(TreeNodes.NVLIST, t1, t2);
    }

    private TreeNode prlist() {
        TreeNode t1 = printitem(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = prlist();
        }
        return new TreeNode(TreeNodes.NPRLST, t1, t2);
    }

    private TreeNode printitem() {
        if (lookahead.getToken() == Tokens.TSTRG) {
            int symbolTableId = symbolTable.addSymbol(SymbolType.LITERAL, lookahead);
            match(Tokens.TSTRG);
            return new TreeNode(TreeNodes.NSTRG, symbolTableId);
        }
        return expr();
    }

    private TreeNode funcs() {
        if (lookahead.getToken() != Tokens.TFUNC) {
            return null;
        }
        TreeNode t = new TreeNode(TreeNodes.NFUNCS, func());
        if (lookahead.getToken() == Tokens.TFUNC) {
            t.setNextChild(funcs());
        }
        return t;
    }

    private TreeNode func() {
        TreeNode t = new TreeNode(TreeNodes.NFUND);
        match(Tokens.TFUNC);
        int symbolTableId = 0;
        if (lookahead.getToken() == Tokens.TIDEN) {
            symbolTableId = symbolTable.addSymbol(SymbolType.FUNCTION, lookahead);
            currentScope = lookahead.getTokenLiteral();
            match(Tokens.TIDEN);
            t.setSymbolTableId(symbolTableId);
        } else {
            error("Expected Identifier");
        }
        match(Tokens.TLPAR);
        TreeNode plistNode = plist();
        if (plistNode != null)
            t.setNextChild(plistNode);
        match(Tokens.TRPAR);
        match(Tokens.TCOLN);
        symbolTable.getSymbol(symbolTableId).setVal(rtype());
        TreeNode funcbodyNode = funcbody();
        t.setNextChild(funcbodyNode.getLeft());
        t.setNextChild(funcbodyNode.getMid());
        currentScope = "@global";
        return t;
    }

    private TreeNode plist() {
        if (lookahead.getToken() == Tokens.TIDEN || lookahead.getToken() == Tokens.TCNST) {
            return params();
        }
        return null;
    }

    private PrimitiveTypes rtype() {
        if (lookahead.getToken() == Tokens.TVOID) {
            match(Tokens.TVOID);
            return PrimitiveTypes.VOID;
        }
        return stype();
    }

    private TreeNode funcbody() {
        TreeNode t = new TreeNode();
        t.setNextChild(locals());
        match(Tokens.TBEGN);
        t.setNextChild(stats());
        match(Tokens.TTEND);
        return t;
    }

    private TreeNode params() {
        TreeNode t1 = param(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = params();
        }
        return new TreeNode(TreeNodes.NPLIST, t1, t2);
    }

    private TreeNode param() {
        if (lookahead.getToken() == Tokens.TCNST) {
            match(Tokens.TCNST);
            TreeNode t = new TreeNode(TreeNodes.NARRC, arrdecl());

            // Add const to array
            symbolTable.getSymbol(t.getLeft().getSymbolTableId()).makeConstArray();
            return t;
        } else if (lookahead.getToken() == Tokens.TIDEN) {
            Token nameIdenToken = parseIdentifierFollowedByColon().get(0);
            int symbolTableId =
                symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
            if (symbolTableId == -1) {
                // The type does not exist in the symbol table, so it must be a primitive type, or
                // undefined. Parse as sdecl
                return new TreeNode(TreeNodes.NSIMP, sdecl(nameIdenToken));
            }
            if (symbolTable.getSymbol(symbolTableId).getSymbolType() == SymbolType.STRUCT_TYPE) {
                // The symbol exists and is a struct, can also be parsed as sdecl
                return new TreeNode(TreeNodes.NSIMP, sdecl(nameIdenToken));
            } else if (symbolTable.getSymbol(symbolTableId).getSymbolType()
                == SymbolType.ARRAY_TYPE) {
                // The symbol exists and is an array, parse as arrdecl
                return new TreeNode(TreeNodes.NSIMP, arrdecl(nameIdenToken));
            }
            return null;
        }
        return null;
    }

    private TreeNode locals() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            return dlist();
        }
        return null;
    }

    private TreeNode dlist() {
        TreeNode t1 = decl(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = dlist();
        }
        return new TreeNode(TreeNodes.NDLIST, t1, t2);
    }

    private TreeNode decl() {
        Token nameIdenToken = parseIdentifierFollowedByColon().get(0);
        int symbolTableId =
            symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
        if (symbolTableId == -1) {
            // The type does not exist in the symbol table, so it must be a primitive type, or
            // undefined. Parse as sdecl
            return sdecl(nameIdenToken);
        }
        if (symbolTable.getSymbol(symbolTableId).getSymbolType() == SymbolType.STRUCT_TYPE) {
            // Struct type, also parse as sdecl
            return sdecl(nameIdenToken);
        } else if (symbolTable.getSymbol(symbolTableId).getSymbolType() == SymbolType.ARRAY_TYPE) {
            // The symbol exists and is an array, parse as arrdecl
            return arrdecl(nameIdenToken);
        }
        return null;
    }

    private String outputHelper(TreeNode node, boolean debug) {
        if (node == null)
            return "";

        String data = "";
        if (debug) {
            data = "<" + node.toString(false) + "> ";
            data += node.getTokenString();
        } else {
            data = node.toString();
        }

        for (int i = 0; i < 3; i++) {
            data += outputHelper(node.getChildByIndex(i), debug);
        }

        if (debug) {
            data += "</" + node.toString(false) + "> ";
        }
        return data;
    }

    @Override
    public String toString() {
        boolean debug =
            System.getenv("DEBUG") != null && System.getenv("DEBUG").compareTo("true") == 0;
        String[] treeList = outputHelper(syntaxTree, debug).split("\\s");
        String formattedTree = "";
        String line = "";
        for (int i = 0; i < treeList.length; i++) {
            line += treeList[i] + " ";
            if (line.length() >= 70) {
                formattedTree += line + "\n";
                line = "";
            }
        }
        if (line.length() > 0)
            formattedTree += line;
        return formattedTree;
    }

    public TreeNode getSyntaxTree() {
        return syntaxTree;
    }
}
