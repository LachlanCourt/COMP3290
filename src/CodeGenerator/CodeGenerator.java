package CodeGenerator;

import Common.CD22ParserException;
import Parser.Parser;

public class CodeGenerator {
    public Parser parser;

    public CodeGenerator(Parser p_) {
        parser = p_;
    }

    public void initialise() {
        // placeholder
    }

    public void run() {
        parser.run();
        System.out.println(parser);
    }
}
