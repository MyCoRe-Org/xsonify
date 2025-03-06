package org.mycore.xsonify.xml;

import java.util.Set;

/**
 * Provides a set of all XML and XML Schema Instance (XSI) built-in attributes.
 * <p>
 * This includes attributes from the "xml" namespace such as "lang",
 * "space", "base", and "id", and attributes from the "xsi" namespace such
 * as "type", "nil", "schemaLocation", and "noNamespaceSchemaLocation".
 * <p>
 * Each built-in attribute is represented as an XmlExpandedName, which is a
 * combination of the attribute's local name and namespace URI.
 */
public final class XmlBuiltInAttributes {

    /**
     * A set of all XML and XML Schema Instance (XSI) built-in attributes.
     * Each attribute is represented as an XmlExpandedName.
     */
    public static final Set<XmlExpandedName> BUILT_IN_ATTRIBUTES = Set.of(
        new XmlExpandedName("lang", XmlNamespace.XML.uri()),
        new XmlExpandedName("space", XmlNamespace.XML.uri()),
        new XmlExpandedName("base", XmlNamespace.XML.uri()),
        new XmlExpandedName("id", XmlNamespace.XML.uri()),

        new XmlExpandedName("type", XmlNamespace.XML_SCHEMA_INSTANCE_URI),
        new XmlExpandedName("nil", XmlNamespace.XML_SCHEMA_INSTANCE_URI),
        new XmlExpandedName("schemaLocation", XmlNamespace.XML_SCHEMA_INSTANCE_URI),
        new XmlExpandedName("noNamespaceSchemaLocation", XmlNamespace.XML_SCHEMA_INSTANCE_URI));

    /**
     * Checks if the given attribute is a build-in attribute.
     * 
     * @param attributeName the attribute to check
     * @return true if so, otherwise false
     */
    public static boolean is(XmlExpandedName attributeName) {
        return BUILT_IN_ATTRIBUTES.contains(attributeName);
    }

}
