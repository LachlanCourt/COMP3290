/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class represents a single error message stored in the error handler
 *******************************************************************************/
package Common;

public class ErrorMessage {
    public enum Errors {
        UNKNOWN,
        UNDEFINED_TOKEN,
        INTEGER_OUT_OF_RANGE,
        FLOAT_OUT_OF_RANGE,
        EXPECTED_IDENTIFIER,
        PROGRAM_IDEN_MISSING,
        NOT_AT_EOF,
        UNEXPECTED_EOF,
        NOT_A_NUMBER,
        NO_STATEMENTS,
        UNDEFINED_TYPE,
        UNDEFINED_VARIABLE,
        EXPECTED_ASSIGNMENT_OPERATOR,
        CUSTOM_ERROR,
        WARNING_CD22_SEMANTIC_CASING,
    }

    // Unix operating systems let us use some fancy colours by top and tailing the strings but
    // unfortunately these are not available on Windows :(
    public static final String RESET =
        System.getProperty("os.name").toLowerCase().contains("windows") ? ""
                                                                        : "\033[0m"; // Text Reset
    public static final String RED =
        System.getProperty("os.name").toLowerCase().contains("windows") ? "" : "\033[0;31m"; // RED
    public static final String YELLOW =
        System.getProperty("os.name").toLowerCase().contains("windows") ? ""
                                                                        : "\033[0;33m"; // YELLOW

    private int row;
    private int col;
    private Errors type;
    private String data;

    private String errorMessage;
    private boolean isWarning;

    /**
     * Private constructor used by the public ones to set defaults for all errors
     */
    private ErrorMessage() {
        isWarning = false;
    }

    public ErrorMessage(int row_, int col_, Errors type_, String data_) {
        this();
        row = row_;
        col = col_;
        type = type_;
        data = data_;
        setErrorMessage();
    }

    public ErrorMessage(int row_, int col_, Errors type_) {
        this();
        row = row_;
        col = col_;
        type = type_;
        setErrorMessage();
    }

    /**
     * Sets the error text so that it is available via a simple query
     */
    private void setErrorMessage() {
        String errorText = getErrorText();
        errorMessage = (isWarning ? "Warning" : "Error") + " on line " + row + " around column "
            + col + ": " + errorText;
    }

    /**
     * Get the error text based on the error type
     * @return the human-readable error from the enum type
     */
    private String getErrorText() {
        // Output a helpful error message for each error type
        switch (type) {
            case UNDEFINED_TOKEN:
                return "Undefined token: " + data;
            case WARNING_CD22_SEMANTIC_CASING:
                isWarning = true;
                return "CD22 should be capitalised";
            case INTEGER_OUT_OF_RANGE:
                return "Integer Out of Range " + data;
            case FLOAT_OUT_OF_RANGE:
                return "Float Out of Range " + data;
            case EXPECTED_IDENTIFIER:
                return "Expected Identifier";
            case PROGRAM_IDEN_MISSING:
                return "Program Identifier Missing";
            case NOT_A_NUMBER:
                return "Expected number or identifier";
            case NO_STATEMENTS:
                return "At least one statement is required";
            case EXPECTED_ASSIGNMENT_OPERATOR:
                return "Expected assignment operator";
            case UNDEFINED_TYPE:
                return "Undefined type";
            case UNDEFINED_VARIABLE:
                return "Undefined variable \"" + data + "\"";
            case CUSTOM_ERROR:
                return data;
            case NOT_AT_EOF:
                return "Unexpected content at end of program";
            case UNEXPECTED_EOF:
                return "Unexpected end of file";
            default:
                return "An error occurred";
        }
    }

    @Override
    public String toString() {
        return errorMessage;
    }

    public String toString(boolean showColouredText) {
        if (showColouredText) {
            return (isWarning ? YELLOW : RED) + this + RESET;
        } else {
            return toString();
        }
    }
}
