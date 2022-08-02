/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class is the main file for a Scanner for the CD22 programming language
 *******************************************************************************/

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

    public void runInDebug(Scanner s) {
        boolean end = false;
        String line = "";
        while (!end) {
            Token t = s.getToken();

            if (t.isUndf()) {
                System.out.println(line);
                line = "";
                System.out.println(t + "\nLexical Error: " + t.getTokenLiteral());
            } else {
                if (line.length() > 60) {
                    System.out.println(line);
                    line = "";
                }
                line += t;
            }

            end = t.isEof();
        }
        if (line.length() > 0) {
            System.out.println(line);
        }
    }
    public void run(String[] args) {
        OutputController outputController = new OutputController();
        SymbolTable symbolTable = new SymbolTable();
        Scanner s = new Scanner(outputController, symbolTable);
        s.loadFile(args[0]);

        runInDebug(s);
        
        outputController.reportErrorsAndWarnings();

        System.out.println("Program Completed Successfully");

        System.out.println("\nSYMBOL TABLE\n" + symbolTable);

    }
}
