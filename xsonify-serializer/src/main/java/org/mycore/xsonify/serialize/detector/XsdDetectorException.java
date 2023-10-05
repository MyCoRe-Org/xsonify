package org.mycore.xsonify.serialize.detector;

/**
 * The {@code XsdDetectorException} class represents exceptions that occur during the
 * operation of XSD detectors within the system.
 *
 * <p>This exception is typically thrown when unexpected situations arise during the
 * detection processes, such as encountering unexpected node types or failing to
 * interpret XSD patterns correctly.</p>
 *
 * @see Exception
 */
public class XsdDetectorException extends Exception {

    /**
     * Constructs a new {@code XsdDetectorException} with the specified detail message.
     *
     * @param message the detail message, saved for later retrieval by the {@link #getMessage()} method
     */
    public XsdDetectorException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code XsdDetectorException} with the specified detail message
     * and cause.
     *
     * <p>Note that the detail message associated with {@code cause} is not automatically
     * incorporated in this exception's detail message.</p>
     *
     * @param message the detail message, saved for later retrieval by the {@link #getMessage()} method
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *              A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown
     */
    public XsdDetectorException(String message, Throwable cause) {
        super(message, cause);
    }

}
