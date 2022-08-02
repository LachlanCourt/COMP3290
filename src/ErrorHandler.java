/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class handles the recording and output of error messages throughout
 ****    the compiling process
 *******************************************************************************/

import java.util.ArrayList;

public class ErrorHandler {
    enum CompilerPhases {
        SCANNING, PARSING, SEMANTIC_ANALYSIS, CODE_GENERATION
    }

    private CompilerPhases phase;
    private ArrayList<ErrorMessage> errors;
    private ArrayList<ErrorMessage> warnings;

    public ErrorHandler() {
        phase = CompilerPhases.SCANNING;
        errors = new ArrayList<ErrorMessage>();
        warnings = new ArrayList<ErrorMessage>();
    }

    /**
     *
     * @param row row number of the error
     * @param col column of the error
     * @param type enum type of error
     * @param data additional data required for certain errors
     */
    public void addError(int row, int col, ErrorMessage.Errors type, String data) {
        errors.add(new ErrorMessage(row, col, type, data));
    }

    public void addError(int row, int col, ErrorMessage.Errors type) {
        errors.add(new ErrorMessage(row, col, type));
    }

    public void addWarning(int row, int col, ErrorMessage.Errors type, String data) {
        warnings.add(new ErrorMessage(row, col, type, data));
    }

    public void addWarning(int row, int col, ErrorMessage.Errors type) {
        warnings.add(new ErrorMessage(row, col, type));
    }

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
