import java.io.File;

public class A1 {
    public static void main (String[] args) {
        A1 a = new A1();
        if (a.validateArgs(args)) {
            a.run(args);
        }
        else {
            System.err.println("Invalid Arguments. Please specify a filename to compile");
            System.exit(1);
        }
    }

    public boolean validateArgs(String[] args) {
        if (args.length < 1) {
            return false;
        }
        File f = new File(args[0]);
        if (!f.exists())
        {
            return false;
        }
        return true;
    }

    public void run(String[] args) {
        System.out.println("Hello");
    }
}
