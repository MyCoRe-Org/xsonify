package org.mycore.xsonify.xml;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Represents an element node in an XML document.</p>
 *
 * <p>Provides methods to manage the node's local name, namespace, children, elements, texts, and attributes.
 * Also manages the namespaces additional to the ones inherited from the parent elements.</p>
 */
public class XmlElement extends XmlContent {

    private XmlName name;

    private final List<XmlContent> content;

    private final List<XmlElement> elements;

    private final List<XmlText> texts;

    private final Map<String, Map<String, XmlAttribute>> attributes;

    private final Map<String, XmlNamespace> additionalNamespaces;

    /**
     * Create a new XmlElement with the specified local name and {@link XmlNamespace#EMPTY}.
     *
     * @param localName the local name of the element
     */
    public XmlElement(String localName) {
        this(localName, XmlNamespace.EMPTY, null);
    }

    /**
     * Create a new XmlElement with the specified local name and namespace.
     *
     * @param localName the local name of the element
     * @param namespace the namespace of the element
     */
    public XmlElement(String localName, XmlNamespace namespace) {
        this(localName, namespace, null);
    }

    /**
     * Create a new XmlElement with the specified local name, namespace, and document.
     *
     * @param localName the local name of the element
     * @param namespace the namespace of the element
     * @param document  the XML document to which the element belongs
     */
    public XmlElement(String localName, XmlNamespace namespace, XmlDocument document) {
        this(new XmlName(localName, namespace), document);
    }

    /**
     * Create new XmlElement with the specified name and document.
     *
     * @param name     name of the element
     * @param document the XML document to which the element belongs
     */
    public XmlElement(XmlName name, XmlDocument document) {
        super(document);
        Objects.requireNonNull(name);
        this.name = name;
        this.content = new ArrayList<>();
        this.elements = new ArrayList<>();
        this.texts = new ArrayList<>();
        this.attributes = new LinkedHashMap<>();
        this.additionalNamespaces = new LinkedHashMap<>();
    }

    /**
     * Returns the XmlName of this element.
     *
     * @return the XmlName of this element.
     */
    public XmlName getName() {
        return name;
    }

    /**
     * Returns the local name of this XML element.
     *
     * @return the local name of this element
     */
    public String getLocalName() {
        return this.name.local();
    }

    /**
     * Returns the qualified name of this XML element.
     * The qualified name is the combination of the namespace prefix and the local name, separated by a colon.
     *
     * @return the qualified name of this element
     */
    public XmlQualifiedName getQualifiedName() {
        return this.name.qualifiedName();
    }

    /**
     * Returns the expanded name of this XML element, which includes the local name and the namespace URI.
     *
     * @return the expanded name of this element
     */
    public XmlExpandedName getExpandedName() {
        return this.name.expandedName();
    }

    /**
     * Sets the XmlDocument for this XmlElement and propagates the same document to all child content of this XmlElement.
     *
     * @param document the XmlDocument to be set
     */
    public void setDocumentAndPropagate(XmlDocument document) {
        super.setDocumentAndPropagate(document);
        for (XmlContent content : this.content) {
            content.setDocumentAndPropagate(document);
        }
    }

    /**
     * Sets an attribute of this XML element.
     *
     * @param attribute the attribute to set
     */
    public void setAttribute(XmlAttribute attribute) {
        String uri = attribute.getNamespace().uri();
        String localName = attribute.getLocalName();
        attributes.computeIfAbsent(uri, k -> new LinkedHashMap<>()).put(localName, attribute);
        attribute.setParent(this);
    }

    /**
     * Sets an attribute with the specified local name, value, and namespace on this XML element.
     *
     * @param localName the local name of the attribute
     * @param value     the value of the attribute
     * @param namespace the namespace of the attribute
     */
    public void setAttribute(String localName, String value, XmlNamespace namespace) {
        this.setAttribute(new XmlAttribute(localName, value, namespace));
    }

    /**
     * Sets an attribute of this XML element using a qualified name and value.
     *
     * @param qualifiedName the qualified name of the attribute to set
     * @param value         the value of the attribute
     */
    public void setAttribute(String qualifiedName, String value) throws XmlException {
        String prefix;
        String localName;

        // If there is no colon in the qualified name, assume it is in the default namespace
        if (!qualifiedName.contains(":")) {
            prefix = "";
            localName = qualifiedName;
        } else {
            // Split the qualified name into prefix and local name
            String[] parts = qualifiedName.split(":", 2);
            prefix = parts[0];
            localName = parts[1];
        }
        // Resolve the prefix to the corresponding namespace URI
        XmlNamespace namespace = getAttributeNamespace(prefix);
        if (namespace == null) {
            throw new XmlException("Unable to determine XmlNamespace for '" + qualifiedName +
                "' The namespace prefix '" + prefix + "' is not defined in the document.");
        }
        // Create the XmlAttribute instance and add it to the attributes map
        setAttribute(new XmlAttribute(localName, value, namespace));
    }

    /**
     * Returns the XmlAttribute with the given localName and the given namespaceUri.
     *
     * @param localName    the local name of the attribute
     * @param namespaceUri the namespace URI of the attribute
     * @return the XmlAttribute, or null if the attribute does not exist
     */
    public XmlAttribute getXmlAttribute(String localName, String namespaceUri) {
        return this.getXmlAttribute(new XmlExpandedName(localName, namespaceUri));
    }

    /**
     * Returns the XmlAttribute with the given expanded name.
     *
     * @param expandedName expanded name
     * @return the XmlAttribute, or null if the attribute does not exist
     */
    public XmlAttribute getXmlAttribute(XmlExpandedName expandedName) {
        Map<String, XmlAttribute> attributeNamespaces = attributes.get(expandedName.uri());
        if (attributeNamespaces == null) {
            return null;
        }
        return attributeNamespaces.get(expandedName.local());
    }

    /**
     * Returns the value of a specified attribute using a qualified name.
     *
     * @param qualifiedName the qualified name of the attribute
     * @return the value of the attribute, or null if the attribute does not exist
     */
    public XmlAttribute getXmlAttribute(String qualifiedName) {
        // If there is no colon in the qualified name, assume it is in the default namespace
        if (!qualifiedName.contains(":")) {
            return getXmlAttribute(qualifiedName, XmlNamespace.EMPTY);
        }

        // Split the qualified name into prefix and local name
        String[] parts = qualifiedName.split(":", 2);
        String prefix = parts[0];
        String localName = parts[1];

        // Resolve the prefix to the corresponding namespace URI
        XmlNamespace ns = getAttributeNamespace(prefix);
        if (ns == null) {
            return null; // The prefix is not defined in the current element's scope
        }
        return getXmlAttribute(localName, ns);
    }

    /**
     * Returns the XmlAttribute with the given localName and namespace.
     *
     * @param localName the local name of the attribute
     * @param namespace the namespace of the attribute
     * @return the value of the attribute, or null if the attribute does not exist
     */
    public XmlAttribute getXmlAttribute(String localName, XmlNamespace namespace) {
        return getXmlAttribute(localName, namespace.uri());
    }

    /**
     * Returns the value of a specified attribute in a specified namespace.
     *
     * @param localName    the local name of the attribute
     * @param namespaceUri the namespace URI of the attribute
     * @return the value of the attribute, or null if the attribute does not exist
     */
    public String getAttribute(String localName, String namespaceUri) {
        return this.getAttribute(new XmlExpandedName(localName, namespaceUri));
    }

    /**
     * Returns the value of a specified attribute in a specified namespace.
     *
     * @param expandedName expanded name
     * @return the value of the attribute, or null if the attribute does not exist
     */
    public String getAttribute(XmlExpandedName expandedName) {
        XmlAttribute xmlAttribute = getXmlAttribute(expandedName);
        return xmlAttribute != null ? xmlAttribute.getValue() : null;
    }

    /**
     * Returns the value of a specified attribute in a specified namespace.
     *
     * @param name name of the attribute
     * @return the value of the attribute, or null if the attribute does not exist
     */
    public String getAttribute(XmlName name) {
        Map<String, XmlAttribute> localNameAttributeMap = this.attributes.get(name.uri());
        if (localNameAttributeMap == null) {
            return null;
        }
        XmlAttribute xmlAttribute = localNameAttributeMap.get(name.local());
        return xmlAttribute != null ? xmlAttribute.getValue() : null;
    }

    /**
     * Returns the value of a specified attribute using a qualified name.
     *
     * @param qualifiedName the qualified name of the attribute
     * @return the value of the attribute, or null if the attribute does not exist
     */
    public String getAttribute(String qualifiedName) {
        XmlAttribute xmlAttribute = getXmlAttribute(qualifiedName);
        return xmlAttribute != null ? xmlAttribute.getValue() : null;
    }

    /**
     * Returns a list of all attributes of this XML element.
     *
     * @return a list of all attributes
     */
    public List<XmlAttribute> getAttributes() {
        return attributes.values()
            .stream()
            .flatMap(map -> map.values().stream())
            .toList();
    }

    /**
     * Adds the given XML content to this XML element.
     *
     * @param content the XML content to add
     */
    public void add(XmlContent content) {
        if (content instanceof XmlElement) {
            addElement((XmlElement) content);
        } else if (content instanceof XmlText) {
            addText((XmlText) content);
        }
    }

    /**
     * Adds the given XML content to this XML element.
     *
     * @param content the XML content to add
     */
    public void addAll(List<XmlContent> content) {
        content.forEach(this::add);
    }

    /**
     * Adds the given XML text to this XML element.
     *
     * @param text the XML text to add
     */
    public void addText(String text) {
        this.addText(new XmlText(text));
    }

    /**
     * Adds the given XML text to this XML element.
     *
     * @param text the XML text to add
     */
    public void addText(XmlText text) {
        this.content.add(text);
        this.texts.add(text);
        text.setParent(this);
    }

    /**
     * Adds the given XML element to this XML element.
     *
     * @param element the XML element to add
     */
    public void addElement(XmlElement element) {
        this.content.add(element);
        this.elements.add(element);
        element.setParent(this);
    }

    /**
     * Sets the text for this XML element. Be aware that this does clear all existing content of this element.
     *
     * @param text text to be set
     */
    public void setText(String text) {
        this.clear();
        addText(new XmlText(text));
    }

    /**
     * Clears all the content of this XmlElement.
     * <p>
     * This operation detaches all child contents (both XmlElements and XmlTexts) from this XmlElement.
     * After a call to this method, the XmlElement will be empty, and all its former children will have their parent
     * and document reference set to null.
     * </p>
     */
    public void clear() {
        this.content.forEach(XmlContent::detach);
    }

    /**
     * <p>Removes the specified XmlContent object from this XmlElement's content.</p>
     * <p>After removal, the parent and document properties of the XmlContent are set to null.</p>
     *
     * @param content the XmlContent object to be removed
     */
    public void remove(XmlContent content) {
        this.content.remove(content);
        if (content instanceof XmlElement) {
            this.elements.remove(content);
        } else if (content instanceof XmlText) {
            this.texts.remove(content);
        }
        content.setParent(null);
    }

    /**
     * Returns an unmodifiable copy of all child elements of this XML element.
     *
     * @return a list of child elements
     */
    public List<XmlElement> getElements() {
        return List.copyOf(this.elements);
    }

    /**
     * Checks if this XML element has any content. Content includes text or element nodes.
     *
     * @return true if this element has content, false otherwise
     */
    public boolean hasContent() {
        return !this.content.isEmpty();
    }

    /**
     * Checks if this XML element has any text.
     *
     * @return true if this element has text, false otherwise
     */
    public boolean hasText() {
        return !this.texts.isEmpty();
    }

    /**
     * Checks if this XML element has any child elements.
     *
     * @return true if this element has child elements, false otherwise
     */
    public boolean hasElements() {
        return !this.elements.isEmpty();
    }

    /**
     * Checks if this XML element has any attributes.
     *
     * @return true if this element has attributes, false otherwise
     */
    public boolean hasAttributes() {
        return !this.attributes.isEmpty();
    }

    /**
     * Checks if this XML element is mixed, i.e., if it has both text and child elements.
     *
     * @return true if this element is mixed, false otherwise
     */
    public boolean isMixed() {
        return hasElements() && hasText();
    }

    /**
     * Returns an unmodifiable copy of the list of XmlTexts associated with this XmlElement.
     *
     * @return An unmodifiable list of XmlTexts.
     */
    public List<XmlText> getTexts() {
        return List.copyOf(this.texts);
    }

    /**
     * Returns the concatenated normalized text of this XML element.
     *
     * @return the concatenated normalized text
     */
    public String getTextNormalized() {
        return this.texts.stream()
            .map(XmlText::normalize)
            .collect(Collectors.joining(""));
    }

    /**
     * Returns the concatenated text of this XML element.
     *
     * @return the concatenated text
     */
    public String getText() {
        return this.texts.stream()
            .map(XmlText::get)
            .collect(Collectors.joining(""));
    }

    /**
     * Returns the URI of this XML element's namespace.
     *
     * @return the URI
     */
    public String getUri() {
        return this.name.namespace().uri();
    }

    /**
     * Returns the prefix of this XML element's namespace.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return this.name.namespace().prefix();
    }

    /**
     * Returns an unmodifiable copy of all content of this XML element.
     *
     * @return a list of child nodes
     */
    public List<XmlContent> getContent() {
        return List.copyOf(this.content);
    }

    /**
     * Returns the namespace of this XML element.
     *
     * @return the namespace
     */
    public XmlNamespace getNamespace() {
        return this.name.namespace();
    }

    /**
     * <p>Returns the {@link XmlNamespace} that corresponds to the given prefix.</p>
     * This involves searching up the tree, so the results depend on the current location of the element.
     * Returns null if there is no namespace in scope with the given prefix at this point in the document.
     *
     * @param prefix the prefix for which to return the namespace
     * @return the namespace for the given prefix, or null if the prefix doesn't match any namespace
     */
    public XmlNamespace getNamespace(String prefix) {
        Map<String, XmlNamespace> inheritedNamespaces = getNamespacesInScope();
        XmlNamespace namespace = inheritedNamespaces.get(prefix);
        if (namespace != null) {
            return namespace;
        }
        return XmlNamespace.getDefaultNamespace(prefix);
    }

    /**
     * Returns the first element with the given qualified name.
     *
     * @param qualifiedName the qualified name
     * @return the element or null
     */
    public XmlElement getElement(String qualifiedName) {
        return getElement(XmlQualifiedName.of(qualifiedName));
    }

    /**
     * Returns the first element with the given qualified name.
     *
     * @param qualifiedName the qualified name
     * @return the element or null
     */
    public XmlElement getElement(XmlQualifiedName qualifiedName) {
        return element(qualifiedName).findFirst().orElse(null);
    }

    /**
     * Returns all child elements with the given qualified name.
     *
     * @param qualifiedName the qualified name to match
     * @return a list of matching child elements
     */
    public List<XmlElement> getElements(String qualifiedName) {
        return getElements(XmlQualifiedName.of(qualifiedName));
    }

    /**
     * Returns all child elements with a given XmlName.
     *
     * @param name the XmlName name to match
     * @return a list of matching child elements
     */
    public List<XmlElement> getElements(XmlName name) {
        return element(name).collect(Collectors.toList());
    }

    /**
     * Returns all child elements with a given qualified name.
     *
     * @param qualifiedName the qualified name to match
     * @return a list of matching child elements
     */
    public List<XmlElement> getElements(XmlQualifiedName qualifiedName) {
        return element(qualifiedName).collect(Collectors.toList());
    }

    private Stream<XmlElement> element(XmlQualifiedName qualifiedName) {
        return getElements().stream()
            .filter(element -> element.getQualifiedName().equals(qualifiedName));
    }

    private Stream<XmlElement> element(XmlName name) {
        return getElements().stream()
            .filter(element -> element.getName().equals(name));
    }

    public int indexOf(XmlContent content) {
        return getContent().indexOf(content);
    }

    public int indexOfElement(XmlElement element) {
        return getElements().indexOf(element);
    }

    /**
     * Sets an additional namespace declaration to this XML element.
     * This will be used during output for stylistic reason.
     *
     * @param namespace the namespace to add
     */
    public void setAdditionalNamespace(XmlNamespace namespace) {
        if (this.getNamespace().equals(namespace) || XmlNamespace.isDefaultNamespace(namespace)) {
            return;
        }
        this.additionalNamespaces.put(namespace.prefix(), namespace);
    }

    /**
     * Returns an additional namespace of this XML element.
     *
     * @param prefix the prefix of the namespace
     * @return the namespace, or null if not found
     */
    public XmlNamespace getAdditionalNamespace(String prefix) {
        return this.additionalNamespaces.get(prefix);
    }

    public void removeAdditionalNamespace(XmlNamespace namespace) {
        this.additionalNamespaces.remove(namespace.prefix(), namespace);
    }

    public void clearAdditionalNamespaces() {
        this.additionalNamespaces.clear();
    }

    /**
     * Sets the namespace of this XML element.
     *
     * @param namespace the new namespace
     */
    public void setNamespace(XmlNamespace namespace) {
        this.name = new XmlName(this.name.local(), namespace);
    }

    /**
     * Returns the namespace for a given prefix. Searches in the following order:
     * <ol>
     *     <li>Default namespaces 'xml', 'xmlns' and empty ''</li>
     *     <li>namespace of the element</li>
     *     <li>additional namespaces of the element</li>
     *     <li>inherited namespaces</li>
     * </ol>
     *
     * @param prefix the prefix to find the namespace for
     * @return the namespace, or null if not found
     */
    public XmlNamespace getAttributeNamespace(String prefix) {
        XmlNamespace defaultNamespace = XmlNamespace.getDefaultNamespace(prefix);
        if (defaultNamespace != null) {
            return defaultNamespace;
        }
        if (getNamespace().prefix().equals(prefix)) {
            return getNamespace();
        }
        XmlNamespace additionalNamespace = getAdditionalNamespace(prefix);
        if (additionalNamespace != null) {
            return additionalNamespace;
        }
        return getNamespacesInScope().get(prefix);
    }

    public List<XmlNamespace> getAdditionalNamespaces() {
        return new ArrayList<>(this.additionalNamespaces.values());
    }

    /**
     * Returns all namespaces appearing in this element. This includes:
     * <ul>
     *     <li>namespace of the element itself - if its not empty</li>
     *     <li>additional declared namespaces</li>
     *     <li>namespaces of attributes - if they are not empty</li>
     * </ul>
     *
     * @return prefix:namespace map of all namespaces appearing on this element locally
     */
    public LinkedHashMap<String, XmlNamespace> getNamespacesLocal() {
        LinkedHashMap<String, XmlNamespace> namespaces = new LinkedHashMap<>();
        // element namespace
        if (!XmlNamespace.EMPTY.equals(getNamespace())) {
            namespaces.putIfAbsent(getNamespace().prefix(), getNamespace());
        }
        // additional namespaces
        for (XmlNamespace additionalNamespace : getAdditionalNamespaces()) {
            namespaces.putIfAbsent(additionalNamespace.prefix(), additionalNamespace);
        }
        // attribute namespaces
        for (XmlAttribute attribute : getAttributes()) {
            if (attribute.getNamespace().equals(XmlNamespace.EMPTY)) {
                continue;
            }
            namespaces.putIfAbsent(attribute.getNamespace().prefix(), attribute.getNamespace());
        }
        return namespaces;
    }

    /**
     * Returns a map of the inherited namespaces of this XML element.
     * The map's keys are the prefixes of the namespaces, and the values are the namespaces themselves.
     *
     * @return a map of inherited namespaces
     */
    public LinkedHashMap<String, XmlNamespace> getNamespacesInScope() {
        LinkedHashMap<String, XmlNamespace> namespaces = new LinkedHashMap<>();
        getNamespacesInScope(this, namespaces);
        return namespaces;
    }

    /**
     * Recursive helper method to collect inherited namespaces from the given XML element and its ancestors.
     * The collected namespaces are stored in the given map.
     *
     * @param element    the XML element to collect namespaces from
     * @param namespaces the map to store the collected namespaces
     */
    private void getNamespacesInScope(XmlElement element, LinkedHashMap<String, XmlNamespace> namespaces) {
        // add xml namespace first
        namespaces.putIfAbsent(XmlNamespace.XML.prefix(), XmlNamespace.XML);
        // add local namespaces
        element.getNamespacesLocal().forEach(namespaces::putIfAbsent);
        // add parent namespaces
        XmlElement parent = element.getParent();
        if (parent != null) {
            getNamespacesInScope(parent, namespaces);
            return;
        }
        // parent is null, element = root, add empty namespace if not already defined
        namespaces.putIfAbsent(XmlNamespace.EMPTY.prefix(), XmlNamespace.EMPTY);
    }

    /**
     * Returns all namespaces this XML element introduces first.
     *
     * @return a collection of namespaces
     */
    public LinkedHashMap<String, XmlNamespace> getNamespacesIntroduced() {
        LinkedHashMap<String, XmlNamespace> namespacesInScope = getNamespacesInScope();
        XmlElement parent = this.getParent();
        // root element
        if (parent == null) {
            namespacesInScope.remove(XmlNamespace.EMPTY.prefix(), XmlNamespace.EMPTY);
            namespacesInScope.remove(XmlNamespace.XML.prefix());
            return namespacesInScope;
        }
        // all other -> difference between parent and this element
        Map<String, XmlNamespace> parentNamespacesInScope = parent.getNamespacesInScope();
        for (Map.Entry<String, XmlNamespace> entry : parentNamespacesInScope.entrySet()) {
            namespacesInScope.remove(entry.getKey(), entry.getValue());
        }
        return namespacesInScope;
    }

    /**
     * <p>Creates a deep copy of this XML element for a given XML document.
     * The copy includes all child elements, attributes, and additional namespaces.</p>
     * <p><b>IMPORTANT:</b> The parent is not set for this element.</p>
     *
     * @param document the XML document to associate the copy with
     * @return the copied XML element
     */
    public XmlElement copy(XmlDocument document) {
        XmlElement copy = new XmlElement(this.name, document);
        this.content.forEach(child -> copy.add(child.copy(document)));
        this.attributes.entrySet().stream()
            .flatMap(entry -> entry.getValue().values().stream())
            .forEach(copy::setAttribute);
        this.additionalNamespaces.values().forEach(copy::setAdditionalNamespace);
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.toPrettyXml(sb, "");
        return sb.toString();
    }

    /**
     * Returns a pretty-printed XML string of this element.
     *
     * @return the pretty-printed XML string
     */
    @Override
    public String toPrettyXml() {
        StringBuilder sb = new StringBuilder();
        toXml(sb, "", true, true);
        return sb.toString();
    }

    /**
     * Returns an XML string of this element.
     *
     * @return the XML string
     */
    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder();
        toXml(sb, "", false, true);
        return sb.toString();
    }

    public String toXmlNoContent() {
        StringBuilder sb = new StringBuilder();
        toXml(sb, "", false, false);
        return sb.toString();
    }

    @Override
    void toPrettyXml(StringBuilder sb, String indent) {
        toXml(sb, indent, true, true);
    }

    @Override
    void toXml(StringBuilder sb) {
        toXml(sb, "", false, true);
    }

    private void toXml(StringBuilder sb, String indent, boolean pretty, boolean renderContent) {
        if (pretty) {
            sb.append(indent);
        }
        sb.append("<");
        sb.append(getQualifiedName());
        getNamespacesIntroduced().values()
            .forEach(ns -> sb.append(" ").append(ns));
        getAttributes().forEach(attr -> {
            sb.append(" ")
                .append(attr.getQualifiedName())
                .append("=").append("\"")
                .append(attr.getValue())
                .append("\"");
        });
        if (!renderContent || !hasContent()) {
            sb.append("/>");
            if (pretty) {
                sb.append("\n");
            }
            return;
        }
        sb.append(">");
        if (hasElements() && pretty) {
            sb.append("\n");
        }
        if (pretty) {
            getContent().forEach(content -> content.toPrettyXml(sb, indent + "  "));
        } else {
            contentToXml(sb);
        }
        if (hasElements() && pretty) {
            sb.append(indent);
        }
        sb.append("</").append(getQualifiedName()).append(">");
        if (pretty) {
            sb.append("\n");
        }
    }

    /**
     * Returns a string representation of the content of this XML element.
     * The string is encoded using the specified character set.
     *
     * @param charset the character set to use for encoding
     * @return the encoded content
     */
    public String encodeContent(Charset charset) {
        StringBuilder sb = new StringBuilder();
        contentToXml(sb);
        ByteBuffer buffer = charset.encode(sb.toString());
        return charset.decode(buffer).toString();
    }

    /**
     * Append the content of this XmlElement to the given StringBuilder. The content is separated by whitespaces, if,
     * and only if, there is a whitespace present in a text node. Leading and trailing whitespaces are removed.
     *
     * @param sb the string builder to append to
     */
    private void contentToXml(StringBuilder sb) {
        trailingContent().forEach(trailingInfo -> {
            trailingInfo.content().toXml(sb);
            if (trailingInfo.trailing) {
                sb.append(" ");
            }
        });
    }

    /**
     * Determines which child nodes should have a trailing whitespace.
     * <ul>
     *     <li>Only text nodes can have trailing whitespaces</li>
     *     <li>The last node never has trailing whitespace</li>
     *     <li>The last character of a text node is a whitespace</li>
     * </ul>
     * This method should be used for text normalization.
     *
     * @return a list of child nodes with additional information about trailing whitespaces
     */
    public List<TrailingInfo> trailingContent() {
        List<TrailingInfo> trailingInfos = new ArrayList<>();
        for (int i = 0; i < content.size(); i++) {
            XmlContent childContent = getContent().get(i);
            // check for text and last element
            if (!(childContent instanceof XmlText xmlText) || i >= content.size() - 1) {
                trailingInfos.add(new TrailingInfo(childContent, false));
                continue;
            }
            String text = xmlText.get();
            // check for whitespace at the end
            if (!Character.isWhitespace(text.charAt(text.length() - 1))) {
                trailingInfos.add(new TrailingInfo(childContent, false));
                continue;
            }
            trailingInfos.add(new TrailingInfo(childContent, true));
        }
        return trailingInfos;
    }

    /**
     * Same as {@link #decodeContent(String, Charset, Collection)}, but with empty root namespaces.
     *
     * @param content the encoded content string
     * @param charset the character set to use for decoding
     * @return list of XmlElement objects
     */
    public static List<XmlContent> decodeContent(String content, Charset charset)
        throws ParserConfigurationException, SAXException, IOException, XmlParseException {
        return decodeContent(content, charset, new ArrayList<>());
    }

    /**
     * Returns a list of XmlElement objects constructed from the specified encoded content string.
     * The string is decoded using the specified character set.
     *
     * @param content    the encoded content string
     * @param charset    the character set to use for decoding
     * @param namespaces collection of namespaces to correctly parse the given content
     * @return list of XmlElement objects
     */
    public static List<XmlContent> decodeContent(String content, Charset charset, Collection<XmlNamespace> namespaces)
        throws ParserConfigurationException, SAXException, IOException, XmlParseException {
        // build xml string
        StringBuilder sb = new StringBuilder();
        sb.append("<root");
        namespaces.forEach(namespace -> sb.append(" ").append(namespace.toString()));
        sb.append(">");
        sb.append(new String(content.getBytes(charset), charset));
        sb.append("</root>");

        // parse
        XmlParser parser = new XmlSaxParser();
        try (InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes(charset))) {
            XmlDocument document = parser.parse(inputStream);
            List<XmlContent> xmlContentList = document.getRoot().getContent();
            xmlContentList.forEach(XmlContent::detach);
            return xmlContentList;
        }
    }

    /**
     * Record for storing information about a child node and whether it should have a trailing whitespace.
     */
    public record TrailingInfo(XmlContent content, boolean trailing) {
    }

}
