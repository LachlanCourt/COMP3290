/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class contains some common functions and lists that are used throughout
 ****    the compiling process
 *******************************************************************************/
package Common;

import Parser.TreeNode;
import Scanner.Token;
import Scanner.Token.Tokens;
import java.util.*;

public class Utils {
    private static Utils self;
    // Definition Lists for custom matching
    private static final ArrayList<String> validPunctuation = new ArrayList<>(Arrays.asList(",",
        "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", "!", "\"", ":", ";", "."));
    private static final ArrayList<String> validStandaloneOperators = new ArrayList<>(Arrays.asList(
        ",", "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", ":", ";", "."));
    private static final ArrayList<String> validDoubleOperators =
        new ArrayList<>(Arrays.asList("!=", "==", "<=", ">=", "+=", "-=", "/=", "*="));
    private static final ArrayList<String> letters =
        new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));
    private static final ArrayList<String> numbers =
        new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
    private static final ArrayList<String> keywords = new ArrayList<>(Arrays.asList("cd22",
        "constants", "types", "def", "arrays", "main", "begin", "end", "array", "of", "func",
        "void", "const", "int", "float", "bool", "for", "repeat", "until", "if", "else", "elif",
        "input", "print", "printline", "return", "not", "and", "or", "xor", "true", "false"));

    private static HashMap<String, Tokens> tokenMap;

    private static final SymbolTable symbolTable = SymbolTable.getSymbolTable();

    public enum MatchTypes {
        LETTER,
        NUMBER,
        PUNCTUATION,
        STANDALONE_OPERATOR,
        DOUBLE_OPERATOR,
        KEYWORD,
        IDENTIFIER,
        UNDEFINED,
        WHITESPACE
    }

    /**
     * Singleton style constructor for utils to prevent it being declared multiple times
     * unnecessarily
     */
    private Utils() {
        for (int i = 0; i < 26; i++) {
            letters.add(letters.get(i).toUpperCase());
        }
        buildTokensMap();
    }

    public static Utils getUtils() {
        if (self == null) {
            self = new Utils();
        }
        return self;
    }

    /**
     * Initialise the keyword to token hashmap with values
     */
    private void buildTokensMap() {
        tokenMap = new HashMap<>();
        tokenMap.put("cd22", Tokens.TCD22);
        tokenMap.put("constants", Tokens.TCONS);
        tokenMap.put("types", Tokens.TTYPS);
        tokenMap.put("def", Tokens.TTDEF);
        tokenMap.put("arrays", Tokens.TARRS);
        tokenMap.put("main", Tokens.TMAIN);
        tokenMap.put("begin", Tokens.TBEGN);
        tokenMap.put("end", Tokens.TTEND);
        tokenMap.put("array", Tokens.TARAY);
        tokenMap.put("of", Tokens.TTTOF);
        tokenMap.put("func", Tokens.TFUNC);
        tokenMap.put("void", Tokens.TVOID);
        tokenMap.put("const", Tokens.TCNST);
        tokenMap.put("int", Tokens.TINTG);
        tokenMap.put("float", Tokens.TFLOT);
        tokenMap.put("bool", Tokens.TBOOL);
        tokenMap.put("for", Tokens.TTFOR);
        tokenMap.put("repeat", Tokens.TREPT);
        tokenMap.put("until", Tokens.TUNTL);
        tokenMap.put("if", Tokens.TIFTH);
        tokenMap.put("else", Tokens.TELSE);
        tokenMap.put("elif", Tokens.TELIF);
        tokenMap.put("input", Tokens.TINPT);
        tokenMap.put("print", Tokens.TPRNT);
        tokenMap.put("printline", Tokens.TPRLN);
        tokenMap.put("return", Tokens.TRETN);
        tokenMap.put("not", Tokens.TNOTT);
        tokenMap.put("and", Tokens.TTAND);
        tokenMap.put("or", Tokens.TTTOR);
        tokenMap.put("xor", Tokens.TTXOR);
        tokenMap.put("true", Tokens.TTRUE);
        tokenMap.put("false", Tokens.TFALS);
        tokenMap.put(",", Tokens.TCOMA);
        tokenMap.put("[", Tokens.TLBRK);
        tokenMap.put("]", Tokens.TRBRK);
        tokenMap.put("(", Tokens.TLPAR);
        tokenMap.put(")", Tokens.TRPAR);
        tokenMap.put("=", Tokens.TEQUL);
        tokenMap.put("+", Tokens.TPLUS);
        tokenMap.put("-", Tokens.TMINS);
        tokenMap.put("*", Tokens.TSTAR);
        tokenMap.put("/", Tokens.TDIVD);
        tokenMap.put("%", Tokens.TPERC);
        tokenMap.put("^", Tokens.TCART);
        tokenMap.put("<", Tokens.TLESS);
        tokenMap.put(">", Tokens.TGRTR);
        tokenMap.put(":", Tokens.TCOLN);
        tokenMap.put(";", Tokens.TSEMI);
        tokenMap.put(".", Tokens.TDOTT);
        tokenMap.put("!=", Tokens.TNEQL);
        tokenMap.put("==", Tokens.TEQEQ);
        tokenMap.put("<=", Tokens.TLEQL);
        tokenMap.put(">=", Tokens.TGEQL);
        tokenMap.put("+=", Tokens.TPLEQ);
        tokenMap.put("-=", Tokens.TMNEQ);
        tokenMap.put("/=", Tokens.TDVEQ);
        tokenMap.put("*=", Tokens.TSTEQ);
    }

    /**
     * Gets a token given a string initialiser
     *
     * @param initialiser value to be checked for a token
     * @return the enum value that matches the initialiser key, or TUNDF if it does not exist
     */
    public Tokens getTokenFromInitialiser(String initialiser) {
        if (tokenMap.containsKey(initialiser))
            return tokenMap.get(initialiser);
        return Tokens.TUNDF;
    }

    /**
     * Gets a string initialiser from a token
     *
     * @param token enum value to be returned as a string
     * @return string key that matches the enum value, or null if it does not exist
     */
    public String getInitialiserFromToken(Tokens token) {
        // The value in the map is lowercase so have an explicit check for CD22
        if (token == Tokens.TCD22)
            return "CD22";
        // Loop through the token map to find the token which matches the argument, and return the
        // associated string
        for (Map.Entry<String, Tokens> entry : tokenMap.entrySet()) {
            if (entry.getValue() == token)
                return entry.getKey();
        }
        return null;
    }

    /**
     * Matches a given string based on a specified type
     *
     * @param candidate a string to be matched
     * @param matcher   a type to match the string to
     * @return true if the candidate matches the specified type and false if not
     */
    public boolean matches(String candidate, MatchTypes matcher) {
        switch (matcher) {
            case LETTER:
                return letters.contains(candidate);
            case NUMBER:
                return numbers.contains(candidate);
            case KEYWORD:
                return keywords.contains(candidate);
            case PUNCTUATION:
                return validPunctuation.contains(candidate);
            case DOUBLE_OPERATOR:
                return validDoubleOperators.contains(candidate);
            case STANDALONE_OPERATOR:
                return validStandaloneOperators.contains(candidate);
            case IDENTIFIER:
                return matchesIdentifier(candidate);
            case WHITESPACE:
                return candidate.compareTo("\n") == 0 || candidate.compareTo(" ") == 0
                    || candidate.charAt(0) == 9;
            case UNDEFINED:
                return !(matches(candidate, MatchTypes.LETTER, MatchTypes.NUMBER,
                    MatchTypes.PUNCTUATION, MatchTypes.WHITESPACE));
        }
        return false;
    }

    // Overloaded matches methods to simplify calls with multiple optional matches
    public boolean matches(String candidate, MatchTypes matcher1, MatchTypes matcher2) {
        return matches(candidate, matcher1) || matches(candidate, matcher2);
    }

    public boolean matches(
        String candidate, MatchTypes matcher1, MatchTypes matcher2, MatchTypes matcher3) {
        return matches(candidate, matcher1, matcher2) || matches(candidate, matcher3);
    }

    public boolean matches(String candidate, MatchTypes matcher1, MatchTypes matcher2,
        MatchTypes matcher3, MatchTypes matcher4) {
        return matches(candidate, matcher1, matcher2) || matches(candidate, matcher3, matcher4);
    }

    /**
     * Matches strings that start with a letter and only contain strings and letters
     *
     * @param candidate a candidate string to be determined whether it matches the form of an
     *                  identifier
     * @return boolean value representing whether it matches
     */
    public boolean matchesIdentifier(String candidate) {
        if (!letters.contains(String.valueOf(candidate.charAt(0))))
            return false;
        for (String c : candidate.split("")) {
            if (!letters.contains(c) && !numbers.contains(c))
                return false;
        }
        return true;
    }

    // Overloaded functions to retrieve a series of Tokens as parameters and return those same
    // values as an arraylist
    public ArrayList<Tokens> getTokenList(Tokens first) {
        return new ArrayList<>(Collections.singletonList(first));
    }

    public ArrayList<Tokens> getTokenList(Tokens first, Tokens second) {
        return new ArrayList<>(Arrays.asList(first, second));
    }

    public ArrayList<Tokens> getTokenList(Tokens first, Tokens second, Tokens third) {
        return new ArrayList<>(Arrays.asList(first, second, third));
    }

    public ArrayList<Tokens> getTokenList(
        Tokens first, Tokens second, Tokens third, Tokens fourth) {
        return new ArrayList<>(Arrays.asList(first, second, third, fourth));
    }

    public ArrayList<Tokens> getTokenList(
        Tokens first, Tokens second, Tokens third, Tokens fourth, Tokens fifth) {
        return new ArrayList<>(Arrays.asList(first, second, third, fourth, fifth));
    }

    public ArrayList<Tokens> getTokenList(
        Tokens first, Tokens second, Tokens third, Tokens fourth, Tokens fifth, Tokens sixth) {
        return new ArrayList<>(Arrays.asList(first, second, third, fourth, fifth, sixth));
    }

    public ArrayList<Tokens> getTokenList(Tokens first, Tokens second, Tokens third, Tokens fourth,
        Tokens fifth, Tokens sixth, Tokens seventh) {
        return new ArrayList<>(Arrays.asList(first, second, third, fourth, fifth, sixth, seventh));
    }

    public ArrayList<Tokens> getTokenList(Tokens first, Tokens second, Tokens third, Tokens fourth,
        Tokens fifth, Tokens sixth, Tokens seventh, Tokens eighth) {
        return new ArrayList<>(
            Arrays.asList(first, second, third, fourth, fifth, sixth, seventh, eighth));
    }

    public void calculateValue(TreeNode left, TreeNode right, TreeNode operation) {
        // Any of these cases cannot be folded
        if (left.getNodeDataType() == TreeNode.VariableTypes.COMPLEX
            || right.getNodeDataType() == TreeNode.VariableTypes.COMPLEX
            || (left.getNodeType() == TreeNode.TreeNodes.NSIMV
                && symbolTable.getSymbol(left.getSymbolTableId()).getSymbolType()
                    != SymbolTable.SymbolType.CONSTANT)
            || (right.getNodeType() == TreeNode.TreeNodes.NSIMV
                && symbolTable.getSymbol(right.getSymbolTableId()).getSymbolType()
                    != SymbolTable.SymbolType.CONSTANT)
            || left.getSymbolTableId() == 0 || right.getSymbolTableId() == 0) {
            return;
        }

        Double leftVal;
        if (symbolTable.getSymbol(left.getSymbolTableId()) instanceof LiteralSymbol) {
            // If the value is already a literal symbol
            leftVal = Double.parseDouble(
                ((LiteralSymbol) symbolTable.getSymbol(left.getSymbolTableId())).getVal());
        } else if (symbolTable.getSymbol(
                       symbolTable.getSymbol(left.getSymbolTableId()).getForeignSymbolTableId())
                       instanceof LiteralSymbol) {
            // If the value is a constant
            leftVal = Double.parseDouble(
                ((LiteralSymbol) symbolTable.getSymbol(
                     symbolTable.getSymbol(left.getSymbolTableId()).getForeignSymbolTableId()))
                    .getVal());
        } else {
            // Likely a struct variable, which cannot be folded
            return;
        }
        Double rightVal;
        if (symbolTable.getSymbol(right.getSymbolTableId()) instanceof LiteralSymbol) {
            // If the value is already a literal symbol
            rightVal = Double.parseDouble(
                ((LiteralSymbol) symbolTable.getSymbol(right.getSymbolTableId())).getVal());
        } else if (symbolTable.getSymbol(
                       symbolTable.getSymbol(right.getSymbolTableId()).getForeignSymbolTableId())
                       instanceof LiteralSymbol) {
            // If the value is a constant
            rightVal = Double.parseDouble(
                ((LiteralSymbol) symbolTable.getSymbol(
                     symbolTable.getSymbol(right.getSymbolTableId()).getForeignSymbolTableId()))
                    .getVal());
        } else {
            // Likely a struct variable, which cannot be folded
            return;
        }

        Double newVal = null;

        switch (operation.getNodeType()) {
            case NADD:
                newVal = leftVal + rightVal;
                break;
            case NSUB:
                newVal = leftVal - rightVal;
                break;
            case NMUL:
                newVal = leftVal * rightVal;
                break;
            case NDIV:
                newVal = leftVal / rightVal;
                break;
            case NPOW:
                newVal = Math.pow(leftVal, rightVal);
                break;
            case NAND:
                newVal = leftVal == 1.0 && rightVal == 1.0 ? 1.0 : 0.0;
                break;
            case NOR:
                newVal = leftVal == 1.0 || rightVal == 1.0 ? 1.0 : 0.0;
                break;
            case NXOR:
                newVal =
                    (leftVal == 1.0 || rightVal == 1.0) && !leftVal.equals(rightVal) ? 1.0 : 0.0;
                break;
        }

        if (newVal != null) {
            operation.setSymbolTableId(symbolTable.addSymbol(SymbolTable.SymbolType.LITERAL,
                new Token(Tokens.TFLOT, String.valueOf(newVal), 0, 0)));
        }
    }

    public TreeNode.VariableTypes resolvePrimativeTypeToVariableType(
        SymbolTable.PrimitiveTypes symbolType) {
        switch (symbolType) {
            case INTEGER:
                return TreeNode.VariableTypes.INTEGER;
            case FLOAT:
                return TreeNode.VariableTypes.FLOAT;
            case BOOLEAN:
                return TreeNode.VariableTypes.BOOLEAN;
            default:
                return TreeNode.VariableTypes.UNKNOWN;
        }
    }

    public SymbolTable.PrimitiveTypes resolveVariableTypeToPrimitiveType(
        TreeNode.VariableTypes symbolType) {
        switch (symbolType) {
            case INTEGER:
                return SymbolTable.PrimitiveTypes.INTEGER;
            case FLOAT:
                return SymbolTable.PrimitiveTypes.FLOAT;
            case BOOLEAN:
                return SymbolTable.PrimitiveTypes.BOOLEAN;
            default:
                return SymbolTable.PrimitiveTypes.UNKNOWN;
        }
    }

    public void flattenNodes(ArrayList<TreeNode> params, TreeNode node, TreeNode.TreeNodes type) {
        if (node == null)
            return;
        if (node.getNodeType() == type) {
            flattenNodes(params, node.getLeft(), type);
            flattenNodes(params, node.getMid(), type);
        } else {
            params.add(node);
        }
    }

    public int checkFunctionArgs(int symbolTableId, TreeNode functionParams) {
        Symbol functionSymbol = symbolTable.getSymbol(symbolTableId);
        if (functionSymbol.getForeignTreeNode() == null)
            return 0;
        ArrayList<TreeNode> receiving = new ArrayList<>();
        flattenNodes(receiving, functionSymbol.getForeignTreeNode(), TreeNode.TreeNodes.NPLIST);
        ArrayList<TreeNode> sending = new ArrayList<>();
        flattenNodes(sending, functionParams, TreeNode.TreeNodes.NEXPL);

        // Check the number of arguments passed to the function match the number of arguments
        // received
        if (receiving.size() != sending.size()) {
            return -2;
        }

        // Check the types of each argument matches
        for (int i = 0; i < receiving.size(); i++) {
            TreeNode receivingNode = receiving.get(i);
            TreeNode sendingNode = sending.get(i);
            switch (receivingNode.getNodeType()) {
                case NSIMP:
                    if (symbolTable.getSymbol(receivingNode.getLeft().getSymbolTableId())
                            instanceof PrimitiveTypeSymbol) {
                        TreeNode.VariableTypes receivingVariableType =
                            resolvePrimativeTypeToVariableType(
                                ((PrimitiveTypeSymbol) symbolTable.getSymbol(
                                     receivingNode.getLeft().getSymbolTableId()))
                                    .getVal());
                        if (receivingVariableType != sendingNode.getNodeDataType()
                            && !(receivingVariableType == TreeNode.VariableTypes.FLOAT
                                && sendingNode.getNodeDataType() == TreeNode.VariableTypes.INTEGER))
                            return -1;
                    } else {
                        if (typesDontMatch(receivingNode, sendingNode))
                            return -1;
                    }
                    break;
                case NARRP:
                    if (typesDontMatch(receivingNode, sendingNode))
                        return -1;
                    if (symbolTable.getSymbol(sendingNode.getSymbolTableId()).getSymbolType()
                        == SymbolTable.SymbolType.CONSTANT_ARRAY)
                        return -1;
                    break;
                case NARRC:
                    if (typesDontMatch(receivingNode, sendingNode))
                        return -1;
                    break;
            }
        }
        return 0;
    }

    private boolean typesDontMatch(TreeNode first, TreeNode second) {
        return symbolTable.getSymbol(first.getLeft().getSymbolTableId()).getForeignSymbolTableId()
            != symbolTable.getSymbol(second.getSymbolTableId()).getForeignSymbolTableId();
    }
}
