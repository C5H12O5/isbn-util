package com.github.c5h12o5.isbn.range;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Optional;

/**
 * This handler is used to parse the XML file containing the ISBN ranges.
 *
 * @author c5h12o5
 * @since 1.0.0
 */
public class RangeHandler extends DefaultHandler {

    /** The result of the parsing */
    private RangeCache result;

    /**
     * This enum represents the different stages of parsing the XML file.
     */
    private enum ParsingStage {
        /** The stage of parsing the registration group ranges */
        REGISTRATION_GROUP("EAN.UCCPrefixes"),
        /** The stage of parsing the registrant ranges */
        REGISTRANT("RegistrationGroups");

        /** The trigger element of the parsing stage */
        private final String trigger;

        ParsingStage(String trigger) {
            this.trigger = trigger;
        }

        /**
         * Returns the parsing stage matching the trigger element.
         *
         * @param trigger the trigger element of the parsing stage
         * @return the parsing stage matching the trigger element
         */
        public static ParsingStage match(String trigger) {
            for (ParsingStage stage : values()) {
                if (stage.trigger.equals(trigger)) {
                    return stage;
                }
            }
            return null;
        }
    }

    private static final String RULE = "Rule";
    private static final String RANGE = "Range";
    private static final String LENGTH = "Length";
    private static final String PREFIX = "Prefix";
    private static final String MESSAGE_DATE = "MessageDate";

    private String currentText;
    private String currentPrefix;
    private Range currentRange;
    private ParsingStage currentStage;

    /**
     * Returns the result of the parsing.
     *
     * @return the result of the parsing
     */
    public RangeCache getResult() {
        return result;
    }

    /**
     * Receive notification of the beginning of the document.
     *
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void startDocument() throws SAXException {
        result = new RangeCache();
    }

    /**
     * Receive notification of the start of an element.
     *
     * @param uri        The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  The local name (without prefix), or the empty string if Namespace processing is not being
     *                   performed.
     * @param qName      The qualified name (with prefix), or the empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // switch to the next parsing stage if matching
        currentStage = Optional.ofNullable(ParsingStage.match(qName)).orElse(currentStage);
        // create a new range if the start element is a rule
        if (RULE.equals(qName)) {
            currentRange = new Range();
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param uri       The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace
     *                  processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being
     *                  performed.
     * @param qName     The qualified name (with prefix), or the empty string if qualified names are not available.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (RULE.equals(qName)) {
            // add current range to the result if the end element is a rule
            if (currentStage == ParsingStage.REGISTRATION_GROUP) {
                result.addRegistrationGroupRange(currentPrefix, currentRange);
            } else if (currentStage == ParsingStage.REGISTRANT) {
                result.addRegistrantRange(currentPrefix, currentRange);
            }
        } else if (RANGE.equals(qName)) {
            currentRange.setRange(currentText);
        } else if (LENGTH.equals(qName)) {
            currentRange.setLength(currentText);
        } else if (PREFIX.equals(qName)) {
            currentPrefix = currentText;
        } else if (MESSAGE_DATE.equals(qName)) {
            result.setMessageDate(currentText);
        }
    }

    /**
     * Receive notification of character data inside an element.
     *
     * @param ch     The characters.
     * @param start  The start position in the character array.
     * @param length The number of characters to use from the character array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentText = new String(ch, start, length).trim();
    }
}
