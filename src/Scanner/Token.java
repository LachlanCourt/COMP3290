/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class represents a token that has been identified by the Scanner
 *******************************************************************************/
package Scanner;

public class Token {
    @SuppressWarnings("SpellCheckingInspection")
    public enum Tokens {
        TTEOF,
        TCD22,
        TCONS,
        TTYPS,
        TTDEF,
        TARRS,
        TMAIN,
        TBEGN,
        TTEND,
        TARAY,
        TTTOF,
        TFUNC,
        TVOID,
        TCNST,
        TINTG,
        TFLOT,
        TBOOL,
        TTFOR,
        TREPT,
        TUNTL,
        TIFTH,
        TELSE,
        TELIF,
        TINPT,
        TPRNT,
        TPRLN,
        TRETN,
        TNOTT,
        TTAND,
        TTTOR,
        TTXOR,
        TTRUE,
        TFALS,
        TCOMA,
        TLBRK,
        TRBRK,
        TLPAR,
        TRPAR,
        TEQUL,
        TPLUS,
        TMINS,
        TSTAR,
        TDIVD,
        TPERC,
        TCART,
        TLESS,
        TGRTR,
        TCOLN,
        TSEMI,
        TDOTT,
        TLEQL,
        TGEQL,
        TNEQL,
        TEQEQ,
        TPLEQ,
        TMNEQ,
        TSTEQ,
        TDVEQ,
        TIDEN,
        TILIT,
        TFLIT,
        TSTRG,
        TUNDF,
        NUM_PARSE_ERROR
    }

    private final String tokenLiteral;
    private final Tokens token;
    private int row;
    private int col;

    public Token(Tokens tokenType_, String tokenLiteral_, int row_, int col_) {
        row = row_;
        col = col_;
        tokenLiteral = tokenLiteral_;
        token = tokenType_;
    }

    /**
     * The EOF token is a special kind that requires no other data. It's value does not matter.
     */
    public Token() {
        token = Tokens.TTEOF;
        tokenLiteral = "";
    }

    public boolean isNotEof() {
        return token != Tokens.TTEOF;
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
     *
     * @return a stringified version of the token
     */
    @Override
    public String toString() {
        // If we are not in debug mode, output per spec
        if (System.getenv("DEBUG") == null || System.getenv("DEBUG").compareTo("true") != 0) {
            StringBuilder out = new StringBuilder(token.name() + " ");

            switch (token) {
                case TILIT:
                case TFLIT:
                case TSTRG:
                case TIDEN:
                    // As per specification, literal values should be padded as a multiple of 6 with
                    // at least one trailing space
                    int size = (tokenLiteral.length() / 6) * 6 + 6;
                    out.append(tokenLiteral).append(" ".repeat(size - tokenLiteral.length()));
            }

            return out.toString();
        } else {
            // In debug mode, output some additional information from the token
            String out = "(" + token.name() + " ," + row + "," + col + ")";
            return out + " " + tokenLiteral;
        }
    }
}
