import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class Scanner {
    private ArrayList<String> listing;
    private java.util.Scanner fileScanner;

    private static ArrayList<String> keywords = new ArrayList<String>(Arrays.asList("CD22", "constants", "types", "def", "arrays", "main", "begin", "end", "array", "of", "func", "void", "const", "int", "float", "bool", "for", "repeat", "until", "if", "else", "elif", "input", "print", "printline", "return", "not", "and", "or", "xor", "true", "false"));

    public Scanner() {
        listing = new ArrayList<String>();

    }

    public void loadFile(String filename) {
        fileScanner = null;
        try {
            fileScanner = new java.util.Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            System.err.println("File " + filename + "does not exist");
            System.exit(1);
        }
    }

    public boolean eof() {
        return fileScanner.hasNext();
    }

    public String getToken() {
        return fileScanner.nextLine();
    }
}
