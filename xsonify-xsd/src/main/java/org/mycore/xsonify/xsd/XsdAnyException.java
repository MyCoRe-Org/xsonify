package org.mycore.xsonify.xsd;

/**
 * Represents an exception thrown when encountering an {@code xs:any} or {@code xs:anyAttribute} node in the XSD schema.
 * <p>
 * The exception indicates that the processing logic encountered an ambiguous or wildcard node in the XSD schema,
 * making further processing or determination unclear.
 * </p>
 */
public class XsdAnyException extends XsdException {

    /**
     * Constructs a new {@code XsdAnyException} with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public XsdAnyException(String message) {
        super(message);
    }

}
