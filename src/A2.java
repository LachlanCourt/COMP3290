/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class is the main file for a Scanner and Parser for the CD22
 ****    programming language
 *******************************************************************************/

import CodeGenerator.CodeGenerator;
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
        return f.exists();
    }

    /**
     * Main run method of A2
     * @param args CLA passed from main
     */
    public void run(String[] args) {
        // Create instances of the necessary classes
        OutputController oc = new OutputController();
        SymbolTable st = SymbolTable.getSymbolTable();
        Scanner s = new Scanner(oc);
        Parser p = new Parser(s, st, oc);
        CodeGenerator cg = new CodeGenerator(p, oc);

        // Initialise the output controller and scanner passing the filename of the source code
        oc.initialise(args[0]);
        s.initialise(args[0]);

        // Initialise the parser which gets the token stream from the scanner. As undefined tokens
        // should not be parsed, check for errors before continuing, which will terminate if any are
        // found
        p.initialise();
        if (oc.hasErrors())
            oc.reportErrorsAndWarnings();

        // Initialise and run code generator's debug routine to output the syntax tree
        cg.initialise();
        cg.run();

        // Output the symbol table when debugging
        if (System.getenv("DEBUG") != null && System.getenv("DEBUG").compareTo("true") == 0) {
            System.out.println("\nSYMBOL TABLE\n" + st);
        }

        // Report any errors and warnings found so far in the compilation
        oc.reportErrorsAndWarnings();
    }
}
