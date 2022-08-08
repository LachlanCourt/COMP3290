package Parser;
import Scanner.Scanner;
import Scanner.Token;

public class Parser {
    private Scanner s;
    public Parser(Scanner s_) {
        s = s_;
    }

    public void init() {

    }
    public void run() {
            boolean end = false;
            String line = "";
            while (!end) {
                Token t = s.getToken();

                if (t.isUndf()) {
                    System.out.println(line);
                    line = "";
                    System.out.println(t + "\nLexical Error: " + t.getTokenLiteral());
                } else {
                    if (line.length() > 60 || (System.getenv("DEBUG") != null && System.getenv("DEBUG").compareTo("true") == 0)) {
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

}
