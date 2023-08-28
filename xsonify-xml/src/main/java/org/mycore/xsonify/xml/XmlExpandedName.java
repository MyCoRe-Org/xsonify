package org.mycore.xsonify.xml;

import java.util.function.Function;

/**
 * Represents an XML expanded name, as defined in the XML Namespaces specification.
 * <p>
 * An expanded name consists of a local part and a namespace URI,
 * which provides a fully-qualified name that is unique across all XML documents.
 * The namespace URI is optional and defaults to an empty string if not provided,
 * meaning that the name is not in any namespace.
 * <p>
 * The expanded name comes in the form of "{namespaceURI}localName".
 */
public record XmlExpandedName(String local, String uri) {

    /**
     * Creates an instance of XmlExpandedName with the given local name and namespace URI.
     *
     * @param local the local name
     * @param uri   the namespace URI
     */
    public XmlExpandedName(String local, String uri) {
        this.local = local;
        this.uri = uri == null ? "" : uri;
    }

    /**
     * Returns the string representation of this expanded name,
     * in the format "{namespaceURI}localName" for names in a namespace,
     * or just "localName" for names not in a namespace.
     *
     * @return the string representation of this expanded name
     */
    @Override
    public String toString() {
        return (!uri.isEmpty() ? "{" + uri + "}" : "") + local;
    }

    /**
     * <p>Creates an instance of XmlExpandedName from a string representation of an expanded name.</p>
     * <p>The string should be in the format "{namespaceURI}localName" for names in a namespace,
     * or just "localName" for names not in a namespace.</p>
     *
     * @param expandedName the string representation of an expanded name
     * @return an instance of XmlExpandedName
     */
    public static XmlExpandedName of(String expandedName) {
        int index = expandedName.indexOf('}');
        if (index == -1) {
            return new XmlExpandedName(expandedName, "");
        }
        String local = expandedName.substring(index + 1);
        String uri = expandedName.substring(1, index);
        return new XmlExpandedName(local, uri);
    }

    /**
     * <p>Creates an instance of XmlExpandedName from a qualified name and a function
     * that maps prefixes to namespace URIs.</p>
     * <p>The qualified name should be in the format "prefix:localName" for names in a namespace,
     * or just "localName" for names not in a namespace. The function should return the
     * namespace URI for a given prefix, or null if the prefix is not recognized.</p>
     *
     * @param qualifiedName       the qualified name
     * @param prefixToUriResolver a function that maps prefixes to namespace URIs
     * @return an instance of XmlExpandedName
     */
    public static XmlExpandedName of(XmlQualifiedName qualifiedName, Function<String, String> prefixToUriResolver) {
        String uri = prefixToUriResolver.apply(qualifiedName.prefix());
        return new XmlExpandedName(qualifiedName.localName(), uri);
    }

}
