/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class handles the generation of the program listing during the
 ****    compiling process
 *******************************************************************************/
package Common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Listing {
    private String currentLine;
    private int lineNumber;

    private String filename;

    public Listing() {
        currentLine = "";
        lineNumber = 1;
    }

    public void initialise(String filename_) {
        filename = filename_;

        // Open and close the file to clear it and start fresh
        try {
            new FileWriter(filename).close();
        } catch (IOException e) {
            System.err.println("An error occurred creating the listing file");
            e.printStackTrace();
        }
    }

    /**
     * Output the current line to the listing file and clear the currentLine variable
     */
    private void outputCurrentLine() {
        try {
            FileWriter writer = new FileWriter(filename, true);
            writer.write(lineNumber + currentLine + "\n");
            writer.close();
            currentLine = "";
        } catch (IOException e) {
            System.err.println("An error occurred outputting the listing to file");
            e.printStackTrace();
        }
    }

    /**
     * Add a single character to the listing
     * @param c character to be added
     */
    public void addCharacter(String c) {
        if (c.compareTo("\n") == 0) {
            outputCurrentLine();
            lineNumber++;
        } else {
            currentLine += c;
        }
    }

    /**
     * Force output the final line of the code to the listing as there may not be a newline
     * character at the end of the CD22 source
     */
    public void flushListing() {
        outputCurrentLine();
    }

    /**
     * Outputs a provided list of errors to the listing file
     * @param errors a list of errors or warnings to be written to the listing
     */
    public void addErrorsToListing(ArrayList<ErrorMessage> errors) {
        try {
            FileWriter writer = new FileWriter(filename, true);
            writer.write("\n");
            for (ErrorMessage e : errors) {
                writer.write(e.toString(false) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("An error occurred outputting the program listing to file");
            e.printStackTrace();
        }
    }
}
