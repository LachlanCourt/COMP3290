/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class is a placeholder for the code generator, which currently
 ****    runs the Parser's debug routine
 *******************************************************************************/
package CodeGenerator;

import Common.OutputController;
import Parser.Parser;

public class CodeGenerator {
    private final Parser parser;
    private final OutputController outputController;

    public CodeGenerator(Parser p_, OutputController oc_) {
        parser = p_;
        outputController = oc_;
    }

    public void initialise() {
        // placeholder
    }

    /**
     * Run the parser, and then output the syntax tree. In order for the parser to run, it must have
     * already been initialised
     */
    public void run() {
        parser.run();
        outputController.out(parser.toString());
    }
}
