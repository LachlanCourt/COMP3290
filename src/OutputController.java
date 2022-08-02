/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class handles both the error handling and program listing of the
 ****    compiling process
 *******************************************************************************/

import java.io.File;
import java.io.FileNotFoundException;

public class OutputController {
    private ErrorHandler errorHandler;
    private Listing listing;

    public OutputController() {
        errorHandler = new ErrorHandler();
        listing = new Listing();
    }

    public void reportErrors() {
        if (errorHandler.hasErrors()) {
            System.err.println("Errors exist within the compiling process:\n");
            for (ErrorMessage error : errorHandler.getErrors()) {
                System.err.println(error);
            }
        }
    }

    public void reportWarnings() {
        if (errorHandler.hasWarnings()) {
            System.err.println("\nWarnings exist within the compiling process:\n");
            for (ErrorMessage warning : errorHandler.getWarnings()) {
                System.err.println(warning);
            }
        }
    }

    public void reportErrorsAndWarnings() {
        reportErrors();
        reportWarnings();
        if (errorHandler.hasErrors()) System.exit(1);
    }

    public void addError(int currentRow, int currentColumn, ErrorMessage.Errors error, String tokenLiteral) {
        errorHandler.addError(currentRow, currentColumn, error, tokenLiteral);
    }

    public void addWarning(int currentRow, int currentColumn, ErrorMessage.Errors warning) {
        errorHandler.addWarning(currentRow, currentColumn, warning);
    }

    public void addListingCharacter(String c) {
        listing.addCharacter(c);
    }

    public void flushListing() {
        listing.flushListing();
    }

    public String getCharacterFromListing(int row, int col) {
        java.util.Scanner fileScanner = null;
        try {
            fileScanner = new java.util.Scanner(new File("listing.txt"));
        } catch (FileNotFoundException e) {
            System.err.println("Listing file does not exist");
            System.exit(1);
        }

        String line = "";
        for (int i = 0; i < row; i++) {
            line = fileScanner.nextLine();
        }

        line = line.substring(String.valueOf(row).length());
        return String.valueOf(line.charAt(col));
    }
}
