import java.io.FileWriter;
import java.io.IOException;

public class Listing {
    private String currentLine;
    private int lineNumber;

    public Listing() {
        currentLine = "";
        lineNumber = 1;

        // Open and close the file to clear it and start fresh
        try {
            new FileWriter("listing.txt").close();
        } catch (IOException e) {
            System.err.println("An error occurred creating the listing file");
            e.printStackTrace();
        }
    }

    private void outputCurrentLine() {
        try {
            FileWriter writer = new FileWriter("listing.txt", true);
            writer.write(lineNumber + currentLine + "\n");
            writer.close();
            currentLine = "";
        } catch (IOException e) {
            System.err.println("An error occurred outputting the listing to file");
            e.printStackTrace();
        }

    }

    public void addCharacter(String c) {
        if (c.compareTo("\n") == 0) {
            outputCurrentLine();
            lineNumber++;
        } else {
            currentLine += c;
        }
    }

    public void flushListing() {
        outputCurrentLine();
    }
}
