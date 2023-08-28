package org.mycore.xsonify.xsd;

/**
 * Exception while parsing a xsd.
 */
public class XsdParseException extends RuntimeException {

    public XsdParseException(String message) {
        super(message);
    }

    public XsdParseException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
