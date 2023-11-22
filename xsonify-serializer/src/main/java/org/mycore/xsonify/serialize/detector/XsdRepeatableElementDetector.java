package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.node.XsdAll;
import org.mycore.xsonify.xsd.node.XsdAny;
import org.mycore.xsonify.xsd.node.XsdChoice;
import org.mycore.xsonify.xsd.node.XsdComplexContent;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdDatatype;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdExtension;
import org.mycore.xsonify.xsd.node.XsdGroup;
import org.mycore.xsonify.xsd.node.XsdNode;
import org.mycore.xsonify.xsd.node.XsdRestriction;
import org.mycore.xsonify.xsd.node.XsdSequence;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A detector for identifying repeatable XML elements based on XSD definitions.
 * <p>
 * This class analyzes an XSD schema and determines if a given XML path refers to a repeatable element.
 * The analysis considers elements, complex types, and groups within the XSD.
 * </p>
 */
public class XsdRepeatableElementDetector implements XsdDetector<Boolean> {

    private final XsdRoot root;

    private final Map<XsdNode, Map<XsdNode, Boolean>> rep;

    /**
     * Initializes a new instance of {@link XsdRepeatableElementDetector} using the provided XSD schema.
     *
     * @param xsd The XSD schema to be analyzed.
     * @throws XsdDetectorException if an error occurs during initialization.
     */
    public XsdRepeatableElementDetector(Xsd xsd) throws XsdDetectorException {
        this.root = createRoot(xsd);
        this.rep = new LinkedHashMap<>();
        create(xsd);
    }

    /**
     * Determines if the specified XML path refers to a repeatable element in the XSD.
     *
     * @param path The XML path to be checked.
     * @return {@code true} if the element at the given XML path is repeatable, {@code false} otherwise.
     * @throws XsdDetectorException if the element is not found in the XSD definition or another error occurs.
     */
    @Override
    public Boolean detect(XmlPath path) throws XsdDetectorException {
        if (path.isEmpty() || path.size() == 1) {
            return false;
        }
        Node rootNode = this.root.getElementNode(path.root().name().expandedName());
        if (rootNode == null) {
            throw new XsdDetectorException("Unable to find element " + path.root().name() + " in xsd definition.");
        }
        Boolean repeatable = isRepeatable(rootNode, path, 1, false);
        return repeatable != null ? repeatable : false;
    }

    private Boolean isRepeatable(Node node, XmlPath path, int index, boolean isRepeatable) {
        XmlExpandedName elementName = path.at(index).name().expandedName();
        RepeatableInfo repeatableInfo = node.children().get(elementName);
        // found an element
        if (repeatableInfo != null) {
            // it's the last element
            if (path.size() - 1 == index) {
                return isRepeatable || repeatableInfo.repeatable();
            }
            // go deeper
            return isRepeatable(repeatableInfo.node(), path, index + 1, false);
        }
        // found something else -> go deeper
        boolean found = false;
        for (RepeatableInfo childRepeatableInfo : node.children().values()) {
            if (XsdElement.TYPE.equals(childRepeatableInfo.node().getType())) {
                continue;
            }
            Boolean repeatable = isRepeatable(childRepeatableInfo.node(), path, index,
                childRepeatableInfo.repeatable() || isRepeatable);
            if (Boolean.TRUE.equals(repeatable)) {
                return Boolean.TRUE;
            } else if (repeatable != null) {
                found = true;
            }
        }
        return found ? Boolean.FALSE : null;
    }

    /**
     * Constructs the root of the XSD schema tree.
     *
     * @param xsd The XSD schema to be transformed into a tree.
     * @return The root of the constructed XSD schema tree.
     */
    private XsdRoot createRoot(Xsd xsd) {
        XsdRoot root = new XsdRoot();
        xsd.getNamedNodes(
                XsdElement.class,
                XsdComplexType.class,
                XsdGroup.class
            ).stream()
            .map(Node::new)
            .forEach(root::add);
        return root;
    }

    private void create(Xsd xsd) throws XsdDetectorException {
        // elements
        for (Node elementNode : this.root.getElementNodes()) {
            handleRootElement(xsd, elementNode);
        }
        // complex types
        for (Node complexTypeNode : this.root.getComplexTypeNodes()) {
            handleRootComplexType(xsd, complexTypeNode);
        }
        // groups
        for (Node groupNode : this.root.getGroupNodes()) {
            handeRootGroup(xsd, groupNode);
        }
    }

    private void handleRootElement(Xsd xsd, Node node) throws XsdDetectorException {
        XsdElement xsdElement = xsd.getNamedNode(XsdElement.class, node.getName());
        XsdDatatype datatype = xsdElement.getDatatype();
        if (datatype != null) {
            if (datatype instanceof XsdComplexType) {
                Node complexTypeNode = this.root.getComplexTypeNode(datatype.getName());
                node.put(complexTypeNode.getName(), new RepeatableInfo(complexTypeNode, false));
            }
            // we don't care for simple types
        } else {
            for (XsdNode xsdChildNode : xsdElement.getChildren()) {
                create(xsdChildNode, node, false);
            }
        }
    }

    private void handleRootComplexType(Xsd xsd, Node node) throws XsdDetectorException {
        XsdComplexType xsdComplexType = xsd.getNamedNode(XsdComplexType.class, node.getName());
        for (XsdNode xsdChildNode : xsdComplexType.getChildren()) {
            create(xsdChildNode, node, false);
        }
    }

    private void handeRootGroup(Xsd xsd, Node node) throws XsdDetectorException {
        XsdGroup xsdGroup = xsd.getNamedNode(XsdGroup.class, node.getName());
        for (XsdNode xsdChildNode : xsdGroup.getChildren()) {
            create(xsdChildNode, node, false);
        }
    }

    private void create(XsdNode xsdNode, Node elementNode, boolean isRepeatable) throws XsdDetectorException {
        if (!XsdElement.ELEMENT_NODES.contains(xsdNode.getClass())) {
            return;
        }
        Integer maxOccurs = getMaxOccurs(xsdNode);
        boolean forceRepeatable = isRepeatable || (maxOccurs != null && maxOccurs > 1);
        // element
        if (XsdElement.TYPE.equals(xsdNode.getType())) {
            createElement((XsdElement) xsdNode, elementNode, forceRepeatable);
            return;
        }
        // group
        if (XsdGroup.TYPE.equals(xsdNode.getType())) {
            XsdGroup groupReference = ((XsdGroup) xsdNode).getReference();
            if (groupReference != null) {
                Node groupNode = this.root.getGroupNode(groupReference.getName());
                elementNode.put(groupReference.getName(), new RepeatableInfo(groupNode, forceRepeatable));
            }
            return;
        }
        // resolve children
        for (XsdNode xsdChildNode : xsdNode.getChildren()) {
            create(xsdChildNode, elementNode, forceRepeatable);
        }
    }

    private void createElement(XsdElement xsdElement, Node elementNode, boolean forceRepeatable)
        throws XsdDetectorException {
        XsdElement reference = xsdElement.getReference();
        XsdDatatype datatype = xsdElement.getDatatype();
        if (reference != null) {
            Node globalElementNode = this.root.getElementNode(xsdElement.getReference().getName());
            boolean hasSameNodeAlready = elementNode.has(globalElementNode.getName());
            elementNode.put(globalElementNode.getName(),
                new RepeatableInfo(globalElementNode, hasSameNodeAlready || forceRepeatable));

            rep.putIfAbsent(elementNode.xsdNode, new LinkedHashMap<>());
            Map<XsdNode, Boolean> nodeMap = rep.get(elementNode.xsdNode);
            nodeMap.put(reference, hasSameNodeAlready || forceRepeatable);

        } else if (datatype != null) {
            if (datatype instanceof XsdComplexType) {
                Node globalComplexTypeNode = this.root.getComplexTypeNode(xsdElement.getDatatype().getName());
                Node childElementNode = new Node(xsdElement);
                elementNode.put(childElementNode.getName(), new RepeatableInfo(childElementNode, forceRepeatable));
                childElementNode.put(globalComplexTypeNode.getName(),
                    new RepeatableInfo(globalComplexTypeNode, false));
            }
        } else {
            rep.putIfAbsent(elementNode.xsdNode, new LinkedHashMap<>());
            Map<XsdNode, Boolean> nodeMap = rep.get(elementNode.xsdNode);
            nodeMap.put(xsdElement, forceRepeatable);

            Node childElementNode = new Node(xsdElement);
            elementNode.put(xsdElement.getName(), new RepeatableInfo(childElementNode, forceRepeatable));
            for (XsdNode xsdChildNode : xsdElement.getChildren()) {
                create(xsdChildNode, childElementNode, false);
            }
        }
    }

    public Integer getMaxOccurs(XsdNode xsdNode) throws XsdDetectorException {
        // maxOccurs is set
        String maxOccurs = xsdNode.getAttribute("maxOccurs");
        if (maxOccurs != null) {
            return maxOccurs.equals("unbounded") ? Integer.MAX_VALUE : Integer.parseInt(maxOccurs);
        }
        // maxOccurs is not set -> return default values depending on type
        switch (xsdNode.getType()) {
        case XsdComplexType.TYPE, XsdComplexContent.TYPE, XsdRestriction.TYPE, XsdExtension.TYPE -> {
            return null;
        }
        case XsdElement.TYPE, XsdGroup.TYPE -> {
            return xsdNode.getParent() == null ? null : 1;
        }
        case XsdChoice.TYPE, XsdAll.TYPE, XsdSequence.TYPE -> {
            return xsdNode.getParent().getType().equals(XsdGroup.TYPE) ? null : 1;
        }
        case XsdAny.TYPE -> {
            // relevant case?
            return 1;
        }
        }
        throw new XsdDetectorException("Unexpected Type " + xsdNode.getType());
    }

    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        this.root.nodes().forEach(e -> sb.append(e.toTreeString()).append("\n"));
        return sb.toString();
    }

    /**
     * Holds information about the repeatability and node of an element.
     */
    record RepeatableInfo(Node node, Boolean repeatable) {
    }

    private static class XsdRoot {

        private final Map<XmlExpandedName, Node> elementMap;
        private final Map<XmlExpandedName, Node> complexTypeMap;
        private final Map<XmlExpandedName, Node> groupMap;

        public XsdRoot() {
            this.elementMap = new LinkedHashMap<>();
            this.complexTypeMap = new LinkedHashMap<>();
            this.groupMap = new LinkedHashMap<>();
        }

        public void add(Node node) {
            switch (node.getType()) {
            case XsdElement.TYPE -> this.elementMap.put(node.getName(), node);
            case XsdComplexType.TYPE -> this.complexTypeMap.put(node.getName(), node);
            case XsdGroup.TYPE -> this.groupMap.put(node.getName(), node);
            }
        }

        public Collection<Node> getElementNodes() {
            return this.elementMap.values();
        }

        public Collection<Node> getComplexTypeNodes() {
            return this.complexTypeMap.values();
        }

        public Collection<Node> getGroupNodes() {
            return this.groupMap.values();
        }

        public Node getElementNode(XmlExpandedName elementName) {
            return this.elementMap.get(elementName);
        }

        public Node getComplexTypeNode(XmlExpandedName complexTypeName) {
            return this.complexTypeMap.get(complexTypeName);
        }

        public Node getGroupNode(XmlExpandedName groupName) {
            return this.groupMap.get(groupName);
        }

        public List<Node> nodes() {
            return Stream.of(elementMap.values(), complexTypeMap.values(), groupMap.values())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        }
    }

    private static class Node {

        private final XsdNode xsdNode;
        private final Map<XmlExpandedName, RepeatableInfo> children;

        private Node(XsdNode xsdNode) {
            this.xsdNode = xsdNode;
            this.children = new LinkedHashMap<>();
        }

        public Map<XmlExpandedName, RepeatableInfo> children() {
            return this.children;
        }

        public void put(XmlExpandedName name, RepeatableInfo repeatableInfo) {
            this.children.put(name, repeatableInfo);
        }

        /**
         * Checks if the given name is already set.
         *
         * @param name the name to check
         * @return true if there is already an entry with the given name, otherwise false
         */
        public boolean has(XmlExpandedName name) {
            return this.children.containsKey(name);
        }

        @Override
        public String toString() {
            return xsdNode.toString() + " (" + getType() + ")";
        }

        private XmlExpandedName getName() {
            return xsdNode.getName();
        }

        private String getType() {
            return xsdNode.getType();
        }

        private String toTreeString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this).append("\n");
            this.toTreeString(sb, "|  ");
            return sb.toString();
        }

        private void toTreeString(StringBuilder sb, String indent) {
            this.children.forEach((name, repeatableInfo) -> {
                Node node = repeatableInfo.node();
                boolean isNamedNode = node.isNamedNode();
                sb.append(indent);
                if (isNamedNode) {
                    sb.append("-> ");
                }
                sb.append(name).append(" (").append(repeatableInfo.repeatable()).append(")").append('\n');
                if (!isNamedNode) {
                    node.toTreeString(sb, indent + "|  ");
                }
            });
        }

        public boolean isNamedNode() {
            return xsdNode.getParent() == null;
        }

    }

}
