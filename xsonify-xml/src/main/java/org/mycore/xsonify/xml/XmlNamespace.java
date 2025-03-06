package org.mycore.xsonify.xml;

/**
 * Represents an XML namespace as defined by the XML Namespaces specification.
 * <p>
 * An XML namespace is a pair consisting of a prefix and a namespace URI. The prefix may be empty,
 * in which case the namespace is the default namespace. The URI may also be empty,
 * in which case the namespace is undeclared.
 * <p>
 * This class also defines some commonly used namespaces as constants, such as the empty namespace,
 * the XML namespace, and the XMLNS namespace.
 * <p>
 * Furthermore, it provides the URIs for the XML Schema and the XML Schema Instance namespaces as constants.
 *
 * @param prefix the namespace prefix. e.g. mods
 * @param uri the namespace uri
 */
public record XmlNamespace(String prefix, String uri) {

    public static final XmlNamespace EMPTY;

    public static final XmlNamespace XML;

    public static final XmlNamespace XMLNS;

    public static final String XML_SCHEMA_URI;

    public static final String XML_SCHEMA_INSTANCE_URI;

    static {
        EMPTY = new XmlNamespace("", "");
        XML = new XmlNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        XMLNS = new XmlNamespace("xmlns", "http://www.w3.org/2000/xmlns/");

        XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";
        XML_SCHEMA_INSTANCE_URI = "http://www.w3.org/2001/XMLSchema-instance";
    }

    /**
     * Checks if the namespace has a prefix.
     *
     * @return true if the namespace has a non-empty prefix, false otherwise
     */
    public boolean hasPrefix() {
        return prefix != null && !prefix.isEmpty();
    }

    /**
     * Returns a string representation of the namespace in the format "xmlns:prefix=\"uri\"",
     * or "xmlns=\"uri\"" if the prefix is empty.
     *
     * @return a string representation of the namespace
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("xmlns");
        if (!prefix().isEmpty()) {
            sb.append(":").append(prefix());
        }
        sb.append("=\"").append(uri()).append("\"");
        return sb.toString();
    }

    /**
     * Checks if the namespace is the empty namespace.
     *
     * @return true if the namespace is the empty namespace, false otherwise
     */
    public boolean isEmptyNamespace() {
        return EMPTY.equals(this);
    }

    /**
     * Checks whether the namespace is equals xml, xmlns or empty.
     *
     * @param namespace the namespace to check
     * @return true if the namespace is a default namespace
     */
    public static boolean isDefaultNamespace(XmlNamespace namespace) {
        return EMPTY.equals(namespace) || XML.equals(namespace) || XMLNS.equals(namespace);
    }

    /**
     * Returns the default {@link XmlNamespace} that corresponds to the given prefix.
     * The returned namespace can be {@link XmlNamespace#EMPTY} for an empty string,
     * {@link XmlNamespace#XML} for "xml", or {@link XmlNamespace#XMLNS} for "xmlns".
     *
     * @param prefix the prefix for which to return the default namespace
     * @return the default namespace for the given prefix, or null if the prefix doesn't match any default namespace
     */
    public static XmlNamespace getDefaultNamespace(String prefix) {
        if (prefix.equals(XmlNamespace.EMPTY.prefix())) {
            return XmlNamespace.EMPTY;
        }
        if (prefix.equals(XmlNamespace.XML.prefix())) {
            return XmlNamespace.XML;
        }
        if (prefix.equals(XmlNamespace.XMLNS.prefix())) {
            return XmlNamespace.XMLNS;
        }
        return null;
    }

}
