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
        s.loadFile(args[0]);
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

        outputController.reportErrorsAndWarnings();

        System.out.println("Program Completed Successfully");

        System.out.println("\nSYMBOL TABLE\n" + symbolTable);

    }
}
