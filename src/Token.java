import java.util.ArrayList;
import java.util.Arrays;

enum Tokens {
    TTEOF("TTEOF"), TCD22("TCD22"), TCONS("TCONS"), TTYPS("TTYPS"), TTDEF("TTDEF"), TARRS("TARRS"), TMAIN("TMAIN"), TBEGN("TBEGN"), TTEND("TTEND"), TARAY("TARAY"), TTTOF("TTTOF"), TFUNC("TFUNC"), TVOID("TVOID"), TCNST("TCNST"), TINTG("TINTG"), TFLOT("TFLOT"), TBOOL("TBOOL"), TTFOR("TTFOR"), TREPT("TREPT"), TUNTL("TUNTL"), TIFTH("TIFTH"), TELSE("TELSE"), TELIF("TELIF"), TINPT("TINPT"), TPRNT("TPRNT"), TPRLN("TPRLN"), TRETN("TRETN"), TNOTT("TNOTT"), TTAND("TTAND"), TTTOR("TTTOR"), TTXOR("TTXOR"), TTRUE("TTRUE"), TFALS("TFALS"), TCOMA("TCOMA"), TLBRK("TLBRK"), TRBRK("TRBRK"), TLPAR("TLPAR"), TRPAR("TRPAR"), TEQUL("TEQUL"), TPLUS("TPLUS"), TMINS("TMINS"), TSTAR("TSTAR"), TDIVD("TDIVD"), TPERC("TPERC"), TCART("TCART"), TLESS("TLESS"), TGRTR("TGRTR"), TCOLN("TCOLN"), TSEMI("TSEMI"), TDOTT("TDOTT"), TLEQL("TLEQL"), TGEQL("TGEQL"), TNEQL("TNEQL"), TEQEQ("TEQEQ"), TPLEQ("TPLEQ"), TMNEQ("TMNEQ"), TSTEQ("TSTEQ"), TDVEQ("TDVEQ"), TIDEN("TIDEN"), TILIT("TILIT"), TFLIT("TFLIT"), TSTRG("TSTRG"), TUNDF("TUNDF");

    private String value;

    Tokens(String initialiser) {
        value = initialiser;
    }

    public String getValue() {
        return value;
    }

    public static Tokens getToken(String initialiser) {
        switch (initialiser) {
            case "CD22" -> {
                return TCD22;
            }
            case "constants" -> {
                return TCONS;
            }
            case "types" -> {
                return TTYPS;
            }
            case "def" -> {
                return TTDEF;
            }
            case "arrays" -> {
                return TARRS;
            }
            case "main" -> {
                return TMAIN;
            }
            case "begin" -> {
                return TBEGN;
            }
            case "end" -> {
                return TTEND;
            }
            case "array" -> {
                return TARAY;
            }
            case "of" -> {
                return TTTOF;
            }
            case "func" -> {
                return TFUNC;
            }
            case "void" -> {
                return TVOID;
            }
            case "const" -> {
                return TCNST;
            }
            case "int" -> {
                return TINTG;
            }
            case "float" -> {
                return TFLOT;
            }
            case "bool" -> {
                return TBOOL;
            }
            case "for" -> {
                return TTFOR;
            }
            case "repeat" -> {
                return TREPT;
            }
            case "until" -> {
                return TUNTL;
            }
            case "if" -> {
                return TIFTH;
            }
            case "else" -> {
                return TELSE;
            }
            case "elif" -> {
                return TELIF;
            }
            case "input" -> {
                return TINPT;
            }
            case "print" -> {
                return TPRNT;
            }
            case "printline" -> {
                return TPRLN;
            }
            case "return" -> {
                return TRETN;
            }
            case "not" -> {
                return TNOTT;
            }
            case "and" -> {
                return TTAND;
            }
            case "or" -> {
                return TTTOR;
            }
            case "xor" -> {
                return TTXOR;
            }
            case "true" -> {
                return TTRUE;
            }
            case "false" -> {
                return TFALS;
            }
            case "," -> {
                return TCOMA;
            }
            case "[" -> {
                return TLBRK;
            }
            case "]" -> {
                return TRBRK;
            }
            case "(" -> {
                return TLPAR;
            }
            case ")" -> {
                return TRPAR;
            }
            case "=" -> {
                return TEQUL;
            }
            case "+" -> {
                return TPLUS;
            }
            case "-" -> {
                return TMINS;
            }
            case "*" -> {
                return TSTAR;
            }
            case "/" -> {
                return TDIVD;
            }
            case "%" -> {
                return TPERC;
            }
            case "^" -> {
                return TCART;
            }
            case "<" -> {
                return TLESS;
            }
            case ">" -> {
                return TGRTR;
            }
            case ":" -> {
                return TCOLN;
            }
            case ";" -> {
                return TSEMI;
            }
            case "." -> {
                return TDOTT;
            }
            case "!=" -> {
                return TNEQL;
            }
            case "==" -> {
                return TEQEQ;
            }
            case "<=" -> {
                return TLEQL;
            }
            case ">=" -> {
                return TGEQL;
            }
            case "+=" -> {
                return TPLEQ;
            }
            case "-=" -> {
                return TMNEQ;
            }
            case "/=" -> {
                return TDVEQ;
            }
            case "*=" -> {
                return TSTEQ;
            }
            default -> {
                return TUNDF;
            }
        }
    }
};


public class Token {

    private static ArrayList<String> keywords = new ArrayList<String>(Arrays.asList("CD22", "constants", "types", "def", "arrays", "main", "begin", "end", "array", "of", "func", "void", "const", "int", "float", "bool", "for", "repeat", "until", "if", "else", "elif", "input", "print", "printline", "return", "not", "and", "or", "xor", "true", "false"));
    private static ArrayList<String> validPunctuation = new ArrayList<String>(Arrays.asList(",", "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", "!", "\"", ":", ";", "."));
    private static ArrayList<String> validStandaloneOperators = new ArrayList<String>(Arrays.asList(",", "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", ":", ";", "."));
    private static ArrayList<String> validDoubleOperators = new ArrayList<String>(Arrays.asList("!=", "==", "<=", ">=", "+=", "-=", "/=", "*="));

    private String tokenLiteral = null;

    private Tokens token = Tokens.TUNDF;
    private int lexeme;
    private int row;
    private int col;
    private boolean debug = false;

    public Token(String tokenLiteral_, int row_, int col_, boolean debug_) {
        row = row_;
        col = col_;
        debug = debug_;
        if (tokenLiteral_.compareTo("") != 0) {
            tokenLiteral = tokenLiteral_;
            for (String keyword : keywords) {
                if (keyword.compareTo(tokenLiteral) == 0) {
                    token = Tokens.getToken(keyword);
                    return;
                }
            }
            for (String operator : validStandaloneOperators) {
                if (operator.compareTo(tokenLiteral) == 0) {
                    token = Tokens.getToken(operator);
                    return;
                }
            }
            for (String operator : validDoubleOperators) {
                if (operator.compareTo(tokenLiteral) == 0) {
                    token = Tokens.getToken(operator);
                    return;
                }
            }
            //TODO add these to symbol table
            if (tokenLiteral.startsWith("\"") && tokenLiteral.endsWith("\"")) {
                token = Tokens.TSTRG;
                return;
            }

            if (tokenLiteral.charAt(0) >= 48 && tokenLiteral.charAt(0) <= 57) {
                // Float literal
                if (tokenLiteral.contains(".")) {
                    token = Tokens.TFLOT;
                    return;
                }
                // Integer literal
                token = Tokens.TINTG;
                return;
            }

            // Indentifier
            if (!validPunctuation.contains(tokenLiteral)) {
                token = Tokens.TIDEN;
            }
        }
    }

    public Token(boolean eof) {
        token = Tokens.TTEOF;
        tokenLiteral = "";
    }

    public boolean isEof() {
        return token == Tokens.TTEOF;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("(");
        out.append(token.getValue() + ",");
        out.append(lexeme + ",");
        out.append(row + ",");
        out.append(col + ")");
        return out.toString() + (debug ? " " + tokenLiteral : "");
    }

}
