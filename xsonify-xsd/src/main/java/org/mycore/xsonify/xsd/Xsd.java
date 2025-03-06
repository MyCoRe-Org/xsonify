package org.mycore.xsonify.xsd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdAttributeGroup;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdGroup;
import org.mycore.xsonify.xsd.node.XsdNode;
import org.mycore.xsonify.xsd.node.XsdSimpleType;

/**
 * Represents an XSD (XML Schema Definition) and provides methods to interact with its structure and elements.
 * This class encapsulates the XSD's target namespace, associated documents, and named nodes.
 */
public class Xsd {

    /** A list of node types that can have named references in the XSD. */
    public static final List<Class<? extends XsdNode>> NAMED_TYPES = List.of(
        XsdElement.class, XsdGroup.class, XsdComplexType.class, XsdSimpleType.class,
        XsdAttributeGroup.class, XsdAttribute.class);

    private final String targetNamespace;

    private final LinkedHashMap<String, XmlDocument> documentMap;

    private final LinkedHashMap<Class<? extends XsdNode>, Map<XmlExpandedName, XsdNode>> namedMap;

    /**
     * Constructor to initialize the XSD with the given target namespace and document map.
     *
     * @param targetNamespace the target namespace for the XSD
     * @param documentMap     a map of associated XML documents
     */
    public Xsd(String targetNamespace, LinkedHashMap<String, XmlDocument> documentMap) {
        this.targetNamespace = targetNamespace != null ? targetNamespace : XmlNamespace.EMPTY.uri();
        this.documentMap = documentMap;
        this.namedMap = new LinkedHashMap<>();
        NAMED_TYPES.forEach(type -> this.namedMap.put(type, new LinkedHashMap<>()));
    }

    /**
     * Returns the target namespace of the XSD.
     *
     * @return the target namespace
     */
    public String getTargetNamespace() {
        return targetNamespace;
    }

    /**
     * Returns the named node of the given type with the given name.
     *
     * @param type  node type
     * @param local local name
     * @param uri   namespace uri
     * @return the found node or null
     */
    public <T extends XsdNode> T getNamedNode(Class<T> type, String local, String uri) {
        return getNamedNode(type, new XmlExpandedName(local, uri));
    }

    /**
     * Returns the named node of the given type with the given name. Be aware that the prefix of the reference is
     * ignored. Only the local name and the namespace uri is taken into consideration to retrieve the {@link XsdNode}.
     *
     * @param type      node type
     * @param reference named reference.
     * @return the found node or null
     */
    public <T extends XsdNode> T getNamedNode(Class<T> type, XmlName reference) {
        return getNamedNode(type, reference.expandedName());
    }

    /**
     * Same as {@link #getNamedNode(Class, XmlExpandedName)}. Resolves the expanded name for you.
     *
     * @param type         node type
     * @param expandedName expanded name of the node
     * @return the found node or null
     */
    public <T extends XsdNode> T getNamedNode(Class<T> type, String expandedName) {
        return getNamedNode(type, XmlExpandedName.of(expandedName));
    }

    /**
     * Returns the named node of the given type with the given name.
     *
     * @param type      node type
     * @param reference named reference.
     * @return the found node or null
     */
    public <T extends XsdNode> T getNamedNode(Class<T> type, XmlExpandedName reference) {
        if (!NAMED_TYPES.contains(type)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<XmlExpandedName, T> nodeMap = (Map<XmlExpandedName, T>) this.namedMap.get(type);
        if (nodeMap == null) {
            return null;
        }
        return nodeMap.get(reference);
    }

    /**
     * <p>Returns all nodes with the given localName.</p>
     * <p>For example, if the given localName would be "element", this could return a list including "my:element",
     * "your:element" and "element".
     *
     * @param type      node type
     * @param localName local name of the node
     * @return list of nodes, list is empty if none of the nodes match the given localName
     */
    @SuppressWarnings("unchecked")
    public <T extends XsdNode> List<T> getNamedNodes(Class<T> type, String localName) {
        return (List<T>) namedMap.get(type).entrySet()
            .stream()
            .filter(entry -> entry.getKey().local().equals(localName))
            .map(Map.Entry::getValue)
            .toList();
    }

    /**
     * Returns a collection of all named nodes in the XSD.
     *
     * @return a list of all named nodes.
     */
    public List<? extends XsdNode> getNamedNodes() {
        return this.namedMap.values().stream()
            .flatMap(nodeMap -> nodeMap.values().stream())
            .collect(Collectors.toList());
    }

    /**
     * Returns all local named nodes of the given types.
     *
     * @param types types to collect
     * @return collection of nodes
     */
    @SafeVarargs
    public final Collection<XsdNode> getNamedNodes(Class<? extends XsdNode>... types) {
        Collection<XsdNode> collection = new ArrayList<>();
        for (Class<? extends XsdNode> type : types) {
            Map<XmlExpandedName, ? extends XsdNode> nodes = namedMap.get(type);
            if (nodes != null) {
                collection.addAll(nodes.values());
            }
        }
        return collection;
    }

    /**
     * Adds a named node to the XSD if its type is recognized in {@link #NAMED_TYPES}.
     *
     * @param node the node to add.
     */
    public void addNamedNode(XsdNode node) {
        if (!NAMED_TYPES.contains(node.getClass())) {
            return;
        }
        Map<XmlExpandedName, XsdNode> nodeMap = this.namedMap.get(node.getClass());
        if (nodeMap.containsKey(node.getName())) {
            XsdNode original = nodeMap.get(node.getName());
            /*
            TODO: this should be included -> only out commented because mods namespace uses different xlink than mycore
            throw new XsdParseException("Overwriting existing node " + node.getName().toString() + "\n" +
                original.getElement() + " from " + original.getDocument().getSchemaLocation() +
                "\nwith\n" +
                node.getElement() + " from " + node.getDocument().getSchemaLocation());*/
        }
        nodeMap.put(node.getName(), node);
    }

    /**
     * Retrieves a map of named nodes, organized by their class type and expanded names.
     *
     * @return a map of nodes, grouped by their types and expanded names.
     */
    public LinkedHashMap<Class<? extends XsdNode>, Map<XmlExpandedName, XsdNode>> getNamedMap() {
        return namedMap;
    }

    /**
     * Collects all nodes of the specified type across the entire XSD schema.
     *
     * <p>This method retrieves all instances of the given {@link XsdNode} type, such as {@link XsdElement},
     * {@link XsdAttribute}, etc., from the entire schema.</p>
     *
     * @param <T>  the type of nodes to collect
     * @param type the {@code Class} object representing the type of nodes to collect
     * @return a {@link Collection} containing all nodes of the specified type
     *
     * <h4>Example Usage:</h4>
     * <pre>{@code
     * // Collect all XsdElement nodes in the schema
     * Collection<XsdElement> elements = xsd.collect(XsdElement.class);
     * }</pre>
     */
    public final <T extends XsdNode> Collection<T> collect(Class<T> type) {
        List<T> nodes = new ArrayList<>();
        for (XsdNode node : getNamedNodes()) {
            collect(node, type, nodes);
        }
        return nodes;
    }

    /**
     * Collects all nodes of the specified type within the subtree rooted at the given node.
     *
     * <p>This method retrieves all instances of the specified {@link XsdNode} type that are descendants
     * of the provided root node.</p>
     *
     * @param <T>  the type of nodes to collect
     * @param node the root {@link XsdNode} from which to start the collection
     * @param type the {@code Class} object representing the type of nodes to collect
     * @return a {@link List} containing all nodes of the specified type within the subtree
     *
     * <h4>Example Usage:</h4>
     * <pre>{@code
     * // Assume 'complexType' is an XsdComplexType instance
     * List<XsdAttribute> attributes = xsd.collect(complexType, XsdAttribute.class);
     * }</pre>
     */
    public <T extends XsdNode> List<T> collect(XsdNode node, Class<T> type) {
        List<T> nodes = new ArrayList<>();
        collect(node, type, nodes);
        return nodes;
    }

    @SuppressWarnings("unchecked")
    private <T extends XsdNode> void collect(XsdNode node, Class<T> type, Collection<T> collection) {
        if (type.isAssignableFrom(node.getClass())) {
            collection.add((T) node);
        }
        for (XsdNode childNode : node.getChildren()) {
            collect(childNode, type, collection);
        }
    }

    /**
     * Collects all nodes that match any of the specified types across the entire XSD schema.
     *
     * <p>This method allows collecting multiple types of {@code XsdNode} instances in a single call.</p>
     *
     * @param types the {@code Class} objects representing the types of nodes to collect
     * @return a {@link List} containing all nodes that match any of the specified types
     *
     * <h4>Example Usage:</h4>
     * <pre>{@code
     * // Collect all XsdElement and XsdAttribute nodes in the schema
     * List<XsdNode> elementsAndAttributes = xsd.collect(XsdElement.class, XsdAttribute.class);
     * }</pre>
     */
    @SafeVarargs
    public final List<XsdNode> collect(Class<? extends XsdNode>... types) {
        List<XsdNode> nodes = new ArrayList<>();
        List<Class<? extends XsdNode>> nodeTypes = Arrays.asList(types);
        for (XsdNode node : getNamedNodes()) {
            collect(node, nodeTypes, nodes);
        }
        return nodes;
    }

    /**
     * Collects all nodes that match any of the specified types within the subtree rooted at the given node.
     *
     * <p>This method retrieves all instances of the specified {@code XsdNode} types that are descendants
     * of the provided root node.</p>
     *
     * @param node  the root {@link XsdNode} from which to start the collection
     * @param types a {@link List} of {@code Class} objects representing the types of nodes to collect
     * @return a {@link List} containing all nodes that match any of the specified types within the subtree
     *
     * <h4>Example Usage:</h4>
     * <pre>{@code
     * // Assume 'complexType' is an XsdComplexType instance
     * List<Class<? extends XsdNode>> typesToCollect = Arrays.asList(XsdElement.class, XsdAttribute.class);
     * List<XsdNode> elementsAndAttributes = xsd.collect(complexType, typesToCollect);
     * }</pre>
     */
    public List<XsdNode> collect(XsdNode node, List<Class<? extends XsdNode>> types) {
        List<XsdNode> nodes = new ArrayList<>();
        collect(node, types, nodes);
        return nodes;
    }

    /**
     * Recursively collects nodes that match any of the specified types and adds them to the provided collection.
     *
     * @param node        the current {@link XsdNode} being inspected
     * @param types       a {@link List} of {@code Class} objects representing the types of nodes to collect
     * @param collection  the {@link Collection} to which matching nodes are added
     */
    private void collect(XsdNode node, List<Class<? extends XsdNode>> types, Collection<XsdNode> collection) {
        if (types.contains(node.getClass())) {
            collection.add(node);
        }
        for (XsdNode childNode : node.getChildren()) {
            collect(childNode, types, collection);
        }
    }

    /**
     * Collects all nodes present in the entire XSD schema.
     *
     * <p>This method retrieves every {@link XsdNode} instance defined within the schema, traversing all
     * named nodes and their descendants.</p>
     *
     * @return a {@link Collection} containing all nodes in the schema
     *
     * <h4>Example Usage:</h4>
     * <pre>{@code
     * // Collect all nodes in the schema
     * Collection<XsdNode> allNodes = xsd.collectAll();
     * }</pre>
     */
    public Collection<XsdNode> collectAll() {
        List<XsdNode> collectedNodes = new ArrayList<>();
        for (XsdNode node : getNamedNodes()) {
            collectAll(node, collectedNodes);
        }
        return collectedNodes;
    }

    private void collectAll(XsdNode node, List<XsdNode> collection) {
        collection.add(node);
        for (XsdNode childNode : node.getChildren()) {
            collectAll(childNode, collection);
        }
    }

    public Map<String, XmlDocument> getDocumentMap() {
        return documentMap;
    }

    /**
     * Collects all the namespaces available. This runs through all xsd documents and their children.
     *
     * @return map of prefix : namespace set
     */
    public LinkedHashMap<String, LinkedHashSet<XmlNamespace>> collectNamespaces() {
        LinkedHashMap<String, LinkedHashSet<XmlNamespace>> namespaces = new LinkedHashMap<>();
        documentMap.values().stream()
            .flatMap(xmlDocument -> xmlDocument.collectNamespaces().entrySet().stream())
            .forEach(entry -> {
                namespaces.putIfAbsent(entry.getKey(), new LinkedHashSet<>());
                namespaces.get(entry.getKey()).addAll(entry.getValue());
            });
        return namespaces;
    }

    /**
     * Collects the namespaces which are matching the given prefix. This runs through all xsd documents and their
     * children.
     *
     * @param prefix the namespace prefix
     * @return set of namespaces which match the prefix
     */
    public Set<XmlNamespace> collectNamespaces(String prefix) {
        return collectNamespaces().get(prefix);
    }

    /**
     * Builds the element, attribute and xs:any cache for each @{@link XsdElement}. This will be called by the
     * {@link XsdParser} after all processing has been done. Usually it is not required to call this method manually,
     * except if you changed the structure and called {@link #clearCache()}.
     */
    public void buildCache() {
        this.collect(XsdElement.class).forEach(XsdElement::buildCache);
    }

    /**
     * Clears the element and attribute cache of each {@link XsdElement}. This should be called if the xsd structure has
     * changed.
     */
    public void clearCache() {
        this.collect(XsdElement.class).forEach(XsdElement::clearCache);
    }

    /**
     * Resolves an {@link XmlPath} to a list of corresponding {@link XsdNode} nodes within the XSD schema.
     *
     * <p>This method navigates through the XSD schema based on the provided {@code XmlPath}. It supports
     * resolving both element and attribute paths. The resolution process involves the following steps:
     * </p>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>{@code
     * // Assume an XML schema with a root element 'library' containing 'book' elements,
     * // each 'book' having a 'title' attribute.
     * XmlPath path = XmlPath.of("/library/book/@title");
     * List<? extends XsdNode> nodes = xsd.resolvePath(path);
     * // nodes now contains the XsdElement instance for 'library', the XsdElement for 'book',
     * // and the XsdAttribute for 'title'.
     * }</pre>
     *
     * <h4>Notes</h4>
     * <ul>
     *   <li>The method supports both element and attribute paths. When resolving attribute paths, it ensures
     *       that the attribute is correctly associated with its parent element.</li>
     *   <li>To resolve only element nodes without considering attributes, use {@link #resolveElementPath(XmlPath)}
     *       instead.</li>
     * </ul>
     *
     * @param path the {@link XmlPath} representing the sequence of XML elements and/or attributes to resolve in the schema.
     *             The path should follow the structure of the XML document and include element names and namespaces as necessary.
     * @return a {@link List} of {@link XsdNode} instances corresponding to the elements and/or attributes in the path,
     *         maintaining the order from root to leaf.
     * @throws XsdNoSuchNodeException if any element or attribute in the path cannot be found in the schema.
     *                                This indicates that the schema does not define the specified node at the expected location in the hierarchy.
     * @throws XsdAnyException        if the path traversal reaches an {@code xs:any} or {@code xs:anyAttribute} node,
     *                                making further resolution ambiguous because {@code xs:any} allows for any element or attribute.
     * @throws XsdAmbiguousNodeException  if multiple matching elements are found, causing ambiguity.
     * @see XmlPath
     * @see XsdNode
     * @see XsdElement
     * @see XsdAttribute
     */
    public List<? extends XsdNode> resolvePath(XmlPath path)
        throws XsdNoSuchNodeException, XsdAnyException, XsdAmbiguousNodeException {
        if (path.isEmpty()) {
            return new ArrayList<>();
        }
        if (path.last().isAttribute()) {
            List<XsdElement> elementNodes = resolveElementPath(path.elements());
            XsdElement xsdElement = elementNodes.get(elementNodes.size() - 1);
            XsdAttribute attribute = resolvePathForAttribute(xsdElement, path.last().name());
            List<XsdNode> result = new ArrayList<>(elementNodes);
            result.add(attribute);
            return result;
        }
        return resolveElementPath(path);
    }

    /**
     * Resolves an {@link XmlPath} to a list of corresponding {@link XsdElement} nodes within the XSD schema.
     *
     * <p>This method navigates through the XSD schema starting from the root element specified in the path.
     * It attempts to find each element in the path hierarchy by matching element names and namespaces.
     * The method returns a list of {@code XsdElement} instances that represent the sequence of elements
     * in the provided {@code XmlPath}, maintaining the order from root to leaf.</p>
     *
     * <h4>Example Usage</h4>
     * <pre>{@code
     * // Assume an XML schema with a root element 'bookstore' containing 'book' elements, which in turn
     * // contain 'title' elements.
     * XmlPath path = XmlPath.of("/bookstore/book/title");
     * List<XsdElement> elements = xsd.resolveElementPath(path);
     * // elements now contains the XsdElement instances for 'bookstore', 'book', and 'title'.
     * }</pre>
     *
     * <h4>Notes</h4>
     * <ul>
     *   <li>The method only resolves element nodes. If the path includes attributes, they will be ignored by this method.
     *       To resolve attributes, use {@link #resolvePath(XmlPath)} instead.</li>
     *   <li>This method is useful when you need to validate or inspect the schema definitions for a specific element
     *       hierarchy in an XML document.</li>
     * </ul>
     *
     * @param path the {@link XmlPath} representing the sequence of XML elements to resolve in the schema.
     *             The path should consist of element names and namespaces corresponding to the XML structure.
     * @return a {@link List} of {@link XsdElement} instances corresponding to the elements in the path,
     *         from the root element to the leaf element.
     * @throws XsdNoSuchNodeException if an element in the path cannot be found in the schema.
     *                                This indicates that the schema does not define the specified element
     *                                at the expected location in the hierarchy.
     * @throws XsdAnyException if the path reaches an {@code xs:any} element in the schema,
     *                         making further resolution ambiguous because {@code xs:any} allows for any element.
     * @throws XsdAmbiguousNodeException  if multiple matching elements are found, causing ambiguity.
     * @see XmlPath
     * @see XsdElement
     */
    public List<XsdElement> resolveElementPath(XmlPath path)
        throws XsdNoSuchNodeException, XsdAnyException, XsdAmbiguousNodeException {
        if (path.isEmpty()) {
            return new ArrayList<>();
        }
        List<XsdElement> nodes = new ArrayList<>();
        XmlName root = path.root().name();
        XsdElement headNode = this.getNamedNode(XsdElement.class, root);
        if (headNode == null) {
            throw new XsdNoSuchNodeException(root + " could not be found!");
        }
        nodes.add(headNode);
        XsdElement next = headNode;
        for (int i = 1; i < path.size(); i++) {
            XmlPath.Node node = path.at(i);
            if (XmlPath.Type.ELEMENT.equals(node.type())) {
                next = resolvePathForElement(next, node.name());
                nodes.add(next);
            }
        }
        return nodes;
    }

    /**
     * Returns the {@link XsdNode} matching the element.
     *
     * @param element the requested element
     * @return xsd node matching the element
     * @throws XsdNoSuchNodeException thrown if a node with the requested name couldn't be found
     * @throws XsdAnyException        thrown if the path reaches a xs:any, and it's unclear how to process further
     * @throws XsdAmbiguousNodeException  if multiple matching elements are found, causing ambiguity.
     */
    public XsdElement resolveXmlElement(XmlElement element)
        throws XsdNoSuchNodeException, XsdAnyException, XsdAmbiguousNodeException {
        XmlPath path = XmlPath.of(element);
        List<? extends XsdNode> nodes = resolvePath(path);
        return !nodes.isEmpty() ? (XsdElement) nodes.get(nodes.size() - 1) : null;
    }

    /**
     * Resolves a specific element within the XSD schema hierarchy.
     *
     * @param parent        the parent {@link XsdElement} within which to search for the child element.
     * @param elementToFind the {@link XmlName} representing the name and namespace of the element to find.
     * @return the resolved {@link XsdElement} if exactly one matching element is found.
     * @throws XsdNoSuchNodeException     if no matching element is found and no {@code xs:any} is present.
     * @throws XsdAnyException            if no matching element is found but an {@code xs:any} allows for any element.
     * @throws XsdAmbiguousNodeException  if multiple matching elements are found, causing ambiguity.
     */
    private XsdElement resolvePathForElement(XsdElement parent, XmlName elementToFind) throws XsdAnyException,
        XsdNoSuchNodeException, XsdAmbiguousNodeException {
        XmlExpandedName name = elementToFind.expandedName();
        List<XsdElement> children = parent.collectElements();
        Set<XsdElement> candidates = new LinkedHashSet<>();
        for (XsdElement childNode : children) {
            XsdElement namedNode = childNode.getReferenceOrSelf();
            if (namedNode.getName().equals(name)) {
                candidates.add(namedNode);
            }
        }
        if (candidates.size() == 1) {
            return candidates.iterator().next();
        }
        if (!candidates.isEmpty()) {
            throw new XsdAmbiguousNodeException("Ambiguous element definition found for '" + name + "': " + candidates);
        }
        if (parent.hasAny()) {
            throw new XsdAnyException(
                "element '" + elementToFind + "' not found but xs:any matches in parent '" + parent.getName() + "'");
        }
        throw new XsdNoSuchNodeException(
            "element '" + elementToFind + "' could not be found in parent '" + parent.getName() + "'");
    }

    private XsdAttribute resolvePathForAttribute(XsdElement parent, XmlName attributeName)
        throws XsdAnyException, XsdNoSuchNodeException {
        // add uri to attribute if empty
        // this is needed because attributes usually have an empty namespace, but the xsd definition uses namespaces
        XmlExpandedName resolvedAttributeName = attributeName.expandedName();
        if (resolvedAttributeName.uri().isEmpty() && !parent.getName().uri().isEmpty()) {
            resolvedAttributeName = new XmlExpandedName(attributeName.local(), parent.getName().uri());
        }
        // find corresponding attribute
        List<XsdAttribute> attributes = parent.collectAttributes();
        for (XsdAttribute attributeNode : attributes) {
            XsdAttribute namedNode = attributeNode.getReferenceOrSelf();
            if (namedNode.getName().equals(resolvedAttributeName)) {
                return namedNode;
            }
        }
        if (parent.hasAnyAttribute()) {
            throw new XsdAnyException("attribute '" + resolvedAttributeName +
                "' not found but xs:anyAttribute matches in node '" + parent.getName() + "'");
        }
        throw new XsdNoSuchNodeException(
            "attribute '" + resolvedAttributeName + "' could not be found in node '" + parent.getName() + "'");
    }

    @Override
    public String toString() {
        return "targetNamespace: " + getTargetNamespace() + System.lineSeparator() + toTreeString();
    }

    /**
     * Converts the XSD to a tree-structured string representation.
     *
     * @return the tree representation of the XSD.
     */
    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        this.getNamedMap().forEach((nodeClass, map) -> {
            sb.append(System.lineSeparator())
                .append(nodeClass.getSimpleName().substring(3)).append(":")
                .append(System.lineSeparator());
            map.forEach(toTreeString(sb));
        });
        return sb.toString();
    }

    protected BiConsumer<Object, XsdNode> toTreeString(StringBuilder sb) {
        return (name, node) -> node.toTreeString(sb, "  ");
    }

}
