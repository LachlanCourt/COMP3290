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
import Scanner.Scanner;
import Scanner.Token;

public class Parser {
    private Scanner s;
    public Parser(Scanner s_) {
        s = s_;
    }

    /**
     * Placeholder initialisation function
     */
    public void init() {

    }

    /**
     * Main run method of the parser
     */
    public void run() {
            boolean reachedEndOfFile = false;
            String line = "";
            while (!reachedEndOfFile) {
                // Get a token from the scanner
                Token t = s.getToken();

                // Output lexical errors on a newline
                if (t.isUndf()) {
                    System.out.println(line);
                    line = "";
                    System.out.println(t + "\nLexical Error: " + t.getTokenLiteral());
                } else {
                    // Break to a newline once exceeding 60 characters if we are not in debug mode to match spec
                    if (line.length() > 60 || (System.getenv("DEBUG") != null && System.getenv("DEBUG").compareTo("true") == 0)) {
                        System.out.println(line);
                        line = "";
                    }
                    line += t;
                }

                // Ensure we stop reading once reaching the end of file
                reachedEndOfFile = t.isEof();
            }
            // The loop only outputs a line when exceeding 60 characters long, so print out the last line if there is
            // anything still there
            if (line.length() > 0) {
                System.out.println(line);
            }
        }

}
