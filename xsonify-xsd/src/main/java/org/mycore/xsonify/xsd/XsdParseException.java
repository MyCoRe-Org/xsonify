package org.mycore.xsonify.xsd;

/**
 * Represents an exception that occurs during the parsing of an XSD (XML Schema Definition).
 * <p>
 * This exception can be used to indicate errors or abnormal conditions encountered
 * while parsing or processing an XSD document.
 * </p>
 */
public class XsdParseException extends Exception {

    /**
     * Constructs a new XsdParseException with the specified detail message.
     *
     * @param message The detail message which is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public XsdParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new XsdParseException with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically
     * incorporated into this exception's detail message.
     * </p>
     *
     * @param message   The detail message which is saved for later retrieval by the
     *                  {@link #getMessage()} method.
     * @param throwable The cause (which is saved for later retrieval by the {@link #getCause()}
     *                  method). A {@code null} value is permitted, and indicates that the cause
     *                  is nonexistent or unknown.
     */
    public XsdParseException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
