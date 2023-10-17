package org.mycore.xsonify.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mycore.xsonify.serialize.SerializerSettings.AdditionalNamespaceDeclarationStrategy;
import org.mycore.xsonify.serialize.SerializerSettings.NamespaceDeclaration;
import org.mycore.xsonify.serialize.SerializerSettings.XsAnyNamespaceStrategy;
import org.mycore.xsonify.xml.XmlContent;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlException;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlNamespaceDeclarationAncestorStrategy;
import org.mycore.xsonify.xml.XmlNamespaceDeclarationRootStrategy;
import org.mycore.xsonify.xml.XmlNamespaceDeclarationStrategy;
import org.mycore.xsonify.xml.XmlQualifiedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdAnyException;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class Json2XmlSerializer extends SerializerBase {

    private XmlName rootName;

    private final Map<String, XmlNamespace> namespaceMap;

    public Json2XmlSerializer(Xsd xsd) throws SerializationException {
        this(xsd, new SerializerSettings());
    }

    public Json2XmlSerializer(Xsd xsd, SerializerSettings settings) throws SerializationException {
        this(xsd, settings, new SerializerStyle());
    }

    public Json2XmlSerializer(Xsd xsd, SerializerSettings settings, SerializerStyle style) throws
        SerializationException {
        super(xsd, settings, style);
        this.rootName = null;
        this.namespaceMap = new LinkedHashMap<>();
    }

    public void setRootName(XmlName rootName) {
        this.rootName = rootName;
    }

    public XmlName getRootName() {
        return rootName;
    }

    public Map<String, XmlNamespace> getNamespaceMap() {
        return namespaceMap;
    }

    public Json2XmlSerializer setNamespaces(List<XmlNamespace> namespaces) {
        this.namespaceMap.clear();
        namespaces.forEach(namespace -> this.namespaceMap.put(namespace.prefix(), namespace));
        return this;
    }

    public Json2XmlSerializer addNamespace(XmlNamespace namespace) {
        this.namespaceMap.put(namespace.prefix(), namespace);
        return this;
    }

    public XmlDocument serialize(ObjectNode jsonObject) throws SerializationException {
        XmlDocument xmlDocument = createXmlDocument(jsonObject);
        optimizeNamespaceDeclaration(xmlDocument);
        return xmlDocument;
    }

    private XmlDocument createXmlDocument(ObjectNode json) throws SerializationException {
        ObjectNode rootValue = getRootValue(json);
        SerializationNode serializationNode = toJsonNode(rootValue);
        XsdElement xsdElement = getRootXsdNode(json, serializationNode);
        XmlName rootName = getRootName() != null ? getRootName() : getName(xsdElement, null);
        XmlDocument xmlDocument = new XmlDocument();
        XmlElement rootElement = new XmlElement(rootName, xmlDocument);
        xmlDocument.setRoot(rootElement);
        SerializationContext rootContext = new SerializationContext(null, rootElement, xsdElement, serializationNode);
        handleObject(rootContext);
        return xmlDocument;
    }

    private void optimizeNamespaceDeclaration(XmlDocument xmlDocument) throws SerializationException {
        NamespaceDeclaration namespaceDeclaration = settings().namespaceDeclaration();
        if (NamespaceDeclaration.ADD.equals(namespaceDeclaration)) {
            // no need to optimize, namespace information comes from the json source, and we will keep it that way
            return;
        }
        AdditionalNamespaceDeclarationStrategy strategySetting = settings().additionalNamespaceDeclarationStrategy();
        if (AdditionalNamespaceDeclarationStrategy.NONE.equals(strategySetting)) {
            // no need to optimize
            return;
        }
        // choose between MOVE_TO_ROOT and MOVE_TO_ANCESTOR
        XmlNamespaceDeclarationStrategy strategy =
            AdditionalNamespaceDeclarationStrategy.MOVE_TO_ROOT.equals(strategySetting) ?
            new XmlNamespaceDeclarationRootStrategy() :
            new XmlNamespaceDeclarationAncestorStrategy();
        try {
            strategy.apply(xmlDocument);
        } catch (XmlException e) {
            throw new SerializationException("Unable to apply namespace declaration strategy to xml document", e);
        }
    }

    private void serializeElement(String jsonKey, JsonNode jsonValue, SerializationContext parentContext)
        throws SerializationException {
        if (jsonValue.isArray()) {
            serializeArray(jsonKey, (ArrayNode) jsonValue, parentContext);
        } else if (jsonValue.isObject()) {
            SerializationNode serializationNode = toJsonNode((ObjectNode) jsonValue);
            serializeNode(jsonKey, serializationNode, parentContext);
        } else if (jsonValue.isValueNode()) {
            SerializationNode serializationNode = new SerializationNode();
            serializationNode.text = jsonValue.asText();
            serializeNode(jsonKey, serializationNode, parentContext);
        } else {
            throw new SerializationException("Unable to serialize '" + jsonKey + "'.");
        }
    }

    private void serializeNode(String jsonKey, SerializationNode serializationNode, SerializationContext parentContext)
        throws SerializationException {
        XsdElement xsdNode = getXsdElement(jsonKey, serializationNode, parentContext);
        XmlName name = getName(xsdNode, jsonKey);
        XmlElement element = new XmlElement(name, parentContext.getDocument());
        SerializationContext context = new SerializationContext(parentContext, element, xsdNode, serializationNode);
        parentContext.children().add(context);
        handleObject(context);
    }

    private void serializeArray(String jsonKey, ArrayNode jsonArray, SerializationContext parentContext)
        throws SerializationException {
        for (JsonNode childJson : jsonArray) {
            serializeElement(jsonKey, childJson, parentContext);
        }
    }

    private void serializeMixedContent(JsonNode mixedContent, SerializationContext parentContext)
        throws SerializationException {
        XmlElement parentElement = parentContext.element();
        if (mixedContent.isValueNode()) {
            String contentAsString = mixedContent.asText();
            try {
                List<XmlContent> xmlContentList = XmlElement.decodeContent(contentAsString, StandardCharsets.UTF_8,
                    parentElement.getNamespacesInScope().values());
                xmlContentList.forEach(parentElement::add);
            } catch (Exception exc) {
                throw new SerializationException("Unable to serialize mixed content: " + contentAsString, exc);
            }
        } else if (mixedContent.isArray()) {
            serializeMixedContent((ArrayNode) mixedContent, parentContext);
        } else {
            throw new SerializationException("Unable to serialize mixed content: " + mixedContent);
        }
    }

    private void serializeMixedContent(ArrayNode mixedContentArray, SerializationContext parentContext)
        throws SerializationException {
        XmlElement parentElement = parentContext.element();
        for (JsonNode mixedContent : mixedContentArray) {
            if (mixedContent.isValueNode()) {
                parentElement.addText(mixedContent.asText());
            } else if (mixedContent.isObject()) {
                serializeMixedContent((ObjectNode) mixedContent, parentContext);
            }
        }
    }

    private void serializeMixedContent(ObjectNode jsonValue, SerializationContext parentContext)
        throws SerializationException {
        String jsonKey = jsonValue.get(style().mixedContentElementNameKey()).asText();
        serializeElement(jsonKey, jsonValue, parentContext);
    }

    private SerializationNode toJsonNode(ObjectNode jsonObject) {
        SerializationNode serializationNode = new SerializationNode();
        for (Map.Entry<String, JsonNode> entry : jsonObject.properties()) {
            if(entry.getKey().equals(style().mixedContentElementNameKey())) {
                serializationNode.namespacePrefix = XmlQualifiedName.of(entry.getValue().asText()).prefix();
            } else if (entry.getKey().equals(style().namespacePrefixKey())) {
                serializationNode.namespacePrefix = entry.getValue().asText();
            } else if (entry.getKey().startsWith(style().xmlnsPrefix())) {
                XmlNamespace namespace = getNamespace(entry.getKey(), entry.getValue());
                serializationNode.namespaces.put(namespace.prefix(), namespace);
            } else if (entry.getKey().startsWith(style().attributePrefix())) {
                String attributeName = entry.getKey().substring(style().attributePrefix().length());
                serializationNode.attributes.put(attributeName, entry.getValue().asText());
            } else if (entry.getKey().equals(style().textKey())) {
                serializationNode.text = entry.getValue().asText();
            } else if (entry.getKey().equals(style().mixedContentKey())) {
                serializationNode.mixedContent = entry.getValue();
            } else {
                serializationNode.children.put(entry.getKey(), entry.getValue());
            }
        }
        return serializationNode;
    }

    private void handleObject(SerializationContext context) throws SerializationException {
        // add element to parent
        // TODO remove when preserving order is implemented (context has child/parent relation which should be used)
        XmlElement parentElement = context.getParentElement();
        if (parentElement != null) {
            parentElement.addElement(context.element());
        }

        // namespaces & attributes & text
        handleNamespace(context);
        handleAdditionalNamespaces(context);
        handleAttributes(context);
        handleText(context);

        // mixed content
        JsonNode mixedContent = context.jsonNode().getMixedContent();
        if (mixedContent != null) {
            serializeMixedContent(mixedContent, context);
            return;
        }

        // children
        for (Map.Entry<String, JsonNode> child : context.jsonNode().getChildren().entrySet()) {
            serializeElement(child.getKey(), child.getValue(), context);
        }
        // TODO order xs:sequence nodes
    }

    private void handleNamespace(SerializationContext context) {
        XmlElement element = context.element();
        if (useEmptyNamespaceForXsAny(context)) {
            element.setNamespace(XmlNamespace.EMPTY);
            return;
        }
        if (element.getNamespace() != null) {
            return;
        }
        String uri = context.xsdElement() != null ? context.xsdElement().getUri() :
                     getUri(context.serializationNode, context.parentContext);
        String prefix = context.jsonNode().getNamespacePrefix();
        if (prefix != null && uri != null) {
            element.setNamespace(new XmlNamespace(prefix, uri));
            return;
        }
        element.setNamespace(getNamespace(context, uri));
    }

    private boolean useEmptyNamespaceForXsAny(SerializationContext context) {
        boolean isChildOfXsAny =
            context.xsdElement() == null &&
                context.parentContext().xsdElement() != null &&
                context.parentContext().xsdElement().hasAny();
        if (!isChildOfXsAny) {
            return false;
        }
        NamespaceDeclaration namespaceDeclaration = settings().namespaceDeclaration();
        if (NamespaceDeclaration.OMIT.equals(namespaceDeclaration)) {
            return XsAnyNamespaceStrategy.USE_EMPTY.equals(settings().xsAnyNamespaceStrategy());
        }
        String prefix = context.jsonNode().getNamespacePrefix();
        return prefix == null || XmlNamespace.EMPTY.prefix().equals(prefix);
    }

    private void handleAdditionalNamespaces(SerializationContext context) {
        XmlElement element = context.element();
        String currentNamespacePrefix = element.getNamespace().prefix();
        context.jsonNode().getNamespaces().forEach((prefix, namespace) -> {
            if (!prefix.equals(currentNamespacePrefix)) {
                element.setAdditionalNamespace(namespace);
            }
        });
    }

    private void handleAttributes(SerializationContext context) throws SerializationException {
        for (Map.Entry<String, String> entry : context.jsonNode().getAttributes().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            handleAttribute(context, name, value);
        }
    }

    private void handleAttribute(SerializationContext context, String attributeName, String attributeValue)
        throws SerializationException {
        XmlElement element = context.element();
        // handle attributes with prefix
        if (attributeName.contains(":")) {
            String[] split = attributeName.split(":");
            String prefix = split[0];
            String localName = split[1];
            XmlNamespace namespace = getNamespace(element, prefix);
            if (namespace == null) {
                throw new SerializationException("Undeclared '" + prefix + "' namespace prefix found. Please add" +
                    " the prefix to the namespace map. Otherwise serialisation is not possible.");
            }
            element.setAttribute(localName, attributeValue, namespace);
            return;
        }
        // handle xs:any attributes
        if (context.xsdElement() == null) {
            element.setAttribute(attributeName, attributeValue, XmlNamespace.EMPTY);
            return;
        }
        // get xsd attribute nodes -> in best case there should be only one
        List<XsdAttribute> attributeNodes = XsdAttribute.resolveReferences(
            context.xsdElement().collectAttributes(attributeName));
        String elementNamespace = context.element().getNamespace().uri();
        if (attributeNodes.size() > 1) {
            attributeNodes = attributeNodes.stream()
                .filter(attributeNode -> elementNamespace.equals(attributeNode.getUri()))
                .toList();
            if (attributeNodes.size() > 1) {
                throw new SerializationException(
                    "Multiple XSD attribute definitions found for '" + attributeName + "' in '" +
                        element.getName() + "'. Unable to determine which one to choose: " + attributeNodes.stream()
                        .map(XsdNode::getName)
                        .map(XmlExpandedName::toString)
                        .collect(Collectors.joining(",")));
            }
        }
        if (attributeNodes.isEmpty()) {
            throw new SerializationException("Invalid '" + attributeName + "' attribute found in " + element.getName());
        }
        // assign attribute -> determine if prefix is required based on the element namespace
        XsdNode attributeNode = attributeNodes.get(0);
        String attributeNamespaceUri = attributeNode.getUri();
        XmlNamespace attributeNamespace = elementNamespace.equals(attributeNamespaceUri) ?
                                          XmlNamespace.EMPTY :
                                          getNamespace(context, attributeNamespaceUri);
        element.setAttribute(attributeName, attributeValue, attributeNamespace);
    }

    private void handleText(SerializationContext context) {
        XmlElement element = context.element();
        SerializationNode serializationNode = context.jsonNode();
        if (serializationNode.getText() != null) {
            element.setText(serializationNode.getText());
        }
    }

    /**
     * Searches the namespace map and the context for a namespace based on the given uri.
     * TODO: be aware that this uses getInheritedNamespaces() if xmlElement. If switched to retain order of elements
     *  using the context nodes, this will not work! Have to go through the context instead of the elements.
     *
     * @param context current context
     * @param uri     uri of the namespace
     * @return namespace or null
     */
    private XmlNamespace getNamespace(SerializationContext context, String uri) {
        if (uri == null || uri.isEmpty()) {
            return XmlNamespace.EMPTY;
        }
        // check preset
        XmlNamespace namespace = getNamespaceMap().values().stream()
            .filter(xmlNamespace -> xmlNamespace.uri().equals(uri))
            .findFirst()
            .orElse(null);
        if (namespace != null) {
            return namespace;
        }
        // check parent elements with same uri
        XmlElement parentElement = context.getParentElement();
        if (parentElement != null) {
            namespace = parentElement.getNamespacesInScope().values().stream()
                .filter(ns -> uri.equals(ns.uri()))
                .findFirst()
                .orElse(null);
            if (namespace != null) {
                return namespace;
            }
        }
        return new XmlNamespace("", uri);
    }

    /**
     * Gets the root name for the given json object.
     * <ul>
     *     <li>if omitRootElement() is set to false, the first entry is taken as root</li>
     * </ul>
     *
     * @param rootJson the json root object
     * @return the name of the root
     * @throws SerializationException if neither the root name is set manually, nor omitRootElement is set to false
     */
    public String getRootName(ObjectNode rootJson) throws SerializationException {
        // root name is not determinable
        if (settings().omitRootElement()) {
            throw new SerializationException("Cannot determine xml root name. Either use 'setRootName()' manually, " +
                "or provide a json where the settings 'omitRootElement()' is set to false.");
        }
        // root name is defined in the first json object of the given root json
        Set<Map.Entry<String, JsonNode>> entries = rootJson.properties();
        if (entries.isEmpty()) {
            throw new SerializationException(rootJson + " doesn't have a root node");
        }
        Map.Entry<String, JsonNode> rootEntry = entries.iterator().next();
        JsonNode rootElement = rootEntry.getValue();
        if (!rootElement.isObject()) {
            throw new SerializationException("root '" + rootEntry.getKey() +
                "' should be an object {} but is " + rootElement);
        }
        return rootEntry.getKey();
    }

    private XmlName getName(XsdNode xsdNode, String jsonKey) {
        if (xsdNode == null && jsonKey == null) {
            throw new NullPointerException("Unable to get name of element.");
        }
        if (xsdNode != null) {
            return new XmlName(xsdNode.getLocalName(), null);
        }
        return new XmlName(XmlQualifiedName.of(jsonKey).localName(), null);
    }

    private ObjectNode getRootValue(ObjectNode json) {
        return settings().omitRootElement() ?
               json :
               (ObjectNode) json.iterator().next();
    }

    private XsdElement getRootXsdNode(ObjectNode rootJson, SerializationNode serializationNode)
        throws SerializationException {
        if (getRootName() != null) {
            XsdElement rootNode = xsd().getNamedNode(XsdElement.class, getRootName());
            if (rootNode != null) {
                return rootNode;
            }
            throw new SerializationException("Unable to find root node '" + getRootName() + "' in xsd definition");
        }
        String jsonKey = getRootName(rootJson);
        String localName = XmlQualifiedName.of(jsonKey).localName();
        List<XsdElement> candidates = xsd().getNamedNodes(XsdElement.class, localName);
        if (candidates.isEmpty()) {
            throw new SerializationException("Unable to find root node '" + localName + "' in xsd definition");
        }
        XsdElement xsdElement = getXsdElement(serializationNode, null, candidates);
        if (xsdElement == null) {
            throw new SerializationException("Unable to find root node '" + localName + "' in xsd definition");
        }
        return xsdElement;
    }

    private XsdElement getXsdElement(String jsonKey, SerializationNode serializationNode,
        SerializationContext parentContext)
        throws SerializationException {
        String localName = XmlQualifiedName.of(jsonKey).localName();
        // xs:any check
        if (parentContext.xsdElement() == null) {
            return null;
        }
        // collect
        List<XsdElement> candidates = parentContext.xsdElement().collectElements().stream()
            .map(XsdElement::getReferenceOrSelf)
            .filter(xsdChildNode -> xsdChildNode.getLocalName().equals(localName))
            .distinct()
            .toList();
        if (candidates.isEmpty()) {
            // in case the parent element has a xs:any element
            if (parentContext.xsdElement().hasAny()) {
                return null;
            }
            // not found -> can't recover from this
            throw new SerializationException("'" + localName + "' is not a valid child of '" +
                parentContext.xsdElement().getLocalName() + "'.");
        }
        XsdElement xsdElement = getXsdElement(serializationNode, parentContext, candidates);
        if (xsdElement == null) {
            throw new SerializationException("Multiple element definitions of '" + localName + "' found in " +
                parentContext.xsdElement().getLocalName() + ". Getting the XsdNode is therefore ambiguous.");
        }
        return xsdElement;
    }

    private XsdElement getXsdElement(SerializationNode serializationNode, SerializationContext parentContext,
        List<XsdElement> candidates) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        String uri = getUri(serializationNode, parentContext);
        if (uri != null) {
            return candidates.stream()
                .filter(xsdNode -> xsdNode.getReferenceOrSelf().getUri().equals(uri))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    private String getUri(SerializationNode serializationNode, SerializationContext parentContext) {
        String prefix = serializationNode.getNamespacePrefix();
        // take prefix from parent if jsonPrefix is null
        if (prefix == null && parentContext != null) {
            prefix = parentContext.element().getPrefix();
        }
        // namespace is set
        XmlNamespace namespace = getNamespaceMap().get(prefix);
        if (namespace != null) {
            return namespace.uri();
        }
        // check namespace json object itself
        if (!NamespaceDeclaration.OMIT.equals(settings().namespaceDeclaration())) {
            namespace = serializationNode.getNamespaces().get(prefix);
            if (namespace != null) {
                return namespace.uri();
            }
        }
        // check parent element
        XmlElement parentElement = parentContext != null ? parentContext.element : null;
        if (parentElement != null) {
            namespace = parentElement.getNamespace(prefix);
            if (namespace != null) {
                return namespace.uri();
            }
        }
        // collect from xsd
        Set<XmlNamespace> xmlNamespaces = xsd().collectNamespaces(prefix);
        if (xmlNamespaces.size() == 1) {
            return xmlNamespaces.iterator().next().uri();
        }
        return null;
    }

    private XmlNamespace getNamespace(String jsonKey, JsonNode jsonNode) {
        String prefix = style().xmlnsPrefix().equals(jsonKey) ? "" :
                        jsonKey.substring(style().xmlnsPrefix().length() + 1);
        String uri = jsonNode.asText();
        return new XmlNamespace(prefix, uri);
    }

    private XmlNamespace getNamespace(XmlElement element, String prefix) throws SerializationException {
        // check namespace of element
        XmlNamespace namespace = element.getNamespace(prefix);
        if (namespace != null) {
            return namespace;
        }
        // check predefined namespaces
        namespace = namespaceMap.get(prefix);
        if (namespace != null) {
            return namespace;
        }
        // check xsd
        try {
            XsdElement node = xsd().resolveXmlElement(element);
            return node.getElement().getNamespacesInScope().get(prefix);
        } catch (NoSuchElementException noSuchElementException) {
            throw new SerializationException("Unable to determine xsd node path for " + element,
                noSuchElementException);
        } catch (XsdAnyException xsdAnyException) {
            // TODO: is this correct?
            return XmlNamespace.EMPTY;
        }
    }

    private static final class SerializationContext {

        private final SerializationContext parentContext;
        private final XmlElement element;
        private final XsdElement xsdElement;
        private final SerializationNode serializationNode;
        private final List<SerializationContext> children;

        private SerializationContext(SerializationContext parentContext, XmlElement element, XsdElement xsdElement,
            SerializationNode serializationNode) {
            this.parentContext = parentContext;
            this.element = element;
            this.xsdElement = xsdElement;
            this.serializationNode = serializationNode;
            this.children = new ArrayList<>();
        }

        public XmlDocument getDocument() {
            return element.getDocument();
        }

        public XmlElement getParentElement() {
            return parentContext != null ? parentContext.element() : null;
        }

        @Override
        public String toString() {
            return element().getName().toString();
        }

        public SerializationContext parentContext() {
            return parentContext;
        }

        public XmlElement element() {
            return element;
        }

        public XsdElement xsdElement() {
            return xsdElement;
        }

        public SerializationNode jsonNode() {
            return serializationNode;
        }

        public List<SerializationContext> children() {
            return children;
        }

    }

    private static class SerializationNode {

        private String namespacePrefix;
        private String text;
        private JsonNode mixedContent;
        private final Map<String, JsonNode> children;
        private final Map<String, XmlNamespace> namespaces;
        private final Map<String, String> attributes;

        public SerializationNode() {
            this.namespacePrefix = null;
            this.children = new LinkedHashMap<>();
            this.namespaces = new LinkedHashMap<>();
            this.attributes = new LinkedHashMap<>();
            this.text = null;
            this.mixedContent = null;
        }

        public String getNamespacePrefix() {
            return namespacePrefix;
        }

        public Map<String, JsonNode> getChildren() {
            return children;
        }

        public Map<String, XmlNamespace> getNamespaces() {
            return namespaces;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public String getText() {
            return text;
        }

        public JsonNode getMixedContent() {
            return mixedContent;
        }

    }

}
