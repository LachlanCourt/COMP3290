/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class is a Scanner for the CD22 programming language
 *******************************************************************************/
package Scanner;

import Common.ErrorMessage.Errors;
import Common.Utils.MatchTypes;
import Scanner.Token.Tokens;

import java.io.File;
import java.io.FileNotFoundException;

public class Scanner {

    private enum ContextStates {
        UNDEFINED, LETTER, NUMBER, PUNCTUATION,
    }

    private enum DecimalStates {
        INITIAL, FOUND_DECIMAL, FOUND_FOLLOWING_NUMBER
    }

    private enum ReaderStates {
        CODE, IN_STRING, IN_SINGLE_LINE_COMMENT, IN_MULTI_LINE_COMMENT, IN_UNDEFINED_TOKEN,
    }

    private java.util.Scanner fileScanner;
    private int scannerRowPosition;
    private int currentRow;
    private int scannerColumnPosition;
    private int currentColumn;

    private String buffer;

    // When scanning an undefined token, we keep consuming until we see a valid character. The issue here is that this
    // valid character has now been read from the file, but we want to return the undefined token and not have the
    // valid character included. So we save it in this buffer and return the undefined token string, then next time
    // the file reader is called, clear this buffer first before reading from the file again.
    private String fileReaderUndefinedTokenBuffer;
    private final Common.OutputController outputController;
    private final Common.SymbolTable symbolTable;
    private final Common.Utils utils;


    public Scanner(Common.OutputController outputController_, Common.SymbolTable symbolTable_) {
        scannerColumnPosition = 0;
        scannerRowPosition = 1;
        buffer = "";
        fileReaderUndefinedTokenBuffer = "";
        outputController = outputController_;
        symbolTable = symbolTable_;
        utils = Common.Utils.getUtils();
    }

    public void init(String filename) {
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

        // Start by assuming we are reading valid code
        ReaderStates readerState = ReaderStates.CODE;

        // The current row and current column are the starting row and column of the buffer we are scanning. The line
        // number and column number may change if a multiline comment begins immediately after the token and no
        // whitespace, as these will be consumed and flushed before this function returns which will increment the line
        // number and column number. Because of this, the line number cannot reliably identify the line of a token
        // when read from the buffer so save it before we start scanning
        currentRow = scannerRowPosition;
        currentColumn = scannerColumnPosition;

        // Loop until we have reached the end of file, ensuring to check that the file reader buffer is empty
        while (!eof() || fileReaderUndefinedTokenBuffer.length() > 0) {
            // If the file reader buffer is empty, read a character from the file and immediately save it to the listing
            // If the buffer is not empty, read out of there first.
            if (fileReaderUndefinedTokenBuffer.length() == 0) {
                character = fileScanner.next();
                outputController.addListingCharacter(character);
            } else {
                character = fileReaderUndefinedTokenBuffer;
                fileReaderUndefinedTokenBuffer = "";
                // For the character to be in the reader buffer, it has already been parsed and the counter incremented
                currentColumn--;
            }

            // Ignore carriage returns
            if (character.charAt(0) == 13) continue;

            // Keep track of the rows and columns from the perspective of the scanner. Note that the counters are separate
            // from the currentRow and currentColumn variables, which keep track of the location of the last token.
            // Comments can cause these values to become out of sync momentarily
            scannerColumnPosition++;
            if (character.compareTo("\n") == 0) {
                scannerRowPosition++;
                scannerColumnPosition = 0;
            }

            // If a " symbol has been found and we are currently scanning code, we have now entered a string
            if (readerState == ReaderStates.CODE && character.compareTo("\"") == 0)
                readerState = ReaderStates.IN_STRING;

            // Check for invalid characters and break on whitespace
            if (readerState == ReaderStates.CODE || readerState == ReaderStates.IN_UNDEFINED_TOKEN) {
                // Will be true for letters, numbers, newlines, and spaces. False for everything else
//                boolean matchFound = utils.matches(character, MatchTypes.LETTER, MatchTypes.NUMBER) || character.compareTo("\n") == 0 || character.compareTo(" ") == 0 || character.charAt(0) == 9;

                // If the character also does not match punctuation, it must be an invalid character. Enter the undefined
                // token state, and consume until a valid character has been seen
                if (utils.matches(character, MatchTypes.UNDEFINED)) {
                    readerState = ReaderStates.IN_UNDEFINED_TOKEN;
                } else if (readerState == ReaderStates.IN_UNDEFINED_TOKEN && !utils.matches(character, MatchTypes.UNDEFINED)) {
                    // By reaching this point, the entire undefined token has been read into the bufferCandidate
                    // if the character in question is anything other than a newline we should save it in the file
                    // reader buffer to interpret in the next pass as it is a valid character
                    if (character.compareTo("\n") != 0) {
                        fileReaderUndefinedTokenBuffer = character;
                    }
                    break;
                }
                // If the character is a space, a newline or a tab, we have grabbed a sufficient sample for the buffer
                if (utils.matches(character, MatchTypes.WHITESPACE)) {
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
            // does not get considered a string)
            if (bufferCandidate.startsWith("\"") && bufferCandidate.endsWith("\"") && bufferCandidate.length() > 1) {
                break;
            }
            // Check if a string has been started but a newline has been reached before the second ", in which case
            // remove the trailing newline character and return where it will be interpreted as an undefined token
            if (readerState == ReaderStates.IN_STRING && character.compareTo("\n") == 0) {
                bufferCandidate = bufferCandidate.substring(0, bufferCandidate.length() - 1);
                break;
            }
        }

        // Extra check for eof to prevent infinite loops if there is whitespace at the end of the file. Otherwise
        // recurse to consume any repeated spaces or newlines mid-code
        if ((!eof() || fileReaderUndefinedTokenBuffer.length() > 0) && bufferCandidate.length() == 0) {
            readFileIntoBufferUntilWhitespace();
        } else {
            // Save the candidate to the buffer to be read from later
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

        // Assume we are undefined, a state which will allow the context to be overwritten by any valid character
        ContextStates contextState = ContextStates.UNDEFINED;
        // Assume we have not found a token string from the buffer yet
        boolean tokenStringFound = false;
        // Used exclusively for matching floats, to keep track of whether we have seen a decimal point or not
        DecimalStates decimalState = DecimalStates.INITIAL;
        // The current index of the buffer we are observing. The buffer is only observed during this loop, not edited
        // or consumed until the end of the function.
        int index;
        // Loop through the buffer and observe a new character each time
        for (index = 0; index < buffer.length(); index++) {
            character = String.valueOf(buffer.charAt(index));

            // Context switching
            switch (contextState) {
                case UNDEFINED -> {
                    // In the undefined state, any valid character will immediately switch context
                    if (utils.matches(character, MatchTypes.LETTER)) contextState = ContextStates.LETTER;

                    if (utils.matches(character, MatchTypes.NUMBER)) contextState = ContextStates.NUMBER;

                    if (utils.matches(character, MatchTypes.PUNCTUATION)) contextState = ContextStates.PUNCTUATION;

                    // Anything that doesn't match one of these will remain as an undefined token until the end of the
                    // buffer as the readFileIntoBufferUntilWhitespace function has already stopped reading into the
                    // buffer at the next valid character so we can assume the entire buffer is undefined
                }
                case LETTER -> {
                    // A punctuation symbol when reading chars will break a token as it can only be made up of letters
                    // and numbers
                    if (utils.matches(character, MatchTypes.PUNCTUATION)) {
                        tokenStringFound = true;
                    } else {
                        // Check for undefined tokens. Punctuation has already been checked so just need to match
                        // numbers and letters
                        if (!(utils.matches(character, MatchTypes.NUMBER, MatchTypes.LETTER))) tokenStringFound = true;
                    }

                }
                case PUNCTUATION -> {
                    // Punctuation tokens can only be made up exclusively of punctuation
                    if (!utils.matches(character, MatchTypes.PUNCTUATION)) tokenStringFound = true;
                    // If the first two punctuation symbols do not combine to make a double operator, parse the first
                    // symbol as its own token and save the second for the next pass
                    if (index == 1 && !utils.matches(buffer.substring(0, 2), MatchTypes.DOUBLE_OPERATOR)) {
                        tokenStringFound = true;
                    }
                    // If i has successfully gotten to this point we have found a double operator
                    if (index > 1) tokenStringFound = true;
                }
                case NUMBER -> {
                    // Numbers could be floats, so we need to check if the punctuation is a decimal
                    if (utils.matches(character, MatchTypes.PUNCTUATION)) {
                        // If we have not seen a decimal point yet, a decimal is valid. If the punctuation is anything
                        // else or if we have already seen a decimal point, then it is no longer valid
                        if (character.compareTo(".") == 0 && decimalState == DecimalStates.INITIAL) {
                            decimalState = DecimalStates.FOUND_DECIMAL;
                        } else {
                            tokenStringFound = true;
                        }
                    }
                    // Numbers cannot contain letters or undefined tokens so break to a token if such has been found
                    else if (utils.matches(character, MatchTypes.LETTER, MatchTypes.UNDEFINED)) tokenStringFound = true;

                        // If we have previously found a decimal and now we have seen another number, we have found a float
                    else if (decimalState == DecimalStates.FOUND_DECIMAL && utils.matches(character, MatchTypes.NUMBER)) {
                        decimalState = DecimalStates.FOUND_FOLLOWING_NUMBER;
                    }
                }
            }
            if (tokenStringFound) break;
        }

        // If we have reached the end of the buffer and have not found a token string yet, return the entire buffer
        if (!tokenStringFound) {
            tokenStringCandidate = buffer;
            buffer = "";
            return tokenStringCandidate;
        }

        // If a number was followed by a decimal but then didn't have a number after that, it should treat it
        // instead as an integer token followed by a dot token. To achieve this, step back an extra character to
        // leave the dot in the buffer
        if (decimalState == DecimalStates.FOUND_DECIMAL) {
            index--;
        }

        // Remove the found token candidate from the buffer, leaving any remaining characters in the buffer
        tokenStringCandidate = buffer.substring(0, index);
        buffer = buffer.substring(index);
        // Update the current column to match where the start of the buffer is now
        currentColumn += index;
        return tokenStringCandidate;
    }

    private Tokens getTokenTypeFromTokenLiteral(String tokenLiteral) {
        if (utils.matches(tokenLiteral.toLowerCase(), MatchTypes.KEYWORD, MatchTypes.STANDALONE_OPERATOR, MatchTypes.DOUBLE_OPERATOR)) {
            if (tokenLiteral.toLowerCase().compareTo("cd22") == 0 && tokenLiteral.compareTo("CD22") != 0)
                outputController.addWarning(currentRow, currentColumn, Errors.WARNING_CD22_SEMANTIC_CASING);
            return Tokens.getToken(tokenLiteral.toLowerCase());
        }

        if (tokenLiteral.startsWith("\"") && tokenLiteral.endsWith("\"") && tokenLiteral.length() > 1) {
            return Tokens.TSTRG;
        }

        if (utils.matches(String.valueOf(tokenLiteral.charAt(0)), MatchTypes.NUMBER)) {
            // Float literal
            if (tokenLiteral.contains(".")) {
                return Tokens.TFLIT;
            }
            // Integer literal
            return Tokens.TILIT;
        }

        // Indentifier
        if (utils.matches(tokenLiteral, MatchTypes.IDENTIFIER)) {
            return Tokens.TIDEN;
        }
        return Tokens.TUNDF;
    }

    public Token getToken() {
        // If the buffer is empty, read from the file a sample to interpret
        if (buffer.length() == 0) {
            readFileIntoBufferUntilWhitespace();
        }
        // If the buffer is empty and we the scanner is at the end of the file, there is no more code to scan so return
        // an end of file token. If the buffer still has something in it, we need to still interpret it even if the
        // scanner is at the end of file
        if (buffer.length() == 0 && eof()) {
            outputController.flushListing();
            fileScanner.close();
            return new Token(true);
        }

        // Create a new token by reading a candidate token string out of the buffer
        String tokenLiteral = getTokenStringFromBuffer();
        Tokens tokenType = getTokenTypeFromTokenLiteral(tokenLiteral);
        Integer symbolTableId = null;
        switch (tokenType) {
            case TILIT, TFLIT, TSTRG, TIDEN -> symbolTableId = symbolTable.addSymbol(tokenLiteral, null);
        }
        Token token = new Token(tokenType, tokenLiteral, symbolTableId, currentRow, currentColumn);

        // If the token is undefined we add an error to the error handler
        if (token.isUndf())
            outputController.addError(currentRow, currentColumn, Errors.UNDEFINED_TOKEN, token.getTokenLiteral());

        return token;
    }
}
