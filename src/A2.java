/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class is the main file for a Scanner for the CD22 programming language
 *******************************************************************************/

import Common.OutputController;
import Common.SymbolTable;
import Parser.Parser;
import Scanner.Scanner;
import java.io.File;

public class A2 {
    public static void main(String[] args) {
        A2 a = new A2();
        // Only run the compiler if a valid filename has been provided
        if (a.validateArgs(args)) {
            a.run(args);
        } else {
            System.err.println("Invalid Arguments. Please specify a filename to compile");
            System.exit(1);
        }
    }

    public boolean validateArgs(String[] args) {
        // Ensure at least 1 argument exists
        if (args.length < 1) {
            return false;
        }
        // Ensure the first argument refers to a valid file
        File f = new File(args[0]);
        if (!f.exists()) {
            return false;
        }
        return true;
    }

    public void run(String[] args) {
        // Create instances of the necessary classes
        OutputController outputController = new OutputController();
        SymbolTable symbolTable = new SymbolTable();
        Scanner s = new Scanner(outputController, symbolTable);
        Parser p = new Parser(s);

        // Initialise the scanner and parser by passing the filename of the source code
        s.init(args[0]);
        p.initialise();

        // Start the parser's debug routine to output the result of a scanner getToken call
        p.run();

        // Report any errors and warnings found so far in the compilation
        outputController.reportErrorsAndWarnings();

        // Output the symbol table when debugging
        if (System.getenv("DEBUG") != null && System.getenv("DEBUG").compareTo("true") == 0) {
            System.out.println("\nSYMBOL TABLE\n" + symbolTable);
        }
    }
}
