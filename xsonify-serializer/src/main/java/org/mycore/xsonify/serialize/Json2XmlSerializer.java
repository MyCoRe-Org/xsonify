package org.mycore.xsonify.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.mycore.xsonify.serialize.SerializerSettings.AdditionalNamespaceDeclarationStrategy;
import org.mycore.xsonify.serialize.SerializerSettings.NamespaceHandling;
import org.mycore.xsonify.serialize.SerializerSettings.XsAnyNamespaceStrategy;
import org.mycore.xsonify.xml.XmlContent;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlNamespaceDeclarationAncestorStrategy;
import org.mycore.xsonify.xml.XmlNamespaceDeclarationRootStrategy;
import org.mycore.xsonify.xml.XmlNamespaceDeclarationStrategy;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xml.XmlQualifiedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Json2XmlSerializer extends SerializerBase {

    private XmlName rootName;

    private final Map<String, XmlNamespace> namespaceMap;

    public Json2XmlSerializer(Xsd xsd) {
        this(xsd, new SerializerSettings());
    }

    public Json2XmlSerializer(Xsd xsd, SerializerSettings settings) {
        this(xsd, settings, new SerializerStyle());
    }

    public Json2XmlSerializer(Xsd xsd, SerializerSettings settings, SerializerStyle style) {
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

    public XmlDocument serialize(JsonObject json) {
        XmlDocument xmlDocument = createXmlDocument(json);
        optimizeNamespaceDeclaration(xmlDocument);
        return xmlDocument;
    }

    private XmlDocument createXmlDocument(JsonObject json) {
        JsonObject rootValue = getRootValue(json);
        JsonNode jsonNode = toJsonNode(rootValue);
        XsdNode xsdNode = getRootXsdNode(json, jsonNode);
        XmlName rootName = getRootName() != null ? getRootName() : getName(xsdNode, null);
        XmlDocument xmlDocument = new XmlDocument();
        XmlElement rootElement = new XmlElement(rootName, xmlDocument);
        xmlDocument.setRoot(rootElement);
        SerializationContext rootContext = new SerializationContext(null, rootElement, xsdNode, jsonNode);
        handleObject(rootContext);
        return xmlDocument;
    }

    private void optimizeNamespaceDeclaration(XmlDocument xmlDocument) {
        NamespaceHandling namespaceHandling = settings().namespaceHandling();
        if (NamespaceHandling.ADD.equals(namespaceHandling)) {
            // no need to optimize, namespace information comes from the json source, and we will keep it that way
            return;
        }
        AdditionalNamespaceDeclarationStrategy strategySetting = settings().additionalNamespaceDeclarationStrategy();
        if (AdditionalNamespaceDeclarationStrategy.NONE.equals(strategySetting)) {
            // no need to optimize
            return;
        }
        // choose between MOVE_TO_ROOT and MOVE_TO_ANCESTOR
        XmlNamespaceDeclarationStrategy strategy = AdditionalNamespaceDeclarationStrategy.MOVE_TO_ROOT.equals(strategySetting) ?
                                                   new XmlNamespaceDeclarationRootStrategy() :
                                                   new XmlNamespaceDeclarationAncestorStrategy();
        strategy.apply(xmlDocument);
    }

    private void serializeElement(String jsonKey, JsonElement jsonValue, SerializationContext parentContext) {
        if (jsonValue.isJsonArray()) {
            serializeArray(jsonKey, jsonValue.getAsJsonArray(), parentContext);
        } else if (jsonValue.isJsonObject()) {
            JsonNode jsonNode = toJsonNode(jsonValue.getAsJsonObject());
            serializeNode(jsonKey, jsonNode, parentContext);
        } else if (jsonValue.isJsonPrimitive()) {
            JsonNode jsonNode = toJsonNode(jsonValue.getAsJsonPrimitive());
            serializeNode(jsonKey, jsonNode, parentContext);
        } else {
            throw new SerializerException("Unable to serialize '" + jsonKey + "'.");
        }
    }

    private void serializeNode(String jsonKey, JsonNode jsonNode, SerializationContext parentContext) {
        XsdNode xsdNode = getXsdNode(jsonKey, jsonNode, parentContext);
        XmlName name = getName(xsdNode, jsonKey);
        XmlElement element = new XmlElement(name, parentContext.getDocument());
        SerializationContext context = new SerializationContext(parentContext, element, xsdNode, jsonNode);
        parentContext.children().add(context);
        handleObject(context);
    }

    private void serializeArray(String jsonKey, JsonArray jsonArray, SerializationContext parentContext) {
        for (JsonElement childJson : jsonArray) {
            serializeElement(jsonKey, childJson, parentContext);
        }
    }

    private void serializeMixedContent(JsonElement mixedContent, SerializationContext parentContext) {
        XmlElement parentElement = parentContext.element();
        if (mixedContent.isJsonPrimitive()) {
            String contentAsString = mixedContent.getAsString();
            try {
                List<XmlContent> xmlContentList = XmlElement.decodeContent(contentAsString, StandardCharsets.UTF_8,
                    parentElement.getNamespacesInScope().values());
                xmlContentList.forEach(parentElement::add);
            } catch (Exception exc) {
                throw new SerializerException("Unable to serialize mixed content: " + contentAsString, exc);
            }
        } else if (mixedContent.isJsonArray()) {
            serializeMixedContent(mixedContent.getAsJsonArray(), parentContext);
        } else {
            throw new SerializerException("Unable to serialize mixed content: " + mixedContent);
        }
    }

    private void serializeMixedContent(JsonArray mixedContentArray, SerializationContext parentContext) {
        XmlElement parentElement = parentContext.element();
        for (JsonElement mixedContent : mixedContentArray) {
            if (mixedContent.isJsonPrimitive()) {
                parentElement.addText(mixedContent.getAsJsonPrimitive().getAsString());
            } else if (mixedContent.isJsonObject()) {
                serializeMixedContent(mixedContent.getAsJsonObject(), parentContext);
            }
        }
    }

    private void serializeMixedContent(JsonObject jsonValue, SerializationContext parentContext) {
        String jsonKey = jsonValue.get(style().mixedContentElementNameKey()).getAsString();
        serializeElement(jsonKey, jsonValue, parentContext);
    }

    private JsonNode toJsonNode(JsonPrimitive jsonPrimitive) {
        JsonNode jsonNode = new JsonNode();
        jsonNode.text = jsonPrimitive.getAsString();
        return jsonNode;
    }

    private JsonNode toJsonNode(JsonObject jsonObject) {
        JsonNode jsonNode = new JsonNode();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getKey().equals(style().namespacePrefixKey())) {
                jsonNode.namespacePrefix = entry.getValue().getAsString();
            } else if (entry.getKey().startsWith(style().xmlnsPrefix())) {
                XmlNamespace namespace = getNamespace(entry.getKey(), entry.getValue());
                jsonNode.namespaces.put(namespace.prefix(), namespace);
            } else if (entry.getKey().startsWith(style().attributePrefix())) {
                String attributeName = entry.getKey().substring(style().attributePrefix().length());
                jsonNode.attributes.put(attributeName, entry.getValue().getAsString());
            } else if (entry.getKey().equals(style().textKey())) {
                jsonNode.text = entry.getValue().getAsString();
            } else if (entry.getKey().equals(style().mixedContentKey())) {
                jsonNode.mixedContent = entry.getValue();
            } else {
                jsonNode.children.put(entry.getKey(), entry.getValue());
            }
        }
        return jsonNode;
    }

    private void handleObject(SerializationContext context) {
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
        JsonElement mixedContent = context.jsonNode().getMixedContent();
        if (mixedContent != null) {
            serializeMixedContent(mixedContent, context);
            return;
        }

        // children
        for (Map.Entry<String, JsonElement> child : context.jsonNode().getChildren().entrySet()) {
            serializeElement(child.getKey(), child.getValue(), context);
        }
        // TODO ignore building xmlns:* -> do that in the aftermath -> still required?
        // TODO order nodes based on Xsd structure
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
        String uri = context.xsdNode() != null ? context.xsdNode().getUri() :
                     getUri(context.jsonNode, context.parentContext);
        String prefix = context.jsonNode().getNamespacePrefix();
        if (prefix != null && uri != null) {
            element.setNamespace(new XmlNamespace(prefix, uri));
            return;
        }
        element.setNamespace(getNamespace(context, uri));
    }

    private boolean useEmptyNamespaceForXsAny(SerializationContext context) {
        boolean isChildOfXsAny =
            context.xsdNode() == null &&
                context.parentContext().xsdNode() != null &&
                context.parentContext().xsdNode().hasAny();
        if (!isChildOfXsAny) {
            return false;
        }
        NamespaceHandling namespaceHandling = settings().namespaceHandling();
        if (NamespaceHandling.OMIT.equals(namespaceHandling)) {
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

    private void handleAttributes(SerializationContext context) {
        context.jsonNode().getAttributes().forEach((name, value) -> {
            handleAttribute(context, name, value);
        });
    }

    private void handleAttribute(SerializationContext context, String attributeName, String attributeValue) {
        XmlElement element = context.element();
        // handle attributes with prefix
        if (attributeName.contains(":")) {
            String[] split = attributeName.split(":");
            String prefix = split[0];
            String localName = split[1];
            XmlNamespace namespace = getNamespace(element, prefix);
            if (namespace == null) {
                throw new SerializerException("Undeclared '" + prefix + "' namespace prefix found. Please add" +
                    " the prefix to the namespace map. Otherwise serialisation is not possible.");
            }
            element.setAttribute(localName, attributeValue, namespace);
            return;
        }
        // handle xs:any attributes
        if (context.xsdNode() == null) {
            element.setAttribute(attributeName, attributeValue, XmlNamespace.EMPTY);
            return;
        }
        // get xsd attribute nodes -> in best case there should be only one
        List<XsdNode> attributeNodes = XsdNode.resolveReferences(context.xsdNode().collectAttributes(attributeName));
        String elementNamespace = context.element().getNamespace().uri();
        if (attributeNodes.size() > 1) {
            attributeNodes = attributeNodes.stream()
                .filter(attributeNode -> elementNamespace.equals(attributeNode.getUri()))
                .toList();
            if (attributeNodes.size() > 1) {
                throw new SerializerException(
                    "Multiple XSD attribute definitions found for '" + attributeName + "' in '" +
                        element.getName() + "'. Unable to determine which one to choose: " + attributeNodes.stream()
                        .map(XsdNode::getName)
                        .map(XmlExpandedName::toString)
                        .collect(Collectors.joining(",")));
            }
        }
        if (attributeNodes.isEmpty()) {
            throw new SerializerException("Invalid '" + attributeName + "' attribute found in " + element.getName());
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
        JsonNode jsonNode = context.jsonNode();
        if (jsonNode.getText() != null) {
            element.setText(jsonNode.getText());
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
     * @throws SerializerException if neither the root name is set manually, nor omitRootElement is set to false
     */
    public String getRootName(JsonObject rootJson) throws SerializerException {
        // root name is not determinable
        if (settings().omitRootElement()) {
            throw new SerializerException("Cannot determine xml root name. Either use 'setRootName()' manually, " +
                "or provide a json where the settings 'omitRootElement()' is set to false.");
        }
        // root name is defined in the first json object of the given root json
        Set<Map.Entry<String, JsonElement>> entries = rootJson.entrySet();
        if (entries.isEmpty()) {
            throw new SerializerException(rootJson + " doesn't have a root node");
        }
        Map.Entry<String, JsonElement> rootEntry = entries.iterator().next();
        JsonElement rootElement = rootEntry.getValue();
        if (!rootElement.isJsonObject()) {
            throw new SerializerException("root '" + rootEntry.getKey() +
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

    private JsonObject getRootValue(JsonObject json) {
        return settings().omitRootElement() ?
               json :
               json.entrySet().iterator().next().getValue().getAsJsonObject();
    }

    private XsdNode getRootXsdNode(JsonObject rootJson, JsonNode jsonNode) {
        if (getRootName() != null) {
            XsdNode rootNode = xsd().getNamedNode(XsdNodeType.ELEMENT, getRootName());
            if (rootNode != null) {
                return rootNode;
            }
            throw new SerializerException(
                "Unable to find root node '" + getRootName() + "' in xsd definition");
        }
        String jsonKey = getRootName(rootJson);
        String localName = XmlQualifiedName.of(jsonKey).localName();
        List<XsdNode> candidates = xsd().getNamedNodes(XsdNodeType.ELEMENT, localName);
        if (candidates.isEmpty()) {
            throw new SerializerException(
                "Unable to find root node '" + localName + "' in xsd definition");
        }
        XsdNode xsdNode = getXsdNode(jsonNode, null, candidates);
        if (xsdNode == null) {
            throw new SerializerException(
                "Unable to find root node '" + localName + "' in xsd definition");
        }
        return xsdNode;
    }

    private XsdNode getXsdNode(String jsonKey, JsonNode jsonNode, SerializationContext parentContext) {
        String localName = XmlQualifiedName.of(jsonKey).localName();
        // xs:any check
        if (parentContext.xsdNode() == null) {
            return null;
        }
        // collect
        List<XsdNode> candidates = parentContext.xsdNode().collectElements().stream()
            .map(XsdNode::getReferenceOrSelf)
            .filter(xsdChildNode -> xsdChildNode.getLocalName().equals(localName))
            .distinct()
            .toList();
        if (candidates.isEmpty()) {
            // in case the parent element has a xs:any element
            if (parentContext.xsdNode().hasAny()) {
                return null;
            }
            // not found -> can't recover from this
            throw new SerializerException("'" + localName + "' is not a valid child of '" +
                parentContext.xsdNode().getLocalName() + "'.");
        }
        XsdNode xsdNode = getXsdNode(jsonNode, parentContext, candidates);
        if (xsdNode == null) {
            throw new SerializerException("Multiple element definitions of '" + localName + "' found in " +
                parentContext.xsdNode().getLocalName() + ". Getting the XsdNode is therefore ambiguous.");
        }
        return xsdNode;
    }

    private XsdNode getXsdNode(JsonNode jsonNode, SerializationContext parentContext,
        List<XsdNode> candidates) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        String uri = getUri(jsonNode, parentContext);
        if (uri != null) {
            return candidates.stream()
                .filter(xsdNode -> xsdNode.getReferenceOrSelf().getUri().equals(uri))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    private String getUri(JsonNode jsonNode, SerializationContext parentContext) {
        String prefix = jsonNode.getNamespacePrefix();
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
        if (!NamespaceHandling.OMIT.equals(settings().namespaceHandling())) {
            namespace = jsonNode.getNamespaces().get(prefix);
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

    private XmlNamespace getNamespace(String jsonKey, JsonElement jsonElement) {
        String prefix = style().xmlnsPrefix().equals(jsonKey) ? "" :
                        jsonKey.substring(style().xmlnsPrefix().length() + 1);
        String uri = jsonElement.getAsString();
        return new XmlNamespace(prefix, uri);
    }

    private XmlNamespace getNamespace(XmlElement element, String prefix) {
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
        XmlPath path = XmlPath.of(element);
        List<XsdNode> nodes = xsd().resolvePath(path);
        XsdNode lastNode = !nodes.isEmpty() ? nodes.get(nodes.size() - 1) : null;
        if (lastNode == null) {
            throw new SerializerException("Unable to determine xsd node path for " + element);
        }
        return lastNode.getElement().getNamespacesInScope().get(prefix);
    }

    private static final class SerializationContext {

        private final SerializationContext parentContext;
        private final XmlElement element;
        private final XsdNode xsdNode;
        private final JsonNode jsonNode;
        private final List<SerializationContext> children;

        private SerializationContext(SerializationContext parentContext, XmlElement element, XsdNode xsdNode,
            JsonNode jsonNode) {
            this.parentContext = parentContext;
            this.element = element;
            this.xsdNode = xsdNode;
            this.jsonNode = jsonNode;
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

        public XsdNode xsdNode() {
            return xsdNode;
        }

        public JsonNode jsonNode() {
            return jsonNode;
        }

        public List<SerializationContext> children() {
            return children;
        }

    }

    private static class JsonNode {

        private String namespacePrefix;
        private String text;
        private JsonElement mixedContent;
        private final Map<String, JsonElement> children;
        private final Map<String, XmlNamespace> namespaces;
        private final Map<String, String> attributes;

        public JsonNode() {
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

        public Map<String, JsonElement> getChildren() {
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

        public JsonElement getMixedContent() {
            return mixedContent;
        }

    }

}
