package com.github.c5h12o5.isbn;

/**
 * Signals that an error occurred while using the ISBN utility.
 *
 * @author c5h12o5
 * @since 1.0.0
 */
public class ISBNException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ISBNException() {
        super();
    }

    public ISBNException(String message) {
        super(message);
    }

    public ISBNException(String message, Throwable cause) {
        super(message, cause);
    }
}