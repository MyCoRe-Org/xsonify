package org.mycore.xsonify.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A lightweight API providing functionality for navigating XML data similar to XPath, but with a simpler approach.
 * This class supports selection of elements and attributes.
 * <p>
 * Example usage:
 * <pre>
 *     XmlPath path = XmlPath.of(someXmlElement);
 *     for (XmlPath.Node node : path) {
 *         // process node
 *     }
 * </pre>
 */
public class XmlPath implements Iterable<XmlPath.Node> {

    /**
     * Enum defining types of XML nodes that can be handled by XmlPath.
     */
    public enum Type {
        ELEMENT, ATTRIBUTE;
    }

    /**
     * Contains the nodes of this XmlPath.
     */
    public List<Node> nodes;

    /**
     * Creates an empty XmlPath.
     */
    public XmlPath() {
        this.nodes = new ArrayList<>();
    }

    /**
     * Add a new node to the path.
     *
     * @param node the node to be added
     */
    public void add(Node node) {
        this.nodes.add(node);
    }

    /**
     * Add a new node to the path with the provided name and type.
     *
     * @param name the name of the node to be added
     * @param type the type of the node to be added
     */
    public void add(XmlName name, Type type) {
        this.nodes.add(new Node(name, type));
    }

    /**
     * Add a new node to the path at the specified index with the provided name and type.
     *
     * @param index the index at which the node should be inserted
     * @param name  the name of the node to be added
     * @param type  the type of the node to be added
     */
    public void add(int index, XmlName name, Type type) {
        this.nodes.add(index, new Node(name, type));
    }

    /**
     * Checks if the path is empty.
     *
     * @return {@code true} if empty, otherwise {@code false}
     */
    public boolean isEmpty() {
        return this.nodes.isEmpty();
    }

    /**
     * Returns the number of nodes in the path.
     *
     * @return the size of the path
     */
    public int size() {
        return this.nodes.size();
    }

    /**
     * Returns the root node (first node in the path).
     *
     * @return the root node, or {@code null} if empty
     */
    public Node root() {
        if (this.nodes.isEmpty()) {
            return null;
        }
        return this.nodes.get(0);
    }

    /**
     * Returns the last node in the path.
     *
     * @return the last node, or {@code null} if empty
     */
    public Node last() {
        if (this.nodes.isEmpty()) {
            return null;
        }
        return this.nodes.get(this.nodes.size() - 1);
    }

    /**
     * Retrieves the node at the specified index.
     *
     * @param index the index of the node
     * @return the node at the given index
     */
    public Node at(int index) {
        return this.nodes.get(index);
    }

    /**
     * Returns all nodes in the path.
     *
     * @return the list of nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public Iterator<Node> iterator() {
        return nodes.iterator();
    }

    /**
     * Returns a new XmlPath containing only element nodes, stopping at the first attribute.
     *
     * @return a new XmlPath with only element nodes
     */
    public XmlPath elements() {
        XmlPath elementsPath = new XmlPath();
        for (Node node : nodes) {
            if (node.isElement()) {
                elementsPath.add(node);
            } else {
                break;
            }
        }
        return elementsPath;
    }

    /**
     * Returns a new XmlPath that represents the relative path from the given base.
     * For instance, if this path is /a/b/c/d and base is /a/b, then the method returns /c/d.
     *
     * @param base the base path to subtract
     * @return the relative XmlPath
     * @throws IllegalArgumentException if the base path is not a prefix of this path
     */
    public XmlPath relativeTo(XmlPath base) {
        if (base.size() > this.size()) {
            throw new IllegalArgumentException("Base path is longer than the current path.");
        }
        for (int i = 0; i < base.size(); i++) {
            if (!this.at(i).equals(base.at(i))) {
                throw new IllegalArgumentException("Base path is not a prefix of the current path.");
            }
        }
        XmlPath relativePath = new XmlPath();
        for (int i = base.size(); i < this.size(); i++) {
            relativePath.add(this.at(i));
        }
        return relativePath;
    }

    @Override
    public String toString() {
        return toString((node) -> node.name.toString());
    }

    /**
     * Returns a string representation of the path, ignoring namespace prefixes.
     *
     * @return the string representation without prefixes
     */
    public String toStringIgnorePrefix() {
        return toString((node) -> node.name.local());
    }

    private String toString(Function<Node, String> nameResolver) {
        StringBuilder sb = new StringBuilder();
        nodes.forEach(node -> {
            sb.append("/");
            if (Type.ATTRIBUTE.equals(node.type)) {
                sb.append("@");
            }
            sb.append(nameResolver.apply(node));
        });
        return sb.toString();
    }

    /**
     * Create an XmlPath instance based on the specified XmlElement.
     *
     * @param element the XmlElement to generate the path from
     * @return a new XmlPath representing the hierarchy of the specified XmlElement
     */
    public static XmlPath of(XmlElement element) {
        XmlPath xmlPath = new XmlPath();
        XmlElement parent = element.getParent();
        if (parent != null) {
            do {
                xmlPath.add(0, parent.getName(), Type.ELEMENT);
            } while ((parent = parent.getParent()) != null);
        }
        xmlPath.add(element.getName(), Type.ELEMENT);
        return xmlPath;
    }

    /**
     * Create an XmlPath instance based on the specified XmlAttribute.
     *
     * @param attribute the XmlAttribute to generate the path from
     * @return a new XmlPath representing the hierarchy of the specified XmlAttribute
     */
    public static XmlPath of(XmlAttribute attribute) {
        XmlElement element = attribute.getParent();
        XmlPath xmlPath = XmlPath.of(element);
        xmlPath.add(attribute.getName(), Type.ATTRIBUTE);
        return xmlPath;
    }

    /**
     * Create an XmlPath instance based on the specified string path. Be aware that omitting the namespace map will
     * lead to an unqualified path.
     *
     * @param path the string representation of the path
     * @return a new XmlPath representing the given path
     */
    public static XmlPath of(String path) {
        return of(path, new HashMap<>());
    }

    /**
     * Create an XmlPath instance based on the specified string path.
     *
     * @param path         the string representation of the path
     * @param namespaceMap a map of namespaces used in the path
     * @return a new XmlPath representing the given path
     */
    public static XmlPath of(String path, Map<String, XmlNamespace> namespaceMap) {
        XmlPath xmlPath = new XmlPath();
        for (String s : path.split("/")) {
            if (!s.isEmpty()) {
                Node node = createNode(s, namespaceMap);
                xmlPath.add(node);
            }
        }
        return xmlPath;
    }

    /**
     * Represents a node in the path, with a name and a type.
     */
    private static Node createNode(String name, Map<String, XmlNamespace> namespaceMap) {
        Type type = name.startsWith("@") ? Type.ATTRIBUTE : Type.ELEMENT;
        String qualifiedName = Type.ATTRIBUTE.equals(type) ? name.substring(1) : name;
        return new Node(getName(qualifiedName, type, namespaceMap), type);
    }

    private static XmlName getName(String qualifiedName, Type type, Map<String, XmlNamespace> namespaceMap) {
        return XmlName.of(qualifiedName, prefix -> {
            if (Type.ATTRIBUTE.equals(type) && XmlNamespace.EMPTY.prefix().equals(prefix)) {
                return XmlNamespace.EMPTY.uri();
            }
            XmlNamespace namespace = namespaceMap.get(prefix);
            if (namespace != null) {
                return namespace.uri();
            }
            XmlNamespace defaultNamespace = XmlNamespace.getDefaultNamespace(prefix);
            if (defaultNamespace != null) {
                return defaultNamespace.uri();
            }
            throw new RuntimeException("Unable to get namespace uri for '" + qualifiedName + "'.");
        });
    }

    /**
     * Represents a node in the path.
     * 
     * @param name of the node e.g. mods:titleInfo
     * @param type of the node either element or attribute
     */
    public record Node(XmlName name, Type type) {

        public boolean isAttribute() {
            return Type.ATTRIBUTE.equals(type);
        }

        public boolean isElement() {
            return Type.ELEMENT.equals(type);
        }

    }

}
