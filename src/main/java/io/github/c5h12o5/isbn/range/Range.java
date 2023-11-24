package io.github.c5h12o5.isbn.range;

import io.github.c5h12o5.isbn.ISBNException;

import java.io.Serializable;

/**
 * This class represents a range of 7-digit numbers, which are used to identify the length of an ISBN element.
 *
 * @author c5h12o5
 * @since 1.0.0
 */
public class Range implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String HYPHEN = "-";
    private static final int RANGE_ARRAY_LENGTH = 2;
    public static final int RANGE_STR_LENGTH = 7;

    /** The start number of the range. */
    private int start;

    /** The end number of the range. */
    private int end;

    /** The length of the element in the range. */
    private int length;

    /**
     * Set the start and end number of the range.
     *
     * @param range the range in the format 'start-end'
     */
    public void setRange(String range) {
        String[] startEnd = (null != range ? range.split(HYPHEN) : null);
        if (null == startEnd || startEnd.length != RANGE_ARRAY_LENGTH) {
            throw new ISBNException("Range must be in the format 'start-end'");
        }
        this.start = parseInt(startEnd[0]);
        this.end = parseInt(startEnd[1]);
    }

    /**
     * Set the length of the element in the range.
     *
     * @param length the element length
     */
    public void setLength(String length) {
        this.length = parseInt(length);
    }

    /**
     * Get the length of the element in the range.
     *
     * @return the element length
     */
    public int getLength() {
        return length;
    }

    /**
     * Check if the given number is in the range.
     *
     * @param number the number to check
     * @return {@code true} if the number is in the range, otherwise {@code false}
     */
    public boolean contains(String number) {
        int value = parseInt(number);
        return value >= start && value <= end;
    }

    /**
     * Parse the given string argument to an integer value.
     *
     * @param str the string argument to parse
     * @return the parsed integer value
     */
    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new ISBNException("Invalid number: " + str);
        }
    }
}
