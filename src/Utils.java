/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    01/08/2022
 ****    This class contains some common functions and lists that are used throughout
 ****    the compiling process
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;

public class Utils {

    private static Utils self;
    private static final ArrayList<String> validPunctuation = new ArrayList<String>(Arrays.asList(",", "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", "!", "\"", ":", ";", "."));
    private static final ArrayList<String> validStandaloneOperators = new ArrayList<String>(Arrays.asList(",", "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", ":", ";", "."));
    private static final ArrayList<String> letters = new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));
    private static final ArrayList<String> numbers = new ArrayList<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
    private static final ArrayList<String> validDoubleOperators = new ArrayList<String>(Arrays.asList("!=", "==", "<=", ">=", "+=", "-=", "/=", "*="));
    private static final ArrayList<String> keywords = new ArrayList<String>(Arrays.asList("CD22", "constants", "types", "def", "arrays", "main", "begin", "end", "array", "of", "func", "void", "const", "int", "float", "bool", "for", "repeat", "until", "if", "else", "elif", "input", "print", "printline", "return", "not", "and", "or", "xor", "true", "false"));

    enum MatchTypes {
        LETTER,
        NUMBER,
        PUNCTUATION,
        STANDALONE_OPERATOR,
        DOUBLE_OPERATOR,
        KEYWORD,
        IDENTIFIER,
        UNDEFINED
    }
    /**
     * Singleton style constructor for utils to prevent it being declared multiple times unnecessarily
     */
    private Utils() {
        for (int i = 0; i < 26; i++) {
            letters.add(letters.get(i).toUpperCase());
        }
    }

    public static Utils getUtils() {
        if (self == null) {
            self = new Utils();
        }
        return self;
    }

    public ArrayList<String> getValidDoubleOperators() {
        return validDoubleOperators;
    }

    public ArrayList<String> getValidStandaloneOperators() {
        return validStandaloneOperators;
    }

    public static ArrayList<String> getKeywords() {
        return keywords;
    }

    public boolean matches(String candidate, MatchTypes matcher) {
        boolean matchFound = false;

        switch (matcher) {
            case LETTER -> matchFound = letters.contains(candidate);
            case NUMBER -> matchFound = numbers.contains(candidate);
            case KEYWORD -> matchFound = keywords.contains(candidate);
            case PUNCTUATION -> matchFound = validPunctuation.contains(candidate);
            case DOUBLE_OPERATOR -> matchFound = validDoubleOperators.contains(candidate);
            case STANDALONE_OPERATOR -> matchFound = validStandaloneOperators.contains(candidate);
            case IDENTIFIER -> matchFound = matchesIdentifier(candidate);
            case UNDEFINED -> matchFound = !(matches(candidate, MatchTypes.LETTER) || matches(candidate, MatchTypes.NUMBER) || matches(candidate, MatchTypes.PUNCTUATION));
        }

        return matchFound;
    }

    public boolean matches(String candidate, MatchTypes matcher1, MatchTypes matcher2) {
        return matches(candidate, matcher1) || matches(candidate, matcher2);
    }

    public boolean matches(String candidate, MatchTypes matcher1, MatchTypes matcher2, MatchTypes matcher3) {
        return matches(candidate, matcher1, matcher2) || matches(candidate, matcher3);
    }

    /**
     * Matches strings that start with a letter and only contain strings and letters
     * @param candidate a candidate string to be determined whether it matches the form of an identifier
     * @return boolean value representing whether it matches
     */
    public boolean matchesIdentifier(String candidate) {
        if (!letters.contains(String.valueOf(candidate.charAt(0)))) return false;
        for (String c : candidate.split("")) {
            if (!letters.contains(c) && !numbers.contains(c)) return false;
        }
        return true;
    }
}
