package org.mycore.xsonify.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import org.mycore.xsonify.xsd.XsdNode;
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

    public JsonObject serialize(XmlDocument document) throws SerializationException {
        try {
            JsonObject jsonContent = new JsonObject();
            serializeElement(document.getRoot(), jsonContent, null);
            if (settings().omitRootElement()) {
                return jsonContent;
            }
            JsonObject json = new JsonObject();
            json.add(getName(document.getRoot()), jsonContent);
            return json;
        } catch (XsdDetectorException detectorException) {
            throw new SerializationException(detectorException);
        }
    }

    private void serializeElement(XmlElement element, JsonObject json, XmlNamespace parentNamespace)
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
            List<JsonElement> jsonContent = serializeChildElements(xmlContent, element.getNamespace());
            String name = getName(xmlContent);
            if (useArray(xmlContent)) {
                JsonArray arr = new JsonArray();
                jsonContent.forEach(arr::add);
                json.add(name, arr);
            } else if (jsonContent.size() == 1) {
                json.add(name, jsonContent.get(0));
            } else {
                throw new SerializationException(
                    "Expected exactly one occurrence of " + name + ". But found an additional.");
            }
        }
    }

    private List<JsonElement> serializeChildElements(List<XmlElement> children, XmlNamespace parentNamespace)
        throws SerializationException, XsdDetectorException {
        List<JsonElement> jsonList = new ArrayList<>();
        for (XmlElement childElement : children) {
            jsonList.add(serializeChildElement(childElement, parentNamespace));
        }
        return jsonList;
    }

    private JsonElement serializeChildElement(XmlElement element, XmlNamespace parentNamespace)
        throws SerializationException, XsdDetectorException {
        if (hasPlainText(element)) {
            XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(XmlPath.of(element));
            String text = element.getTextNormalized();
            return switch (jsonPrimitive) {
                case BOOLEAN -> new JsonPrimitive(Boolean.parseBoolean(text));
                case NUMBER -> new JsonPrimitive(new BigDecimal(text));
                case STRING -> new JsonPrimitive(getText(element));
            };
        }
        JsonObject childObject = new JsonObject();
        serializeElement(element, childObject, parentNamespace);
        return childObject;
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
            return isSimpleType(xsdElement);
        } catch (XsdAnyException xsdAnyException) {
            return false;
        }
    }

    private Boolean isSimpleType(XsdNode xsdNode) throws SerializationException {
        if (xsdNode == null) {
            return null;
        }
        // check if we already found a simpletype
        if (XsdSimpleType.TYPE.equals(xsdNode.getType())) {
            return true;
        }
        // check @type
        String type = xsdNode.getElement().getAttribute("type", XmlNamespace.EMPTY.uri());
        if (type != null) {
            if (XsdBuiltInDatatypes.is(XmlExpandedName.of(type))) {
                return true;
            }
            XsdNode linkedNode = xsdNode.getLinkedNode();
            if (linkedNode != null) {
                return isSimpleType(linkedNode);
            }
            throw new SerializationException("Unable to @type '" + type + "'.");
        }
        // check first child
        if (xsdNode.getChildren().isEmpty()) {
            throw new SerializationException("XsdNode has neither @type nor any children '" + xsdNode + "'.");
        }
        XsdNode child = xsdNode.getChildren().get(0);
        // TODO extension/restriction
        return XsdSimpleType.TYPE.equals(child.getType());
    }

    private void handleNamespaces(XmlElement element, JsonObject json, XmlNamespace parentNamespace) {
        NamespaceHandling namespaceHandling = settings().namespaceHandling();
        if (NamespaceHandling.OMIT.equals(namespaceHandling) ||
            (NamespaceHandling.ADD_IF_XS_ANY.equals(namespaceHandling) && !isChildOfXsAny(element))) {
            return;
        }
        // element namespace
        XmlNamespace namespace = element.getNamespace();
        if (!namespace.equals(parentNamespace) && !namespace.prefix().isEmpty()) {
            json.addProperty(style().namespacePrefixKey(), namespace.prefix());
        }
        // introduced namespaces
        element.getNamespacesIntroduced().values().forEach(ns -> json.addProperty(getXmlnsPrefix(ns), ns.uri()));
    }

    private void handleText(XmlElement element, JsonObject json) {
        XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(XmlPath.of(element));
        String text = element.getTextNormalized();
        switch (jsonPrimitive) {
        case BOOLEAN -> json.addProperty(style().textKey(), Boolean.parseBoolean(text));
        case NUMBER -> json.addProperty(style().textKey(), new BigDecimal(text));
        case STRING -> json.addProperty(style().textKey(), getText(element));
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

    private void handleAttributes(XmlElement element, JsonObject json) throws XsdDetectorException {
        for (XmlAttribute attribute : element.getAttributes()) {
            handleAttribute(attribute, json);
        }
    }

    private void handleAttribute(XmlAttribute attribute, JsonObject json) throws XsdDetectorException {
        final String attributeName = getAttributeName(attribute);
        final String attributeValue = attribute.getValue();
        XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(XmlPath.of(attribute));
        switch (jsonPrimitive) {
        case BOOLEAN -> json.addProperty(attributeName, Boolean.parseBoolean(attributeValue));
        case NUMBER -> json.addProperty(attributeName, new BigDecimal(attributeValue));
        case STRING -> json.addProperty(attributeName, attributeValue);
        }
    }

    private void handleMixedContent(XmlElement element, JsonObject json)
        throws SerializationException, XsdDetectorException {
        if (SerializerSettings.MixedContentHandling.UTF_8_ENCODING.equals(settings().mixedContentHandling())) {
            String mixedContentAsString = element.encodeContent(StandardCharsets.UTF_8);
            if (!mixedContentAsString.isEmpty()) {
                json.addProperty(style().mixedContentKey(), mixedContentAsString);
            }
            return;
        }
        serializeMixedContent(element, json);
    }

    private void serializeMixedContent(XmlElement element, JsonObject json)
        throws SerializationException, XsdDetectorException {
        JsonArray content = new JsonArray();
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
        json.add(style().mixedContentKey(), content);
    }

    private void serializeMixedContentText(XmlText text, JsonArray content, boolean normalize, boolean trailing) {
        String textAsString = normalize ? text.normalize() : text.get();
        if (normalize && trailing) {
            textAsString += " ";
        }
        content.add(textAsString);
    }

    private void serializeMixedContentElement(XmlElement element, XmlElement parentElement, JsonArray content)
        throws SerializationException, XsdDetectorException {
        JsonObject childObject = new JsonObject();
        serializeMixedContentElement(element, childObject, parentElement.getNamespace());
        content.add(childObject);
    }

    private void serializeMixedContentElement(XmlElement element, JsonObject json, XmlNamespace parentNamespace)
        throws SerializationException, XsdDetectorException {
        // NAME
        json.addProperty(style().mixedContentElementNameKey(), element.getLocalName());

        // NAMESPACES
        handleNamespaces(element, json, parentNamespace);

        // ATTRIBUTES
        handleAttributes(element, json);

        // CHILDREN
        serializeMixedContent(element, json);
    }

}
