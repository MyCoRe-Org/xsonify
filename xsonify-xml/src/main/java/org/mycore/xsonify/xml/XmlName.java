package org.mycore.xsonify.xml;

import java.util.function.Function;

/**
 * Represents an XML name that consists of a local part and a namespace, defined by the XML Namespaces specification.
 * <p>
 * The namespace includes a prefix and a namespace URI. If the namespace has a prefix, the qualified name of the XML
 * name will be in the form "prefix:localName". Otherwise, it will just be the local name.
 *
 * @param local local name like 'title'
 * @param namespace namespace part with prefix and uri
 */
public record XmlName(String local, XmlNamespace namespace) {

    /**
     * Returns the prefix of the namespace of this XML name.
     *
     * @return the namespace prefix
     */
    public String prefix() {
        return namespace.prefix();
    }

    /**
     * Returns the URI of the namespace of this XML name.
     *
     * @return the namespace URI
     */
    public String uri() {
        return namespace.uri();
    }

    /**
     * Returns the qualified name of this XML name, which includes the namespace prefix (if it exists) and the local name.
     *
     * @return the qualified name
     */
    public XmlQualifiedName qualifiedName() {
        return new XmlQualifiedName(namespace.hasPrefix() ? prefix() : "", local);
    }

    /**
     * Returns an {@link XmlExpandedName} object that represents the expanded name of this XML name.
     *
     * @return an XmlExpandedName that represents the expanded name
     */
    public XmlExpandedName expandedName() {
        return new XmlExpandedName(local, namespace.uri());
    }

    /**
     * Creates an instance of XmlName from a string representation of a qualified name and a function
     * that maps prefixes to namespace URIs.
     * The string should be in the format "prefix:localName" for names in a namespace,
     * or just "localName" for names not in a namespace.
     *
     * @param qualifiedName       the string representation of a qualified name
     * @param prefixToUriResolver a function that maps prefixes to namespace URIs
     * @return an instance of XmlName
     */
    public static XmlName of(String qualifiedName, Function<String, String> prefixToUriResolver) {
        String[] nameSplit = qualifiedName.split(":");
        String localName = nameSplit.length == 1 ? qualifiedName : nameSplit[1];
        String prefix = nameSplit.length == 1 ? "" : nameSplit[0];
        String uri = prefixToUriResolver.apply(prefix);
        return new XmlName(localName, new XmlNamespace(prefix, uri));
    }

    @Override
    public String toString() {
        return this.namespace != null ? this.qualifiedName().toString() : this.local;
    }

}
