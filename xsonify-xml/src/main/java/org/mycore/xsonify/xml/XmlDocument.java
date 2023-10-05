package org.mycore.xsonify.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Represents a structured XML document.</p>
 *
 * <p>It holds a single root XmlElement, and provides methods for generating XML strings,
 * setting the root element, getting the root element, and collecting namespaces used in the document.</p>
 */
public class XmlDocument {

    private XmlElement root;

    /**
     * Sets the root element of this XmlDocument.
     *
     * @param root The root XmlElement to be set.
     * @return This XmlDocument with the newly set root.
     */
    public XmlDocument setRoot(XmlElement root) {
        this.root = root;
        return this;
    }

    /**
     * Retrieves the root element of this XmlDocument.
     *
     * @return The root XmlElement of this XmlDocument.
     */
    public XmlElement getRoot() {
        return root;
    }

    /**
     * Generates an XML string from this document. The formatting of the XML string
     * depends on the value of the pretty parameter.
     *
     * @param pretty If true, the XML string will be pretty-formatted.
     *               If false, the XML string will not be formatted.
     * @return An XML string representation of this document.
     */
    public String toXml(boolean pretty) {
        StringBuilder sb = new StringBuilder();
        if (pretty) {
            this.root.toPrettyXml(sb, "");
        } else {
            this.root.toXml(sb);
        }
        return sb.toString();
    }

    /**
     * Collects all namespaces used in this document.
     * Namespaces are represented as a map with prefix strings as keys and a set of {@link XmlNamespace} objects as values.
     *
     * @return A map of all namespaces used in this document.
     */
    public LinkedHashMap<String, LinkedHashSet<XmlNamespace>> collectNamespaces() {
        LinkedHashMap<String, LinkedHashSet<XmlNamespace>> namespaces = new LinkedHashMap<>();
        collectNamespaces(root, namespaces);
        return namespaces;
    }

    private void collectNamespaces(XmlElement element, Map<String, LinkedHashSet<XmlNamespace>> namespaces) {
        Map<String, XmlNamespace> namespacesLocal = element.getNamespacesLocal();
        namespacesLocal.forEach((prefix, namespace) -> {
            if (XmlNamespace.isDefaultNamespace(namespace)) {
                return;
            }
            namespaces.putIfAbsent(prefix, new LinkedHashSet<>());
            namespaces.get(prefix).add(namespace);
        });
        for (XmlElement childElement : element.getElements()) {
            collectNamespaces(childElement, namespaces);
        }
    }

    public Map<String, XmlNamespace> collectNamespacesSqueezed() throws XmlException {
        Map<String, XmlNamespace> map = new HashMap<>();
        for (Map.Entry<String, LinkedHashSet<XmlNamespace>> entry : collectNamespaces().entrySet()) {
            if (entry.getValue().size() != 1) {
                throw new XmlException("Multiple namespaces bound to prefix '" + entry.getKey() + "': " +
                    entry.getValue());
            }
            if (map.put(entry.getKey(), entry.getValue().iterator().next()) != null) {
                throw new XmlException("Duplicate key");
            }
        }
        return map;
    }

    /**
     * Queries the first XmlElement that matches the given XmlPath.
     *
     * @param path The XmlPath to use for querying the XmlElement.
     * @return The first XmlElement that matches the path or null if no element is found.
     * @throws XmlException if the path query is invalid.
     */
    public XmlElement queryFirstElement(XmlPath path) throws XmlException {
        List<XmlElement> elements = queryElements(path);
        return !elements.isEmpty() ? elements.get(0) : null;
    }

    /**
     * Queries the XmlDocument and returns a list of XmlElements that match the given XmlPath.
     *
     * @param path The XmlPath to use for querying the XmlElements.
     * @return A list of XmlElements that match the path or an empty list if no elements are found.
     * @throws XmlException if the path query is invalid.
     */
    public List<XmlElement> queryElements(XmlPath path) throws XmlException {
        XmlName rootName = path.root().name();
        if (!this.root.getName().equals(rootName)) {
            return Collections.emptyList();
        }
        List<XmlElement> elementList = List.of(this.root);
        if (path.size() == 1) {
            return elementList;
        }
        for (int i = 1; i < path.size(); i++) {
            XmlPath.Node node = path.at(i);
            if (!XmlPath.Type.ELEMENT.equals(node.type())) {
                throw new XmlException("Invalid path query.");
            }
            elementList = elementList.stream()
                .flatMap(element -> element.getElements(node.name()).stream())
                .toList();
            if (elementList.isEmpty()) {
                return Collections.emptyList();
            }
        }
        return elementList;
    }

    /**
     * Queries the first attribute that matches the given XmlPath.
     *
     * @param path The XmlPath to use for querying the attribute.
     * @return Value of the first attribute or null.
     * @throws XmlException if the path query is invalid.
     */
    public String queryFirstAttribute(XmlPath path) throws XmlException {
        List<String> attributes = queryAttributes(path);
        return !attributes.isEmpty() ? attributes.get(0) : null;
    }

    /**
     * Queries the XmlDocument and returns a list of attribute values that match the given XmlPath.
     *
     * @param path The XmlPath to use for querying the attributes.
     * @return A list of attribute values that match the path or an empty list if no attributes are found.
     * @throws XmlException if the path query is invalid.
     */
    public List<String> queryAttributes(XmlPath path) throws XmlException {
        XmlPath.Node attributeNode = path.last();
        // check if last node is attribute node
        if(!attributeNode.type().equals(XmlPath.Type.ATTRIBUTE)) {
            return new ArrayList<>();
        }
        // get element path
        XmlPath elementPath = new XmlPath();
        for (XmlPath.Node node : path) {
            if(attributeNode.equals(node)) {
                break;
            }
            elementPath.add(node);
        }
        // query element path
        List<XmlElement> elements = queryElements(elementPath);
        // go through attributes
        List<String> attributes = new ArrayList<>();
        for(XmlElement element : elements) {
            String attribute = element.getAttribute(attributeNode.name());
            if(attribute != null) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

}
