package io.github.c5h12o5.isbn.range;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to store and manage ranges of ISBN registration groups and registrants.
 *
 * @author c5h12o5
 * @since 1.0.0
 */
public class RangeCache implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String EMPTY = "";
    private static final String HYPHEN = "-";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    /** The message date is the date the range file was last updated. */
    private LocalDateTime messageDate;

    /** The registration group ranges are the ranges of numbers assigned to each registration group. */
    private final Map<String, List<Range>> registrationGroupRanges = new HashMap<>();

    /** The registrant ranges are the ranges of numbers assigned to each registrant. */
    private final Map<String, List<Range>> registrantRanges = new HashMap<>();

    /**
     * Get the range message date.
     *
     * @return the message date from the range file
     */
    public LocalDateTime getMessageDate() {
        return messageDate;
    }

    /**
     * Set the range message date.
     *
     * @param messageDate the message date from the range file
     */
    public void setMessageDate(String messageDate) {
        try {
            this.messageDate = LocalDateTime.parse(messageDate, DTF);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Add a registration group range.
     *
     * @param prefix the prefix before the registration group element
     * @param range  the range of numbers assigned to the registration group
     */
    public void addRegistrationGroupRange(String prefix, Range range) {
        addRange(prefix, range, registrationGroupRanges);
    }

    /**
     * Add a registrant range.
     *
     * @param prefix the prefix before the registrant element
     * @param range  the range of numbers assigned to the registrant
     */
    public void addRegistrantRange(String prefix, Range range) {
        addRange(prefix, range, registrantRanges);
    }

    /**
     * Add a range to the specified ranges map.
     *
     * @param prefix the prefix before the specified element
     * @param range  the range of numbers assigned to the specified element
     * @param ranges the specified ranges map to add the range
     */
    private void addRange(String prefix, Range range, Map<String, List<Range>> ranges) {
        if (null == range || range.getLength() <= 0 || range.getLength() > Range.RANGE_STR_LENGTH) {
            return;
        }
        ranges.computeIfAbsent(prefix.replace(HYPHEN, EMPTY), k -> new ArrayList<>()).add(range);
    }

    /**
     * Find the registration group element.
     *
     * @param prefix the prefix before the registration group element
     * @param isbn   the ISBN to extract the registration group element
     * @return the registration group element
     */
    public String findRegistrationGroup(String prefix, String isbn) {
        return findElement(prefix, isbn, registrationGroupRanges);
    }

    /**
     * Find the registrant element.
     *
     * @param prefix the prefix before the registrant element
     * @param isbn   the ISBN to extract the registrant element
     * @return the registrant element
     */
    public String findRegistrant(String prefix, String isbn) {
        return findElement(prefix, isbn, registrantRanges);
    }

    /**
     * Find an ISBN element in the specified ranges map.
     *
     * @param prefix the prefix before the specified element
     * @param isbn   the ISBN to extract the specified element
     * @param ranges the specified ranges map to search
     * @return the specified element
     */
    private String findElement(String prefix, String isbn, Map<String, List<Range>> ranges) {
        if (prefix == null || isbn == null) {
            return null;
        }
        List<Range> rangeList = ranges.get(prefix);
        if (rangeList == null) {
            return null;
        }

        // extract the 7-digit number after the prefix from the given ISBN
        int prefixLength = prefix.length();
        String number = (isbn + Range.ZERO_RANGE_STR).substring(prefixLength, prefixLength + Range.RANGE_STR_LENGTH);
        Optional<Range> range = rangeList.stream().filter(r -> r.contains(number)).findFirst();

        // extract the element from the 7-digit number by the matched length
        return range.map(r -> number.substring(0, r.getLength())).orElse(null);
    }
}
