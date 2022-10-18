/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class managed the parsing for the CD22 programming language compiler.
 ****    It makes use of a recursive descent top-down algorithm to build a
 ****    syntax tree and adds values to the symbol table as they are found
 *******************************************************************************/
package Parser;

import Common.*;
import Common.ErrorMessage.Errors;
import Common.SymbolTable.PrimitiveTypes;
import Common.SymbolTable.SymbolType;
import Parser.TreeNode.TreeNodes;
import Parser.TreeNode.VariableTypes;
import Scanner.Scanner;
import Scanner.Token;
import Scanner.Token.Tokens;
import java.util.ArrayList;
import java.util.Queue;

@SuppressWarnings("SpellCheckingInspection")
public class Parser {
    private Queue<Token> tokenStream;
    private final Scanner scanner;
    private Token lookahead;
    private Token previousLookahead;

    private TreeNode syntaxTree;

    private final SymbolTable symbolTable;

    private String currentScope;
    private boolean foundReturnStatement;

    private final OutputController outputController;
    private final Utils utils;

    // Constructor
    public Parser(Scanner s_, SymbolTable symbolTable_, OutputController outputController_) {
        scanner = s_;
        symbolTable = symbolTable_;
        outputController = outputController_;
        utils = Utils.getUtils();
    }

    /**
     * Initialisation function, gets a token list from the scanner and prepares the parser for a
     * parse
     */
    public void initialise() {
        tokenStream = scanner.getTokenStream();
        currentScope = "@global";
    }

    /**
     * Gets a token from the token stream if there is more than one left, or repeatedly returns the
     * last one if there is only one
     *
     * @return the next token in the token stream
     */
    public Token getToken() {
        if (tokenStream.size() > 1) {
            return tokenStream.poll();
        } else {
            return tokenStream.peek();
        }
    }

    /**
     * Add a parser error to the error handler
     *
     * @param error   error type found
     * @param message additional error data if necessary
     */
    private void error(Errors error, String message) throws CD22ParserException {
        outputController.addError(
            previousLookahead.getRow(), previousLookahead.getCol(), error, message);
        throw new CD22ParserException();
    }

    /**
     * Add a parser error to the error handler
     *
     * @param message additional error data if necessary
     */
    private void error(String message) throws CD22ParserException {
        outputController.addError(
            previousLookahead.getRow(), previousLookahead.getCol(), Errors.CUSTOM_ERROR, message);
        throw new CD22ParserException();
    }

    /**
     * Add a parser error to the error handler
     *
     * @param error error type found
     */
    private void error(Errors error) throws CD22ParserException {
        outputController.addError(previousLookahead.getRow(), previousLookahead.getCol(), error);
        throw new CD22ParserException();
    }

    /**
     * Add a parser error to the error handler without throwing a parsing exception
     *
     * @param error   error type found
     * @param message additional error data if necessary
     */
    private void errorWithoutException(Errors error, String message) {
        outputController.addError(
            previousLookahead.getRow(), previousLookahead.getCol(), error, message);
    }

    /**
     * Add a parser error to the error handler without throwing a parsing exception
     *
     * @param error error type found
     */
    private void errorWithoutException(Errors error) {
        outputController.addError(previousLookahead.getRow(), previousLookahead.getCol(), error);
    }

    /**
     * Add a parser error to the error handler without throwing a parsing exception
     *
     * @param message additional error data if necessary
     */
    private void errorWithoutException(String message) {
        outputController.addError(
            previousLookahead.getRow(), previousLookahead.getCol(), Errors.CUSTOM_ERROR, message);
    }

    /**
     * Try to match the given token and throw an exception if a match is not found
     *
     * @param token to be matched with the lookahead token
     */
    private void match(Tokens token) throws CD22ParserException {
        if (token == lookahead.getToken()) {
            // The previous lookahead is used for error messages
            previousLookahead = lookahead;
            lookahead = getToken();
        } else
            error("Expected \"" + utils.getInitialiserFromToken(token) + "\"");
    }

    /**
     * Main run method of the parser
     */
    public void run() {
        // Get the first token
        lookahead = getToken();
        // Prevent null pointer exceptions if the program is missing the CD22 keyword
        previousLookahead = lookahead;
        try {
            // Kick off the recursive decent
            syntaxTree = program();
        } catch (CD22ParserException e) {
            // We want to catch the exception so the program doesn't crash, but errors will be
            // outputted to the user via the output controller so no need to do anything here
        } catch (CD22EofException e) {
            // End of file reached while in panic mode error recovery. The error that set off panic
            // mode will exist in the error handler so no need to do anything here
        }
        // If we have finished parsing and found "end CD22 identifier" but there are still tokens,
        // this is an error
        if (lookahead.isNotEof() && !outputController.hasErrors()) {
            errorWithoutException(Errors.NOT_AT_EOF);
        }
    }

    // ----------------- ERROR RECOVERY ------------------

    /**
     * Panic mode error recovery function. Retrieve a list of tokens that the caller could
     * resynchronise on, and consume tokens until one of those is found, or throws an exception if
     * the end of file is reached
     *
     * @param synchronisingTokens a list of tokens that can be used to resynchronise
     */
    public void panic(ArrayList<Tokens> synchronisingTokens) throws CD22EofException {
        // Loop while there are valid tokens
        while (lookahead.isNotEof()) {
            if (synchronisingTokens.contains(lookahead.getToken())) {
                return;
            }
            lookahead = getToken();
        }
        // End of file was reached without finding a synchronising token
        errorWithoutException(Errors.UNEXPECTED_EOF);
        throw new CD22EofException();
    }

    /**
     * Tries to match a colon but will also match semicolons, commas, or epsilon while adding an
     * error
     */
    private void gracefullyMatchColon() throws CD22ParserException {
        // Best case
        if (lookahead.getToken() == Tokens.TCOLN) {
            match(Tokens.TCOLN);
            return;
        }
        // A semicolon or a comma may be a typo by the developer
        if (lookahead.getToken() == Tokens.TSEMI) {
            match(Tokens.TSEMI);
            errorWithoutException("Found \";\" instead of \":\"");
            return;
        } else if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            errorWithoutException("Found \",\" instead of \":\"");
            return;
        }
        // A completely different token is here
        errorWithoutException("Expected \":\"");
    }

    /**
     * Tries to match a semicolon but will also match colons, commas, or epsilon while adding an
     * error
     */
    private void gracefullyMatchSemicolon() throws CD22ParserException {
        // Best case
        if (lookahead.getToken() == Tokens.TSEMI) {
            match(Tokens.TSEMI);
            return;
        }
        // A colon or a comma may be a typo by the developer
        if (lookahead.getToken() == Tokens.TCOLN) {
            match(Tokens.TCOLN);
            errorWithoutException("Found \":\" instead of \";\"");
            return;
        } else if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            errorWithoutException("Found \",\" instead of \";\"");
            return;
        }
        // A completely different token is here
        errorWithoutException("Expected \";\"");
    }

    // ----------------- GENERIC PARSING FUNCTIONS ------------------

    /**
     * Parses an identifier followed by a colon, and returns the identifier token found
     *
     * @return the identifier token found before the colon as an arraylist
     */
    public ArrayList<Token> parseIdentifierFollowedByColon() throws CD22ParserException {
        ArrayList<Token> list = new ArrayList<>();
        // Match the lookahead or add an error
        if (lookahead.getToken() == Tokens.TIDEN) {
            list.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        // Match the colon
        gracefullyMatchColon();
        return list;
    }

    /**
     * Parses two identifiers with a colon between them
     *
     * @return a list of both identifier tokens
     */
    public ArrayList<Token> parseColonSeparatedIdentifiers() throws CD22ParserException {
        ArrayList<Token> list = parseIdentifierFollowedByColon();
        if (lookahead.getToken() == Tokens.TIDEN) {
            list.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        return list;
    }

    //--------------------- START PARSE TREE ---------------------------

    /**
     * Program non-terminal
     *
     * @return program node
     */
    private TreeNode program() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NPROG);

        // Try parsing CD22 and the program identifier
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
            // Resync if CD22 or the identifier are missing
            panic(utils.getTokenList(
                Tokens.TCONS, Tokens.TARRS, Tokens.TTYPS, Tokens.TFUNC, Tokens.TMAIN));
        }

        // Primary program parsing
        t.setNextChild(globals());
        t.setNextChild(funcs());
        t.setNextChild(mainbody());
        if (lookahead.isNotEof()) {
            error(Errors.NOT_AT_EOF);
            throw new CD22EofException();
        }
        return t;
    }

    /**
     * Parse constants, types and arrays
     *
     * @return globals node
     */
    private TreeNode globals() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NGLOB);
        // Parse constants
        t.setNextChild(consts());

        // Resynchronise
        if (lookahead.getToken() != Tokens.TTYPS && lookahead.getToken() != Tokens.TARRS
            && lookahead.getToken() != Tokens.TFUNC && lookahead.getToken() != Tokens.TMAIN) {
            errorWithoutException("Unexpected token \"" + lookahead.getTokenLiteral() + "\"");
            // Prevent silent errors if the "Types" keyword is missing but types are declared - as
            // all these fields are optional, without this check error recovery will skip all the
            // way to main and miss all arrays and functions
            panic(utils.getTokenList(Tokens.TTYPS, Tokens.TARRS, Tokens.TFUNC, Tokens.TMAIN));
        }

        // Parse types
        t.setNextChild(types());

        // Resynchronise
        if (lookahead.getToken() != Tokens.TARRS && lookahead.getToken() != Tokens.TFUNC
            && lookahead.getToken() != Tokens.TMAIN) {
            errorWithoutException("Unexpected token \"" + lookahead.getTokenLiteral() + "\"");
            // Prevent silent errors
            panic(utils.getTokenList(Tokens.TARRS, Tokens.TFUNC, Tokens.TMAIN));
        }

        // Parse arrays
        t.setNextChild(arrays());

        // Resynchronise
        if (lookahead.getToken() != Tokens.TFUNC && lookahead.getToken() != Tokens.TMAIN) {
            errorWithoutException("Unexpected token \"" + lookahead.getTokenLiteral() + "\"");
            // Prevent silent errors
            panic(utils.getTokenList(Tokens.TFUNC, Tokens.TMAIN));
        }

        return t;
    }

    /**
     * Parse constants or epsilon
     *
     * @return initlist node or null
     */
    private TreeNode consts() throws CD22ParserException, CD22EofException {
        // Constants are optional
        if (lookahead.getToken() == Tokens.TCONS) {
            match(Tokens.TCONS);
            return initlist();
        }
        return null;
    }

    /**
     * Parse constants list
     *
     * @return initlist node
     */
    private TreeNode initlist() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2 = null;

        // Try parsing an init and resynchronise if it fails
        try {
            t1 = init();
        } catch (CD22ParserException e) {
            // Resynchronise to either a comma, globals keyword, func, or main keywords
            panic(utils.getTokenList(
                Tokens.TCOMA, Tokens.TTYPS, Tokens.TARRS, Tokens.TFUNC, Tokens.TMAIN));
        }

        // If a keyword for other sections of the program has been found we can stop parsing
        // constants
        if (lookahead.getToken() == Tokens.TTYPS || lookahead.getToken() == Tokens.TARRS
            || lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        // If there is a comma, parse another initlist recursively
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = initlist();
        }
        return new TreeNode(TreeNodes.NILIST, t1, t2);
    }

    /**
     * Parse a single constant
     *
     * @return init node
     */
    private TreeNode init() throws CD22ParserException {
        TreeNode t = new TreeNode(TreeNodes.NINIT);
        // Parse the identifier of the constant and create a symbol table entry
        int symbolTableId = 0;
        if (lookahead.getToken() == Tokens.TIDEN) {
            symbolTableId =
                symbolTable.addSymbol(SymbolType.CONSTANT, lookahead, currentScope, true);
            t.setSymbolTableId(symbolTableId);
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        match(Tokens.TEQUL);
        // Parse the value of the constant, and add the value to the variable symbol
        TreeNode exprNode = expr();
        Symbol contantSymbol = symbolTable.getSymbol(symbolTableId);
        contantSymbol.setForeignSymbolTableId(exprNode.getSymbolTableId());
        ((PrimitiveTypeSymbol) contantSymbol)
            .setVal(utils.resolveVariableTypeToPrimitiveType(exprNode.getNodeDataType()));
        t.setNextChild(exprNode);
        return t;
    }

    /**
     * Parse types
     *
     * @return typelist node or null
     */
    private TreeNode types() throws CD22ParserException, CD22EofException {
        // Types are optional
        if (lookahead.getToken() == Tokens.TTYPS) {
            match(Tokens.TTYPS);
            return typelist();
        }
        return null;
    }

    /**
     * Parse type list
     *
     * @return typeslist node
     */
    private TreeNode typelist() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2;

        // Try parsing a type and resync if it fails
        try {
            t1 = type();
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TTEND, Tokens.TARRS, Tokens.TFUNC, Tokens.TMAIN));
            if (lookahead.getToken() == Tokens.TTEND) {
                match(Tokens.TTEND);
            }
        }

        // If we have found another keyword from elsewhere in the program we can stop parsing types
        if (lookahead.getToken() == Tokens.TARRS || lookahead.getToken() == Tokens.TFUNC
            || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }

        // Types are not separated by commas so unless another important keyword has been found we
        // can recursively call the typelist again
        t2 = typelist();

        return new TreeNode(TreeNodes.NTYPEL, t1, t2);
    }

    /**
     * Parse a single type
     *
     * @return type node
     */
    private TreeNode type() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode();
        // Parse the name of the type
        Token typeNameToken = null;
        if (lookahead.getToken() == Tokens.TIDEN) {
            typeNameToken = lookahead;
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        match(Tokens.TTDEF);
        // Check if we are parsing an array or a struct type
        if (lookahead.getToken() == Tokens.TARAY) {
            // Array
            // Create a symbol with the type name
            int symbolTableId = symbolTable.addSymbol(SymbolType.ARRAY_TYPE, typeNameToken);
            t.setSymbolTableId(symbolTableId);

            match(Tokens.TARAY);
            match(Tokens.TLBRK);
            // Parse the length of the array, save the expr node for the type symbol table entry
            TreeNode exprNode = expr();
            // SEMANTICS array lengths must be integers
            if (exprNode.getNodeDataType() != VariableTypes.INTEGER) {
                error(Errors.REQUIRED_INTEGER);
            }
            t.setNextChild(exprNode);
            match(Tokens.TRBRK);
            match(Tokens.TTTOF);
            // Parse the struct type of the array or error if it is missing
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

            // Get the symbol added at the start of the type parsing and add foreign IDs to indicate
            // type and size
            Symbol newSymbol = symbolTable.getSymbol(symbolTableId);
            newSymbol.setForeignSymbolTableId(typeId);
            newSymbol.setForeignSymbolTableId("size", exprNode.getSymbolTableId());

        } else if (lookahead.getToken() == Tokens.TIDEN) {
            // Struct
            // Create a symbol with the type name
            int symbolTableId = symbolTable.addSymbol(SymbolType.STRUCT_TYPE, typeNameToken);
            t.setSymbolTableId(symbolTableId);
            // Set the scope as struct fiels are stored under the scope of the struct name in the
            // symbol table
            currentScope = typeNameToken.getTokenLiteral();
            // Parse the struct fields
            t.setNextChild(fields());
            // Reset the scope now that the struct parsing is complete
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

    /**
     * Parse the fields of a struct
     *
     * @return field list node
     */
    private TreeNode fields() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2 = null;

        // Try parsing a field and resync if fails
        try {
            t1 = sdecl(false);
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TCOMA, Tokens.TTEND, Tokens.TARRS, Tokens.TFUNC));
        }

        // If we see end we have finished parsing the struct, otherwise recursively parse another
        // field
        if (lookahead.getToken() == Tokens.TTEND) {
            return t1;
        } else if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = fields();
        }
        return new TreeNode(TreeNodes.NFLIST, t1, t2);
    }

    /**
     * Parse a simple declaration
     *
     * @param allowStructTypes flag indicating whether struct types are allowed in this sdecl
     * @return sdecl node
     */
    private TreeNode sdecl(boolean allowStructTypes) throws CD22ParserException {
        return sdecl(parseIdentifierFollowedByColon().get(0), allowStructTypes);
    }

    /**
     * Parse a simple declaration
     *
     * @return sdecl node
     */
    private TreeNode sdecl() throws CD22ParserException {
        return sdecl(parseIdentifierFollowedByColon().get(0), true);
    }

    /**
     * Parse a simple declaration
     *
     * @param nameIdenToken an iden token that has already been parsed that indicates a sdecl name
     * @return sdecl node
     */
    private TreeNode sdecl(Token nameIdenToken) throws CD22ParserException {
        return sdecl(nameIdenToken, true);
    }

    /**
     * Parse a simple declaration
     *
     * @param nameIdenToken    an iden token that has already been parsed that indicates a sdecl
     *                         name
     * @param allowStructTypes flag indicating whether struct types are allowed in this sdecl
     * @return sdecl node
     */
    private TreeNode sdecl(Token nameIdenToken, boolean allowStructTypes)
        throws CD22ParserException {
        TreeNode t = new TreeNode(TreeNodes.NTDECL);
        // If an identifier is found, and we are allowing struct types in this definition
        if (lookahead.getToken() == Tokens.TIDEN && allowStructTypes) {
            // structid
            // Add the sdecl to the symbol table with the given name
            int symbolTableId =
                symbolTable.addSymbol(SymbolType.VARIABLE, nameIdenToken, currentScope);
            t.setSymbolTableId(symbolTableId);
            // Add the type of the struct given the name of the variable
            int typeId =
                symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
            // If the type does not exist or is not a valid struct type
            if (typeId == -1
                || symbolTable.getSymbol(typeId).getSymbolType() != SymbolType.STRUCT_TYPE)
                errorWithoutException(Errors.UNDEFINED_TYPE);
            // Add the type to the sdecl symbol
            symbolTable.getSymbol(symbolTableId).setForeignSymbolTableId(typeId);
            match(Tokens.TIDEN);
        } else {
            // stype
            // Add the sdecl to the symbol table with the given name, adding the boolean flag to the
            // adder tp indicate that the symbol is a primitive type
            int symbolTableId =
                symbolTable.addSymbol(SymbolType.VARIABLE, nameIdenToken, currentScope, true);
            if (symbolTableId == -1) {
                errorWithoutException(Errors.IDEN_ALREADY_DEFINED);
            }
            t.setSymbolTableId(symbolTableId);

            // Get the symbol and set its type to an stype
            if (symbolTable.getSymbol(symbolTableId) instanceof PrimitiveTypeSymbol) {
                ((PrimitiveTypeSymbol) symbolTable.getSymbol(symbolTableId)).setVal(stype());
            } else {
                // An error has occured, likely related to the variable already being defined
                // in this scope
                stype();
            }

            t.setNodeType(TreeNodes.NSDECL);
        }
        return t;
    }

    /**
     * Parse a primitive type
     *
     * @return primitive type
     */
    private PrimitiveTypes stype() throws CD22ParserException {
        // Only accept int, float, or bool
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
                // Any other value is not a primitive type, add an error which will throw an
                // exception
                error(Errors.UNDEFINED_TYPE);
                // Will never hit this because the above line throws an exception
                return PrimitiveTypes.UNKNOWN;
        }
    }

    /**
     * Parse the arrays types
     *
     * @return arrdecls node or null
     */
    private TreeNode arrays() throws CD22ParserException, CD22EofException {
        // Arrays are optional
        if (lookahead.getToken() == Tokens.TARRS) {
            match(Tokens.TARRS);
            return arrdecls();
        }
        return null;
    }

    /**
     * Parse the array declarations
     *
     * @return arrdecls node
     */
    private TreeNode arrdecls() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2 = null;

        // Try parsing an array declaration, resync if it fails
        try {
            t1 = arrdecl();
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TCOMA, Tokens.TFUNC, Tokens.TMAIN));
        }

        // If we've reached another important keyword in the program we can stop parsing array
        // declarations
        if (lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        // Array declarations are comma separated, match another arrdecl recursively
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = arrdecls();
        }
        return new TreeNode(TreeNodes.NALIST, t1, t2);
    }

    /**
     * Parse an array declaration
     *
     * @return arrdecl node
     */
    private TreeNode arrdecl() throws CD22ParserException {
        // An arrdecl is two identifiers separated by a colon. Analysis will happen in the
        // overloaded function this one recalls but at the moment just parse the idens
        ArrayList<Token> idenList = parseColonSeparatedIdentifiers();
        return arrdecl(idenList);
    }

    /**
     * Requires colon to have already been parsed. Used to look ahead at the next identifier and
     * then call arrdecl once it is known that it is an array type
     *
     * @param nameIdenToken The array type name that has already been parsed
     * @return arrdecl node
     */
    private TreeNode arrdecl(Token nameIdenToken) throws CD22ParserException {
        // Add the name token to the list
        ArrayList<Token> idenList = new ArrayList<>();
        idenList.add(nameIdenToken);
        // Parse the type name
        if (lookahead.getToken() == Tokens.TIDEN) {
            idenList.add(lookahead);
            match(Tokens.TIDEN);
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        // Call the overloaded function
        return arrdecl(idenList);
    }

    /**
     * Parse an arrdecl
     *
     * @param idenList a list of two identifier tokens
     * @return arrdecl
     */
    private TreeNode arrdecl(ArrayList<Token> idenList) {
        TreeNode t = new TreeNode(TreeNodes.NARRD);

        // Create a symbol table entry with the variable name
        int symbolTableId =
            symbolTable.addSymbol(SymbolType.VARIABLE, idenList.get(0), currentScope);
        t.setSymbolTableId(symbolTableId);

        // Get the type ID from the symbol table, which should match an array type
        int typeId =
            symbolTable.getSymbolIdFromReference(idenList.get(1).getTokenLiteral(), currentScope);
        if (typeId == -1 || symbolTable.getSymbol(typeId).getSymbolType() != SymbolType.ARRAY_TYPE)
            errorWithoutException(Errors.UNDEFINED_TYPE);
        // Add the type to the variable entry from earlier
        symbolTable.getSymbol(symbolTableId).setForeignSymbolTableId(typeId);

        return t;
    }

    /**
     * Parse an expression
     *
     * @return expr node
     */
    private TreeNode expr() throws CD22ParserException {
        TreeNode t = new TreeNode();
        // Parse the left hand side of the equation
        t.setNextChild(term());
        // Sets node type, if it is not epsilon
        exprr(t);
        if (t.getNodeType() == null) {
            // Epsilon path of recursive exprr rule. If no following token was found, just return
            // the term as its own node
            return t.getLeft();
        }
        if (t.calculateNodeVariableTypeAndValue() == -1) {
            errorWithoutException(Errors.BAD_EXPR_TYPE);
        }
        return t;
    }

    /**
     * Recursive expr rule to prevent infinite loops
     *
     * @param t the parent node of the rule, to assign a node type to
     * @return expr node
     */
    private void exprr(TreeNode t) throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TPLUS) {
            // Match a + and update the node name
            match(Tokens.TPLUS);
            t.setNodeType(TreeNodes.NADD);
        } else if (lookahead.getToken() == Tokens.TMINS) {
            // Match a - and update the node name
            match(Tokens.TMINS);
            t.setNodeType(TreeNodes.NSUB);
        } else {
            // No right hand side
            return;
        }

        // Add a term as the right hand side
        TreeNode termNode = new TreeNode(term());
        exprr(termNode);
        if (termNode.getNodeType() == null) {
            t.setNextChild(termNode.getLeft());
        } else {
            if (termNode.calculateNodeVariableTypeAndValue() == -1) {
                errorWithoutException(Errors.BAD_EXPR_TYPE);
            }
            t.setNextChild(termNode);
        }
    }

    /**
     * Parse a term
     *
     * @return term node
     */
    private TreeNode term() throws CD22ParserException {
        TreeNode t = new TreeNode();
        // Parse a fact as the left hand side
        t.setNextChild(fact());
        // Sets node type, if it is not epsilon
        termr(t);
        if (t.getNodeType() == null) {
            // Epsilon path of recursive termr rule. If no following token was found, just return
            // the fact as its own node
            return t.getLeft();
        }
        if (t.calculateNodeVariableTypeAndValue() == -1) {
            errorWithoutException(Errors.BAD_EXPR_TYPE);
        }
        return t;
    }

    /**
     * Term recursive rule to prevent infinite loops
     *
     * @param t parent node to assign a type to
     * @return term node
     */
    private void termr(TreeNode t) throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TSTAR) {
            // Match a * and return a fact as the right hand side
            match(Tokens.TSTAR);
            t.setNodeType(TreeNodes.NMUL);
        } else if (lookahead.getToken() == Tokens.TDIVD) {
            // Match a / and return a fact as the right hand side
            match(Tokens.TDIVD);
            t.setNodeType(TreeNodes.NDIV);
        } else if (lookahead.getToken() == Tokens.TPERC) {
            // Match a % and return a fact as the right hand side
            match(Tokens.TPERC);
            t.setNodeType(TreeNodes.NMOD);
        } else {
            // No right hand side
            return;
        }
        // Add a term as the right hand side
        TreeNode factNode = new TreeNode(fact());
        termr(factNode);
        if (factNode.getNodeType() == null) {
            t.setNextChild(factNode.getLeft());
        } else {
            if (factNode.calculateNodeVariableTypeAndValue() == -1) {
                errorWithoutException(Errors.BAD_EXPR_TYPE);
            }
            t.setNextChild(factNode);
        }
    }

    /**
     * Parse a fact node
     *
     * @return fact node
     */
    private TreeNode fact() throws CD22ParserException {
        TreeNode t = new TreeNode();
        // Parse an exponent as the left hand side
        t.setNextChild(exponent());
        // Sets node type, if it is not epsilon
        factr(t);
        if (t.getNodeType() == null) {
            // Epsilon path of recursive factr rule. If no following token was found, just return
            // the exponent as its own node
            return t.getLeft();
        }
        if (t.calculateNodeVariableTypeAndValue() == -1) {
            errorWithoutException(Errors.BAD_EXPR_TYPE);
        }
        return t;
    }

    /**
     * Fact recursive rule to prevent infinite loops
     *
     * @param t parent node to assign a type to
     * @return fact node
     */
    private void factr(TreeNode t) throws CD22ParserException {
        // Match a ^ and return an exponent as the right hand side
        if (lookahead.getToken() == Tokens.TCART) {
            match(Tokens.TCART);
            t.setNodeType(TreeNodes.NPOW);
            TreeNode exponentNode = new TreeNode(exponent());
            factr(exponentNode);
            if (exponentNode.getNodeType() == null) {
                if (exponentNode.getLeft().getNodeDataType() != VariableTypes.INTEGER) {
                    error(Errors.BAD_EXPR_TYPE);
                }
                t.setNextChild(exponentNode.getLeft());

            } else {
                if (exponentNode.calculateNodeVariableTypeAndValue() == -1
                    || exponentNode.getNodeDataType() != VariableTypes.INTEGER) {
                    error(Errors.BAD_EXPR_TYPE);
                }
                t.setNextChild(exponentNode);
            }
        }
    }

    /**
     * Parse an exponent
     *
     * @return exponent node
     */
    private TreeNode exponent() throws CD22ParserException {
        // Match an integer literal
        if (lookahead.getToken() == Tokens.TILIT) {
            TreeNode t =
                new TreeNode(TreeNodes.NILIT, symbolTable.addSymbol(SymbolType.LITERAL, lookahead));
            t.setNodeDataType(VariableTypes.INTEGER);
            match(Tokens.TILIT);
            return t;
            // Match a float literal
        } else if (lookahead.getToken() == Tokens.TFLIT) {
            TreeNode t =
                new TreeNode(TreeNodes.NFLIT, symbolTable.addSymbol(SymbolType.LITERAL, lookahead));
            t.setNodeDataType(VariableTypes.FLOAT);
            match(Tokens.TFLIT);
            return t;
            // Match true
        } else if (lookahead.getToken() == Tokens.TTRUE) {
            TreeNode t = new TreeNode(TreeNodes.NTRUE,
                symbolTable.addSymbol(SymbolType.LITERAL,
                    new Token(Tokens.TTRUE, "1.0", 0,
                        0))); //, symbolTable.addSymbol(SymbolType.LITERAL, lookahead));
            t.setNodeDataType(VariableTypes.BOOLEAN);

            match(Tokens.TTRUE);
            return t;
            // Match false
        } else if (lookahead.getToken() == Tokens.TFALS) {
            TreeNode t = new TreeNode(TreeNodes.NFALS,
                symbolTable.addSymbol(SymbolType.LITERAL,
                    new Token(Tokens.TTRUE, "0.0", 0,
                        0))); //, symbolTable.addSymbol(SymbolType.LITERAL, lookahead));));
            t.setNodeDataType(VariableTypes.BOOLEAN);

            match(Tokens.TFALS);
            return t;
            // Match a (<bool>)
        } else if (lookahead.getToken() == Tokens.TLPAR) {
            match(Tokens.TLPAR);
            TreeNode t = bool();
            match(Tokens.TRPAR);
            return t;
            // Match a function call
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
        // Hopefully one of the above gets matched!
        error(Errors.NOT_A_NUMBER);
        return null;
    }

    /**
     * Parse a bool expression
     *
     * @return bool node
     */
    private TreeNode bool() throws CD22ParserException {
        TreeNode t = new TreeNode();
        // Set the left hand side
        t.setNextChild(rel());
        // Call the recursive rule and pass down the parent
        boolr(t);
        // If the recursive bool function did not assign a node type, just return the rel parsed
        // earlier
        if (t.getNodeType() == null) {
            return t.getLeft();
        }
        if (t.calculateNodeVariableTypeAndValue() == -1) {
            error(Errors.BAD_EXPR_TYPE);
        }
        return t;
    }

    /**
     * Recursive bool function to prevent infinite loops
     *
     * @param t the parent node to assign a type to
     */
    private void boolr(TreeNode t) throws CD22ParserException {
        // Add a logical operation
        t.setNextChild(logop());
        // If a logical operation existed, we can set the node type as a boolean expression
        if (t.getMid() != null) {
            t.setNodeType(TreeNodes.NBOOL);
            TreeNode boolrNode = new TreeNode(rel());
            boolr(boolrNode);

            if (boolrNode.getNodeType() == null) {
                t.setNextChild(boolrNode.getLeft());
            } else {
                if (boolrNode.calculateNodeVariableTypeAndValue() == -1) {
                    error(Errors.BAD_EXPR_TYPE);
                }
                t.setNextChild(boolrNode);
            }
        }
    }

    /**
     * Parse a logical operation
     *
     * @return logop node
     */
    private TreeNode logop() throws CD22ParserException {
        // Match and
        if (lookahead.getToken() == Tokens.TTAND) {
            match(Tokens.TTAND);
            return new TreeNode(TreeNodes.NAND);
            // Match or
        } else if (lookahead.getToken() == Tokens.TTTOR) {
            match(Tokens.TTTOR);
            return new TreeNode(TreeNodes.NOR);
            // Match xor
        } else if (lookahead.getToken() == Tokens.TTXOR) {
            match(Tokens.TTXOR);
            return new TreeNode(TreeNodes.NXOR);
        }
        // No logical operation was found
        return null;
    }

    /**
     * Parse a rel node
     *
     * @return rel node
     */
    private TreeNode rel() throws CD22ParserException {
        // Assume the node is not "notted" and then check for a NOT token
        boolean not = false;
        if (lookahead.getToken() == Tokens.TNOTT) {
            match(Tokens.TNOTT);
            not = true;
        }
        // Parse the first two aspects of the node, saving them both for interpretation
        TreeNode exprNode = expr();
        TreeNode relopNode = relop();
        // If the relop node did not exist, return just the expr node, as a child of a not node if
        // necessary
        if (relopNode == null) {
            if (not) {
                TreeNode notNode = new TreeNode(TreeNodes.NNOT, exprNode);
                notNode.setNodeDataType(VariableTypes.BOOLEAN);
                return notNode;
            }
            return exprNode;
        }
        // If the relop node did exist, assign the two expressions as children to it
        relopNode.setNextChild(exprNode);
        relopNode.setNextChild(expr());
        relopNode.setNodeDataType(VariableTypes.BOOLEAN);

        // Return the relop node, as a child of a not node if necessary
        if (not) {
            TreeNode notNode = new TreeNode(TreeNodes.NNOT, relopNode);
            notNode.setNodeDataType(VariableTypes.BOOLEAN);
            return notNode;
        }
        return relopNode;
    }

    /**
     * Parse a rel operation
     *
     * @return relop node
     */
    private TreeNode relop() throws CD22ParserException {
        // Match ==
        if (lookahead.getToken() == Tokens.TEQEQ) {
            match(Tokens.TEQEQ);
            return new TreeNode(TreeNodes.NEQL);
            // Match !=
        } else if (lookahead.getToken() == Tokens.TNEQL) {
            match(Tokens.TNEQL);
            return new TreeNode(TreeNodes.NNEQ);
            // Match >
        } else if (lookahead.getToken() == Tokens.TGRTR) {
            match(Tokens.TGRTR);
            return new TreeNode(TreeNodes.NGRT);
            // Match <
        } else if (lookahead.getToken() == Tokens.TLESS) {
            match(Tokens.TLESS);
            return new TreeNode(TreeNodes.NLSS);
            // Match <=
        } else if (lookahead.getToken() == Tokens.TLEQL) {
            match(Tokens.TLEQL);
            return new TreeNode(TreeNodes.NLEQ);
            // Match >=
        } else if (lookahead.getToken() == Tokens.TGEQL) {
            match(Tokens.TGEQL);
            return new TreeNode(TreeNodes.NGEQ);
        } else {
            // No relop found
            return null;
        }
    }

    /**
     * Match a function call
     *
     * @param nameIdenToken variable name of the function
     * @return fncall node
     */
    private TreeNode fncall(Token nameIdenToken) throws CD22ParserException {
        // Create a new node with the symbol table ID referring to the function definition in the
        // symbol table
        int symbolTableId =
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), "@global");
        if (symbolTableId == -1
            || symbolTable.getSymbol(symbolTableId).getSymbolType() != SymbolType.FUNCTION)
            error(Errors.UNDEFINED_FUNCTION);
        TreeNode t = new TreeNode(TreeNodes.NFCALL, symbolTableId);
        t.setNodeDataType(utils.resolvePrimativeTypeToVariableType(
            ((PrimitiveTypeSymbol) symbolTable.getSymbol(symbolTableId)).getVal()));
        match(Tokens.TLPAR);
        // If the function call does not immediately close the parentheses, try parsing an elist
        TreeNode elistNode = null;
        if (lookahead.getToken() != Tokens.TRPAR) {
            elistNode = elist();
        }
        int returnCode = utils.checkFunctionArgs(symbolTableId, elistNode);
        if (returnCode == -1) {
            errorWithoutException(Errors.BAD_ARG_TYPE);
        } else if (returnCode == -2) {
            errorWithoutException(Errors.BAD_ARG_LENGTH);
        }
        t.setNextChild(elistNode);
        match(Tokens.TRPAR);
        return t;
    }

    /**
     * Parse an elist
     *
     * @return elist node
     */
    private TreeNode elist() throws CD22ParserException {
        // Parse a bool expression
        TreeNode t1 = bool(), t2 = null;
        // If we have reached a ) assume it is the end of the elist
        if (lookahead.getToken() == Tokens.TRPAR) {
            return t1;
        }
        // elist values are comma separated
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = elist();
        }
        return new TreeNode(TreeNodes.NEXPL, t1, t2);
    }

    /**
     * Parse a var node
     *
     * @return var node
     */
    private TreeNode var() throws CD22ParserException {
        // Parse an identifier and then call the overloaded function
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return var(token);
        }
        // No variable name found
        error(Errors.EXPECTED_IDENTIFIER);
        return null;
    }

    /**
     * Parse a var node
     *
     * @param nameIdenToken name of the variable
     * @return var node
     */
    private TreeNode var(Token nameIdenToken) throws CD22ParserException {
        // Get the symbol indicated by the variable name
        int symbolTableId =
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), currentScope);

        if (symbolTableId == -1) {
            // We want to throw an exception error here, as if the variable has not been defined
            // then there's no point trying to parse the following expression and include subsequent
            // type checks
            error(Errors.UNDEFINED_VARIABLE, nameIdenToken.getTokenLiteral());
        }
        // Check if we are indexing an array
        if (lookahead.getToken() == Tokens.TLBRK) {
            // Create the node and assign the variable name symbol table ID, but not the node type
            // yet
            TreeNode t = new TreeNode();
            t.setSymbolTableId(symbolTableId);
            match(Tokens.TLBRK);
            // Parse the index value in the square brackets
            TreeNode exprNode = expr();
            if (exprNode.getNodeDataType() != VariableTypes.INTEGER) {
                error(Errors.REQUIRED_INTEGER);
            }
            t.setNextChild(exprNode);
            match(Tokens.TRBRK);
            // Access struct field if one is present
            if (lookahead.getToken() == Tokens.TDOTT) {
                // SEMANTICS this function checks that the struct field exists and assigns the
                // expected type
                matchStructVar(t, symbolTableId);
                // Change the node type as we know now that it is a field
                t.setNodeType(TreeNodes.NARRV);
            } else {
                // We are not accessing a struct field, the variable is instead the entire struct
                t.setNodeType(TreeNodes.NAELT);
                // SEMANTICS Set expected type
                int arrayType = symbolTable.getSymbol(symbolTableId).getForeignSymbolTableId();
                int structType = symbolTable.getSymbol(arrayType).getForeignSymbolTableId();
                // Change the symbol table ID to reference the array type, rather than the array
                // itself
                t.setSymbolTableId(arrayType);
                t.setExpectedType(VariableTypes.COMPLEX, structType);
            }
            // Either return with whole struct or individual field if the above statement ran
            return t;
        } else if (lookahead.getToken() == Tokens.TDOTT) {
            // SEMANTICS this funciton checks that the struct field exists and assigns the expected
            // type
            TreeNode t = matchStructVar(new TreeNode(), symbolTableId);
            // Change the node type as we know now it is a struct field
            t.setNodeType(TreeNodes.NSTRV);
            return t;
        }
        // If it is not an array or a struct it is just a simple variable
        TreeNode t = new TreeNode(TreeNodes.NSIMV, symbolTableId);
        if (symbolTable.getSymbol(symbolTableId) instanceof PrimitiveTypeSymbol
            && symbolTable.getSymbol(symbolTableId).getSymbolType() != SymbolType.CONSTANT) {
            t.setExpectedType(utils.resolvePrimativeTypeToVariableType(
                ((PrimitiveTypeSymbol) symbolTable.getSymbol(symbolTableId)).getVal()));
        } else if (symbolTable.getSymbol(symbolTableId).getSymbolType() == SymbolType.CONSTANT) {
            PrimitiveTypeSymbol constSymbol =
                ((PrimitiveTypeSymbol) symbolTable.getSymbol(symbolTableId));
            t.setExpectedType(utils.resolvePrimativeTypeToVariableType(constSymbol.getVal()));
        } else {
            t.setExpectedType(VariableTypes.COMPLEX,
                symbolTable.getSymbol(symbolTableId).getForeignSymbolTableId());
        }
        return t;
    }

    /**
     * Matches a struct field
     *
     * @param t              variable node
     * @param variableNameId name ID of the symbol table entry
     * @return The node passed in with additional type information
     */
    public TreeNode matchStructVar(TreeNode t, int variableNameId) throws CD22ParserException {
        t.setSymbolTableId(variableNameId);
        match(Tokens.TDOTT);
        // The field name must be an identifier
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
            // within the struct and add an error if it does not. Will also fail semantically if the
            // type is invalid
            int fieldId = symbolTable.getSymbolIdFromReference(
                lookahead.getTokenLiteral(), symbolTable.getSymbol(structTypeId).getRef(), false);
            if (fieldId == -1)
                errorWithoutException(Errors.UNDEFINED_VARIABLE, lookahead.getTokenLiteral());
            // Match the identifier now we have finished type checking with it, and add the field to
            // the node's children
            match(Tokens.TIDEN);
            t.setNextChild(new TreeNode(TreeNodes.NSIMV, fieldId));
            // SEMANTICS set expected type, so we can check the expression that's assigning to it
            if (fieldId != -1) {
                t.setExpectedType(utils.resolvePrimativeTypeToVariableType(
                    ((PrimitiveTypeSymbol) symbolTable.getSymbol(fieldId)).getVal()));
            }

        } else {
            // Invalid field name
            error(Errors.EXPECTED_IDENTIFIER);
        }
        return t;
    }

    /**
     * Parse the main body of the program
     *
     * @return main node
     */
    private TreeNode mainbody() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NMAIN);
        match(Tokens.TMAIN);
        currentScope = "@main";

        try {
            // Local variable list
            t.setNextChild(slist());
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TBEGN));
        }

        match(Tokens.TBEGN);
        // Statements
        t.setNextChild(stats());

        currentScope = "@global";
        match(Tokens.TTEND);
        match(Tokens.TCD22);
        // Ensure the program finishes with an ID
        if (lookahead.getToken() == Tokens.TIDEN) {
            int symbolTableId =
                symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
            match(Tokens.TIDEN);
            t.setSymbolTableId(symbolTableId);
            // SEMANTICS
            if (symbolTable.getSymbol(symbolTableId).getSymbolType() != SymbolType.PROGRAM_IDEN) {
                errorWithoutException(Errors.PROGRAM_IDEN_MISMATCH);
            }
        } else {
            error(Errors.EXPECTED_IDENTIFIER);
        }
        return t;
    }

    /**
     * Parse an slist
     *
     * @return slist node
     */
    private TreeNode slist() throws CD22ParserException {
        // Parse at least one sdecl
        TreeNode t1 = sdecl(), t2;
        // If we hit begin we have finished parsing sdecls
        if (lookahead.getToken() == Tokens.TBEGN) {
            return t1;
        }
        // sdecls are comma separated
        match(Tokens.TCOMA);
        // Recursively call slist
        t2 = slist();
        return new TreeNode(TreeNodes.NSDLST, t1, t2);
    }

    /**
     * Parses statements
     *
     * @return stats node
     */
    private TreeNode stats() throws CD22ParserException, CD22EofException {
        TreeNode t = null;
        // Parse a for or if statement
        if (lookahead.getToken() == Tokens.TTFOR || lookahead.getToken() == Tokens.TIFTH) {
            // Try parsing a for or if statement, and resync if it fails
            try {
                t = strstat();
            } catch (CD22ParserException e) {
                // Without knowing where the for or if statements end, we can only resynchronise on
                // an "END". If we resynchronised on the same tokens as stats, we would resynch
                // inside the for/if block but execution would not be inside the forstat or ifstat
                // functions. In this case the end of what would be an if or for stat becomes the
                // end of the main function, and we break out of stats altogether. Trust that the
                // developer remembered the end keyword and resync here only.
                panic(utils.getTokenList(Tokens.TTEND));
                match(Tokens.TTEND);
                return stats();
            }
            // Parse all other statements, that don't finish with "end"
        } else if (lookahead.getToken() == Tokens.TREPT || lookahead.getToken() == Tokens.TIDEN
            || lookahead.getToken() == Tokens.TINPT || lookahead.getToken() == Tokens.TPRNT
            || lookahead.getToken() == Tokens.TPRLN || lookahead.getToken() == Tokens.TRETN) {
            // Try parsing a statement followed by a semicolon, resync if it fails
            try {
                t = stat();
                gracefullyMatchSemicolon();
            } catch (CD22ParserException e) {
                panic(utils.getTokenList(Tokens.TSEMI, Tokens.TTFOR, Tokens.TIFTH, Tokens.TREPT,
                    Tokens.TINPT, Tokens.TPRNT, Tokens.TPRLN, Tokens.TRETN));
                if (lookahead.getToken() == Tokens.TSEMI) {
                    // If we have resynchronized on a semicolon we actually care about the data
                    // after it
                    gracefullyMatchSemicolon();
                }
                return stats();
            }
        }
        // Check that there is at least one statement
        if (t != null) {
            // Epsilon path, no more statements
            if (lookahead.getToken() != Tokens.TTFOR && lookahead.getToken() != Tokens.TIFTH
                && lookahead.getToken() != Tokens.TREPT && lookahead.getToken() != Tokens.TIDEN
                && lookahead.getToken() != Tokens.TINPT && lookahead.getToken() != Tokens.TPRNT
                && lookahead.getToken() != Tokens.TPRLN && lookahead.getToken() != Tokens.TRETN) {
                return t;
            }
            // Return with a recursive statements call
            return new TreeNode(TreeNodes.NSTATS, t, stats());
        }
        // No statements found (Or we failed to parse any)
        error(Errors.NO_STATEMENTS);
        return null;
    }

    /**
     * Parse a single statement
     *
     * @return stat node
     */
    private TreeNode stat() throws CD22ParserException, CD22EofException {
        // Match repeat
        if (lookahead.getToken() == Tokens.TREPT) {
            return reptstat();
            // Match io
        } else if (lookahead.getToken() == Tokens.TINPT || lookahead.getToken() == Tokens.TPRNT
            || lookahead.getToken() == Tokens.TPRLN) {
            return iostat();
            // Match return
        } else if (lookahead.getToken() == Tokens.TRETN) {
            return returnstat();
        }
        // Peep one ahead to see if this is a function call or an assignment statement
        Token token = lookahead;
        match(Tokens.TIDEN);
        if (lookahead.getToken() == Tokens.TLPAR) {
            // Function call
            return callstat(token);
        } else {
            // Variable assignment statement
            return asgnstat(token);
        }
    }

    /**
     * Parse a repeat statement block
     *
     * @return reptstat node
     */
    private TreeNode reptstat() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NREPT);
        match(Tokens.TREPT);
        match(Tokens.TLPAR);
        // Parse the assignment list inside the repeat statement header, which can be null
        TreeNode asgnlistNode = asgnlist();
        if (asgnlistNode != null)
            t.setNextChild(asgnlistNode);
        match(Tokens.TRPAR);
        // Parse the statements in the repeat block
        t.setNextChild(stats());
        match(Tokens.TUNTL);
        // Parse the boolean condition for looping
        TreeNode boolNode = bool();
        if (boolNode.getNodeDataType() != VariableTypes.BOOLEAN)
            errorWithoutException(Errors.BAD_EXPR_TYPE);
        t.setNextChild(boolNode);
        return t;
    }

    /**
     * Parse an io statement
     *
     * @return iostat node
     */
    private TreeNode iostat() throws CD22ParserException {
        // Match input
        if (lookahead.getToken() == Tokens.TINPT) {
            match(Tokens.TINPT);
            return new TreeNode(TreeNodes.NINPUT, vlist());
            // Match print
        } else if (lookahead.getToken() == Tokens.TPRNT) {
            match(Tokens.TPRNT);
            return new TreeNode(TreeNodes.NPRINT, prlist());
            // Match printline
        } else {
            match(Tokens.TPRLN);
            return new TreeNode(TreeNodes.NPRLN, prlist());
        }
    }

    /**
     * Parse a return statement
     *
     * @return returnstat node
     */
    private TreeNode returnstat() throws CD22ParserException {
        TreeNode t = new TreeNode(TreeNodes.NRETN);
        match(Tokens.TRETN);

        int functionSymbolTableId = symbolTable.getSymbolIdFromReference(currentScope, "@global");
        PrimitiveTypeSymbol functionSymbol = functionSymbolTableId != -1
            ? ((PrimitiveTypeSymbol) symbolTable.getSymbol(functionSymbolTableId))
            : null;
        // Match either void or an expression for the return value
        if (lookahead.getToken() == Tokens.TVOID) {
            match(Tokens.TVOID);
            if (functionSymbol != null && functionSymbol.getVal() != PrimitiveTypes.VOID) {
                errorWithoutException(Errors.BAD_RETURN_TYPE);
            }
        } else {
            TreeNode exprNode = expr();
            t.setNextChild(exprNode);

            if (functionSymbol != null
                && exprNode.getNodeDataType()
                    != utils.resolvePrimativeTypeToVariableType(functionSymbol.getVal())) {
                errorWithoutException(Errors.BAD_RETURN_TYPE);
            }
        }
        foundReturnStatement = true;
        return t;
    }

    /**
     * Parse a str stat node
     *
     * @return strstat node
     */
    private TreeNode strstat() throws CD22ParserException, CD22EofException {
        // This function only gets called if the lookahead is for or if, so if it is not for it must
        // be if
        if (lookahead.getToken() == Tokens.TTFOR) {
            return forstat();
        }
        return ifstat();
    }

    /**
     * Parse a call statement node
     *
     * @param nameIdenToken name of the function being called
     * @return callstat node
     */
    private TreeNode callstat(Token nameIdenToken) throws CD22ParserException {
        // Create a new node with the ID of the called function as the symbol table ID
        int symbolTableId =
            symbolTable.getSymbolIdFromReference(nameIdenToken.getTokenLiteral(), "@global");
        if (symbolTableId == -1
            || symbolTable.getSymbol(symbolTableId).getSymbolType() != SymbolType.FUNCTION)
            error(Errors.UNDEFINED_FUNCTION);

        TreeNode t = new TreeNode(TreeNodes.NCALL, symbolTableId);
        if (((PrimitiveTypeSymbol) symbolTable.getSymbol(symbolTableId)).getVal()
            != PrimitiveTypes.VOID) {
            errorWithoutException(Errors.NON_VOID_RETURN_TYPE);
        }

        match(Tokens.TLPAR);
        TreeNode elistNode = null;
        if (lookahead.getToken() != Tokens.TRPAR) {
            // Match an expression list inside parentheses for the function call
            elistNode = elist();
        }
        int returnCode = utils.checkFunctionArgs(symbolTableId, elistNode);
        if (returnCode == -1) {
            errorWithoutException(Errors.BAD_ARG_TYPE);
        } else if (returnCode == -2) {
            errorWithoutException(Errors.BAD_ARG_LENGTH);
        }
        t.setNextChild(elistNode);
        match(Tokens.TRPAR);
        return t;
    }

    /**
     * Match an assignment statement
     *
     * @return asgnstat node
     */
    private TreeNode asgnstat() throws CD22ParserException {
        // This function is called as a last resort else in the stat function, so we need to ensure
        // it is an identifier
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            // Call the overloaded function
            return asgnstat(token);
        }
        error(Errors.EXPECTED_IDENTIFIER);
        return null;
    }

    /**
     * Match an assingment statement
     *
     * @param nameIdenToken variable name to be assigned to
     * @return asgnstat node
     */
    private TreeNode asgnstat(Token nameIdenToken) throws CD22ParserException {
        // Parse the identifier as a variable to get that node
        TreeNode varNode = var(nameIdenToken);
        // Parse an assignment operator
        TreeNode t = asgnop();
        // Set the asgnstat operator node symbol table ID to that of the variable
        t.setSymbolTableId(varNode.getSymbolTableId());
        // Set the variable as a child, and parse any boolean expressions that follow it
        t.setNextChild(varNode);
        TreeNode boolNode = bool();
        if (varNode.getExpectedType() != boolNode.getNodeDataType()
            && !(varNode.getExpectedType() == VariableTypes.FLOAT
                && boolNode.getNodeDataType() == VariableTypes.INTEGER)) {
            error(Errors.BAD_EXPR_TYPE);
        }
        t.setNextChild(boolNode);
        return t;
    }

    /**
     * Parse a for statement header and statement block
     *
     * @return forstat node
     */
    private TreeNode forstat() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NFORL);
        match(Tokens.TTFOR);
        match(Tokens.TLPAR);
        // Parse an optional assignment list between initialiser parentheses
        TreeNode asgnlistNode = asgnlist();
        if (asgnlistNode != null)
            t.setNextChild(asgnlistNode);
        gracefullyMatchSemicolon();
        // Parse a conditional loop statement
        TreeNode boolNode = bool();
        if (boolNode.getNodeDataType() != VariableTypes.BOOLEAN)
            errorWithoutException(Errors.BAD_EXPR_TYPE);
        t.setNextChild(boolNode);
        match(Tokens.TRPAR);
        // Parse for loop statement block
        t.setNextChild(stats());
        match(Tokens.TTEND);
        return t;
    }

    /**
     * Parse an if statement
     *
     * @return ifstat node
     */
    private TreeNode ifstat() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode();
        match(Tokens.TIFTH);
        match(Tokens.TLPAR);
        // Parse conditional entry statement
        TreeNode boolNode = bool();
        if (boolNode.getNodeDataType() != VariableTypes.BOOLEAN)
            errorWithoutException(Errors.BAD_EXPR_TYPE);
        t.setNextChild(boolNode);
        match(Tokens.TRPAR);
        // Parse statement block
        t.setNextChild(stats());
        // If we've reached the end of the first statement block with no further if or else then the
        // parse is complete
        if (lookahead.getToken() == Tokens.TTEND) {
            t.setNodeType(TreeNodes.NIFTH);
            match(Tokens.TTEND);
        } else if (lookahead.getToken() == Tokens.TELSE) {
            // If we've read an else statement, match it and parse the else statement block
            t.setNodeType(TreeNodes.NIFTE);
            match(Tokens.TELSE);
            t.setNextChild(stats());
            match(Tokens.TTEND);
        } else {
            // If we've seen an if else statement, match it and parse it as another standard if node
            t.setNodeType(TreeNodes.NIFEF);
            match(Tokens.TELIF);
            match(Tokens.TLPAR);
            TreeNode childNode = new TreeNode(TreeNodes.NIFTH);
            // Conditional entry statement
            TreeNode childBoolNode = bool();
            if (childBoolNode.getNodeDataType() != VariableTypes.BOOLEAN)
                errorWithoutException(Errors.BAD_EXPR_TYPE);
            childNode.setNextChild(childBoolNode);
            match(Tokens.TRPAR);
            // If else statement block
            childNode.setNextChild(stats());
            match(Tokens.TTEND);
            t.setNextChild(childNode);
        }
        return t;
    }

    /**
     * Parse an optional assignment list
     *
     * @return alist node or null
     */
    private TreeNode asgnlist() throws CD22ParserException {
        if (lookahead.getToken() == Tokens.TIDEN) {
            return alist();
        }
        return null;
    }

    /**
     * Parse an assignment list
     *
     * @return alist node
     */
    private TreeNode alist() throws CD22ParserException {
        // Parse an assignment statement
        TreeNode t1 = asgnstat(), t2;
        // ASsignment statements are comma separated
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            // Recursively call alist
            t2 = alist();
        }
        return new TreeNode(TreeNodes.NASGNS, t1, t2);
    }

    /**
     * Parse an assignment operator
     *
     * @return asgnop node
     */
    private TreeNode asgnop() throws CD22ParserException {
        // Match =
        if (lookahead.getToken() == Tokens.TEQUL) {
            match(Tokens.TEQUL);
            return new TreeNode(TreeNodes.NASGN);
            // Match +=
        } else if (lookahead.getToken() == Tokens.TPLEQ) {
            match(Tokens.TPLEQ);
            return new TreeNode(TreeNodes.NPLEQ);
            // Match -=
        } else if (lookahead.getToken() == Tokens.TMNEQ) {
            match(Tokens.TMNEQ);
            return new TreeNode(TreeNodes.NMNEQ);
            // Match *=
        } else if (lookahead.getToken() == Tokens.TSTEQ) {
            match(Tokens.TSTEQ);
            return new TreeNode(TreeNodes.NSTEA);
            // Match /=
        } else if (lookahead.getToken() == Tokens.TDVEQ) {
            match(Tokens.TDVEQ);
            return new TreeNode(TreeNodes.NDVEQ);
        }
        // No operator found
        error(Errors.EXPECTED_ASSIGNMENT_OPERATOR);
        return null;
    }

    /**
     * Parse a variable list
     *
     * @return varlist node
     */
    private TreeNode vlist() throws CD22ParserException {
        // Parse a variable
        TreeNode t1 = var(), t2;
        // Variable lists are comma separated
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            // Recursively call vlist
            t2 = vlist();
        }
        return new TreeNode(TreeNodes.NVLIST, t1, t2);
    }

    /**
     * Parse print item list
     *
     * @return prlist node
     */
    private TreeNode prlist() throws CD22ParserException {
        // Parse a printitem node
        TreeNode t1 = printitem(), t2;
        // prlists are comma separated
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            // Recursively call prlist
            t2 = prlist();
        }
        return new TreeNode(TreeNodes.NPRLST, t1, t2);
    }

    /**
     * Parse a print item
     *
     * @return printitem node
     */
    private TreeNode printitem() throws CD22ParserException {
        // If the token is a string, add it to the symbol table and return a string node
        if (lookahead.getToken() == Tokens.TSTRG) {
            int symbolTableId = symbolTable.addSymbol(SymbolType.LITERAL, lookahead);
            match(Tokens.TSTRG);
            return new TreeNode(TreeNodes.NSTRG, symbolTableId);
        }
        // If the token is not a string parse it as an expression
        return expr();
    }

    /**
     * Parse the funcs of the program
     *
     * @return funcs node
     */
    private TreeNode funcs() throws CD22ParserException, CD22EofException {
        // Funcs are optional
        if (lookahead.getToken() != Tokens.TFUNC) {
            return null;
        }
        TreeNode t = new TreeNode(TreeNodes.NFUNCS);

        // Try parsing a function and resync if it fails
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

        // If there are more functions to parse, recursively call
        if (lookahead.getToken() == Tokens.TFUNC) {
            t.setNextChild(funcs());
        }
        return t;
    }

    /**
     * Parse a function
     *
     * @return func node
     */
    private TreeNode func() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode(TreeNodes.NFUND);
        match(Tokens.TFUNC);
        // Add the function name to the symbol table as a primitive type so that it's return value
        // can be stored
        int symbolTableId = 0;
        if (lookahead.getToken() == Tokens.TIDEN) {
            symbolTableId = symbolTable.addSymbol(SymbolType.FUNCTION, lookahead, "@global", true);
            if (symbolTableId == -1) {
                errorWithoutException(Errors.IDEN_ALREADY_DEFINED);
            }
            currentScope = lookahead.getTokenLiteral();
            match(Tokens.TIDEN);
            t.setSymbolTableId(symbolTableId);
        } else {
            // No function name specified
            error(Errors.EXPECTED_IDENTIFIER);
        }
        match(Tokens.TLPAR);
        // Match optional parameter list
        TreeNode plistNode = plist();
        if (plistNode != null)
            t.setNextChild(plistNode);
        match(Tokens.TRPAR);
        gracefullyMatchColon();
        // Match the return type, which may be void
        Symbol functionSymbol = symbolTable.getSymbol(symbolTableId);
        if (functionSymbol instanceof PrimitiveTypeSymbol) {
            ((PrimitiveTypeSymbol) functionSymbol).setVal(rtype());
            functionSymbol.setForeignTreeNode(plistNode);
        } else {
            // An error has occured, likely related to the variable already being defined in
            // this scope
            rtype();
        }
        // Match the function body and pull its children up to attach to the func node instead
        TreeNode funcbodyNode = funcbody();
        t.setNextChild(funcbodyNode.getLeft());
        t.setNextChild(funcbodyNode.getMid());
        currentScope = "@global";
        return t;
    }

    /**
     * Parse a parameter list
     *
     * @return plist node
     */
    private TreeNode plist() throws CD22ParserException, CD22EofException {
        // Parameters are optional and will either be identifiers and types, or that prefixed with
        // "const" for arrays
        if (lookahead.getToken() == Tokens.TIDEN || lookahead.getToken() == Tokens.TCNST) {
            return params();
        }
        return null;
    }

    /**
     * Parse the return type of function
     *
     * @return primitive return type
     */
    private PrimitiveTypes rtype() throws CD22ParserException {
        // If the return type is void
        if (lookahead.getToken() == Tokens.TVOID) {
            match(Tokens.TVOID);
            return PrimitiveTypes.VOID;
        }
        // Parse as a regular stype
        return stype();
    }

    /**
     * Parse the function body with no type as it is only a placeholder to return to the func
     * function
     *
     * @return a node with local variables and statements
     */
    private TreeNode funcbody() throws CD22ParserException, CD22EofException {
        TreeNode t = new TreeNode();
        // Parse local variables
        t.setNextChild(locals());
        match(Tokens.TBEGN);
        // Parse statement block
        foundReturnStatement = false;
        t.setNextChild(stats());
        match(Tokens.TTEND);
        if (!foundReturnStatement)
            errorWithoutException(Errors.MISSING_RETURN);
        return t;
    }

    /**
     * Parse function parameters
     *
     * @return params node
     */
    private TreeNode params() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2;

        // Try parsing a param, and resync if it fails
        try {
            t1 = param();
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TCOMA, Tokens.TRPAR));
        }

        // Params are comma separated
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            // Recursively call the params function
            t2 = params();
        }
        return new TreeNode(TreeNodes.NPLIST, t1, t2);
    }

    /**
     * Parse a parameter
     *
     * @return param node
     */
    private TreeNode param() throws CD22ParserException {
        // If the variable is a constant
        if (lookahead.getToken() == Tokens.TCNST) {
            match(Tokens.TCNST);
            // Match a regular arrdecl initially
            TreeNode t = new TreeNode(TreeNodes.NARRC, arrdecl());
            // Then add const to the type
            symbolTable.getSymbol(t.getLeft().getSymbolTableId()).makeConstArray();
            return t;
        } else if (lookahead.getToken() == Tokens.TIDEN) {
            // Get the name of the variable
            Token nameIdenToken = parseIdentifierFollowedByColon().get(0);
            // Find the specified type of the variable
            int typeId =
                symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
            if (typeId == -1) {
                // The type does not exist in the symbol table, so it must be a primitive type, or
                // undefined. Parse as sdecl which handles the undefined case already
                return new TreeNode(TreeNodes.NSIMP, sdecl(nameIdenToken));
            }
            if (symbolTable.getSymbol(typeId).getSymbolType() == SymbolType.STRUCT_TYPE) {
                // The symbol exists and is a struct, can also be parsed as sdecl
                return new TreeNode(TreeNodes.NSIMP, sdecl(nameIdenToken));
            } else if (symbolTable.getSymbol(typeId).getSymbolType() == SymbolType.ARRAY_TYPE) {
                // The symbol exists and is an array, parse as arrdecl
                return new TreeNode(TreeNodes.NARRP, arrdecl(nameIdenToken));
            }

            return null;
        }
        // Params are optional so can be null
        return null;
    }

    /**
     * Parse local variables of a function
     *
     * @return dlist node
     */
    private TreeNode locals() throws CD22ParserException, CD22EofException {
        // Local variables are optional
        if (lookahead.getToken() == Tokens.TIDEN) {
            return dlist();
        }
        return null;
    }

    private TreeNode dlist() throws CD22ParserException, CD22EofException {
        TreeNode t1 = null, t2;

        // Try parsing a declaration and resync if it fails
        try {
            t1 = decl();
        } catch (CD22ParserException e) {
            panic(utils.getTokenList(Tokens.TCOMA, Tokens.TBEGN, Tokens.TFUNC, Tokens.TMAIN));
            if (lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
                // Hot potato
                throw new CD22ParserException();
            }
        }

        // Declarations are comma separated
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            // Recursively call the declaration list
            t2 = dlist();
        }
        return new TreeNode(TreeNodes.NDLIST, t1, t2);
    }

    /**
     * Parse a declaration
     *
     * @return decl node
     */
    private TreeNode decl() throws CD22ParserException {
        // Parse the name of the variable
        Token nameIdenToken = parseIdentifierFollowedByColon().get(0);
        // Find the specified type of the variable
        int typeId =
            symbolTable.getSymbolIdFromReference(lookahead.getTokenLiteral(), currentScope);
        if (typeId == -1) {
            // The type does not exist in the symbol table, so it must be a primitive type, or
            // undefined. Parse as sdecl which handles the undefined case already
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

    /**
     * Recursive function to output the syntax tree
     *
     * @param node  node to start recursing from
     * @param debug flag indicating what format the data should be outputted in
     * @return stringified syntax tree of the node passed in as an argument
     */
    private String outputHelper(TreeNode node, boolean debug) {
        // Return on null children
        if (node == null)
            return "";

        // If in debug, output node name tagged with <> otherwise just output the node name
        StringBuilder data;
        if (debug) {
            data = new StringBuilder("<" + node.toString(false) + "> ");
            // Output the lexeme after the >
            data.append(node.getTokenString());
        } else {
            data = new StringBuilder(node.toString());
        }

        // Recursively output the children
        for (int i = 0; i < 3; i++) {
            data.append(outputHelper(node.getChildByIndex(i), debug));
        }

        // If in debug, add the XML closing tag
        if (debug) {
            data.append("</").append(node.toString(false)).append("> ");
        }
        return data.toString();
    }

    /**
     * Return syntax tree as a string
     *
     * @return stringified syntax tree
     */
    @Override
    public String toString() {
        // Grab the environement debug status
        boolean debugEnvironment =
            System.getenv("DEBUG") != null && System.getenv("DEBUG").compareTo("true") == 0;

        // Split the node data into a primitive array
        String[] treeList = outputHelper(syntaxTree, debugEnvironment).split("\\s");
        StringBuilder formattedTree = new StringBuilder();
        StringBuilder line = new StringBuilder();
        // Loop through and add lines of 10 columns where each word is padded to a multiple of 7
        boolean stringVal = false;
        for (String outValue : treeList) {
            line.append(outValue);
            // Ensure strings are padded correctly
            if (outValue.contains("\"")) {
                stringVal = !stringVal;
                if (outValue.length() > 1 && outValue.endsWith("\""))
                    stringVal = false;
                if (!stringVal) {
                    int size = (line.length() / 7) * 7 + 7;
                    line.append(" ".repeat(size - line.length()));
                    if (line.length() >= 70) {
                        formattedTree.append(line).append("\n");
                        line = new StringBuilder();
                    }
                    continue;
                }
            }
            // This output will automatically trim strings to single space separated words, but if
            // there are multiple spaces they will still carry through for codegen
            if (stringVal) {
                line.append(" ");
            } else {
                // Pad a regular node to multiple of 7 characters
                int size = (outValue.length() / 7) * 7 + 7;
                line.append(" ".repeat(size - outValue.length()));
                if (line.length() >= 70) {
                    formattedTree.append(line).append("\n");
                    line = new StringBuilder();
                }
            }
        }
        // Output the last line
        if (line.length() > 0)
            formattedTree.append(line);
        return formattedTree.toString();
    }

    /**
     * Placeholder for the code generation to be able to grab the tree
     *
     * @return the syntax tree after parsing
     */
    public TreeNode getSyntaxTree() {
        return syntaxTree;
    }
}
