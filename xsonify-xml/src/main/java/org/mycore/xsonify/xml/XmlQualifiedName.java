package org.mycore.xsonify.xml;

/**
 * A record that represents a qualified XML name consisting of a prefix and a local name.
 * <p>
 * A qualified XML name is typically used to refer to elements and attributes in XML documents.
 * It might have a prefix associated with a namespace and a local name which is the actual
 * element or attribute name.
 * <p>
 * Example:
 * <br>
 * In the XML fragment {@code <pfx:elementName>}, "pfx" is the prefix and "elementName" is the local name.
 * The full qualified name is represented as "pfx:elementName".
 *
 * @param prefix the namespace prefix. e.g. mods
 * @param localName the local name e.g. titleInfo
 */
public record XmlQualifiedName(String prefix, String localName) {

    /**
     * Returns a string representation of the qualified XML name.
     * <p>
     * If the prefix is present and non-empty, it is appended with a colon before the local name.
     * Otherwise, only the local name is returned.
     * </p>
     *
     * @return the string representation of the qualified XML name.
     */
    @Override
    public String toString() {
        return (hasPrefix() ? (prefix + ":") : "") + localName;
    }

    /**
     * Checks if the qualified name has a non-empty prefix.
     *
     * @return {@code true} if the prefix is present and non-empty; {@code false} otherwise.
     */
    public boolean hasPrefix() {
        return prefix != null && !prefix.isEmpty();
    }

    /**
     * Creates an instance of {@code XmlQualifiedName} from a given qualified name string.
     * <p>
     * The qualified name string is expected to be in the format "prefix:localName". If there is no colon,
     * then the entire string is treated as the local name with an empty prefix.
     * </p>
     * Examples:
     * <ul>
     *   <li>{@code XmlQualifiedName.of("pfx:elementName")} would result in prefix "pfx" and local name "elementName".</li>
     *   <li>{@code XmlQualifiedName.of("elementName")} would result in an empty prefix and local name "elementName".</li>
     * </ul>
     *
     * @param qualifiedName the qualified name string.
     * @return an instance of {@code XmlQualifiedName} representing the provided qualified name.
     * @throws IllegalArgumentException if the provided string contains more than one colon.
     */
    public static XmlQualifiedName of(String qualifiedName) {
        String[] split = qualifiedName.split(":");
        if (split.length > 2) {
            throw new IllegalArgumentException(
                "The provided qualifiedName '" + qualifiedName + "' contains more than one colon!");
        }
        String prefix = split.length == 1 ? "" : split[0];
        String localName = split.length == 1 ? split[0] : split[1];
        return new XmlQualifiedName(prefix, localName);
    }

}
