package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlException;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;

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

public class Xsd {

    public static final List<XsdNodeType> NAMED_TYPES = List.of(
        XsdNodeType.ELEMENT, XsdNodeType.GROUP, XsdNodeType.COMPLEXTYPE, XsdNodeType.SIMPLETYPE,
        XsdNodeType.ATTRIBUTEGROUP, XsdNodeType.ATTRIBUTE
    );

    private final String targetNamespace;

    private final LinkedHashMap<String, XmlDocument> documentMap;

    private final LinkedHashMap<XsdNodeType, Map<XmlExpandedName, XsdNode>> namedMap;

    public Xsd(String targetNamespace, LinkedHashMap<String, XmlDocument> documentMap) {
        this.targetNamespace = targetNamespace != null ? targetNamespace : XmlNamespace.EMPTY.uri();
        this.documentMap = documentMap;
        this.namedMap = new LinkedHashMap<>();
        NAMED_TYPES.forEach(type -> this.namedMap.put(type, new LinkedHashMap<>()));
    }

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
    public XsdNode getNamedNode(XsdNodeType type, String local, String uri) {
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
    public XsdNode getNamedNode(XsdNodeType type, XmlName reference) {
        return getNamedNode(type, reference.expandedName());
    }

    /**
     * Returns the named node of the given type with the given name.
     *
     * @param type      node type
     * @param reference named reference.
     * @return the found node or null
     */
    public XsdNode getNamedNode(XsdNodeType type, XmlExpandedName reference) {
        if (!NAMED_TYPES.contains(type)) {
            return null;
        }
        return this.namedMap.get(type).get(reference);
    }

    /**
     * Same as {@link #getNamedNode(XsdNodeType, XmlExpandedName)}. Resolves the expanded name for you.
     *
     * @param type         node type
     * @param expandedName expanded name of the node
     * @return the found node or null
     */
    public XsdNode getNamedNode(XsdNodeType type, String expandedName) {
        return getNamedNode(type, XmlExpandedName.of(expandedName));
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
    public List<XsdNode> getNamedNodes(XsdNodeType type, String localName) {
        return namedMap.get(type).entrySet()
            .stream()
            .filter(entry -> entry.getKey().local().equals(localName))
            .map(Map.Entry::getValue)
            .toList();
    }

    /**
     * Sets the named node.
     *
     * @param node node itself
     */
    public void setNamedNode(XsdNode node) {
        if (!NAMED_TYPES.contains(node.getNodeType())) {
            return;
        }
        this.namedMap.get(node.getNodeType()).put(node.getName(), node);
    }

    public void addNamedNode(XsdNode node) {
        if (!NAMED_TYPES.contains(node.getNodeType())) {
            return;
        }
        Map<XmlExpandedName, XsdNode> nodeMap = this.namedMap.get(node.getNodeType());
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

    public LinkedHashMap<XsdNodeType, Map<XmlExpandedName, XsdNode>> getNamedMap() {
        return namedMap;
    }

    public List<XsdNode> getNamedNodes() {
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
    public Collection<XsdNode> getNamedNodes(XsdNodeType... types) {
        Collection<XsdNode> collection = new ArrayList<>();
        for (XsdNodeType type : types) {
            Map<XmlExpandedName, XsdNode> nodes = namedMap.get(type);
            if (nodes != null) {
                collection.addAll(nodes.values());
            }
        }
        return collection;
    }

    /**
     * <p>Collects nodes of the given type.</p>
     * <p>Runs through the hierarchy to get the required nodes.</p>
     *
     * @param types types to collect
     * @return collection of nodes
     */
    public Collection<XsdNode> collect(XsdNodeType... types) {
        List<XsdNode> nodes = new ArrayList<>();
        List<XsdNodeType> nodeTypes = Arrays.asList(types);
        for (XsdNode node : getNamedNodes()) {
            collect(node, nodeTypes, nodes);
        }
        return nodes;
    }

    public void collect(XsdNode node, List<XsdNodeType> types, Collection<XsdNode> collection) {
        if (types.contains(node.getNodeType())) {
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
        XsdNode headNode = this.getNamedNode(XsdNodeType.ELEMENT, root);
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

    public XsdNode resolveXmlElement(XmlElement element) {
        XmlPath path = XmlPath.of(element);
        List<XsdNode> nodes = resolvePath(path);
        return !nodes.isEmpty() ? nodes.get(nodes.size() - 1) : null;
    }

    private XsdNode resolvePathForElement(XsdNode parent, XmlName elementToFind) {
        List<XsdNode> children = parent.collectElements();
        for (XsdNode childNode : children) {
            XsdNode namedNode = childNode.getReferenceOrSelf();
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
        List<XsdNode> attributes = parent.collectAttributes();
        for (XsdNode attributeNode : attributes) {
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
        this.getNamedMap().forEach((type, map) -> {
            sb.append("\n").append(type).append(":\n");
            map.forEach(toTreeString(sb));
        });
        return sb.toString();
    }

    protected BiConsumer<Object, XsdNode> toTreeString(StringBuilder sb) {
        return (name, node) -> node.toTreeString(sb, "  ");
    }

}
