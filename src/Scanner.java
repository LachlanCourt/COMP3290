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

    private enum ReaderStates {
        CODE, IN_STRING, IN_SINGLE_LINE_COMMENT, IN_MULTI_LINE_COMMENT, IN_UNDEFINED_TOKEN,
    }

    private java.util.Scanner fileScanner;
    private int lineCounter;
    private int currentRow;
    private int columnCounter;
    private int currentColumn;

    private String buffer;

    private String readerBuffer;
    private OutputController outputController;
    private SymbolTable symbolTable;

    private static ArrayList<String> validPunctuation = new ArrayList<String>(Arrays.asList(",", "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", "!", "\"", ":", ";", "."));
    private static ArrayList<String> validDoubleOperators = new ArrayList<String>(Arrays.asList("!=", "==", "<=", ">=", "+=", "-=", "/=", "*="));

    public Scanner(OutputController outputController_, SymbolTable symbolTable_) {
        columnCounter = 0;
        lineCounter = 1;
        buffer = "";
        readerBuffer = "";
        outputController = outputController_;
        symbolTable = symbolTable_;
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

        ReaderStates readerState = ReaderStates.CODE;

        // The current row and current column are the starting row and column of the buffer we are scanning. The line
        // number and column number may change if a multiline comment begins immediately after the token and no
        // whitespace, as these will be consumed and flushed before this function returns which will increment the line
        // number and column number. Because of this, the line number cannot reliably identify the line of a token
        // when read from the buffer so save it before we start scanning
        currentRow = lineCounter;
        currentColumn = columnCounter;
        while (!eof() || readerBuffer.length() > 0) {
            if (readerBuffer.length() == 0) {
                character = fileScanner.next();
                outputController.addListingCharacter(character);
            } else {
                character = readerBuffer;
                // For the character to be in the reader buffer, it has already been parsed and the counter incremented
                currentColumn--;
                readerBuffer = "";
            }

            // Ignore carriage returns
            if (character.charAt(0) == 13) continue;

            // Keep track of rows and columns
            columnCounter++;
            if (character.compareTo("\n") == 0) {
                lineCounter++;
                columnCounter = 0;
            }

            if (readerState == ReaderStates.CODE && character.compareTo("\"") == 0)
                readerState = ReaderStates.IN_STRING;

            // Check for invalid characters and break on whitespace
            if (readerState == ReaderStates.CODE || readerState == ReaderStates.IN_UNDEFINED_TOKEN) {
                Pattern pattern = Pattern.compile("[a-z0-9\s\n]", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(character);
                boolean matchFound = matcher.find();

                if (!validPunctuation.contains(character) && !matchFound) {
                    readerState = ReaderStates.IN_UNDEFINED_TOKEN;
                } else if (readerState == ReaderStates.IN_UNDEFINED_TOKEN && (validPunctuation.contains(character) || matchFound)) {
                    if (character.compareTo("\n") != 0) readerBuffer = character;
                    break;
                }
                if (character.compareTo(" ") == 0 || character.compareTo("\n") == 0) {
                    break;
                }
            }

            bufferCandidate += character;

            // Check if adding that character has caused the context to change to inside a comment
            if (bufferCandidate.contains("/--") && readerState == ReaderStates.CODE)
                readerState = ReaderStates.IN_SINGLE_LINE_COMMENT;
            if (bufferCandidate.contains("/**") && readerState == ReaderStates.CODE)
                readerState = ReaderStates.IN_MULTI_LINE_COMMENT;

            // Check if a single line comment has ended at a newline
            if (readerState == ReaderStates.IN_SINGLE_LINE_COMMENT && character.compareTo("\n") == 0) {
                bufferCandidate = bufferCandidate.substring(0, bufferCandidate.indexOf("/--"));
                break;
            }
            // Check if a multiline comment has ended at a **/ or if the file has terminated
            if (readerState == ReaderStates.IN_MULTI_LINE_COMMENT && (bufferCandidate.endsWith("**/") || eof())) {
                bufferCandidate = bufferCandidate.substring(0, bufferCandidate.indexOf("/**"));
                break;
            }
            // Check if a string has ended (must start and end with a " and be at least 2 characters long so a single "
            // does not get considered a string
            if (bufferCandidate.startsWith("\"") && bufferCandidate.endsWith("\"") && bufferCandidate.length() > 1) {
                break;
            }
            // Check if a string has been started but a newline has been reached before the second ", in which case
            // remove the trailing newline character
            if (readerState == ReaderStates.IN_STRING && character.compareTo("\n") == 0) {
                bufferCandidate = bufferCandidate.substring(0, bufferCandidate.length() - 1);
                break;
            }
        }

        // Extra check for eof to prevent infinite loops if there is whitespace at the end of the file
        if ((!eof() || readerBuffer.length() > 0) && bufferCandidate.compareTo("") == 0) {
            readFileIntoBufferUntilWhitespace();
        } else {
            buffer = bufferCandidate;
        }
    }

    /**
     * This context matcher allows us to handle cases where there is no whitespace between a sample but there is
     * no syntactical way that it could be a single token
     * It follows some simple rules:
     * - Any sample that starts with a character is assumed to be an identifier all the way until a punctuation mark is reached
     * - Any sample that begins with a number ends at the next character or punctuation mark unless it is a decimal point followed by more numbers
     * - Any sample that begins with punctuation ends either at the next character, number, or when it no longer matches a valid operator
     *
     * @return A candidate token string to be passed to the Token constructor from the buffer
     * @brief Scans the buffer and finds the next valid token string by following some basic syntactical rules
     */
    private String getTokenStringFromBuffer() {


        String tokenStringCandidate = "";
        String character;

        // This could either be a full string or just a phrase that starts with a " . In either case, return it as a
        // token candidate as regardless of whether the string ends or not, strings cannot be multiline so if it was
        // not terminated correctly it should become an undefined token
        if (buffer.startsWith("\"")) {
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
                    if (validPunctuation.contains(character)) {
                        tokenStringFound = true;
                    } else {
                        // Check for undefined tokens. Punctuation has already been checked so just need to match
                        // numbers and letters
                        Pattern pattern = Pattern.compile("[a-z0-9]", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(character);
                        boolean matchFound = matcher.find();
                        if (!matchFound) tokenStringFound = true;
                    }

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
                        } else {
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
            // If a number was followed by a decimal but then didn't have a number after that, it should treat it
            // instead as an integer token followed by a dot token. To achieve this, step back an extra character to
            // leave the dot in the buffer
            i--;
        }

        tokenStringCandidate = buffer.substring(0, i);
        buffer = buffer.substring(i);
        currentColumn += i;
        return tokenStringCandidate;
    }

    public Token getToken() {
        if (buffer.length() == 0) {
            readFileIntoBufferUntilWhitespace();
        }
        if (buffer.length() == 0 && eof()) {
            outputController.flushListing();
            return new Token(true);
        }

        Token token = new Token(outputController, symbolTable, getTokenStringFromBuffer(), currentRow, currentColumn);
        if (token.isUndf())
            outputController.addError(currentRow, currentColumn, ErrorMessage.Errors.UNDEFINED_TOKEN, token.getTokenLiteral());

        return token;
    }
}
