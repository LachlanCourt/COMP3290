import java.util.ArrayList;

public class ErrorHandler {
    enum CompilerPhases {
        SCANNING, PARSING, SEMANTIC_ANALYSIS, CODE_GENERATION
    }

    private CompilerPhases phase;
    private ArrayList<ErrorMessage> errors;

    public ErrorHandler() {
        phase = CompilerPhases.SCANNING;
        errors = new ArrayList<ErrorMessage>();
    }

    public void addError(int row, int col, ErrorMessage.Errors type, String data) {
        errors.add(new ErrorMessage(row, col, type, data));
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public ArrayList<ErrorMessage> getErrors() {
        return errors;
    }
}
