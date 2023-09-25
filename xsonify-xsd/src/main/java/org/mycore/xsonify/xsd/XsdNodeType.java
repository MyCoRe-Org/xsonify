package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlElement;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * <p>Enumeration of different types of XSD elements.</p>
 * <p>Provides utility methods and lists to categorize and identify XSD node types.</p>
 * Implementation details:
 * <ul>
 *   <li>Use uppercase: avoid reserved java keywords.</li>
 *   <li>Omit underscore: faster string comparison.</li>
 * </ul>
 */
public enum XsdNodeType {
    IMPORT, INCLUDE, REDEFINE,
    ELEMENT, GROUP,
    COMPLEXTYPE, SIMPLETYPE,
    CHOICE, ALL, SEQUENCE, ANY,
    SIMPLECONTENT, COMPLEXCONTENT,
    ATTRIBUTE, ATTRIBUTEGROUP, ANYATTRIBUTE,
    RESTRICTION, EXTENSION;

    /**
     * List of nodes which can contain elements. Either as a child or somewhere down their hierarchy.
     */
    public static final List<XsdNodeType> ELEMENT_CONTAINER_NODES = List.of(
        INCLUDE, REDEFINE,
        ELEMENT, GROUP,
        COMPLEXTYPE,
        CHOICE, ALL, SEQUENCE,
        COMPLEXCONTENT,
        RESTRICTION, EXTENSION
    );

    /**
     * List of nodes which can contain attributes. Either as a child or somewhere down their hierarchy.
     */
    public static final List<XsdNodeType> ATTRIBUTE_CONTAINER_NODES = List.of(
        INCLUDE, REDEFINE,
        ELEMENT, GROUP,
        COMPLEXTYPE, SIMPLETYPE,
        CHOICE, ALL, SEQUENCE,
        SIMPLECONTENT, COMPLEXCONTENT,
        ATTRIBUTEGROUP,
        RESTRICTION, EXTENSION
    );

    public boolean is(XsdNodeType... types) {
        return Arrays.stream(types).anyMatch(type -> type == this);
    }

    public static XsdNodeType of(XmlElement element) {
        XsdNodeType type;
        try {
            String name = element.getLocalName().toUpperCase(Locale.ROOT);
            type = XsdNodeType.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return type;
    }

}
