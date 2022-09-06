/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class represents a token that has been indentified by the Scanner
 *******************************************************************************/
package Scanner;

public class Token {
    public enum Tokens {
        TTEOF("TTEOF "),
        TCD22("TCD22 "),
        TCONS("TCONS "),
        TTYPS("TTYPS "),
        TTDEF("TTDEF "),
        TARRS("TARRS "),
        TMAIN("TMAIN "),
        TBEGN("TBEGN "),
        TTEND("TTEND "),
        TARAY("TARAY "),
        TTTOF("TTTOF "),
        TFUNC("TFUNC "),
        TVOID("TVOID "),
        TCNST("TCNST "),
        TINTG("TINTG "),
        TFLOT("TFLOT "),
        TBOOL("TBOOL "),
        TTFOR("TTFOR "),
        TREPT("TREPT "),
        TUNTL("TUNTL "),
        TIFTH("TIFTH "),
        TELSE("TELSE "),
        TELIF("TELIF "),
        TINPT("TINPT "),
        TPRNT("TPRNT "),
        TPRLN("TPRLN "),
        TRETN("TRETN "),
        TNOTT("TNO "),
        TTAND("TTAND "),
        TTTOR("TTTOR "),
        TTXOR("TTXOR "),
        TTRUE("TTRUE "),
        TFALS("TFALS "),
        TCOMA("TCOMA "),
        TLBRK("TLBRK "),
        TRBRK("TRBRK "),
        TLPAR("TLPAR "),
        TRPAR("TRPAR "),
        TEQUL("TEQUL "),
        TPLUS("TPLUS "),
        TMINS("TMINS "),
        TSTAR("TSTAR "),
        TDIVD("TDIVD "),
        TPERC("TPERC "),
        TCART("TCART "),
        TLESS("TLESS "),
        TGRTR("TGRTR "),
        TCOLN("TCOLN "),
        TSEMI("TSEMI "),
        TDOTT("TDOTT "),
        TLEQL("TLEQL "),
        TGEQL("TGEQL "),
        TNEQL("TNEQL "),
        TEQEQ("TEQEQ "),
        TPLEQ("TPLEQ "),
        TMNEQ("TMNEQ "),
        TSTEQ("TSTEQ "),
        TDVEQ("TDVEQ "),
        TIDEN("TIDEN "),
        TILIT("TILIT "),
        TFLIT("TFLIT "),
        TSTRG("TSTRG "),
        TUNDF("TUNDF "),
        NUM_PARSE_ERROR("");

        private String value;

        Tokens(String initialiser) {
            value = initialiser;
        }

        public String getValue() {
            return value;
        }

        /**
         * Factory pattern to determine predefined keywords and operators
         * @param initialiser string to be determined as a token
         * @return the token type that represents the keyword or operator passed in
         */
        public static Tokens getToken(String initialiser) {
            Tokens t;
            switch (initialiser) {
                case "cd22":
                    return TCD22;
                case "constants":
                    return TCONS;
                case "types":
                    return TTYPS;
                case "def":
                    return TTDEF;
                case "arrays":
                    return TARRS;
                case "main":
                    return TMAIN;
                case "begin":
                    return TBEGN;
                case "end":
                    return TTEND;
                case "array":
                    return TARAY;
                case "of":
                    return TTTOF;
                case "func":
                    return TFUNC;
                case "void":
                    return TVOID;
                case "const":
                    return TCNST;
                case "int":
                    return TINTG;
                case "float":
                    return TFLOT;
                case "bool":
                    return TBOOL;
                case "for":
                    return TTFOR;
                case "repeat":
                    return TREPT;
                case "until":
                    return TUNTL;
                case "if":
                    return TIFTH;
                case "else":
                    return TELSE;
                case "elif":
                    return TELIF;
                case "input":
                    return TINPT;
                case "print":
                    return TPRNT;
                case "printline":
                    return TPRLN;
                case "return":
                    return TRETN;
                case "not":
                    return TNOTT;
                case "and":
                    return TTAND;
                case "or":
                    return TTTOR;
                case "xor":
                    return TTXOR;
                case "true":
                    return TTRUE;
                case "false":
                    return TFALS;
                case ",":
                    return TCOMA;
                case "[":
                    return TLBRK;
                case "]":
                    return TRBRK;
                case "(":
                    return TLPAR;
                case ")":
                    return TRPAR;
                case "=":
                    return TEQUL;
                case "+":
                    return TPLUS;
                case "-":
                    return TMINS;
                case "*":
                    return TSTAR;
                case "/":
                    return TDIVD;
                case "%":
                    return TPERC;
                case "^":
                    return TCART;
                case "<":
                    return TLESS;
                case ">":
                    return TGRTR;
                case ":":
                    return TCOLN;
                case ";":
                    return TSEMI;
                case ".":
                    return TDOTT;
                case "!=":
                    return TNEQL;
                case "==":
                    return TEQEQ;
                case "<=":
                    return TLEQL;
                case ">=":
                    return TGEQL;
                case "+=":
                    return TPLEQ;
                case "-=":
                    return TMNEQ;
                case "/=":
                    return TDVEQ;
                case "*=":
                    return TSTEQ;
                default:
                    return TUNDF;
            }
        }
    }
    ;

    private final String tokenLiteral;

    private final Tokens token;
    private int row;
    private int col;
    private int symbolTableId;

    public Token(Tokens tokenType_, String tokenLiteral_, int row_, int col_) {
        row = row_;
        col = col_;
        tokenLiteral = tokenLiteral_;
        token = tokenType_;
    }

    /**
     * The EOF token is a special kind that requires no other data. A boolean is passed in to
     * prevent accidentally creating one with the implicit default constructor. It's value does not
     * matter.
     * @param eof an arbitrary variable to differentiate from the default constructor
     */
    public Token(boolean eof) {
        token = Tokens.TTEOF;
        tokenLiteral = "";
    }

    public boolean isEof() {
        return token == Tokens.TTEOF;
    }

    public boolean isUndf() {
        return token == Tokens.TUNDF;
    }

    public String getTokenLiteral() {
        return tokenLiteral;
    }

    public Tokens getToken() {
        return token;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * A stringified version of the token, formatted to match specification
     * @return a stringified version of the token
     */
    @Override
    public String toString() {
        // If we are not in debug mode, output per spec
        if (System.getenv("DEBUG") == null || System.getenv("DEBUG").compareTo("true") != 0) {
            StringBuilder out = new StringBuilder(token.getValue());

            switch (token) {
                case TILIT:
                case TFLIT:
                case TSTRG:
                case TIDEN:
                    // As per specification, literal values should be padded as a multiple of 6 with
                    // at least one trailing space
                    int size = (tokenLiteral.length() / 6) * 6 + 6;
                    out.append(tokenLiteral + " ".repeat(size - tokenLiteral.length()));
            }

            return out.toString();
        } else {
            // In debug mode, output some additional information from the token
            StringBuilder out = new StringBuilder("(");
            out.append(token.getValue() + ",");
            out.append(symbolTableId + ",");
            out.append(row + ",");
            out.append(col + ")");
            return out.toString() + " " + tokenLiteral;
        }
    }
}
