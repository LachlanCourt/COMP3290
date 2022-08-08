/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class is the main file for a Scanner for the CD22 programming language
 *******************************************************************************/

import Common.OutputController;
import Common.SymbolTable;
import Scanner.Scanner;
import Parser.Parser;

import java.io.File;

public class A1 {
    public static void main(String[] args) {
        A1 a = new A1();
        if (a.validateArgs(args)) {
            a.run(args);
        } else {
            System.err.println("Invalid Arguments. Please specify a filename to compile");
            System.exit(1);
        }
    }

    public boolean validateArgs(String[] args) {
        if (args.length < 1) {
            return false;
        }
        File f = new File(args[0]);
        if (!f.exists()) {
            return false;
        }
        return true;
    }


    public void run(String[] args) {
        OutputController outputController = new OutputController();
        SymbolTable symbolTable = new SymbolTable();
        Scanner s = new Scanner(outputController, symbolTable);
        Parser p = new Parser(s);

        s.init(args[0]);
        p.init();

        p.run();
        
        outputController.reportErrorsAndWarnings();

        System.out.println("\nSYMBOL TABLE\n" + symbolTable);

    }
}
