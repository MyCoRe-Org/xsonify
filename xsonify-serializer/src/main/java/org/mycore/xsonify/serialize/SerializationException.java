package org.mycore.xsonify.serialize;

/**
 * Represents an exception that occurs during the xml->json or json->xml serialization processes.
 * <p>
 * This exception can be used to wrap and propagate any errors or abnormal
 * conditions that are encountered while serializing data.
 * </p>
 */
public class SerializationException extends Exception {

    /**
     * Constructs a new SerializationException with the specified detail message.
     *
     * @param message The detail message which is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new SerializationException with the specified cause.
     * <p>
     * This constructor is useful for exceptions that are little more than wrappers for other throwables.
     * </p>
     *
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()}
     *              method). A {@code null} value is permitted, and indicates that the cause
     *              is nonexistent or unknown.
     */
    public SerializationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new SerializationException with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically
     * incorporated into this exception's detail message.
     * </p>
     *
     * @param message The detail message which is saved for later retrieval by the
     *                {@link #getMessage()} method.
     * @param throwable The cause (which is saved for later retrieval by the {@link #getCause()}
     *                  method). A {@code null} value is permitted, and indicates that the cause
     *                  is nonexistent or unknown.
     */
    public SerializationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
