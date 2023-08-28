package org.mycore.xsonify.xml;

/**
 * A strategy interface to apply XML namespace declarations to an XML document.
 */
public interface XmlNamespaceDeclarationStrategy {

    /**
     * Applies the namespace declaration strategy to the entire XML document.
     *
     * @param xmlDocument the XML document to apply the strategy to
     */
    default void apply(XmlDocument xmlDocument) {
        apply(xmlDocument.getRoot());
    }

    /**
     * Applies the namespace declaration strategy to a specific XML element.
     *
     * @param element the XML element to which the strategy should be applied
     */
    void apply(XmlElement element);

}
