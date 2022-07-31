public class ErrorMessage {
    enum Errors {
        UNKNOWNM, UNDEFINED_TOKEN, WARNING_CD22_SEMANTIC_CASING,
    }

    public static final String RESET = "\033[0m";  // Text Reset
    public static final String RED = "\033[0;31m";     // RED
    public static final String YELLOW = "\033[0;33m";  // YELLOW

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
        switch (type) {
            case UNDEFINED_TOKEN -> errorText = "Undefined token: " + data;
            case WARNING_CD22_SEMANTIC_CASING -> {
                errorText = "CD22 should be capitalised";
                warning = true;
            }
            default -> errorText = "";
        }
        return (warning ? YELLOW + "Warning" :  RED + "Error") + " on line " + row + " at column " + col + ": " + errorText + RESET;
    }
}
