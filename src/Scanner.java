import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scanner {

    private enum ContextStates {
        UNDF, CHAR, NUM, PUNC,
    }

    private enum DecimalStates {
        INITIAL, FOUND_DECIMAL, FOUND_FOLLOWING_NUMBER
    }

    private ArrayList<String> listing;
    private java.util.Scanner fileScanner;
    private int lineCounter;
    private int columnCounter;
    private String buffer;
    private boolean debug;
    private static ArrayList<String> validPunctuation = new ArrayList<String>(Arrays.asList(",", "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", "!", "\"", ":", ";", "."));
    private static ArrayList<String> validDoubleOperators = new ArrayList<String>(Arrays.asList("!=", "==", "<=", ">=", "+=", "-=", "/=", "*="));

    public Scanner(boolean debug_) {
        listing = new ArrayList<String>();
        columnCounter = 0;
        lineCounter = 1;
        buffer = "";
        debug = debug_;
    }

    public void loadFile(String filename) {
        fileScanner = null;
        try {
            fileScanner = new java.util.Scanner(new File(filename));
            fileScanner.useDelimiter("");
        } catch (FileNotFoundException e) {
            System.err.println("File " + filename + "does not exist");
            System.exit(1);
        }
    }

    private boolean eof() {
        return !fileScanner.hasNext();
    }

    private void readFileIntoBufferUntilWhitespace() {
        String bufferCandidate = "";
        String character;

        boolean inString = false;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;
        while (!eof()) {
            character = fileScanner.next();

            // Ignore carriage returns
            if (character.charAt(0) == 13) continue;

            // Keep track of columns
            columnCounter++;
            if (character.compareTo("\n") == 0) {
                lineCounter++;
                columnCounter = 0;
            }

            if (!inString && character.compareTo("\"") == 0) inString = true;

            // Check for invalid characters and break on whitespace
            if (!inString && !inSingleLineComment && !inMultiLineComment) {
                Pattern pattern = Pattern.compile("[a-z0-9\s\n]", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(character);
                boolean matchFound = matcher.find();
                if (!validPunctuation.contains(character) && !matchFound) {
                    //TODO Add invalid character error
                    System.err.println("Invalid character on line " + lineCounter + " at column " + columnCounter + ": " + character);
                    bufferCandidate = "";
                    continue;
                }
                if (character.compareTo(" ") == 0 || character.compareTo("\n") == 0) {
                    break;
                }
            }

            bufferCandidate += character;

            if (bufferCandidate.startsWith("/--")) inSingleLineComment = true;
            if (bufferCandidate.startsWith("/**")) inMultiLineComment = true;

            if ((inSingleLineComment && character.compareTo("\n") == 0) || (inMultiLineComment && bufferCandidate.endsWith("**/"))) {
                inSingleLineComment = false;
                inMultiLineComment = false;
                bufferCandidate = "";
                continue;
            }

            if (bufferCandidate.startsWith("\"") && bufferCandidate.endsWith("\"") && bufferCandidate.length() > 1)
                break;

        }
        if (bufferCandidate.compareTo("") == 0) {
            readFileIntoBufferUntilWhitespace();
        } else {
            buffer = bufferCandidate;
        }
    }

    private String getTokenStringFromBuffer() {
        // This context matcher allows us to handle cases where there is no whitespace between a sample but there is
        // no syntactical way that it could be a single token
        // It follows some simple rules:
        // Any sample that starts with a character is assumed to be an identifier all the way until a punctuation mark is reached
        // Any sample that begins with a number ends at the next character or punctuation mark unless it is a decimal point followed by more numbers
        // Any sample that begins with punctuation ends either at the next character, number, or when it no longer matches a valid operator

        String tokenStringCandidate = "";
        String character;

        if (buffer.startsWith("\"") && buffer.endsWith("\"")) {
            tokenStringCandidate = buffer;
            buffer = "";
            return tokenStringCandidate;
        }

        ContextStates contextState = ContextStates.UNDF;
        boolean tokenStringFound = false;
        DecimalStates decimalState = DecimalStates.INITIAL;
        int i;
        for (i = 0; i < buffer.length(); i++) {
            character = String.valueOf(buffer.charAt(i));

            // Context switching
            switch (contextState) {
                case UNDF -> {
                    Pattern pattern = Pattern.compile("[a-z]", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(character);
                    boolean matchFound = matcher.find();
                    if (matchFound) contextState = ContextStates.CHAR;

                    pattern = Pattern.compile("[0-9]", Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(character);
                    matchFound = matcher.find();
                    if (matchFound) contextState = ContextStates.NUM;

                    if (validPunctuation.contains(character)) contextState = ContextStates.PUNC;
                }
                case CHAR -> {
                    if (validPunctuation.contains(character)) tokenStringFound = true;

                }
                case PUNC -> {
                    if (!validPunctuation.contains(character)) tokenStringFound = true;
                    if (i == 1 && !validDoubleOperators.contains(buffer.substring(0, 2))) {
                        tokenStringFound = true;
                    }
                    if (i > 1) tokenStringFound = true;
                }
                case NUM -> {
                    if (validPunctuation.contains(character)) {
                        if (character.compareTo(".") == 0 && decimalState == DecimalStates.INITIAL) {
                            decimalState = DecimalStates.FOUND_DECIMAL;
                        }
                        else {
                            tokenStringFound = true;
                        }
                    }
                    Pattern pattern = Pattern.compile("[a-z]", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(character);
                    boolean matchFound = matcher.find();
                    if (matchFound) tokenStringFound = true;

                    pattern = Pattern.compile("[0-9]", Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(character);
                    matchFound = matcher.find();
                    if (decimalState == DecimalStates.FOUND_DECIMAL && matchFound) {
                        decimalState = DecimalStates.FOUND_FOLLOWING_NUMBER;
                    }
                }
            }
            if (tokenStringFound) break;
        }

        if (!tokenStringFound) {
            tokenStringCandidate = buffer;
            buffer = "";
            return tokenStringCandidate;
        }

        if (decimalState == DecimalStates.FOUND_DECIMAL) {
            // If a number was followed by a decimal but then didn't have a number after that, it should treat it instead
            // as an integer token followed by a dot token. To achieve this, step back an extra character to leave the dot
            // in the buffer
            i--;
        }

        tokenStringCandidate = buffer.substring(0, i );
        buffer = buffer.substring(i);
        return tokenStringCandidate;
    }

    public Token getToken() {
        if (eof()) {
            return new Token(true);
        }
        if (buffer.length() == 0) {
            readFileIntoBufferUntilWhitespace();
        }

        return new Token(getTokenStringFromBuffer(), lineCounter, columnCounter, debug);
    }
}
