package org.mycore.xsonify.xsd;

/**
 * The {@code XsdAmbiguousNodeException} is thrown when an ambiguous element definition
 * is encountered within the XSD schema. This occurs when multiple elements with the same
 * name and namespace are found in a context that expects a single, unique element.
 */
public class XsdAmbiguousNodeException extends XsdException {

    /**
     * Constructs a new {@code XsdAmbiguousNodeException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public XsdAmbiguousNodeException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code XsdAmbiguousNodeException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of the exception.
     */
    public XsdAmbiguousNodeException(String message, Throwable cause) {
        super(message, cause);
    }

}
