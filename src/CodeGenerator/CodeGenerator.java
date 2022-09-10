package CodeGenerator;

import Common.CD22ParserException;
import Common.OutputController;
import Parser.Parser;

public class CodeGenerator {
    private Parser parser;
    private OutputController outputController;

    public CodeGenerator(Parser p_, OutputController oc_) {
        parser = p_;
        outputController = oc_;
    }

    public void initialise() {
        // placeholder
    }

    public void run() {
        parser.run();
        outputController.out(parser.toString());
    }
}
