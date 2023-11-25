package io.github.c5h12o5.isbn;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents the structure of an ISBN, comprising the following elements:
 * <br>
 * [GS1 prefix]-[Registration Group element]-[Registrant element]-[Publication element]-[Check-digit]
 *
 * @author c5h12o5
 * @since 1.0.0
 */
public class ISBNObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String EMPTY = "";
    private static final String GS1_PREFIX_978 = "978";

    /**
     * A three-digit number that is made available by GS1, formerly EAN International.
     */
    private final String gs1Prefix;

    /**
     * The country, geographical region or language area participating in the ISBN system.
     */
    private final String registrationGroup;

    /**
     * A particular publisher or imprint within a registration group.
     */
    private final String registrant;

    /**
     * A specific edition of a publication by a specific publisher.
     */
    private final String publication;

    /**
     * The ISBN-13 check digit.
     */
    private final Character isbn13CheckDigit;

    /**
     * The ISBN-10 check digit, it will be {@code null} if the ISBN has a GS1 prefix other than 978.
     */
    private final Character isbn10CheckDigit;

    /**
     * The constructor of ISBN object.
     *
     * @param gs1Prefix         gs1 prefix
     * @param registrationGroup registration group
     * @param registrant        registrant
     * @param publication       publication
     */
    public ISBNObject(String gs1Prefix, String registrationGroup, String registrant, String publication) {
        this.gs1Prefix = gs1Prefix;
        this.registrationGroup = registrationGroup;
        this.registrant = registrant;
        this.publication = publication;
        this.isbn13CheckDigit = calculateISBN13CheckDigit();
        this.isbn10CheckDigit = calculateISBN10CheckDigit();
    }

    public String getGS1Prefix() {
        return gs1Prefix;
    }

    public String getRegistrationGroup() {
        return registrationGroup;
    }

    public String getRegistrant() {
        return registrant;
    }

    public String getPublication() {
        return publication;
    }

    public Character getISBN13CheckDigit() {
        return isbn13CheckDigit;
    }

    public Character getISBN10CheckDigit() {
        return isbn10CheckDigit;
    }

    /**
     * Convert the ISBN object to ISBN-13 format.
     *
     * @return the ISBN-13 string
     */
    public String toISBN13() {
        return toISBN13(null);
    }

    /**
     * Convert the ISBN object to ISBN-13 format.
     *
     * @param separator the separator between each element, if {@code null} then no separator will be added
     * @return the ISBN-13 string
     */
    public String toISBN13(String separator) {
        if (separator == null) {
            separator = EMPTY;
        }
        return gs1Prefix + separator
            + registrationGroup + separator
            + registrant + separator
            + publication + separator
            + isbn13CheckDigit;
    }

    /**
     * Convert the ISBN object to ISBN-10 format.
     *
     * @return the ISBN-10 string, or {@code null} if the ISBN has a GS1 prefix other than 978
     */
    public String toISBN10() {
        return toISBN10(null);
    }

    /**
     * Convert the ISBN object to ISBN-10 format.
     *
     * @param separator the separator between each element, if {@code null} then no separator will be added
     * @return the ISBN-10 string, or {@code null} if the ISBN has a GS1 prefix other than 978
     */
    public String toISBN10(String separator) {
        if (isbn10CheckDigit == null) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }
        return registrationGroup + separator
            + registrant + separator
            + publication + separator
            + isbn10CheckDigit;
    }

    /**
     * Calculate the ISBN-13 check digit using algorithm from
     * <a href="https://en.wikipedia.org/wiki/ISBN#ISBN-13_check_digit_calculation">Wikipedia</a>.
     *
     * @return the ISBN-13 check digit
     */
    private Character calculateISBN13CheckDigit() {
        String isbn12 = gs1Prefix + registrationGroup + registrant + publication;
        int sum = 0;
        for (int i = 0; i < isbn12.length(); i++) {
            int digit = isbn12.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = 10 - sum % 10;
        return (checkDigit == 10) ? '0' : (char) (checkDigit + '0');
    }

    /**
     * Calculate the ISBN-10 check digit using algorithm from
     * <a href="https://en.wikipedia.org/wiki/ISBN#ISBN-10_check_digit_calculation">Wikipedia</a>.
     *
     * @return the ISBN-10 check digit, or {@code null} if the ISBN has a GS1 prefix other than 978
     */
    private Character calculateISBN10CheckDigit() {
        if (!GS1_PREFIX_978.equals(gs1Prefix)) {
            return null;
        }
        String isbn9 = registrationGroup + registrant + publication;
        int sum = 0;
        for (int i = 0; i < isbn9.length(); i++) {
            int digit = isbn9.charAt(i) - '0';
            sum += digit * (10 - i);
        }
        int checkDigit = (11 - sum % 11) % 11;
        return (checkDigit == 10) ? 'X' : (char) (checkDigit + '0');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ISBNObject that = (ISBNObject) o;

        if (!Objects.equals(gs1Prefix, that.gs1Prefix)) {
            return false;
        }
        if (!Objects.equals(registrationGroup, that.registrationGroup)) {
            return false;
        }
        if (!Objects.equals(registrant, that.registrant)) {
            return false;
        }
        return Objects.equals(publication, that.publication);
    }

    @Override
    public int hashCode() {
        int result = gs1Prefix != null ? gs1Prefix.hashCode() : 0;
        result = 31 * result + (registrationGroup != null ? registrationGroup.hashCode() : 0);
        result = 31 * result + (registrant != null ? registrant.hashCode() : 0);
        result = 31 * result + (publication != null ? publication.hashCode() : 0);
        return result;
    }
}
