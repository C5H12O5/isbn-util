package io.github.c5h12o5.isbn;

import io.github.c5h12o5.isbn.range.RangeCache;
import io.github.c5h12o5.isbn.range.RangeHandler;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the main class of the ISBN utility library. It provides methods to parse, format and validate ISBNs.
 *
 * @author c5h12o5
 * @since 1.0.0
 */
public final class ISBN {

    public static final char CHAR_0 = '0';
    public static final char CHAR_9 = '9';
    public static final char CHAR_X = 'X';
    public static final int ISBN_13_LENGTH = 13;
    public static final int ISBN_10_LENGTH = 10;
    public static final int SEPARATED_ISBN_13_LENGTH = ISBN_13_LENGTH + 4;
    public static final int SEPARATED_ISBN_10_LENGTH = ISBN_10_LENGTH + 3;
    public static final String GS1_PREFIX_978 = "978";
    public static final String HYPHEN_SEPARATOR = "-";
    public static final String SPACE_SEPARATOR = " ";

    private static final SAXParser SAX_PARSER;
    private static RangeCache rangeCache;

    static {
        try (InputStream is = ISBN.class.getResourceAsStream("/RangeMessage.xml")) {
            // create a SAX parser that does not validate against external DTDs or external schemas
            SAX_PARSER = SAXParserFactory.newInstance().newSAXParser();
            SAX_PARSER.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            SAX_PARSER.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            // initialize the range cache with the default range message file
            RangeHandler rangeHandler = new RangeHandler();
            SAX_PARSER.parse(is, rangeHandler);
            rangeCache = rangeHandler.getResult();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ISBNException(e.getMessage(), e);
        }
    }

    /**
     * Load external range message file from the given inputStream.
     *
     * @param is the inputStream containing the content to be parsed
     * @return the range cache
     */
    public static synchronized RangeCache loadRangeMessageFile(InputStream is) {
        RangeHandler rangeHandler = new RangeHandler();
        try {
            SAX_PARSER.parse(is, rangeHandler);
        } catch (SAXException | IOException e) {
            throw new ISBNException(e.getMessage(), e);
        }
        rangeCache = rangeHandler.getResult();
        return rangeCache;
    }

    /**
     * Get the range message date.
     *
     * @return the message date of current loaded range message file
     */
    public static LocalDateTime rangeMessageDate() {
        return null != rangeCache ? rangeCache.getMessageDate() : null;
    }

    /**
     * Parse the given ISBN code into an {@link ISBNObject}.
     *
     * @param code the ISBN code to parse
     * @return the parsed {@link ISBNObject}, {@code null} if the input is not a valid ISBN code
     */
    public static ISBNObject parse(String code) {
        String compacted = compact(code);
        if (compacted == null) {
            return null;
        }

        // construct the first 12-digits of ISBN from the compacted code
        String isbn12 = null;
        switch (compacted.length()) {
            case ISBN_13_LENGTH:
                isbn12 = compacted.substring(0, ISBN_13_LENGTH - 1);
                break;
            case ISBN_13_LENGTH - 1:
                isbn12 = compacted;
                break;
            case ISBN_10_LENGTH:
                isbn12 = GS1_PREFIX_978 + compacted.substring(0, ISBN_10_LENGTH - 1);
                break;
            case ISBN_10_LENGTH - 1:
                isbn12 = GS1_PREFIX_978 + compacted;
                break;
            default:
                break;
        }

        // check if the first 12-digits of ISBN is a valid digit sequence
        boolean invalid12 = (isbn12 == null) || isbn12.chars().anyMatch(ch -> (ch < CHAR_0 || ch > CHAR_9));
        if (invalid12) {
            return null;
        }

        // extract the GS1 prefix, registration group, registrant and publication from the first 12-digits of ISBN
        String gs1Prefix = isbn12.substring(0, 3);
        String registrationGroup = rangeCache.findRegistrationGroup(gs1Prefix, isbn12);
        if (registrationGroup == null) {
            return null;
        }
        String registrant = rangeCache.findRegistrant(gs1Prefix + registrationGroup, isbn12);
        if (registrant == null) {
            return null;
        }
        String publication = isbn12.substring((gs1Prefix + registrationGroup + registrant).length());
        return new ISBNObject(gs1Prefix, registrationGroup, registrant, publication);
    }

    /**
     * Remove all non-digit characters from the given ISBN input, except for the last character which can be 'X'.
     *
     * <pre>
     * ISBN.compact(null)                = null
     * ISBN.compact("7-03-014726-X")     = "703014726X"
     * ISBN.compact("978-7-03-038722-6") = "9787030387226"
     * ISBN.compact("978 7 03 038722 6") = "9787030387226"
     * </pre>
     *
     * @param isbn the ISBN to compact
     * @return the compacted ISBN, {@code null} if null String input
     */
    public static String compact(String isbn) {
        if (isbn == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean findCheckDigit = false;
        char[] charArray = isbn.toCharArray();
        for (int i = charArray.length - 1; i >= 0; i--) {
            char ch = charArray[i];
            boolean isDigit = (ch >= CHAR_0 && ch <= CHAR_9);
            if (!findCheckDigit) {
                // find the last digit or 'X' character
                boolean isCharX = (Character.toUpperCase(ch) == CHAR_X);
                if (isDigit || isCharX) {
                    sb.append(ch);
                    findCheckDigit = true;
                }
            } else {
                // find the rest digit characters
                if (isDigit) {
                    sb.append(ch);
                }
            }
        }
        return sb.reverse().toString();
    }

    /**
     * Check if the given ISBN input is not a valid ISBN-13 or ISBN-10 code.
     *
     * <pre>
     * ISBN.isNotValid(null)                = true
     * ISBN.isNotValid("703014726X")        = false
     * ISBN.isNotValid("7-03-014726-X")     = false
     * ISBN.isNotValid("9787030387226")     = false
     * ISBN.isNotValid("978-7-03-038722-6") = false
     * </pre>
     *
     * @param isbn the ISBN to validate
     * @return {@code true} if not a valid ISBN-10 or ISBN-13 code, otherwise {@code false}
     */
    public static boolean isNotValid(String isbn) {
        return !isValid(isbn);
    }

    /**
     * Check if the given ISBN input is either a valid ISBN-13 or ISBN-10 code.
     *
     * <pre>
     * ISBN.isValid(null)                = false
     * ISBN.isValid("703014726X")        = true
     * ISBN.isValid("7-03-014726-X")     = true
     * ISBN.isValid("9787030387226")     = true
     * ISBN.isValid("978-7-03-038722-6") = true
     * </pre>
     *
     * @param isbn the ISBN to validate
     * @return {@code true} if a valid ISBN-10 or ISBN-13 code, otherwise {@code false}
     */
    public static boolean isValid(String isbn) {
        return isValidISBN13(isbn) || isValidISBN10(isbn);
    }

    /**
     * Check if the given ISBN input is a valid ISBN-13 code.
     *
     * <pre>
     * ISBN.isValidISBN13(null)                = false
     * ISBN.isValidISBN13("703014726X")        = false
     * ISBN.isValidISBN13("7-03-014726-X")     = false
     * ISBN.isValidISBN13("9787030387226")     = true
     * ISBN.isValidISBN13("978-7-03-038722-6") = true
     * </pre>
     *
     * @param isbn the ISBN to validate
     * @return {@code true} if a valid ISBN-13 code, otherwise {@code false}
     */
    public static boolean isValidISBN13(String isbn) {
        return isValidISBN13Length(isbn) && isbn.equals(formatISBN13(isbn, extractSeparator(isbn)));
    }

    /**
     * Check if the given ISBN input is a valid ISBN-10 code.
     *
     * <pre>
     * ISBN.isValidISBN10(null)                = false
     * ISBN.isValidISBN10("703014726X")        = true
     * ISBN.isValidISBN10("7-03-014726-X")     = true
     * ISBN.isValidISBN10("9787030387226")     = false
     * ISBN.isValidISBN10("978-7-03-038722-6") = false
     * </pre>
     *
     * @param isbn the ISBN to validate
     * @return {@code true} if a valid ISBN-10 code, otherwise {@code false}
     */
    public static boolean isValidISBN10(String isbn) {
        return isValidISBN10Length(isbn) && isbn.equals(formatISBN10(isbn, extractSeparator(isbn)));
    }

    /**
     * Check if the two given ISBN codes are equal, return {@code false} if either one is not valid.
     *
     * <pre>
     * ISBN.equals(null, null)                           = false
     * ISBN.equals("703014726X", null)                   = false
     * ISBN.equals("703014726X", "7-03-014726-X")        = true
     * ISBN.equals("703014726X", "9787030147264")        = true
     * ISBN.equals("9787030387226", "978-7-03-038722-6") = true
     * </pre>
     *
     * @param isbnA the first ISBN code
     * @param isbnB the second ISBN code
     * @return {@code true} if the two ISBN codes are equal, otherwise {@code false}
     */
    public static boolean equals(String isbnA, String isbnB) {
        if (!isValid(isbnA) || !isValid(isbnB)) {
            return false;
        }
        String isbnA13 = formatISBN13(isbnA);
        return null != isbnA13 && isbnA13.equals(formatISBN13(isbnB));
    }

    /**
     * Format the given ISBN input to ISBN-13 format.
     *
     * @param isbn the ISBN to format
     * @return the formatted ISBN-13 string, {@code null} if the input is not a valid ISBN code
     */
    public static String formatISBN13(String isbn) {
        return formatISBN13(isbn, null);
    }

    /**
     * Format the given ISBN input to ISBN-13 format.
     *
     * @param isbn      the ISBN to format
     * @param separator the separator between each element, if {@code null} then no separator will be added
     * @return the formatted ISBN-13 string, {@code null} if the input is not a valid ISBN code
     */
    public static String formatISBN13(String isbn, String separator) {
        ISBNObject isbnObject = parse(isbn);
        return null != isbnObject ? isbnObject.toISBN13(separator) : null;
    }

    /**
     * Format the given ISBN input to ISBN-10 format.
     *
     * @param isbn the ISBN to format
     * @return the formatted ISBN-10 string, {@code null} if the input is not a valid ISBN code
     */
    public static String formatISBN10(String isbn) {
        return formatISBN10(isbn, null);
    }

    /**
     * Format the given ISBN input to ISBN-10 format.
     *
     * @param isbn      the ISBN to format
     * @param separator the separator between each element, if {@code null} then no separator will be added
     * @return the formatted ISBN-10 string, {@code null} if the input is not a valid ISBN code
     */
    public static String formatISBN10(String isbn, String separator) {
        ISBNObject isbnObject = parse(isbn);
        return null != isbnObject ? isbnObject.toISBN10(separator) : null;
    }

    /**
     * Check if the length of the given ISBN input is valid.
     *
     * @param isbn the ISBN to validate
     * @return {@code true} if the length is valid, otherwise {@code false}
     */
    private static boolean isValidLength(String isbn) {
        return isValidISBN13Length(isbn) || isValidISBN10Length(isbn);
    }

    /**
     * Check if the length of the given ISBN input is valid for ISBN-13.
     *
     * @param isbn the ISBN to validate
     * @return {@code true} if the length is valid, otherwise {@code false}
     */
    private static boolean isValidISBN13Length(String isbn) {
        if (isbn == null) {
            return false;
        }
        int length = isbn.length();
        return (length == ISBN_13_LENGTH || length == SEPARATED_ISBN_13_LENGTH);
    }

    /**
     * Check if the length of the given ISBN input is valid for ISBN-10.
     *
     * @param isbn the ISBN to validate
     * @return {@code true} if the length is valid, otherwise {@code false}
     */
    private static boolean isValidISBN10Length(String isbn) {
        if (isbn == null) {
            return false;
        }
        int length = isbn.length();
        return (length == ISBN_10_LENGTH || length == SEPARATED_ISBN_10_LENGTH);
    }

    /**
     * Extract separator from the given ISBN input.
     *
     * @param isbn the ISBN to extract separator
     * @return the separator, {@code null} if extraction failed
     */
    private static String extractSeparator(String isbn) {
        if (isbn == null) {
            return null;
        }
        Set<Character> separators = new HashSet<>();
        for (char ch : isbn.substring(0, isbn.length() - 1).toCharArray()) {
            if (ch < CHAR_0 || ch > CHAR_9) {
                separators.add(ch);
            }
        }
        return separators.size() == 1 ? separators.iterator().next().toString() : null;
    }
}