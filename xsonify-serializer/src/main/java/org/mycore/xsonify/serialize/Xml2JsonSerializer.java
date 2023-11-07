package org.mycore.xsonify.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mycore.xsonify.serialize.SerializerSettings.NamespaceDeclaration;
import org.mycore.xsonify.serialize.detector.XsdDetectorException;
import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector;
import org.mycore.xsonify.xml.XmlAttribute;
import org.mycore.xsonify.xml.XmlContent;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xml.XmlText;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdAnyException;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;
import org.mycore.xsonify.xsd.node.XsdDatatype;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdNode;
import org.mycore.xsonify.xsd.node.XsdSequence;
import org.mycore.xsonify.xsd.node.XsdSimpleType;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

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
            XmlElement root = document.getRoot();
            SerializationContext rootContext = new SerializationContext(root);
            serializeElement(rootContext);
            if (settings().omitRootElement()) {
                return rootContext.json();
            }
            ObjectNode json = MAPPER.createObjectNode();
            json.set(getName(rootContext), rootContext.json());
            return json;
        } catch (XsdDetectorException | XsdAnyException exception) {
            throw new SerializationException(exception);
        }
    }

    private void serializeElement(SerializationContext context)
        throws SerializationException, XsdDetectorException, XsdAnyException {

        XmlElement element = context.xmlElement();

        // NAMESPACES
        handleNamespaceDeclaration(context);

        // ATTRIBUTES
        handleAttributes(context);

        // MIXED CONTENT
        if (hasMixedContent(context)) {
            handleMixedContent(context);
            return;
        }

        // TEXT
        if (element.hasText()) {
            handleText(context);
        }

        // INDEX
        if (context.parentContext() != null && context.parentContext().useIndex()) {
            context.json().put(style().indexKey(), context.getPositionInParent());
        }

        // CHILDREN
        context.setUseIndex(useIndex(context));
        for (List<SerializationContext> childContextList : context.getGroupedChildren()) {
            SerializationContext firstChildContext = childContextList.get(0);
            String name = getName(firstChildContext);
            JsonNode childNode = useArray(childContextList) ?
                                 serializeChildElements(childContextList) :
                                 serializeChildElement(firstChildContext, name);
            if (childNode != null) {
                context.json().set(name, childNode);
            }
        }
    }

    private ArrayNode serializeChildElements(List<SerializationContext> contextList)
        throws SerializationException, XsdDetectorException, XsdAnyException {
        ArrayNode array = MAPPER.createArrayNode();
        for (SerializationContext childContext : contextList) {
            serializeChildElement(childContext, array);
        }
        return array;
    }

    private JsonNode serializeChildElement(SerializationContext context, String propertyName)
        throws SerializationException, XsdDetectorException, XsdAnyException {
        XmlElement xmlElement = context.xmlElement();
        if (hasPlainText(context)) {
            XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(
                XmlPath.of(xmlElement));
            String text = xmlElement.getTextNormalized();
            SerializationContext parentContext = context.parentContext();
            switch (jsonPrimitive) {
            case BOOLEAN -> parentContext.json().put(propertyName, Boolean.parseBoolean(text));
            case NUMBER -> parentContext.json().put(propertyName, new BigDecimal(text));
            case STRING -> parentContext.json().put(propertyName, getText(xmlElement));
            }
            return null;
        }
        serializeElement(context);
        return context.json();
    }

    private void serializeChildElement(SerializationContext context, ArrayNode jsonArray)
        throws SerializationException, XsdDetectorException, XsdAnyException {
        XmlElement xmlElement = context.xmlElement();
        if (hasPlainText(context)) {
            XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(
                XmlPath.of(xmlElement));
            String text = xmlElement.getTextNormalized();
            switch (jsonPrimitive) {
            case BOOLEAN -> jsonArray.add(Boolean.parseBoolean(text));
            case NUMBER -> jsonArray.add(new BigDecimal(text));
            case STRING -> jsonArray.add(getText(xmlElement));
            }
            return;
        }
        serializeElement(context);
        jsonArray.add(context.json());
    }

    private String getText(XmlElement childElement) {
        return settings().normalizeText() ? childElement.getTextNormalized() : childElement.getText();
    }

    private String getName(SerializationContext context) throws XsdDetectorException {
        XmlElement element = context.xmlElement();
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

    private boolean hasMixedContent(SerializationContext context) throws XsdDetectorException {
        return mixedContentDetector().detect(context.xmlElement());
    }

    private boolean useArray(List<SerializationContext> childContextList) throws XsdDetectorException {
        if (this.repeatableElementDetector() != null) {
            return this.repeatableElementDetector().detect(childContextList.get(0).xmlElement());
        }
        if (SerializerSettings.JsonStructure.ENFORCE_ARRAY.equals(settings().jsonStructure())) {
            return true;
        }
        return childContextList.size() > 1;
    }

    private boolean hasPlainText(SerializationContext context) throws SerializationException {
        XmlElement element = context.xmlElement();
        // ALWAYS WRAP
        if (SerializerSettings.PlainTextHandling.ALWAYS_WRAP.equals(settings().plainTextHandling())) {
            return false;
        }
        // INDEX
        if (context.parentContext() != null && context.parentContext().useIndex()) {
            return false;
        }
        // SIMPLETYPE
        if (isSimpleType(context)) {
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
        NamespaceDeclaration namespaceDeclaration = settings().namespaceDeclaration();
        if (NamespaceDeclaration.OMIT.equals(namespaceDeclaration)) {
            return true;
        }
        if (NamespaceDeclaration.ADD.equals(namespaceDeclaration)) {
            return false;
        }
        return !isChildOfXsAny(element);
    }

    private boolean isSimpleType(SerializationContext context) throws SerializationException {
        XsdElement xsdElement = context.xsdElement().getReferenceOrSelf();
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

    private boolean useIndex(SerializationContext context) {
        boolean hasSequence = context.xsdElement().has(XsdSequence.class, XsdElement.CONTAINER_NODES);
        if (!hasSequence) {
            return false;
        }
        int index = 0;
        for (List<SerializationContext> childContextList : context.getGroupedChildren()) {
            for (SerializationContext childContext : childContextList) {
                if (childContext.positionInParent != index++) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleNamespaceDeclaration(SerializationContext context) {
        NamespaceDeclaration namespaceDeclaration = settings().namespaceDeclaration();
        if (NamespaceDeclaration.OMIT.equals(namespaceDeclaration) ||
            (NamespaceDeclaration.ADD_IF_XS_ANY.equals(namespaceDeclaration) && !isChildOfXsAny(
                context.xmlElement()))) {
            return;
        }
        // xmlElement namespace
        XmlNamespace namespace = context.xmlElement().getNamespace();
        if (!namespace.equals(context.parentNamespace()) && !namespace.prefix().isEmpty()) {
            context.json().put(style().namespacePrefixKey(), namespace.prefix());
        }
        // introduced namespaces
        context.xmlElement()
            .getNamespacesIntroduced()
            .values()
            .forEach(ns ->
                context.json().put(getXmlnsPrefix(ns), ns.uri())
            );
    }

    private void handleText(SerializationContext context) {
        XsdJsonPrimitiveDetector.JsonPrimitive jsonPrimitive = jsonPrimitiveDetector().detect(
            XmlPath.of(context.xmlElement())
        );
        String text = context.xmlElement().getTextNormalized();
        switch (jsonPrimitive) {
        case BOOLEAN -> context.json().put(style().textKey(), Boolean.parseBoolean(text));
        case NUMBER -> context.json().put(style().textKey(), new BigDecimal(text));
        case STRING -> context.json().put(style().textKey(), getText(context.xmlElement()));
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

    private void handleAttributes(SerializationContext context) throws XsdDetectorException {
        for (XmlAttribute attribute : context.xmlElement().getAttributes()) {
            handleAttribute(attribute, context.json());
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

    private void handleMixedContent(SerializationContext context)
        throws SerializationException, XsdDetectorException, XsdAnyException {
        if (SerializerSettings.MixedContentHandling.UTF_8_ENCODING.equals(settings().mixedContentHandling())) {
            String mixedContentAsString = context.xmlElement().encodeContent(StandardCharsets.UTF_8);
            if (!mixedContentAsString.isEmpty()) {
                context.json().put(style().mixedContentKey(), mixedContentAsString);
            }
            return;
        }
        serializeMixedContent(context);
    }

    private void serializeMixedContent(SerializationContext context)
        throws SerializationException, XsdDetectorException, XsdAnyException {
        ArrayNode content = MAPPER.createArrayNode();
        if (settings().normalizeText()) {
            for (XmlElement.TrailingInfo trailingInfo : context.xmlElement().trailingContent()) {
                XmlContent childContent = trailingInfo.content();
                if (childContent instanceof XmlElement) {
                    serializeMixedContentElement(context, (XmlElement) childContent, content);
                } else if (childContent instanceof XmlText) {
                    serializeMixedContentText((XmlText) childContent, content, true, trailingInfo.trailing());
                }
            }
        } else {
            for (XmlContent childContent : context.xmlElement().getContent()) {
                if (childContent instanceof XmlElement) {
                    serializeMixedContentElement(context, (XmlElement) childContent, content);
                } else if (childContent instanceof XmlText) {
                    serializeMixedContentText((XmlText) childContent, content, false, false);
                }
            }
        }
        context.json().set(style().mixedContentKey(), content);
    }

    private void serializeMixedContentText(XmlText text, ArrayNode content, boolean normalize, boolean trailing) {
        String textAsString = normalize ? text.normalize() : text.get();
        if (normalize && trailing) {
            textAsString += " ";
        }
        content.add(textAsString);
    }

    private void serializeMixedContentElement(SerializationContext parentContext, XmlElement element, ArrayNode content)
        throws SerializationException, XsdDetectorException, XsdAnyException {
        SerializationContext context = new SerializationContext(element, parentContext);
        serializeMixedContentElement(context);
        content.add(context.json());
    }

    private void serializeMixedContentElement(SerializationContext context)
        throws SerializationException, XsdDetectorException, XsdAnyException {
        // NAME
        String name = getName(context);
        context.json().put(style().mixedContentElementNameKey(), name);

        // NAMESPACES
        handleNamespaceDeclaration(context);

        // ATTRIBUTES
        handleAttributes(context);

        // CHILDREN
        serializeMixedContent(context);
    }

    private class SerializationContext {

        private final XmlElement xmlElement;
        private final SerializationContext parent;
        private XsdElement xsdElement;
        private final ObjectNode json;
        private final Integer positionInParent;
        private List<SerializationContext> children;
        private Collection<List<SerializationContext>> groupedChildren;
        private boolean useIndex;

        public SerializationContext(XmlElement element) {
            this(element, null);
        }

        public SerializationContext(XmlElement element, SerializationContext parentContext) {
            this.xmlElement = element;
            this.parent = parentContext;
            this.children = null;
            this.groupedChildren = null;
            try {
                this.xsdElement = xsd().resolveXmlElement(element);
            } catch (XsdAnyException anyException) {
                this.xsdElement = null;
            }
            this.json = MAPPER.createObjectNode();
            this.positionInParent = parentContext != null ? parentContext().xmlElement().indexOfElement(element) : null;
            this.useIndex = false;
        }

        public XmlElement xmlElement() {
            return xmlElement;
        }

        public SerializationContext parentContext() {
            return parent;
        }

        public XsdElement xsdElement() {
            return xsdElement;
        }

        public ObjectNode json() {
            return json;
        }

        public XmlNamespace parentNamespace() {
            return parent != null ? parent.getNamespace() : null;
        }

        public XmlNamespace getNamespace() {
            return xmlElement.getNamespace();
        }

        public Integer getPositionInParent() {
            return positionInParent;
        }

        public List<SerializationContext> getChildren() {
            if (this.children == null) {
                buildChildren();
            }
            return this.children;
        }

        public Collection<List<SerializationContext>> getGroupedChildren() {
            if (this.groupedChildren == null) {
                buildGroupedChildren();
            }
            return this.groupedChildren;
        }

        public boolean useIndex() {
            return this.useIndex;
        }

        public void setUseIndex(boolean useIndex) {
            this.useIndex = useIndex;
        }

        private void buildChildren() {
            this.children = new ArrayList<>();
            for (XmlElement childElement : xmlElement.getElements()) {
                SerializationContext childContext = new SerializationContext(childElement, this);
                this.children.add(childContext);
            }
        }

        private void buildGroupedChildren() {
            LinkedHashMap<XmlName, List<SerializationContext>> map = new LinkedHashMap<>();
            for (SerializationContext context : getChildren()) {
                List<SerializationContext> contextList = map.computeIfAbsent(
                    context.xmlElement().getName(), k -> new ArrayList<>()
                );
                contextList.add(context);
            }
            this.groupedChildren = map.values();
        }

        @Override
        public String toString() {
            return xmlElement.toPrettyXml();
        }
    }

}
