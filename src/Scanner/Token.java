/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class represents a token that has been indentified by the Scanner
 *******************************************************************************/
package Scanner;

public class Token {

    enum Tokens {
        TTEOF("TTEOF "), TCD22("TCD22 "), TCONS("TCONS "), TTYPS("TTYPS "), TTDEF("TTDEF "), TARRS("TARRS "), TMAIN("TMAIN "), TBEGN("TBEGN "), TTEND("TTEND "), TARAY("TARAY "), TTTOF("TTTOF "), TFUNC("TFUNC "), TVOID("TVOID "), TCNST("TCNST "), TINTG("TINTG "), TFLOT("TFLOT "), TBOOL("TBOOL "), TTFOR("TTFOR "), TREPT("TREPT "), TUNTL("TUNTL "), TIFTH("TIFTH "), TELSE("TELSE "), TELIF("TELIF "), TINPT("TINPT "), TPRNT("TPRNT "), TPRLN("TPRLN "), TRETN("TRETN "), TNOTT("TNO "), TTAND("TTAND "), TTTOR("TTTOR "), TTXOR("TTXOR "), TTRUE("TTRUE "), TFALS("TFALS "), TCOMA("TCOMA "), TLBRK("TLBRK "), TRBRK("TRBRK "), TLPAR("TLPAR "), TRPAR("TRPAR "), TEQUL("TEQUL "), TPLUS("TPLUS "), TMINS("TMINS "), TSTAR("TSTAR "), TDIVD("TDIVD "), TPERC("TPERC "), TCART("TCART "), TLESS("TLESS "), TGRTR("TGRTR "), TCOLN("TCOLN "), TSEMI("TSEMI "), TDOTT("TDOTT "), TLEQL("TLEQL "), TGEQL("TGEQL "), TNEQL("TNEQL "), TEQEQ("TEQEQ "), TPLEQ("TPLEQ "), TMNEQ("TMNEQ "), TSTEQ("TSTEQ "), TDVEQ("TDVEQ "), TIDEN("TIDEN "), TILIT("TILIT "), TFLIT("TFLIT "), TSTRG("TSTRG "), TUNDF("TUNDF ");

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
                case "cd22" -> t = TCD22;
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
                case "return" -> t = TRETN;
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

    private final String tokenLiteral;

    private final Tokens token;
    private int row;
    private int col;
    private int symbolTableId;

    public Token(Tokens tokenType_, String tokenLiteral_, Integer symbolTableId_, int row_, int col_) {
        row = row_;
        col = col_;
        tokenLiteral = tokenLiteral_;
        token = tokenType_;
        if (symbolTableId_ != null) {
            symbolTableId = symbolTableId_;
        }
    }

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

    @Override
    public String toString() {
        if (System.getenv("DEBUG") == null || System.getenv("DEBUG").compareTo("true") != 0) {
            StringBuilder out = new StringBuilder(token.getValue());

            switch (token) {
                case TILIT, TFLIT, TSTRG, TIDEN -> {
                    // As per specification, literal values should be padded as a multiple of 6 with at least one
                    // trailing space
                    int size = (tokenLiteral.length() / 6) * 6 + 6;
                    out.append(tokenLiteral + " ".repeat(size - tokenLiteral.length()));
                }
            }

            return out.toString();
        } else {
            StringBuilder out = new StringBuilder("(");
            out.append(token.getValue() + ",");
            out.append(symbolTableId + ",");
            out.append(row + ",");
            out.append(col + ")");
            return out.toString() + " " + tokenLiteral;
        }
    }

}
