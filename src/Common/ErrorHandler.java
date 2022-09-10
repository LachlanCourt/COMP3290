/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class handles the recording and output of error messages throughout
 ****    the compiling process
 *******************************************************************************/
package Common;

import java.util.ArrayList;

public class ErrorHandler {
    private final ArrayList<ErrorMessage> errors;
    private final ArrayList<ErrorMessage> warnings;

    public ErrorHandler() {
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
    }

    /**
     * Adds an error to the handler
     * @param row row number of the error
     * @param col column of the error
     * @param type enum type of error
     * @param data additional data required for certain errors
     */
    public void addError(int row, int col, ErrorMessage.Errors type, String data) {
        errors.add(new ErrorMessage(row, col, type, data));
    }

    /**
     * Adds an error to the handler
     * @param row row number of the error
     * @param col column of the error
     * @param type enum type of error
     */
    public void addError(int row, int col, ErrorMessage.Errors type) {
        errors.add(new ErrorMessage(row, col, type));
    }

    /**
     * Adds a warning to the handler
     * @param row row number of the error
     * @param col column of the error
     * @param type enum type of error
     * @param data additional data required for certain warnings
     */
    public void addWarning(int row, int col, ErrorMessage.Errors type, String data) {
        warnings.add(new ErrorMessage(row, col, type, data));
    }

    /**
     * Adds a warning to the handler
     * @param row row number of the error
     * @param col column of the error
     * @param type enum type of error
     */
    public void addWarning(int row, int col, ErrorMessage.Errors type) {
        warnings.add(new ErrorMessage(row, col, type));
    }

    // Getters and queries
    public boolean hasErrors() {
        return errors.size() > 0;
    }
    public boolean hasWarnings() {
        return warnings.size() > 0;
    }

    public ArrayList<ErrorMessage> getErrors() {
        return errors;
    }

    public ArrayList<ErrorMessage> getWarnings() {
        return warnings;
    }
}
