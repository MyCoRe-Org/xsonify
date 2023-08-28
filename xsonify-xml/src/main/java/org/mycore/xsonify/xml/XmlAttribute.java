package org.mycore.xsonify.xml;

import java.util.Objects;

/**
 * XmlAttribute represents an attribute in an XML element.
 * It holds the local name, value and namespace of the attribute.
 * It also maintains a reference to the parent XmlElement to which it belongs.
 *
 * <p>The attribute's namespace is represented by an {@link XmlNamespace},
 * and the attribute's qualified name (i.e., prefix:localName) can be
 * retrieved if the namespace has a prefix.</p>
 */
public class XmlAttribute {
    private final XmlName name;
    private final String value;
    private XmlElement parent;

    /**
     * Constructs a new XmlAttribute with the given local name, value and namespace.
     *
     * @param localName The local name of the attribute.
     * @param value     The value of the attribute.
     * @param namespace The namespace of the attribute.
     */
    public XmlAttribute(String localName, String value, XmlNamespace namespace) {
        this.name = new XmlName(localName, namespace);
        this.value = value;
    }

    public String getLocalName() {
        return this.name.local();
    }

    public String getValue() {
        return value;
    }

    public XmlNamespace getNamespace() {
        return this.name.namespace();
    }

    public XmlElement getParent() {
        return parent;
    }

    public String getQualifiedName() {
        return this.name.qualifiedName().toString();
    }

    public XmlExpandedName getExpandedName() {
        return this.name.expandedName();
    }

    public XmlName getName() {
        return this.name;
    }

    protected void setParent(XmlElement parent) {
        this.parent = parent;
    }

    /**
     * Returns a string representation of the XmlAttribute.
     * The string is formatted as: "prefix:localName="value"".
     *
     * @return A string representation of the XmlAttribute.
     */
    @Override
    public String toString() {
        return getQualifiedName() + "=\"" + value + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        XmlAttribute attribute = (XmlAttribute) o;
        return Objects.equals(name, attribute.name) && Objects.equals(value, attribute.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

}
