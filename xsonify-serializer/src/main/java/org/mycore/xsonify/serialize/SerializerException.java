package org.mycore.xsonify.serialize;

public class SerializerException extends RuntimeException {

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
