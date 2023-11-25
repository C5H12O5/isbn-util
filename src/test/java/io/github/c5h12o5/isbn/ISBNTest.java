package io.github.c5h12o5.isbn;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ISBNTest {

    @Test
    public void loadRangeMessageFile() throws IOException {
        // load the test range message file only contains 979 prefix
        try (InputStream is = ISBNTest.class.getResourceAsStream("/TestRangeMessage.xml")) {
            ISBN.loadRangeMessageFile(is);
        }
        assertEquals(LocalDateTime.of(2023, 11, 11, 11, 11, 11), ISBN.rangeMessageDate());
        assertTrue(ISBN.isValid("9798602405453"));
        assertFalse(ISBN.isValid("9787030387226"));

        // load the default range message file
        try (InputStream is = ISBN.class.getResourceAsStream("/RangeMessage.xml")) {
            ISBN.loadRangeMessageFile(is);
        }
        assertTrue(ISBN.isValid("9798602405453"));
        assertTrue(ISBN.isValid("9787030387226"));
    }

    @Test
    public void parse() {
        assertNull(ISBN.parse(null));
        assertNull(ISBN.parse(""));

        assertEquals(new ISBNObject("978", "7", "03", "014726"), ISBN.parse("703014726X"));
        assertEquals(new ISBNObject("978", "7", "03", "014726"), ISBN.parse("《ISBN》（703014726X)"));
        assertNotEquals(new ISBNObject("978", "7", "03", "038722"), ISBN.parse("703014726X"));

        assertEquals(new ISBNObject("978", "7", "03", "038722"), ISBN.parse("978-7-03-038722-6"));
        assertEquals(new ISBNObject("978", "7", "03", "038722"), ISBN.parse(" ISBN = 978-7-03-038722-6 "));
    }

    @Test
    public void compact() {
        assertNull(ISBN.compact(null));
        assertEquals("", ISBN.compact(""));

        assertEquals("9787030387226", ISBN.compact("978-7-03-038722-6"));
        assertEquals("9787030387226", ISBN.compact(" 9a7b8c7d0e3f0g3h8i7j2k2l6 "));

        assertEquals("703014726X", ISBN.compact("7-03-014726-X"));
        assertEquals("703014726x", ISBN.compact("7-03-014726-x"));
        assertEquals("703014726X", ISBN.compact(" 7a 03b 014726c xxX d"));
        assertEquals("703014726x", ISBN.compact(" 7x 03x 014726x x "));
    }

    @Test
    public void isValid() {
        assertFalse(ISBN.isValid(null));

        assertTrue(ISBN.isValid("703014726X"));
        assertTrue(ISBN.isValid("7-03-014726-X"));
        assertTrue(ISBN.isValid("9787030387226"));
        assertTrue(ISBN.isValid("978 7 03 038722 6"));

        assertFalse(ISBN.isValid(" 703014726X"));
        assertFalse(ISBN.isValid(" 7-03-014726-X"));
        assertFalse(ISBN.isValid(" 9787030387226"));
        assertFalse(ISBN.isValid(" 978-7-03-038722-6"));

        assertFalse(ISBN.isValid("7030147260"));
        assertFalse(ISBN.isValid("7-03-014726-0"));
        assertFalse(ISBN.isValid("9787030387220"));
        assertFalse(ISBN.isValid("978-7-03-038722-0"));

        assertFalse(ISBN.isValid("7-0-3014726-X"));
        assertFalse(ISBN.isValid("7-0301-4726-X"));
        assertFalse(ISBN.isValid("978-70-3-038722-6"));
        assertFalse(ISBN.isValid("978-7-0303-8722-6"));
    }

    @Test
    public void isValidISBN13() {
        assertFalse(ISBN.isValidISBN13(null));

        assertFalse(ISBN.isValidISBN13("703014726X"));
        assertFalse(ISBN.isValidISBN13("7-03-014726-X"));

        assertTrue(ISBN.isValidISBN13("9787030387226"));
        assertTrue(ISBN.isValidISBN13("978-7-03-038722-6"));
    }

    @Test
    public void isValidISBN10() {
        assertFalse(ISBN.isValidISBN10(null));

        assertTrue(ISBN.isValidISBN10("703014726X"));
        assertTrue(ISBN.isValidISBN10("7-03-014726-X"));

        assertFalse(ISBN.isValidISBN10("9787030387226"));
        assertFalse(ISBN.isValidISBN10("978-7-03-038722-6"));
    }

    @Test
    public void testEquals() {
        assertFalse(ISBN.equals(null, null));
        assertFalse(ISBN.equals("703014726X", null));
        assertFalse(ISBN.equals("7030147261", "7030147261"));
        assertFalse(ISBN.equals("7030147261", "7030147262"));
        assertFalse(ISBN.equals("703014726X", "978703014726X"));

        assertTrue(ISBN.equals("703014726X", "703014726X"));
        assertTrue(ISBN.equals("703014726X", "978-7-03-014726-4"));
        assertTrue(ISBN.equals("7-03-014726-X", "9787030147264"));
        assertTrue(ISBN.equals("7030387228", "9787030387226"));
        assertTrue(ISBN.equals("9787030387226", "978-7-03-038722-6"));
    }

    @Test
    public void formatISBN13() {
        assertEquals("9787030147264", ISBN.formatISBN13("7-03-014726-X"));
        assertEquals("9787030387226", ISBN.formatISBN13(" 9787030387226 "));
        assertEquals("979-8-6024-0545-3", ISBN.formatISBN13("9798602405453", "-"));
        assertEquals("979 12 200 0852 5", ISBN.formatISBN13("9791220008525", " "));
        assertEquals("979  12  200  0852  5", ISBN.formatISBN13("9791220008525", "  "));

        assertNull(ISBN.formatISBN13(null));
        assertNull(ISBN.formatISBN13(null, null));
        assertNull(ISBN.formatISBN13("   ", "-"));
    }

    @Test
    public void formatISBN10() {
        assertEquals("7030387228", ISBN.formatISBN10("9787030387226"));
        assertEquals("703014726X", ISBN.formatISBN10(" 7-03-014726-X "));
        assertEquals("7-03-014726-X", ISBN.formatISBN10("7-03-014-72-6X", "-"));
        assertEquals("7_03_014726_X", ISBN.formatISBN10("  7a0301aa4726X--", "_"));

        assertNull(ISBN.formatISBN10("9798602405453"));
        assertNull(ISBN.formatISBN10("9798602405453", "-"));
    }
}