package org.mycore.xsonify.xml;

/**
 * <p>Exception class for errors that occur during XML parsing.</p>
 *
 * <p>This is a runtime exception, so it does not need to be declared in a method's or
 * a constructor's throws clause if it can be thrown by the execution of the method or constructor and
 * propagate outside the method or constructor boundary.</p>
 */
public class XmlParseException extends XmlException {

    /**
     * Constructs a new XmlParseException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public XmlParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new XmlParseException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public XmlParseException(String message) {
        super(message);
    }

}
