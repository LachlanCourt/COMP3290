/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class handles both the error handling and program listing of the
 ****    compiling process
 *******************************************************************************/
package Common;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class OutputController {
    private final ErrorHandler errorHandler;
    private final Listing listing;

    private PrintStream fileOut;

    public OutputController() {
        errorHandler = new ErrorHandler();
        listing = new Listing();
    }

    /**
     * Initialises the output controller by determining the filename the log and listing files
     * should have
     *
     * @param filename_ the path passed into the program to the source code file
     */
    public void initialise(String filename_) {
        String filename;
        // Strip directory path
        String[] arr = filename_.split("/");
        filename = arr.length > 0 ? arr[arr.length - 1] : filename_;
        // Remove existing extension and add .log and .lst
        filename = filename.substring(0, filename.lastIndexOf("."));
        listing.initialise(filename + ".lst");
        filename += ".log";
        try {
            // Set up the file output stream for the log file
            fileOut = new PrintStream(filename);
        } catch (FileNotFoundException e) {
            System.err.println("Log file could not be opened");
            e.printStackTrace();
        }
    }

    /**
     * Output the errors to standard output, and to the log file if requested
     */
    public void reportErrors(boolean outputToFile) {
        // Only output if errors exist
        if (errorHandler.hasErrors()) {
            System.out.println("Errors exist within the compilation process:\n");
            if (outputToFile)
                outputToFile("Errors exist within the compilation process:\n");
            // Loop through the errors and output to both stdout and to file if specified
            for (ErrorMessage error : errorHandler.getErrors()) {
                System.out.println(error.toString(true));
                if (outputToFile)
                    outputToFile(error.toString(false));
            }
        }
    }

    /**
     * Reports errors to only standard output, not to file
     */
    public void reportErrors() {
        reportErrors(false);
    }

    /**
     * Output the warnings to standard output and to a file if requested
     */
    public void reportWarnings(boolean outputToFile) {
        // Only output if warnings exist
        if (errorHandler.hasWarnings()) {
            System.out.println("\nWarnings exist within the compilation process:\n");
            if (outputToFile)
                outputToFile("\nWarnings exist within the compilation process:\n");
            // Loop through the warnings and output to both stdout and to file if specified
            for (ErrorMessage warning : errorHandler.getWarnings()) {
                System.out.println(warning.toString(true));
                if (outputToFile)
                    outputToFile(warning.toString(false));
            }
            String warningCount = "Found " + errorHandler.getWarnings().size() + " warning" + (errorHandler.getWarnings().size() != 1 ? "s" : "");
            System.out.println(warningCount);
            if (outputToFile)
                outputToFile(warningCount);
        }
    }

    /**
     * Report warnings to only standard output, not to file
     */
    public void reportWarnings() {
        reportWarnings(false);
    }

    /**
     * Output all errors and warnings, including adding them to the listing. Exits program with
     * invalid status code if any errors exist
     */
    public void reportErrorsAndWarnings() {
        // Output errors and warnings to both stdout and to file
        reportErrors(true);
        reportWarnings(true);
        // Output errors and warnings to the program listing
        listing.addErrorsToListing(errorHandler.getErrors());
        listing.addErrorsToListing(errorHandler.getWarnings());
        // Exit if there are any errors, to prevent the compiler moving onto the next phase until
        // all errors from that phase are resolved
        if (hasErrors()) {
            String errorCount = "Found " + errorHandler.getErrors().size() + " error" + (errorHandler.getErrors().size() != 1 ? "s" : "");
            System.out.println(errorCount);
            outputToFile(errorCount);
            System.exit(1);
        }

        System.out.println("No errors found");
    }

    /**
     * Add a single error and associated data to the error handler
     *
     * @param currentRow    row of the token
     * @param currentColumn column of the token
     * @param error         the error type
     * @param data          the token that caused the error
     */
    public void addError(
            int currentRow, int currentColumn, ErrorMessage.Errors error, String data) {
        errorHandler.addError(currentRow, currentColumn, error, data);
    }

    /**
     * Add a single error and associated data to the error handler
     *
     * @param currentRow    row of the token
     * @param currentColumn column of the token
     * @param error         the error type
     */
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
     * Re-call the listing's flushListing function to ensure the last line is printed
     */
    public void flushListing() {
        listing.flushListing();
    }

    // Getters and queries

    public boolean hasErrors() {
        return errorHandler.hasErrors();
    }

    public boolean hasWarnings() {
        return errorHandler.hasWarnings();
    }

    public boolean hasWarningsOrErrors() {
        return hasErrors() || hasWarnings();
    }

    /**
     * Output to both stdout and log file
     *
     * @param data to be outputted
     */
    public void out(String data) {
        System.out.println(data);
        outputToFile(data);
    }

    /**
     * Output to log file
     *
     * @param data to be outputted
     */
    private void outputToFile(String data) {
        if (fileOut != null) {
            fileOut.println(data);
        }
    }
}
