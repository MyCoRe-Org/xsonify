package org.mycore.xsonify.xsd;

/**
 * Represents an exception thrown when a node is expected but couldn't be found in the XSD schema.
 */
public class XsdNoSuchNodeException extends Exception {

    /**
     * Constructs a new {@code XsdNoSuchNodeException} with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public XsdNoSuchNodeException(String message) {
        super(message);
    }

}
