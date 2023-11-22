package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdAnyException;
import org.mycore.xsonify.xsd.XsdNoSuchNodeException;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdContent;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdExtension;
import org.mycore.xsonify.xsd.node.XsdList;
import org.mycore.xsonify.xsd.node.XsdNode;
import org.mycore.xsonify.xsd.node.XsdReferenceable;
import org.mycore.xsonify.xsd.node.XsdRestriction;
import org.mycore.xsonify.xsd.node.XsdSimpleType;
import org.mycore.xsonify.xsd.node.XsdUnion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Detector responsible for identifying the JSON primitive type (string, number, or boolean)
 * based on the XML Schema Definition (XSD).
 *
 * <p>
 * The detector analyses the XSD structure, and depending on the XSD elements and attributes,
 * it determines which JSON primitive type should be used for serialization.
 * </p>
 */
public class XsdJsonPrimitiveDetector implements XsdDetector<XsdJsonPrimitiveDetector.JsonPrimitive> {

    /**
     * Enum representing the JSON primitive types: BOOLEAN, NUMBER, and STRING.
     */
    public enum JsonPrimitive {
        BOOLEAN, NUMBER, STRING
    }

    private static final List<XmlExpandedName> NUMBERS = List.of(
        new XmlExpandedName("decimal", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("float", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("double", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("integer", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("int", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("long", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("short", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("byte", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("nonPositiveInteger", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("nonNegativeInteger", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("positiveInteger", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("negativeInteger", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("unsignedByte", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("unsignedInt", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("unsignedLong", "http://www.w3.org/2001/XMLSchema"),
        new XmlExpandedName("unsignedShort", "http://www.w3.org/2001/XMLSchema")
    );

    private static final Map<XmlExpandedName, JsonPrimitive> BUILT_IN_ATTRIBUTES = Map.of(
        new XmlExpandedName("lang", XmlNamespace.XML.uri()), JsonPrimitive.STRING,
        new XmlExpandedName("space", XmlNamespace.XML.uri()), JsonPrimitive.STRING,
        new XmlExpandedName("base", XmlNamespace.XML.uri()), JsonPrimitive.STRING,
        new XmlExpandedName("id", XmlNamespace.XML.uri()), JsonPrimitive.STRING,
        new XmlExpandedName("type", XmlNamespace.XML_SCHEMA_INSTANCE_URI), JsonPrimitive.STRING,
        new XmlExpandedName("nil", XmlNamespace.XML_SCHEMA_INSTANCE_URI), JsonPrimitive.BOOLEAN,
        new XmlExpandedName("schemaLocation", XmlNamespace.XML_SCHEMA_INSTANCE_URI), JsonPrimitive.STRING,
        new XmlExpandedName("noNamespaceSchemaLocation", XmlNamespace.XML_SCHEMA_INSTANCE_URI), JsonPrimitive.STRING
    );

    private static final XmlExpandedName BOOLEAN =
        new XmlExpandedName("boolean", "http://www.w3.org/2001/XMLSchema");

    private final Xsd xsd;

    private final Map<XsdNode, JsonPrimitive> nodeJsonPrimitiveMap;

    /**
     * Constructs a new {@link XsdJsonPrimitiveDetector} based on the provided XSD.
     *
     * @param xsd The XSD used for detection.
     */
    public XsdJsonPrimitiveDetector(Xsd xsd) throws XsdDetectorException {
        this.xsd = xsd;
        this.nodeJsonPrimitiveMap = new LinkedHashMap<>();
        init();
    }

    /**
     * Initializes the detector by analyzing the XSD structure and populating
     * the {@link #nodeJsonPrimitiveMap} with element/attribute nodes and their corresponding JSON primitive types.
     */
    private void init() throws XsdDetectorException {
        List<XsdNode> nodes = this.xsd.collect(XsdElement.class, XsdAttribute.class).stream()
            .filter(XsdReferenceable.class::isInstance)
            .map(XsdReferenceable.class::cast)
            .filter(node -> node.getReference() == null)
            .map(XsdNode.class::cast)
            .toList();
        for (XsdNode node : nodes) {
            if (node instanceof XsdElement) {
                JsonPrimitive primitive = detectElementNode((XsdElement) node);
                nodeJsonPrimitiveMap.put(node, primitive);
            } else if (node instanceof XsdAttribute) {
                JsonPrimitive primitive = detectAttributeNode((XsdAttribute) node);
                nodeJsonPrimitiveMap.put(node, primitive);
            }
        }
    }

    @Override
    public JsonPrimitive detect(XmlPath path) throws XsdDetectorException {
        try {
            // check for built-in attributes
            XmlPath.Node lastNode = path.last();
            if (path.last().isAttribute()) {
                JsonPrimitive builtInPrimitiveValue = BUILT_IN_ATTRIBUTES.get(lastNode.name().expandedName());
                if (builtInPrimitiveValue != null) {
                    return builtInPrimitiveValue;
                }
            }
            // resolve the path
            List<? extends XsdNode> xsdNodes = xsd.resolvePath(path);
            XsdNode last = xsdNodes.get(xsdNodes.size() - 1);
            if (last instanceof XsdReferenceable<?>) {
                XsdNode reference = ((XsdReferenceable<?>) last).getReference();
                last = reference != null ? reference : last;
            }
            return this.nodeJsonPrimitiveMap.get(last);
        } catch (XsdAnyException anyException) {
            return JsonPrimitive.STRING;
        } catch (XsdNoSuchNodeException noSuchNodeException) {
            throw new XsdDetectorException("Unable to detected primitive for path '" + path + "'", noSuchNodeException);
        }
    }

    /**
     * Detects the JSON primitive type for an XSD element.
     *
     * @param element The XSD element to detect.
     * @return The detected JSON primitive type.
     */
    public JsonPrimitive detectElementNode(XsdElement element) throws XsdDetectorException {
        // @type
        XmlExpandedName datatypeName = element.getDatatypeName();
        if (datatypeName != null) {
            return getJsonPrimitive(datatypeName);
        }
        // xs:simpleType
        JsonPrimitive primitive = detectSingleSimpleTypeChild(element);
        if (primitive != null) {
            return primitive;
        }
        // xs:complexType
        XsdComplexType complexType = element.getFirstChild(XsdComplexType.class);
        if (complexType != null) {
            return detectComplexTypeNode(complexType);
        }
        return null;
    }

    /**
     * Detects the JSON primitive type for an XSD attribute.
     *
     * @param attribute The XSD attribute to detect.
     * @return The detected JSON primitive type.
     */
    public JsonPrimitive detectAttributeNode(XsdAttribute attribute) throws XsdDetectorException {
        // @type
        XmlExpandedName datatypeName = attribute.getDatatypeName();
        if (datatypeName != null) {
            return getJsonPrimitive(datatypeName);
        }
        // single xs:simpleType child
        return detectSingleSimpleTypeChild(attribute);
    }

    private JsonPrimitive detectSimpleTypeNode(XsdSimpleType simpleType) throws XsdDetectorException {
        JsonPrimitive result = null;
        for (XsdNode node : simpleType.getChildren()) {
            JsonPrimitive primitive;
            if (node instanceof XsdRestriction) {
                primitive = detectRestrictionNode((XsdRestriction) node);
            } else if (node instanceof XsdUnion) {
                primitive = detectUnionNode((XsdUnion) node);
            } else if (node instanceof XsdList) {
                primitive = detectListNode((XsdList) node);
            } else {
                throw new XsdDetectorException("Invalid child of xs:simpleType '" + node
                    + "'. Only xs:union, xs:list and xs:restriction should be allowed");
            }
            result = max(result, primitive);
            if (JsonPrimitive.STRING.equals(result)) {
                // break early as soon as we find string
                return result;
            }
        }
        return result;
    }

    private JsonPrimitive detectComplexTypeNode(XsdComplexType complexType) throws XsdDetectorException {
        // check complex & simple content
        XsdContent content = complexType.getFirstChild(XsdContent.class);
        if (content != null) {
            return detectContentNode(content);
        }
        return null;
    }

    private JsonPrimitive detectContentNode(XsdContent content) throws XsdDetectorException {
        // check extension
        XsdExtension extension = content.getFirstChild(XsdExtension.class);
        if (extension != null) {
            return detectExtensionNode(extension);
        }
        // check restriction
        XsdRestriction restriction = content.getFirstChild(XsdRestriction.class);
        if (restriction != null) {
            return detectRestrictionNode(restriction);
        }
        return null;
    }

    private JsonPrimitive detectExtensionNode(XsdExtension extension) throws XsdDetectorException {
        return getJsonPrimitive(extension.getBaseName());
    }

    private JsonPrimitive detectRestrictionNode(XsdRestriction restriction) throws XsdDetectorException {
        // check @base first
        JsonPrimitive result = getJsonPrimitive(restriction.getBaseName());
        if (result != null) {
            return result;
        }
        // check inline xs:simpleType
        return detectSingleSimpleTypeChild(restriction);
    }

    private JsonPrimitive detectUnionNode(XsdUnion union) throws XsdDetectorException {
        JsonPrimitive result = null;
        // check @memberTypes first
        for (XmlExpandedName type : union.getMemberTypes()) {
            JsonPrimitive typeResult = getJsonPrimitive(type);
            result = max(result, typeResult);
            if (JsonPrimitive.STRING.equals(result)) {
                return result;
            }
        }
        // multiple xs:simpleType are allowed
        return detectMultipleSingleTypeChild(union);
    }

    private JsonPrimitive detectListNode(XsdList list) throws XsdDetectorException {
        // check @itemType first
        JsonPrimitive result = getJsonPrimitive(list.getItemType());
        if (result != null) {
            return result;
        }
        // one xs:simpleType is allowed
        return detectSingleSimpleTypeChild(list);
    }

    private JsonPrimitive detectMultipleSingleTypeChild(XsdNode parentNode) throws XsdDetectorException {
        JsonPrimitive acc = null;
        for (XsdNode xsdNode : parentNode.getChildren()) {
            if (xsdNode instanceof XsdSimpleType simpleType) {
                JsonPrimitive primitive = detectSimpleTypeNode(simpleType);
                acc = max(acc, primitive);
            }
        }
        return acc;
    }

    private JsonPrimitive detectSingleSimpleTypeChild(XsdNode parentNode) throws XsdDetectorException {
        for (XsdNode xsdNode : parentNode.getChildren()) {
            if (xsdNode instanceof XsdSimpleType simpleType) {
                return detectSimpleTypeNode(simpleType);
            }
        }
        return null;
    }

    /**
     * Determines the higher precedence JSON primitive type between two provided primitives.
     *
     * @param p1 First JSON primitive type.
     * @param p2 Second JSON primitive type.
     * @return The JSON primitive type with higher precedence.
     */
    private JsonPrimitive max(JsonPrimitive p1, JsonPrimitive p2) {
        if (p1 == null) {
            return p2;
        }
        if (p2 == null) {
            return p1;
        }
        return p1.compareTo(p2) >= 0 ? p1 : p2;
    }

    /**
     * Retrieves the JSON primitive type for a specific XSD type.
     *
     * @param type The XSD type for which to get the JSON primitive type.
     * @return The corresponding JSON primitive type, or null if none found.
     */
    private JsonPrimitive getJsonPrimitive(XmlExpandedName type) throws XsdDetectorException {
        if (type == null) {
            return null;
        }
        if (BOOLEAN.equals(type)) {
            return JsonPrimitive.BOOLEAN;
        }
        if (NUMBERS.contains(type)) {
            return JsonPrimitive.NUMBER;
        }
        XsdSimpleType simpleType = xsd.getNamedNode(XsdSimpleType.class, type);
        if (simpleType != null) {
            return detectSimpleTypeNode(simpleType);
        }
        return JsonPrimitive.STRING;
    }

}
