import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Tokens t;
        switch (initialiser) {
            case "CD22" -> t = TCD22;
            case "constants" -> t = TCONS;
            case "types" -> t = TTYPS;
            case "def" -> t = TTDEF;
            case "arrays" -> t = TARRS;
            case "main" -> t = TMAIN;
            case "begin" -> t = TBEGN;
            case "end" -> t = TTEND;
            case "array" -> t = TARAY;
            case "of" -> t = TTTOF;
            case "func" -> t = TFUNC;
            case "void" -> t = TVOID;
            case "const" -> t = TCNST;
            case "int" -> t = TINTG;
            case "float" -> t = TFLOT;
            case "bool" -> t = TBOOL;
            case "for" -> t = TTFOR;
            case "repeat" -> t = TREPT;
            case "until" -> t = TUNTL;
            case "if" -> t = TIFTH;
            case "else" -> t = TELSE;
            case "elif" -> t = TELIF;
            case "input" -> t = TINPT;
            case "print" -> t = TPRNT;
            case "printline" -> t = TPRLN;
            case "t =" -> t = TRETN;
            case "not" -> t = TNOTT;
            case "and" -> t = TTAND;
            case "or" -> t = TTTOR;
            case "xor" -> t = TTXOR;
            case "true" -> t = TTRUE;
            case "false" -> t = TFALS;
            case "," -> t = TCOMA;
            case "[" -> t = TLBRK;
            case "]" -> t = TRBRK;
            case "(" -> t = TLPAR;
            case ")" -> t = TRPAR;
            case "=" -> t = TEQUL;
            case "+" -> t = TPLUS;
            case "-" -> t = TMINS;
            case "*" -> t = TSTAR;
            case "/" -> t = TDIVD;
            case "%" -> t = TPERC;
            case "^" -> t = TCART;
            case "<" -> t = TLESS;
            case ">" -> t = TGRTR;
            case ":" -> t = TCOLN;
            case ";" -> t = TSEMI;
            case "." -> t = TDOTT;
            case "!=" -> t = TNEQL;
            case "==" -> t = TEQEQ;
            case "<=" -> t = TLEQL;
            case ">=" -> t = TGEQL;
            case "+=" -> t = TPLEQ;
            case "-=" -> t = TMNEQ;
            case "/=" -> t = TDVEQ;
            case "*=" -> t = TSTEQ;
            default -> t = TUNDF;
        }
        return t;
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

    public Token(String tokenLiteral_, int row_, int col_) {
        row = row_;
        col = col_;
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
            Pattern pattern = Pattern.compile("[(a-z)+a-z0-9]", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(tokenLiteral);
            boolean matchFound = matcher.find();
            if (matchFound) {
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
        return out.toString() + (System.getenv("DEBUG").compareTo("true") == 0 ? " " + tokenLiteral : "");
    }

}
