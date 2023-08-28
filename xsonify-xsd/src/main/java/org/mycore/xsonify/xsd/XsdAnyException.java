package org.mycore.xsonify.xsd;

/**
 * Thrown by various methods to indicate that the node being requested has matched a xs:any or xs:anyAttribute node,
 * and it's unclear how to process further.
 */
public class XsdAnyException extends RuntimeException {

    public XsdAnyException(String message) {
        super(message);
    }

}
