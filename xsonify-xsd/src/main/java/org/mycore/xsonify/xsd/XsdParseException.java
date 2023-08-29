package org.mycore.xsonify.xsd;

/**
 * Exception thrown during the parsing process of an XSD.
 */
public class XsdParseException extends RuntimeException {

    public XsdParseException(String message) {
        super(message);
    }

    public XsdParseException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
