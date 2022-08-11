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
        UNKNOWNM, UNDEFINED_TOKEN, INTEGER_OUT_OF_RANGE, FLOAT_OUT_OF_RANGE, WARNING_CD22_SEMANTIC_CASING,
    }

    // Unix operating systems let us use some fancy colours by top and tailing the strings but unfortunately these are
    // not available on Windows :(
    public static final String RESET = System.getProperty("os.name").toLowerCase().contains("windows") ? "" : "\033[0m";  // Text Reset
    public static final String RED = System.getProperty("os.name").toLowerCase().contains("windows") ? "" :"\033[0;31m";     // RED
    public static final String YELLOW = System.getProperty("os.name").toLowerCase().contains("windows") ? "" :"\033[0;33m";  // YELLOW

    private int row;
    private int col;
    private Errors type;
    private String data;

    public ErrorMessage(int row_, int col_, Errors type_, String data_) {
        row = row_;
        col = col_;
        type = type_;
        data = data_;
    }

    public ErrorMessage(int row_, int col_, Errors type_) {
        row = row_;
        col = col_;
        type = type_;
    }

    @Override
    public String toString() {
        String errorText;
        boolean warning = false;
        // Output a helpful error message for each error type
        switch (type) {
            case UNDEFINED_TOKEN -> errorText = "Undefined token: " + data;
            case WARNING_CD22_SEMANTIC_CASING -> {
                errorText = "CD22 should be capitalised";
                warning = true;
            }
            case INTEGER_OUT_OF_RANGE -> errorText = "Integer Out of Range " + data;
            case FLOAT_OUT_OF_RANGE -> errorText = "Float Out of Range " + data;
            default -> errorText = "";
        }
        return (warning ? YELLOW + "Warning" : RED + "Error") + " on line " + row + " at column " + col + ": " + errorText + RESET;
    }
}
