package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlExpandedName;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Xsd built-in datatypes.
 *
 * @see <a href="https://www.w3.org/TR/xmlschema-2/#built-in-datatypes">Built-In Datatypes.</a>
 */
public abstract class XsdBuiltInDatatypes {

    /**
     * @see <a href="https://www.w3.org/TR/xmlschema-2/#built-in-primitive-datatypes">Primitive Datatypes</a>
     */
    public static final Set<String> PRIMITIVE_DATATYPES = Set.of("string", "boolean", "decimal", "float", "double",
        "duration", "dateTime", "time", "gYearMonth", "gYear", "gMonthDay", "gDay", "gMonth", "hexBinary",
        "base64Binary", "anyURI", "QName", "NOTATION");

    /**
     * @see <a href="https://www.w3.org/TR/xmlschema-2/#built-in-derived">Derived Datatypes</a>
     */
    public static final Set<String> DERIVED_DATATYPES = Set.of("normalizedString", "token", "language", "NMTOKEN",
        "NMTOKENS", "Name", "NCName", "ID", "IDREF", "IDREFS", "ENTITY", "ENTITIES", "integer", "nonPositiveInteger",
        "negativeInteger", "long", "int", "short", "byte", "nonNegativeInteger", "unsignedLong", "unsignedInt",
        "unsignedShort", "unsignedByte", "positiveInteger");

    public static final Set<XmlExpandedName> EXPANDED_PRIMITIVE_DATATYPES;

    public static final Set<XmlExpandedName> EXPANDED_DERIVED_DATATYPES;

    static {
        EXPANDED_PRIMITIVE_DATATYPES = new LinkedHashSet<>();
        PRIMITIVE_DATATYPES.stream()
            .map(type -> new XmlExpandedName(type, "http://www.w3.org/2001/XMLSchema"))
            .forEach(EXPANDED_PRIMITIVE_DATATYPES::add);
        EXPANDED_DERIVED_DATATYPES = new LinkedHashSet<>();
        DERIVED_DATATYPES.stream()
            .map(type -> new XmlExpandedName(type, "http://www.w3.org/2001/XMLSchema"))
            .forEach(EXPANDED_DERIVED_DATATYPES::add);
    }

    /**
     * Checks whether the given type is a built-in type.
     *
     * @param type the type to check
     * @return true if the type is a xsd built-in type
     */
    public static boolean is(String type) {
        return PRIMITIVE_DATATYPES.contains(type) || DERIVED_DATATYPES.contains(type);
    }

    /**
     * Checks whether the given type is a built-in type.
     *
     * @param type the type to check
     * @return true if the type is a xsd built-in type
     */
    public static boolean is(XmlExpandedName type) {
        return EXPANDED_PRIMITIVE_DATATYPES.contains(type) || EXPANDED_DERIVED_DATATYPES.contains(type);
    }

}
