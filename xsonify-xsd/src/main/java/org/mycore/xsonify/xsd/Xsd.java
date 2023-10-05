package org.mycore.xsonify.xsd;

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
import org.mycore.xsonify.xsd.node.XsdSimpleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Represents an XSD (XML Schema Definition) and provides methods to interact with its structure and elements.
 * This class encapsulates the XSD's target namespace, associated documents, and named nodes.
 */
public class Xsd {

    public static final List<Class<? extends XsdNode>> NAMED_TYPES = List.of(
        XsdElement.class, XsdGroup.class, XsdComplexType.class, XsdSimpleType.class,
        XsdAttributeGroup.class, XsdAttribute.class
    );

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

    public LinkedHashMap<Class<? extends XsdNode>, Map<XmlExpandedName, XsdNode>> getNamedMap() {
        return namedMap;
    }

    public final <T extends XsdNode> Collection<T> collect(Class<T> type) {
        List<T> nodes = new ArrayList<>();
        for (XsdNode node : getNamedNodes()) {
            collect(node, type, nodes);
        }
        return nodes;
    }

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
     * <p>Collects nodes of the given type.</p>
     * <p>Runs through the hierarchy to get the required nodes.</p>
     *
     * @param types types to collect
     * @return collection of nodes
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

    public List<XsdNode> collect(XsdNode node, List<Class<? extends XsdNode>> types) {
        List<XsdNode> nodes = new ArrayList<>();
        collect(node, types, nodes);
        return nodes;
    }

    public void collect(XsdNode node, List<Class<? extends XsdNode>> types, Collection<XsdNode> collection) {
        if (types.contains(node.getClass())) {
            collection.add(node);
        }
        for (XsdNode childNode : node.getChildren()) {
            collect(childNode, types, collection);
        }
    }

    /**
     * <p>Collects all nodes.</p>
     * <p>Runs through the hierarchy.</p>
     *
     * @return collection of nodes
     */
    public Collection<XsdNode> collectAll() {
        List<XsdNode> collectedNodes = new ArrayList<>();
        for (XsdNode node : getNamedNodes()) {
            collectAll(node, collectedNodes);
        }
        return collectedNodes;
    }

    public void collectAll(XsdNode node, List<XsdNode> collection) {
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
     * Builds the element, attribute and xs:any cache for each @{@link XsdNode}. This will be called by the
     * {@link XsdParser} after all processing has been done. Usually it is not required to call this method manually,
     * except if you changed the structure and called {@link #clearCache()}.
     */
    public void buildCache() {
        this.collectAll().forEach(XsdNode::buildCache);
    }

    /**
     * Clears the element and attribute cache of each {@link XsdNode}. This should be called if the xsd structure has
     * changed.
     */
    public void clearCache() {
        this.collectAll().forEach(XsdNode::clearCache);
    }

    /**
     * Returns a list of {@link XsdNode} matching the given path.
     *
     * @param path the requested path
     * @return list of xsd nodes
     * @throws NoSuchElementException thrown if a node with the requested name couldn't be found
     * @throws XsdAnyException        thrown if the path reaches a xs:any or xs:anyAttribute node,
     *                                and it's unclear how to process further
     */
    public List<XsdNode> resolvePath(XmlPath path) throws NoSuchElementException, XsdAnyException {
        if (path.isEmpty()) {
            return new ArrayList<>();
        }
        List<XsdNode> nodes = new ArrayList<>();
        XmlName root = path.root().name();
        XsdNode headNode = this.getNamedNode(XsdElement.class, root);
        if (headNode == null) {
            throw new NoSuchElementException(root + " could not be found!");
        }
        nodes.add(headNode);
        XsdNode next = headNode;
        for (int i = 1; i < path.size(); i++) {
            XmlPath.Node node = path.at(i);
            if (XmlPath.Type.ELEMENT.equals(node.type())) {
                next = resolvePathForElement(next, node.name());
            } else {
                next = resolvePathForAttribute(next, node.name());
            }
            nodes.add(next);
        }
        return nodes;
    }

    /**
     * Returns the {@link XsdNode} matching the element.
     *
     * @param element the requested element
     * @return xsd node matching the element
     * @throws NoSuchElementException thrown if a node with the requested name couldn't be found
     * @throws XsdAnyException        thrown if the path reaches a xs:any, and it's unclear how to process further
     */
    public XsdElement resolveXmlElement(XmlElement element) throws NoSuchElementException, XsdAnyException {
        XmlPath path = XmlPath.of(element);
        List<XsdNode> nodes = resolvePath(path);
        return !nodes.isEmpty() ? (XsdElement) nodes.get(nodes.size() - 1) : null;
    }

    private XsdElement resolvePathForElement(XsdNode parent, XmlName elementToFind) throws XsdAnyException,
        NoSuchElementException {
        List<XsdElement> children = parent.collectElements();
        for (XsdElement childNode : children) {
            XsdElement namedNode = childNode.getReferenceOrSelf();
            if (namedNode.getName().equals(elementToFind.expandedName())) {
                return namedNode;
            }
        }
        if (parent.hasAny()) {
            throw new XsdAnyException(
                "element '" + elementToFind + "' not found but xs:any matches in parent '" + parent.getName() + "'");
        }
        throw new NoSuchElementException(
            "element '" + elementToFind + "' could not be found in parent '" + parent.getName() + "'");
    }

    private XsdNode resolvePathForAttribute(XsdNode parent, XmlName attributeName) {
        // add uri to attribute if empty
        // this is needed because attributes usually have an empty namespace, but the xsd definition uses namespaces
        XmlExpandedName resolvedAttributeName = attributeName.expandedName();
        if (resolvedAttributeName.uri().isEmpty() && !parent.getName().uri().isEmpty()) {
            resolvedAttributeName = new XmlExpandedName(attributeName.local(), parent.getName().uri());
        }
        // find corresponding attribute
        List<XsdAttribute> attributes = parent.collectAttributes();
        for (XsdAttribute attributeNode : attributes) {
            XsdNode namedNode = attributeNode.getReferenceOrSelf();
            if (namedNode.getName().equals(resolvedAttributeName)) {
                return namedNode;
            }
        }
        if (parent.hasAnyAttribute()) {
            throw new XsdAnyException("attribute '" + resolvedAttributeName +
                "' not found but xs:anyAttribute matches in node '" + parent.getName() + "'");
        }
        throw new NoSuchElementException(
            "attribute '" + resolvedAttributeName + "' could not be found in node '" + parent.getName() + "'");
    }

    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        this.getNamedMap().forEach((nodeClass, map) -> {
            sb.append("\n").append(nodeClass.getSimpleName().substring(3)).append(":\n");
            map.forEach(toTreeString(sb));
        });
        return sb.toString();
    }

    protected BiConsumer<Object, XsdNode> toTreeString(StringBuilder sb) {
        return (name, node) -> node.toTreeString(sb, "  ");
    }

}
