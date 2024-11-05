package org.mycore.xsonify.xsd;

/**
 * The {@code XsdException} class serves as the base exception for all XSD-related errors.
 */
public abstract class XsdException extends Exception {

    /**
     * Constructs a new {@code XsdException} with {@code null} as its detail message.
     */
    public XsdException() {
        super();
    }

    /**
     * Constructs a new {@code XsdException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public XsdException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code XsdException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of the exception.
     */
    public XsdException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code XsdException} with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())}.
     *
     * @param cause the cause of the exception.
     */
    public XsdException(Throwable cause) {
        super(cause);
    }

}
