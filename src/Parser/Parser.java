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

import Common.*;
import Common.ErrorMessage.Errors;
import Common.SymbolTable.PrimitiveTypes;
import Common.SymbolTable.SymbolType;
import Parser.TreeNode.TreeNodes;
import Scanner.Scanner;
import Scanner.Token;
import Scanner.Token.Tokens;
import java.io.EOFException;
import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokenStream;
    private int tokenStreamIndex;
    private final Scanner scanner;
    private Token lookahead;
    private Token previousLookahead;

    private TreeNode syntaxTree;

    private final SymbolTable symbolTable;

    private String currentScope;

    private final OutputController outputController;
    private final Utils utils;

    public Parser(Scanner s_, SymbolTable symbolTable_, OutputController outputController_) {
        scanner = s_;
        symbolTable = symbolTable_;
        tokenStreamIndex = 0;
        outputController = outputController_;
        utils = Utils.getUtils();
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

    private void error(String message) throws CD22ParserException {
        outputController.addError(
            previousLookahead.getRow(), previousLookahead.getCol(), Errors.CUSTOM_ERROR, message);
        throw new CD22ParserException();
    }

    private void error(Errors error) throws CD22ParserException {
        outputController.addError(previousLookahead.getRow(), previousLookahead.getCol(), error);
        throw new CD22ParserException();
    }

    private void error(Errors error, String message) throws CD22ParserException {
        outputController.addError(
            previousLookahead.getRow(), previousLookahead.getCol(), error, message);
        throw new CD22ParserException();
    }

    private void errorWithoutException(Errors error) {
        outputController.addError(previousLookahead.getRow(), previousLookahead.getCol(), error);
    }

    private void errorWithoutException(String message) {
        outputController.addError(
            previousLookahead.getRow(), previousLookahead.getCol(), Errors.CUSTOM_ERROR, message);
    }

    private void errorWithoutException(Errors error, String message) {
        outputController.addError(
            previousLookahead.getRow(), previousLookahead.getCol(), error, message);
    }

    private void match(Tokens token) throws CD22ParserException {
        if (token == lookahead.getToken()) {
            previousLookahead = lookahead;
            lookahead = getToken();
        } else
            error("Expected \"" + utils.getInitialiserFromToken(token) + "\"");
    }

    /**
     * Main run method of the parser
     */
    public void run() {
        lookahead = getToken();
        // Prevent null pointer exceptions if the program is missing the CD22 keyword
        previousLookahead = lookahead;
        try {
            syntaxTree = program();
        } catch (CD22ParserException e) {
            // We want to catch the exception so the program doesn't crash, but errors will be
            // outputted to the user via the output controller so no need to do anything here
        } catch (CD22EofException e) {
            // End of file reached while in panic mode error recovery. The error that set off panic
            // mode will exist in the error handler so no need to do anything here
        }
        if (!lookahead.isEof() && !outputController.hasErrors()) {
            errorWithoutException(Errors.NOT_AT_EOF);
        }
    }

    // ----------------- ERROR RECOVERY ------------------
    public void panic(ArrayList<Tokens> synchronisingTokens) throws CD22EofException {
        while (lookahead.getToken() != Tokens.TTEOF) {
            if (synchronisingTokens.contains(lookahead.getToken())) {
                return;
            }
            lookahead = getToken();
        }
        throw new CD22EofException();
    }

    private void gracefullyMatchColon() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TCOLN) {
            match(Tokens.TCOLN);
            return;
        }
        if (lookahead.getToken() == Tokens.TSEMI) {
            match(Tokens.TSEMI);
            errorWithoutException("Found \";\" instead of \":\"");
            return;
        } else if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            errorWithoutException("Found \",\" instead of \":\"");
            return;
        }
        errorWithoutException("Expected \":\"");
    }

    private void gracefullyMatchSemicolon() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TSEMI) {
            match(Tokens.TSEMI);
            return;
        }
        if (lookahead.getToken() == Tokens.TCOLN) {
            match(Tokens.TCOLN);
            errorWithoutException("Found \":\" instead of \";\"");
            return;
        } else if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            errorWithoutException("Found \",\" instead of \";\"");
            return;
        }
        errorWithoutException("Expected \";\"");
    }

    public ArrayList<Token> parseIdentifierFollowedByColon() throws CD22ParserException {
        ArrayList<Token> list = new ArrayList<Token>();
        if (lookahead.getToken() == Tokens.TIDEN) {
            list.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        gracefullyMatchColon();
        return list;
    }

    public ArrayList<Token> parseColonSeparatedIdentifiers() throws CD22ParserException {
        ArrayList<Token> list = new ArrayList<Token>();
        list.add(parseIdentifierFollowedByColon().get(0));
        if (lookahead.getToken() == Tokens.TIDEN) {
            list.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        return list;
    }

    private TreeNode program() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NPROG);

        try {
            // These are really just semantics so if they are not there we should in theory still be
            // able to parse the rest of the program
            match(Tokens.TCD22);
            if (lookahead.getToken() == Tokens.TIDEN) {
                t.setSymbolTableId(
                    symbolTable.addSymbol(SymbolTable.SymbolType.PROGRAM_IDEN, lookahead));
                match(Tokens.TIDEN);
            } else {
                error(Errors.PROGRAM_IDEN_MISSING);
            }
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(
                Tokens.TCONS, Tokens.TARRS, Tokens.TTYPS, Tokens.TFUNC, Tokens.TMAIN));
        }

        t.setNextChild(globals());
        t.setNextChild(funcs());
        t.setNextChild(mainbody());
        if (!lookahead.isEof()) {
            error(Errors.NOT_AT_EOF);
            throw new CD22EofException();
        }
        return t;
    }

    private TreeNode globals() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NGLOB);
        t.setNextChild(consts());

        if (lookahead.getToken() != Tokens.TTYPS && lookahead.getToken() != Tokens.TARRS
            && lookahead.getToken() != Tokens.TFUNC && lookahead.getToken() != Tokens.TMAIN) {
            errorWithoutException("Unexpected token \"" + lookahead.getTokenLiteral() + "\"");
            // Prevent silent errors if the "Types" keyword is missing but types are declared - as
            // all these fields are optional, without this check error recovery will skip all the
            // way to main and miss all arrays and functions
            panic(utils.getTokenList(Tokens.TTYPS, Tokens.TARRS, Tokens.TFUNC, Tokens.TMAIN));
        }

        t.setNextChild(types());

        if (lookahead.getToken() != Tokens.TARRS && lookahead.getToken() != Tokens.TFUNC
            && lookahead.getToken() != Tokens.TMAIN) {
            errorWithoutException("Unexpected token \"" + lookahead.getTokenLiteral() + "\"");
            // Prevent silent errors
            panic(utils.getTokenList(Tokens.TARRS, Tokens.TFUNC, Tokens.TMAIN));
        }

        t.setNextChild(arrays());

        if (lookahead.getToken() != Tokens.TFUNC && lookahead.getToken() != Tokens.TMAIN) {
            errorWithoutException("Unexpected token \"" + lookahead.getTokenLiteral() + "\"");
            // Prevent silent errors
            panic(utils.getTokenList(Tokens.TFUNC, Tokens.TMAIN));
        }

        return t;
    }

    private TreeNode consts() throws CD22ParserException, CD22EofException {
        if (lookahead.getToken() == Tokens.TCONS) {
            match(Tokens.TCONS);
            return initlist();
        }
        return null;
    }

    private TreeNode initlist() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2 = null;

        try {
            t1 = init();
        } catch (CD22ParserException e) {
            // Resynchronise to either a comma, globals keyword, func, or main keywords
            panic(utils.getTokenList(
                Tokens.TCOMA, Tokens.TTYPS, Tokens.TARRS, Tokens.TFUNC, Tokens.TMAIN));
        }

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

    private TreeNode init() throws CD22ParserException {
        TreeNode t = new TreeNode(TreeNodes.NINIT);
        int symbolTableId = 0;
        if (lookahead.getToken() == Tokens.TIDEN) {
            symbolTableId = symbolTable.addSymbol(SymbolType.CONSTANT, lookahead);
            t.setSymbolTableId(symbolTableId);
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        match(Tokens.TEQUL);
        TreeNode exprNode = expr();
        symbolTable.getSymbol(symbolTableId).setForeignSymbolTableId(exprNode.getSymbolTableId());
        t.setNextChild(exprNode);
        return t;
    }

    private TreeNode types() throws CD22ParserException, CD22EofException {
        if (lookahead.getToken() == Tokens.TTYPS) {
            match(Tokens.TTYPS);
            return typelist();
        }
        return null;
    }

    private TreeNode typelist() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2 = null;

        try {
            t1 = type();
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TTEND, Tokens.TARRS, Tokens.TFUNC, Tokens.TMAIN));
            if (lookahead.getToken() == Tokens.TTEND) {
                match(Tokens.TTEND);
            }
        }

        if (lookahead.getToken() == Tokens.TARRS || lookahead.getToken() == Tokens.TFUNC
            || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        t2 = typelist();

        return new TreeNode(TreeNodes.NTYPEL, t1, t2);
    }

    private TreeNode type() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode();
        Token typeNameToken = null;
        if (lookahead.getToken() == Tokens.TIDEN) {
            typeNameToken = lookahead;
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
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
            int typeId = 0;
            if (lookahead.getToken() == Tokens.TIDEN) {
                typeId =
                    symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
                if (typeId == -1)
                    errorWithoutException(Errors.UNDEFINED_TYPE);
                match(Tokens.TIDEN);
            } else {
                error(Errors.EXPECTED_IDENTIFIER);
            }
            t.setNodeType(TreeNodes.NATYPE);

            Symbol newSymbol =
                symbolTable.getSymbol(symbolTable.addSymbol(SymbolType.ARRAY_TYPE, typeNameToken));
            newSymbol.setForeignSymbolTableId(typeId);
            newSymbol.setForeignSymbolTableId("size", exprNode.getSymbolTableId());

        } else if (lookahead.getToken() == Tokens.TIDEN) {
            // Struct
            int symbolTableId = symbolTable.addSymbol(SymbolType.STRUCT_TYPE, typeNameToken);
            t.setSymbolTableId(symbolTableId);
            currentScope = typeNameToken.getTokenLiteral();
            t.setNextChild(fields());
            currentScope = "@global";
            t.setNodeType(TreeNodes.NRTYPE);
        } else {
            // This edge case handles if we enter panic mode error recovery whilst within a struct
            // or array definition. Is slightly different from other lists as types are not
            // separated by commas or semicolons
            return null;
        }
        match(Tokens.TTEND);
        return t;
    }

    private TreeNode fields() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2 = null;

        try {
            t1 = sdecl(false);
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TCOMA, Tokens.TTEND, Tokens.TARRS, Tokens.TFUNC));
        }

        if (lookahead.getToken() == Tokens.TTEND) {
            return t1;
        } else if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = fields();
        }
        return new TreeNode(TreeNodes.NFLIST, t1, t2);
    }

    private TreeNode sdecl(boolean allowStructTypes) throws CD22ParserException {
        return sdecl(parseIdentifierFollowedByColon().get(0), allowStructTypes);
    }

    private TreeNode sdecl() throws CD22ParserException {
        return sdecl(parseIdentifierFollowedByColon().get(0), true);
    }

    private TreeNode sdecl(Token nameIdenToken) throws CD22ParserException {
        return sdecl(nameIdenToken, true);
    }

    private TreeNode sdecl(Token nameIdenToken, boolean allowStructTypes)
        throws CD22ParserException {
        // Grab the symbol table ID for this node either from the NTDECL parent node or from the
        // variable name referencing it
        TreeNode t = new TreeNode(TreeNodes.NTDECL);
        if (lookahead.getToken() == Tokens.TIDEN && allowStructTypes) {
            // structid
            int symbolTableId =
                symbolTable.addSymbol(SymbolType.VARIABLE, nameIdenToken, currentScope);
            t.setSymbolTableId(symbolTableId);
            int typeId =
                symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
            if (typeId == -1)
                errorWithoutException(Errors.UNDEFINED_TYPE);
            symbolTable.getSymbol(symbolTableId).setForeignSymbolTableId(typeId);
            match(Tokens.TIDEN);
        } else {
            // stype
            int symbolTableId =
                symbolTable.addSymbol(SymbolType.VARIABLE, nameIdenToken, currentScope, true);
            t.setSymbolTableId(symbolTableId);

            PrimitiveTypeSymbol typeSymbol =
                (PrimitiveTypeSymbol) symbolTable.getSymbol(symbolTableId);
            typeSymbol.setVal(stype());
            t.setNodeType(TreeNodes.NSDECL);
        }
        return t;
    }

    private PrimitiveTypes stype() throws CD22ParserException {
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
                error(Errors.UNDEFINED_TYPE);
                return PrimitiveTypes.UNKNOWN;
        }
    }

    private TreeNode arrays() throws CD22ParserException, CD22EofException {
        if (lookahead.getToken() == Tokens.TARRS) {
            match(Tokens.TARRS);
            return arrdecls();
        }
        return null;
    }

    private TreeNode arrdecls() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2 = null;

        try {
            t1 = arrdecl();
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TCOMA, Tokens.TFUNC, Tokens.TMAIN));
        }

        if (lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = arrdecls();
        }
        return new TreeNode(TreeNodes.NALIST, t1, t2);
    }

    private TreeNode arrdecl() throws CD22ParserException {
        ArrayList<Token> idenList = parseColonSeparatedIdentifiers();
        return arrdecl(idenList);
    }

    /**
     * Requires colon to have already been parsed. Used to look ahead at the next identifier and
     * then call arrdecl once it is known that it is an array type
     * @param nameIdenToken
     * @return
     */
    private TreeNode arrdecl(Token nameIdenToken) throws CD22ParserException {
        ArrayList<Token> idenList = new ArrayList<Token>();
        idenList.add(nameIdenToken);
        if (lookahead.getToken() == Tokens.TIDEN) {
            idenList.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        return arrdecl(idenList);
    }

    private TreeNode arrdecl(ArrayList<Token> idenList) throws CD22ParserException {
        TreeNode t = new TreeNode(TreeNodes.NARRD);

        int symbolTableId =
            symbolTable.addSymbol(SymbolType.VARIABLE, idenList.get(0), currentScope);
        t.setSymbolTableId(symbolTableId);

        int typeId =
            symbolTable.getSymbolIdFromReference(idenList.get(1).getTokenLiteral(), currentScope);
        if (typeId == -1 || symbolTable.getSymbol(typeId).getSymbolType() != SymbolType.ARRAY_TYPE)
            errorWithoutException(Errors.UNDEFINED_TYPE);
        symbolTable.getSymbol(symbolTableId).setForeignSymbolTableId(typeId);

        return t;
    }

    private TreeNode expr() throws CD22ParserException {
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

    private TreeNode exprr(TreeNode t) throws CD22ParserException {
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

    private TreeNode term() throws CD22ParserException {
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

    private TreeNode termr(TreeNode t) throws CD22ParserException {
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

    private TreeNode fact() throws CD22ParserException {
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

    private TreeNode factr(TreeNode t) throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TCART) {
            match(Tokens.TCART);
            t.setNodeType(TreeNodes.NPOW);
            return exponent();
        }
        return t;
    }

    private TreeNode exponent() throws CD22ParserException {
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
        error(Errors.NOT_A_NUMBER);
        return null;
    }

    private TreeNode bool() throws CD22ParserException {
        TreeNode t = new TreeNode();
        t.setNextChild(rel());
        boolr(t);
        if (t.getNodeType() == null) {
            return t.getLeft();
        }
        return t;
    }

    private TreeNode boolr(TreeNode t) throws CD22ParserException {
        t.setNextChild(logop());
        if (t.getMid() != null) {
            t.setNodeType(TreeNodes.NBOOL);
            t.setNextChild(rel());
        }
        return t;
    }

    private TreeNode logop() throws CD22ParserException {
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

    private TreeNode rel() throws CD22ParserException {
        boolean not = false;
        if (lookahead.getToken() == Tokens.TNOTT) {
            match(Tokens.TNOTT);
            not = true;
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

    private TreeNode relop() throws CD22ParserException {
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

    private TreeNode fncall(Token nameIdenToken) throws CD22ParserException {
        TreeNode t = new TreeNode(TreeNodes.NFCALL,
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), "@global"));
        match(Tokens.TLPAR);
        if (lookahead.getToken() != Tokens.TRPAR) {
            t.setNextChild(elist());
        }
        match(Tokens.TRPAR);
        return t;
    }

    private TreeNode elist() throws CD22ParserException {
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

    private TreeNode var() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return var(token);
        }
        error(Errors.EXPECTED_IDENTIFIER);
        return null;
    }

    private TreeNode var(Token nameIdenToken) throws CD22ParserException {
        int symbolTableId =
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), currentScope);
        if (symbolTableId == -1)
            error(Errors.UNDEFINED_VARIABLE, nameIdenToken.getTokenLiteral());
        if (lookahead.getToken() == Tokens.TLBRK) {
            TreeNode t = new TreeNode();
            t.setSymbolTableId(symbolTableId);
            match(Tokens.TLBRK);
            t.setNextChild(expr());
            match(Tokens.TRBRK);
            // Access struct field
            if (lookahead.getToken() == Tokens.TDOTT) {
                matchStructVar(t, symbolTableId);
                // Change the node type as we know now that it is a field or a struct
                t.setNodeType(TreeNodes.NARRV);

            } else {
                // If the if didn't run it is not accessing a struct field, but is instead the
                // entire struct
                t.setNodeType(TreeNodes.NAELT);
            }
            // Either return with whole struct or individual field if the above if ran
            return t;
        } else if (lookahead.getToken() == Tokens.TDOTT) {
            TreeNode t = matchStructVar(new TreeNode(), symbolTableId);
            // Change the node type as we know now struct variable
            t.setNodeType(TreeNodes.NSTRV);
            return t;
        }
        // Simple variable
        return new TreeNode(TreeNodes.NSIMV, symbolTableId);
    }

    public TreeNode matchStructVar(TreeNode t, int variableNameId) throws CD22ParserException {
        t.setSymbolTableId(variableNameId);
        match(Tokens.TDOTT);
        if (lookahead.getToken() == Tokens.TIDEN) {
            // Get type of variableNameId, either an array or a struct
            int varTypeId = symbolTable.getSymbol(variableNameId).getForeignSymbolTableId();
            // Get type of the struct, assume first that it is a struct but if it is an array pull
            // the type off the array's default foreign ID
            int structTypeId = varTypeId;
            if (symbolTable.getSymbol(varTypeId).getSymbolType() == SymbolType.ARRAY_TYPE) {
                // Arrays require an extra lookup
                structTypeId = symbolTable.getSymbol(varTypeId).getForeignSymbolTableId();
            }
            // Using the struct name as symbol table scope, check if the identified field exists
            // within the struct and add an error if it does not
            int fieldId = symbolTable.getSymbolIdFromReference(
                lookahead.getTokenLiteral(), symbolTable.getSymbol(structTypeId).getRef(), false);
            if (fieldId == -1)
                errorWithoutException(Errors.UNDEFINED_VARIABLE, lookahead.getTokenLiteral());
            match(Tokens.TIDEN);
            t.setNextChild(new TreeNode(TreeNodes.NSIMV, fieldId));
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        return t;
    }

    private TreeNode mainbody() throws CD22ParserException, CD22EofException {
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
            t.setSymbolTableId(symbolTableId);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        return t;
    }

    private TreeNode slist() throws CD22ParserException {
        TreeNode t1 = sdecl(), t2 = null;
        if (lookahead.getToken() == Tokens.TBEGN) {
            return t1;
        }
        match(Tokens.TCOMA);
        t2 = slist();
        return new TreeNode(TreeNodes.NSDLST, t1, t2);
    }

    private TreeNode stats() throws CD22ParserException, CD22EofException {
        TreeNode t = null;
        // Assign t1
        if (lookahead.getToken() == Tokens.TTFOR || lookahead.getToken() == Tokens.TIFTH) {
            try {
                t = strstat();
            } catch (CD22ParserException e) {
                // Without knowing where the for or if statements end, we can only resynchronise on
                // an "END". If we resynchronised on the same tokens as stats, we would resynch
                // inside the for/if block but execution would not be inside the forstat or ifstat
                // functions. In this case the end of what would be an if or for stat becomes the
                // end of the main function and we break out of stats altogether. Trust that the
                // developer remembered the end keyword and resync here only.
                panic(utils.getTokenList(Tokens.TTEND));
                match(Tokens.TTEND);
                return stats();
            }
        } else if (lookahead.getToken() == Tokens.TREPT || lookahead.getToken() == Tokens.TIDEN
            || lookahead.getToken() == Tokens.TINPT || lookahead.getToken() == Tokens.TPRNT
            || lookahead.getToken() == Tokens.TPRLN || lookahead.getToken() == Tokens.TRETN) {
            try {
                t = stat();
                gracefullyMatchSemicolon();
            } catch (CD22ParserException e) {
                panic(utils.getTokenList(Tokens.TSEMI, Tokens.TTFOR, Tokens.TIFTH, Tokens.TREPT,
                    Tokens.TINPT, Tokens.TPRNT, Tokens.TPRLN, Tokens.TRETN));
                if (lookahead.getToken() == Tokens.TSEMI) {
                    gracefullyMatchSemicolon();
                }
                return stats();
            }
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
        error(Errors.NO_STATEMENTS);
        return null;
    }

    private TreeNode stat() throws CD22ParserException, CD22EofException {
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

    private TreeNode reptstat() throws CD22ParserException, CD22EofException {
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

    private TreeNode iostat() throws CD22ParserException {
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

    private TreeNode returnstat() throws CD22ParserException {
        TreeNode t = new TreeNode(TreeNodes.NRETN);
        match(Tokens.TRETN);
        if (lookahead.getToken() == Tokens.TVOID) {
            match(Tokens.TVOID);
        } else {
            t.setNextChild(expr());
        }
        return t;
    }

    private TreeNode strstat() throws CD22ParserException, CD22EofException {
        if (lookahead.getToken() == Tokens.TTFOR) {
            return forstat();
        }
        return ifstat();
    }

    private TreeNode callstat(Token nameIdenToken) throws CD22ParserException {
        TreeNode t = new TreeNode(TreeNodes.NCALL,
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), currentScope));
        match(Tokens.TLPAR);
        if (lookahead.getToken() != Tokens.TRPAR) {
            t.setNextChild(elist());
        }
        match(Tokens.TRPAR);
        return t;
    }

    private TreeNode asgnstat() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return asgnstat(token);
        }
        error(Errors.EXPECTED_IDENTIFIER);
        return null;
    }

    private TreeNode asgnstat(Token nameIdenToken) throws CD22ParserException {
        TreeNode varNode = var(nameIdenToken);
        TreeNode t = asgnop();
        t.setSymbolTableId(varNode.getSymbolTableId());
        t.setNextChild(varNode);
        t.setNextChild(bool());
        return t;
    }

    private TreeNode forstat() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NFORL);
        match(Tokens.TTFOR);
        match(Tokens.TLPAR);
        TreeNode asgnlistNode = asgnlist();
        if (asgnlistNode != null)
            t.setNextChild(asgnlistNode);
        gracefullyMatchSemicolon();
        t.setNextChild(bool());
        match(Tokens.TRPAR);
        t.setNextChild(stats());
        match(Tokens.TTEND);
        return t;
    }

    private TreeNode ifstat() throws CD22ParserException, CD22EofException {
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

    private TreeNode asgnlist() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TIDEN) {
            return alist();
        }
        return null;
    }

    private TreeNode alist() throws CD22ParserException {
        TreeNode t1 = asgnstat(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = alist();
        }
        return new TreeNode(TreeNodes.NASGNS, t1, t2);
    }

    private TreeNode asgnop() throws CD22ParserException {
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
        error(Errors.EXPECTED_ASSIGNMENT_OPERATOR);
        return null;
    }

    private TreeNode vlist() throws CD22ParserException {
        TreeNode t1 = var(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = vlist();
        }
        return new TreeNode(TreeNodes.NVLIST, t1, t2);
    }

    private TreeNode prlist() throws CD22ParserException {
        TreeNode t1 = printitem(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = prlist();
        }
        return new TreeNode(TreeNodes.NPRLST, t1, t2);
    }

    private TreeNode printitem() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TSTRG) {
            int symbolTableId = symbolTable.addSymbol(SymbolType.LITERAL, lookahead);
            match(Tokens.TSTRG);
            return new TreeNode(TreeNodes.NSTRG, symbolTableId);
        }
        return expr();
    }

    private TreeNode funcs() throws CD22ParserException, CD22EofException {
        if (lookahead.getToken() != Tokens.TFUNC) {
            return null;
        }
        TreeNode t = new TreeNode(TreeNodes.NFUNCS);

        try {
            t.setNextChild(func());
        } catch (CD22ParserException e) {
            // Something went wrong in the function we were just parsing. If the next token is
            // another function we can carry on parsing that from here, but if it is the main
            // function we should throw back up to there
            panic(utils.getTokenList(Tokens.TFUNC, Tokens.TMAIN));
            if (lookahead.getToken() == Tokens.TMAIN) {
                // Hot potato
                throw new CD22ParserException();
            }
        }

        if (lookahead.getToken() == Tokens.TFUNC) {
            t.setNextChild(funcs());
        }
        return t;
    }

    private TreeNode func() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NFUND);
        match(Tokens.TFUNC);
        int symbolTableId = 0;
        if (lookahead.getToken() == Tokens.TIDEN) {
            symbolTableId = symbolTable.addSymbol(SymbolType.FUNCTION, lookahead, "@global", true);
            currentScope = lookahead.getTokenLiteral();
            match(Tokens.TIDEN);
            t.setSymbolTableId(symbolTableId);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        match(Tokens.TLPAR);
        TreeNode plistNode = plist();
        if (plistNode != null)
            t.setNextChild(plistNode);
        match(Tokens.TRPAR);
        gracefullyMatchColon();
        ((PrimitiveTypeSymbol) symbolTable.getSymbol(symbolTableId)).setVal(rtype());
        TreeNode funcbodyNode = funcbody();
        t.setNextChild(funcbodyNode.getLeft());
        t.setNextChild(funcbodyNode.getMid());
        currentScope = "@global";
        return t;
    }

    private TreeNode plist() throws CD22ParserException, CD22EofException {
        if (lookahead.getToken() == Tokens.TIDEN || lookahead.getToken() == Tokens.TCNST) {
            return params();
        }
        return null;
    }

    private PrimitiveTypes rtype() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TVOID) {
            match(Tokens.TVOID);
            return PrimitiveTypes.VOID;
        }
        return stype();
    }

    private TreeNode funcbody() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode();
        t.setNextChild(locals());
        match(Tokens.TBEGN);
        t.setNextChild(stats());
        match(Tokens.TTEND);
        return t;
    }

    private TreeNode params() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2;

        try {
            t1 = param();
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TCOMA, Tokens.TRPAR));
        }

        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = params();
        }
        return new TreeNode(TreeNodes.NPLIST, t1, t2);
    }

    private TreeNode param() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TCNST) {
            match(Tokens.TCNST);
            TreeNode t = new TreeNode(TreeNodes.NARRC, arrdecl());

            // Add const to array
            symbolTable.getSymbol(t.getLeft().getSymbolTableId()).makeConstArray();
            return t;
        } else if (lookahead.getToken() == Tokens.TIDEN) {
            Token nameIdenToken = parseIdentifierFollowedByColon().get(0);
            int typeId =
                symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
            if (typeId == -1) {
                // The type does not exist in the symbol table, so it must be a primitive type, or
                // undefined. Parse as sdecl
                return new TreeNode(TreeNodes.NSIMP, sdecl(nameIdenToken));
            }
            if (symbolTable.getSymbol(typeId).getSymbolType() == SymbolType.STRUCT_TYPE) {
                // The symbol exists and is a struct, can also be parsed as sdecl
                return new TreeNode(TreeNodes.NSIMP, sdecl(nameIdenToken));
            } else if (symbolTable.getSymbol(typeId).getSymbolType() == SymbolType.ARRAY_TYPE) {
                // The symbol exists and is an array, parse as arrdecl
                return new TreeNode(TreeNodes.NSIMP, arrdecl(nameIdenToken));
            }
            return null;
        }
        return null;
    }

    private TreeNode locals() throws CD22ParserException, CD22EofException {
        if (lookahead.getToken() == Tokens.TIDEN) {
            return dlist();
        }
        return null;
    }

    private TreeNode dlist() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2;

        try {
            t1 = decl();
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TCOMA, Tokens.TBEGN, Tokens.TFUNC, Tokens.TMAIN));
            if (lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
                // Hot potato
                throw new CD22ParserException();
            }
        }

        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = dlist();
        }
        return new TreeNode(TreeNodes.NDLIST, t1, t2);
    }

    private TreeNode decl() throws CD22ParserException {
        Token nameIdenToken = parseIdentifierFollowedByColon().get(0);
        int typeId =
            symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
        if (typeId == -1) {
            // The type does not exist in the symbol table, so it must be a primitive type, or
            // undefined. Parse as sdecl
            return sdecl(nameIdenToken);
        }
        if (symbolTable.getSymbol(typeId).getSymbolType() == SymbolType.STRUCT_TYPE) {
            // Struct type, also parse as sdecl
            return sdecl(nameIdenToken);
        } else if (symbolTable.getSymbol(typeId).getSymbolType() == SymbolType.ARRAY_TYPE) {
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
        boolean debugEnvironment =
            System.getenv("DEBUG") != null && System.getenv("DEBUG").compareTo("true") == 0;

        String[] treeList = outputHelper(syntaxTree, debugEnvironment).split("\\s");
        String formattedTree = "";
        String line = "";
        for (int i = 0; i < treeList.length; i++) {
            String outValue = treeList[i];
            int size = (outValue.length() / 7) * 7 + 7;
            line += outValue + " ".repeat(size - outValue.length());
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
