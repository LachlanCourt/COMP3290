/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class handles both the error handling and program listing of the
 ****    compiling process
 *******************************************************************************/
package Common;

import java.io.*;

public class OutputController {
    private ErrorHandler errorHandler;
    private Listing listing;
    private String filename;

    private PrintStream fileOut;

    public OutputController() {
        errorHandler = new ErrorHandler();
        listing = new Listing();
    }

    public void initialise(String filename_) {
        // Strip directory path
        String[] arr = filename_.split("/");
        filename = arr.length > 0 ? arr[arr.length - 1] : filename_;
        // Remove existing extension and add .log
        filename = filename.substring(0, filename.lastIndexOf("."));
        listing.initialise(filename + ".lst");
        filename += ".log";
        try {
        fileOut = new PrintStream(filename); } catch (FileNotFoundException e) {
            System.err.println("Log file could not be opened");
        }
    }

    /**
     * Output the errors to standard error
     */
    public void reportErrors(boolean outputToFile) {
        if (errorHandler.hasErrors()) {
            System.out.println("Errors exist within the compilation process:\n");
            if (outputToFile)
                outputToFile("Errors exist within the compilation process:\n");
            for (ErrorMessage error : errorHandler.getErrors()) {
                System.out.println(error.toString(true));
                if (outputToFile)
                    outputToFile(error.toString(false));
            }
        }
    }

    public void reportErrors() {
        reportErrors(false);
    }

    /**
     * Output the warnings to standard error
     */
    public void reportWarnings(boolean outputToFile) {
        if (errorHandler.hasWarnings()) {
            System.out.println("\nWarnings exist within the compilation process:\n");
            if(outputToFile)
                  outputToFile("\nWarnings exist within the compilation process:\n");
            for (ErrorMessage warning : errorHandler.getWarnings()) {
                System.out.println(warning.toString(true));
                if (outputToFile)
                    outputToFile(warning.toString(false));
            }
        }
    }

    public void reportWarnings() {
        reportWarnings(false);
    }

    /**
     * Output all errors and warnings, including adding them to the listing
     */
    public void reportErrorsAndWarnings() {
        reportErrors(true);
        reportWarnings(true);
        listing.addErrorsToListing(errorHandler.getErrors());
        listing.addErrorsToListing(errorHandler.getWarnings());
                if (hasErrors())
                    System.exit(1);
    }

    /**
     * Add a single error and associated data to the error handler
     *
     * @param currentRow    row of the token
     * @param currentColumn column of the token
     * @param error         the error type
     * @param data          the token that caused the error
     */
    public void addError(int currentRow, int currentColumn, ErrorMessage.Errors error, String data) {
        errorHandler.addError(currentRow, currentColumn, error, data);
    }

    public void addError(int currentRow, int currentColumn, ErrorMessage.Errors error) {
        errorHandler.addError(currentRow, currentColumn, error);
    }

    /**
     * Add a single warning and associated data to the error handler
     *
     * @param currentRow    row of the token
     * @param currentColumn column of the token
     * @param warning       the error type
     */
    public void addWarning(int currentRow, int currentColumn, ErrorMessage.Errors warning) {
        errorHandler.addWarning(currentRow, currentColumn, warning);
    }

    /**
     * Add a single character to the listing
     *
     * @param c the character to be added to the listing
     */
    public void addListingCharacter(String c) {
        listing.addCharacter(c);
    }

    /**
     * Re-call the listing's flushListing function
     */
    public void flushListing() {
        listing.flushListing();
    }

    /**
     * Retrieve a specific character from the listing given a specific row and column
     *
     * @param row target row of character
     * @param col target column of character
     * @return the character at the specified location
     */
    public String getCharacterFromListing(int row, int col) {
        java.util.Scanner fileScanner = null;
        try {
            fileScanner = new java.util.Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            System.err.println("Listing file does not exist");
            System.exit(1);
        }

        String line = "";
        // Loop through the listing rows to the specified row
        for (int i = 0; i < row; i++) {
            line = fileScanner.nextLine();
        }

        // Remove the line number from the front of the line
        line = line.substring(String.valueOf(row).length());
        // Return the character at the specified column
        return String.valueOf(line.charAt(col));
    }

    public boolean hasErrors() {
        return errorHandler.hasErrors();
    }

    public boolean hasWarnings() {
        return errorHandler.hasWarnings();
    }

    public boolean hasWarningsOrErrors() {
        return hasErrors() || hasWarnings();
    }

    public void out(String data) {
        System.out.println(data);
        outputToFile(data);
    }

    private void outputToFile(String data) {
      if (fileOut != null) {
          fileOut.println(data);
      }
    }
}
