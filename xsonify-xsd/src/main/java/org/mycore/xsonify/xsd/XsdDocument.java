package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlQualifiedName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class XsdDocument extends XmlDocument {

    public static final List<String> EXPANDED_ATTRIBUTES = Arrays.asList("base", "ref", "type");

    private final String schemaLocation;

    private final String targetNamespace;

    public XsdDocument(String schemaLocation, String targetNamespace) {
        this.schemaLocation = schemaLocation;
        this.targetNamespace = targetNamespace == null ? "" : targetNamespace;
    }

    public String getTargetNamespace() {
        return this.targetNamespace;
    }

    public void expandAttributes() throws XsdParseException {
        this.expandAttributes(getRoot());
    }

    private void expandAttributes(XmlElement element) throws XsdParseException {
        element.getAttributes().stream()
            .filter(attribute -> EXPANDED_ATTRIBUTES.contains(attribute.getLocalName()))
            .forEach(attribute -> expandAttribute(element, attribute.getLocalName(), attribute.getValue()));
        element.getElements().forEach(this::expandAttributes);
    }

    private void expandAttribute(XmlElement element, String name, String value) throws XsdParseException {
        XmlQualifiedName qualifiedName = XmlQualifiedName.of(value);
        XmlExpandedName expandedName = XmlExpandedName.of(qualifiedName, (prefix) -> {
            Map<String, XmlNamespace> nsMap = getAttributeNamespaceMap(element);
            XmlNamespace namespace = nsMap.get(prefix);
            if (namespace != null) {
                return namespace.uri();
            }
            throw new XsdParseException("Missing namespace definition for " + name + "=" + value);
        });
        element.setAttribute(name, expandedName.toString());
    }

    /**
     * Returns the attribute namespace map for the given xml element. This includes the namespaces in scope, the
     * xmlns namespace and the target namespace of the xsd document which is bound to "".
     *
     * @param element
     * @return
     */
    private Map<String, XmlNamespace> getAttributeNamespaceMap(XmlElement element) {
        Map<String, XmlNamespace> nsMap = element.getNamespacesInScope();
        nsMap.put(XmlNamespace.EMPTY.prefix(), new XmlNamespace(XmlNamespace.EMPTY.prefix(), getTargetNamespace()));
        nsMap.putIfAbsent(XmlNamespace.XMLNS.prefix(), XmlNamespace.XMLNS);
        return nsMap;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    @Override
    public String toString() {
        return this.schemaLocation;
    }

    public static XsdDocument of(String schemaLocation, String targetNamespace, XmlDocument document)
        throws XsdParseException {
        XsdDocument xsdDocument = new XsdDocument(schemaLocation, targetNamespace);
        xsdDocument.setRoot(document.getRoot().copy(xsdDocument));
        xsdDocument.expandAttributes();
        return xsdDocument;
    }

}
