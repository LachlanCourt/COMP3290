/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class represents a single error message stored in the error handler
 *******************************************************************************/
package Common;

public class ErrorMessage {
    public enum Errors {
        UNKNOWNM,
        UNDEFINED_TOKEN,
        INTEGER_OUT_OF_RANGE,
        FLOAT_OUT_OF_RANGE,
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

    private void setErrorMessage() {
        String errorText;
        // Output a helpful error message for each error type
        switch (type) {
            case UNDEFINED_TOKEN:
                errorText = "Undefined token: " + data;
                break;
            case WARNING_CD22_SEMANTIC_CASING:
                errorText = "CD22 should be capitalised";
                isWarning = true;
                break;
            case INTEGER_OUT_OF_RANGE:
                errorText = "Integer Out of Range " + data;
                break;
            case FLOAT_OUT_OF_RANGE:
                errorText = "Float Out of Range " + data;
                break;
            default:
                errorText = "";
        }

        errorMessage = (isWarning ? "Warning" : "Error") + " on line " + row + " at column " + col
            + ": " + errorText;
    }

    @Override
    public String toString() {
        return errorMessage;
    }

    public String toString(boolean showColouredText) {
        if (showColouredText) {
            return (isWarning ? YELLOW : RED) + toString() + RESET;
        } else {
            return toString();
        }
    }
}
