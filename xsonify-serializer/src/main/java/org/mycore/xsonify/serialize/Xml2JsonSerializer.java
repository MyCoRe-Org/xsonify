package org.mycore.xsonify.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mycore.xsonify.serialize.SerializerSettings.NamespaceHandling;
import org.mycore.xsonify.serialize.detector.XsdDetectorException;
import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector;
import org.mycore.xsonify.xml.XmlAttribute;
import org.mycore.xsonify.xml.XmlContent;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xml.XmlText;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdAnyException;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;
import org.mycore.xsonify.xsd.node.XsdDatatype;
import org.mycore.xsonify.xsd.node.XsdNode;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdSimpleType;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class Xml2JsonSerializer extends SerializerBase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Xml2JsonSerializer(Xsd xsd) throws SerializationException {
        super(xsd, new SerializerSettings());
    }

    public Xml2JsonSerializer(Xsd xsd, SerializerSettings settings) throws SerializationException {
        super(xsd, settings, new SerializerStyle());
    }

    public Xml2JsonSerializer(Xsd xsd, SerializerSettings settings, SerializerStyle style) throws
        SerializationException {
        super(xsd, settings, style);
    }

    public ObjectNode serialize(XmlDocument document) throws SerializationException {
        try {
            ObjectNode jsonContent = MAPPER.createObjectNode();
            serializeElement(document.getRoot(), jsonContent, null);
            if (settings().omitRootElement()) {
                return jsonContent;
            }
            ObjectNode json = MAPPER.createObjectNode();
            json.set(getName(document.getRoot()), jsonContent);
            return json;
        } catch (XsdDetectorException detectorException) {
            throw new SerializationException(detectorException);
        }
    }

    private void serializeElement(XmlElement element, ObjectNode json, XmlNamespace parentNamespace)
        throws SerializationException, XsdDetectorException {

        // NAMESPACES
        handleNamespaces(element, json, parentNamespace);

        // ATTRIBUTES
        handleAttributes(element, json);

        // MIXED CONTENT
        if (hasMixedContent(element)) {
            handleMixedContent(element, json);
            return;
        }

        // TEXT
        if (element.hasText()) {
            handleText(element, json);
        }

        // CHILDREN
        // - build child map of <name,namespace> pair with their respective elements
        Map<XmlExpandedName, List<XmlElement>> childContentMap = new LinkedHashMap<>();
        for (XmlElement childElement : element.getElements()) {
            List<XmlElement> contentList = childContentMap.computeIfAbsent(childElement.getExpandedName(),
                k -> new ArrayList<>());
            contentList.add(childElement);
        }
        // - run through the map and serialize
        for (Map.Entry<XmlExpandedName, List<XmlElement>> entry : childContentMap.entrySet()) {
            List<XmlElement> xmlContent = entry.getValue();
            String name = getName(xmlContent);
            if (useArray(xmlContent)) {
                serializeChildElements(json, name, xmlContent, element.getNamespace());
            } else {
                serializeChildElement(json, name, xmlContent.get(0), element.getNamespace());
            }
        }
    }

    private void serializeChildElements(ObjectNode json, String propertyName, List<XmlElement> content,
        XmlNamespace parentNamespace) throws SerializationException, XsdDetectorException {
        ArrayNode array = MAPPER.createArrayNode();
        for (XmlElement element : content) {
            serializeChildElement(array, element, parentNamespace);
        }
        json.set(propertyName, array);
    }

    private void serializeChildElement(ObjectNode jsonObject, String propertyName, XmlElement element,
        XmlNamespace parentNamespace) throws SerializationException, XsdDetectorException {
        if (hasPlainText(element)) {
            XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(XmlPath.of(element));
            String text = element.getTextNormalized();
            switch (jsonPrimitive) {
            case BOOLEAN -> jsonObject.put(propertyName, Boolean.parseBoolean(text));
            case NUMBER -> jsonObject.put(propertyName, new BigDecimal(text));
            case STRING -> jsonObject.put(propertyName, getText(element));
            }
            return;
        }
        ObjectNode childObject = MAPPER.createObjectNode();
        serializeElement(element, childObject, parentNamespace);
        jsonObject.set(propertyName, childObject);
    }

    private void serializeChildElement(ArrayNode jsonArray, XmlElement element, XmlNamespace parentNamespace)
        throws SerializationException, XsdDetectorException {
        if (hasPlainText(element)) {
            XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(XmlPath.of(element));
            String text = element.getTextNormalized();
            switch (jsonPrimitive) {
            case BOOLEAN -> jsonArray.add(Boolean.parseBoolean(text));
            case NUMBER -> jsonArray.add(new BigDecimal(text));
            case STRING -> jsonArray.add(getText(element));
            }
            return;
        }
        ObjectNode childObject = MAPPER.createObjectNode();
        serializeElement(element, childObject, parentNamespace);
        jsonArray.add(childObject);
    }

    private String getText(XmlElement childElement) {
        return settings().normalizeText() ? childElement.getTextNormalized() : childElement.getText();
    }

    private String getName(List<XmlElement> elements) throws SerializationException, XsdDetectorException {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (XmlElement element : elements) {
            String name = getName(element);
            names.add(name);
        }
        if (names.size() != 1) {
            throw new SerializationException("name conflict for elements " + elements);
        }
        return names.stream().findFirst().get();
    }

    private String getName(XmlElement element) throws XsdDetectorException {
        if (SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT.equals(settings().elementPrefixHandling())) {
            boolean hasNameConflict = prefixConflictDetector().detect(element);
            return hasNameConflict ? element.getQualifiedName().toString() : element.getLocalName();
        }
        return element.getQualifiedName().toString();
    }

    private String getAttributeName(XmlAttribute attribute) throws XsdDetectorException {
        if (SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT.equals(settings().attributePrefixHandling())) {
            boolean hasNameConflict = prefixConflictDetector().detect(attribute);
            return getAttributeName(hasNameConflict ?
                                    attribute.getQualifiedName() :
                                    attribute.getLocalName());
        }
        return getAttributeName(attribute.getQualifiedName());
    }

    private String getAttributeName(String attributeName) {
        return style().attributePrefix() + attributeName;
    }

    private String getXmlnsPrefix(XmlNamespace namespace) {
        if (namespace.prefix().isEmpty()) {
            return style().xmlnsPrefix();
        }
        return style().xmlnsPrefix() + ":" + namespace.prefix();
    }

    private boolean hasMixedContent(XmlElement element) throws XsdDetectorException {
        return mixedContentDetector().detect(element);
    }

    private boolean useArray(List<XmlElement> elements) throws XsdDetectorException {
        if (this.repeatableElementDetector() != null) {
            return this.repeatableElementDetector().detect(elements.get(0));
        }
        if (SerializerSettings.JsonStructure.ENFORCE_ARRAY.equals(settings().jsonStructure())) {
            return true;
        }
        return elements.size() > 1;
    }

    private boolean hasPlainText(XmlElement element) throws SerializationException {
        // ALWAYS WRAP
        if (SerializerSettings.PlainTextHandling.ALWAYS_WRAP.equals(settings().plainTextHandling())) {
            return false;
        }
        // SIMPLETYPE
        if (isSimpleType(element)) {
            return true;
        }
        // NOT a SIMPLETYPE
        if (SerializerSettings.PlainTextHandling.SIMPLIFY_SIMPLETYPE.equals(settings().plainTextHandling())) {
            return false;
        }
        // check elements and attributes
        if (element.hasElements() || element.hasAttributes()) {
            return false;
        }
        // check namespaces
        if (element.getNamespacesIntroduced().isEmpty()) {
            return true;
        }
        // we introduce at least one namespace -> check the strategy
        NamespaceHandling namespaceHandling = settings().namespaceHandling();
        if (NamespaceHandling.OMIT.equals(namespaceHandling)) {
            return true;
        }
        if (NamespaceHandling.ADD.equals(namespaceHandling)) {
            return false;
        }
        return !isChildOfXsAny(element);
    }

    private Boolean isSimpleType(XmlElement element) throws SerializationException {
        try {
            XsdElement xsdElement = xsd().resolveXmlElement(element);
            return isSimpleType(xsdElement.getReferenceOrSelf());
        } catch (XsdAnyException xsdAnyException) {
            return false;
        }
    }

    private Boolean isSimpleType(XsdElement xsdElement) throws SerializationException {
        if (xsdElement == null) {
            return null;
        }
        // check @type
        String type = xsdElement.getElement().getAttribute("type", XmlNamespace.EMPTY.uri());
        if (type != null) {
            if (XsdBuiltInDatatypes.is(XmlExpandedName.of(type))) {
                return true;
            }
            XsdDatatype datatype = xsdElement.getDatatype();
            return XsdSimpleType.TYPE.equals(datatype.getType());
        }
        // check first child
        if (xsdElement.getChildren().isEmpty()) {
            throw new SerializationException("XsdNode has neither @type nor any children '" + xsdElement + "'.");
        }
        XsdNode child = xsdElement.getChildren().get(0);
        // TODO extension/restriction
        return XsdSimpleType.TYPE.equals(child.getType());
    }

    private void handleNamespaces(XmlElement element, ObjectNode json, XmlNamespace parentNamespace) {
        NamespaceHandling namespaceHandling = settings().namespaceHandling();
        if (NamespaceHandling.OMIT.equals(namespaceHandling) ||
            (NamespaceHandling.ADD_IF_XS_ANY.equals(namespaceHandling) && !isChildOfXsAny(element))) {
            return;
        }
        // element namespace
        XmlNamespace namespace = element.getNamespace();
        if (!namespace.equals(parentNamespace) && !namespace.prefix().isEmpty()) {
            json.put(style().namespacePrefixKey(), namespace.prefix());
        }
        // introduced namespaces
        element.getNamespacesIntroduced().values().forEach(ns -> json.put(getXmlnsPrefix(ns), ns.uri()));
    }

    private void handleText(XmlElement element, ObjectNode json) {
        XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(XmlPath.of(element));
        String text = element.getTextNormalized();
        switch (jsonPrimitive) {
        case BOOLEAN -> json.put(style().textKey(), Boolean.parseBoolean(text));
        case NUMBER -> json.put(style().textKey(), new BigDecimal(text));
        case STRING -> json.put(style().textKey(), getText(element));
        }
    }

    private boolean isChildOfXsAny(XmlElement element) {
        XmlElement parent = element.getParent();
        if (parent == null) {
            return false;
        }
        XsdElement parentXsdNode = xsd().getNamedNode(XsdElement.class, parent.getExpandedName());
        return parentXsdNode != null && parentXsdNode.hasAny();
    }

    private void handleAttributes(XmlElement element, ObjectNode json) throws XsdDetectorException {
        for (XmlAttribute attribute : element.getAttributes()) {
            handleAttribute(attribute, json);
        }
    }

    private void handleAttribute(XmlAttribute attribute, ObjectNode json) throws XsdDetectorException {
        final String attributeName = getAttributeName(attribute);
        final String attributeValue = attribute.getValue();
        XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(XmlPath.of(attribute));
        switch (jsonPrimitive) {
        case BOOLEAN -> json.put(attributeName, Boolean.parseBoolean(attributeValue));
        case NUMBER -> json.put(attributeName, new BigDecimal(attributeValue));
        case STRING -> json.put(attributeName, attributeValue);
        }
    }

    private void handleMixedContent(XmlElement element, ObjectNode json)
        throws SerializationException, XsdDetectorException {
        if (SerializerSettings.MixedContentHandling.UTF_8_ENCODING.equals(settings().mixedContentHandling())) {
            String mixedContentAsString = element.encodeContent(StandardCharsets.UTF_8);
            if (!mixedContentAsString.isEmpty()) {
                json.put(style().mixedContentKey(), mixedContentAsString);
            }
            return;
        }
        serializeMixedContent(element, json);
    }

    private void serializeMixedContent(XmlElement element, ObjectNode json)
        throws SerializationException, XsdDetectorException {
        ArrayNode content = MAPPER.createArrayNode();
        if (settings().normalizeText()) {
            for (XmlElement.TrailingInfo trailingInfo : element.trailingContent()) {
                XmlContent childContent = trailingInfo.content();
                if (childContent instanceof XmlElement) {
                    serializeMixedContentElement((XmlElement) childContent, element, content);
                } else if (childContent instanceof XmlText) {
                    serializeMixedContentText((XmlText) childContent, content, true, trailingInfo.trailing());
                }
            }
        } else {
            for (XmlContent childContent : element.getContent()) {
                if (childContent instanceof XmlElement) {
                    serializeMixedContentElement((XmlElement) childContent, element, content);
                } else if (childContent instanceof XmlText) {
                    serializeMixedContentText((XmlText) childContent, content, false, false);
                }
            }
        }
        json.set(style().mixedContentKey(), content);
    }

    private void serializeMixedContentText(XmlText text, ArrayNode content, boolean normalize, boolean trailing) {
        String textAsString = normalize ? text.normalize() : text.get();
        if (normalize && trailing) {
            textAsString += " ";
        }
        content.add(textAsString);
    }

    private void serializeMixedContentElement(XmlElement element, XmlElement parentElement, ArrayNode content)
        throws SerializationException, XsdDetectorException {
        ObjectNode childObject = MAPPER.createObjectNode();
        serializeMixedContentElement(element, childObject, parentElement.getNamespace());
        content.add(childObject);
    }

    private void serializeMixedContentElement(XmlElement element, ObjectNode json, XmlNamespace parentNamespace)
        throws SerializationException, XsdDetectorException {
        // NAME
        json.put(style().mixedContentElementNameKey(), element.getLocalName());

        // NAMESPACES
        handleNamespaces(element, json, parentNamespace);

        // ATTRIBUTES
        handleAttributes(element, json);

        // CHILDREN
        serializeMixedContent(element, json);
    }

}
