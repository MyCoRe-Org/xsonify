package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.serialize.SerializerException;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XsdRepeatableElementDetector implements XsdDetector<Boolean> {

    private final XsdRoot root;

    public XsdRepeatableElementDetector(Xsd xsd) {
        this.root = createRoot(xsd);
        create(xsd);
    }

    @Override
    public Boolean detect(XmlPath path) {
        if (path.isEmpty() || path.size() == 1) {
            return false;
        }
        Node rootNode = this.root.getElementNode(path.root().name().expandedName());
        if (rootNode == null) {
            throw new SerializerException("Unable to find element " + path.root().name() + " in xsd definition.");
        }
        Boolean repeatable = isRepeatable(rootNode, path, 1, false);
        return repeatable != null ? repeatable : false;
    }

    private Boolean isRepeatable(Node node, XmlPath path, int index, boolean isRepeatable) {
        XmlExpandedName elementName = path.at(index).name().expandedName();
        RepeatableInfo repeatableInfo = node.getChildren().get(elementName);
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
        for (RepeatableInfo repeatableNode : node.getChildren().values()) {
            if (XsdNodeType.ELEMENT.equals(repeatableNode.node().nodeType)) {
                continue;
            }
            Boolean repeatable = isRepeatable(repeatableNode.node(), path, index,
                repeatableNode.repeatable() || isRepeatable);
            if (Boolean.TRUE.equals(repeatable)) {
                return Boolean.TRUE;
            } else if (repeatable != null) {
                found = true;
            }
        }
        return found ? Boolean.FALSE : null;
    }

    record RepeatableInfo(Node node, Boolean repeatable) {
    }

    private XsdRoot createRoot(Xsd xsd) {
        XsdRoot root = new XsdRoot();
        xsd.getNamedNodes(
                XsdNodeType.ELEMENT,
                XsdNodeType.COMPLEXTYPE,
                XsdNodeType.GROUP
            ).stream()
            .map(xsdNode -> new Node(xsdNode.getName(), xsdNode.getNodeType(), true))
            .forEach(root::add);
        return root;
    }

    private void create(Xsd xsd) {
        // elements
        this.root.getElementNodes().forEach(node -> {
            XsdNode xsdNode = xsd.getNamedNode(XsdNodeType.ELEMENT, node.name);
            XsdNode link = xsdNode.getLinkedNode();
            if (link != null) {
                if (XsdNodeType.COMPLEXTYPE.equals(link.getNodeType())) {
                    Node complexTypeNode = this.root.getComplexTypeNode(link.getName());
                    node.put(complexTypeNode.name, new RepeatableInfo(complexTypeNode, false));
                }
            } else {
                xsdNode.getChildren().forEach(xsdChildNode -> create(xsdChildNode, node, false));
            }
        });
        // complex types
        this.root.getComplexTypeNodes().forEach(node -> {
            XsdNode xsdNode = xsd.getNamedNode(XsdNodeType.COMPLEXTYPE, node.name);
            xsdNode.getChildren().forEach(xsdChildNode -> create(xsdChildNode, node, false));
        });
        // groups
        this.root.getGroupNodes().forEach(node -> {
            XsdNode xsdNode = xsd.getNamedNode(XsdNodeType.GROUP, node.name);
            xsdNode.getChildren().forEach(xsdChildNode -> create(xsdChildNode, node, false));
        });
    }

    private void create(XsdNode xsdNode, Node elementNode, boolean isRepeatable) {
        if (!XsdNodeType.ELEMENT_CONTAINER_NODES.contains(xsdNode.getNodeType())) {
            return;
        }
        Integer maxOccurs = getMaxOccurs(xsdNode);
        boolean forceRepeatable = isRepeatable || (maxOccurs != null && maxOccurs > 1);
        // element
        if (XsdNodeType.ELEMENT.equals(xsdNode.getNodeType())) {
            createElement(xsdNode, elementNode, forceRepeatable);
            return;
        }
        // group reference
        if (xsdNode.getLinkedNode() != null) {
            if (XsdNodeType.GROUP.equals(xsdNode.getNodeType())) {
                Node groupNode = this.root.getGroupNode(xsdNode.getLinkedNode().getName());
                elementNode.put(xsdNode.getLinkedNode().getName(), new RepeatableInfo(groupNode, forceRepeatable));
            } else {
                throw new SerializerException("unexpected reference " + xsdNode.getLinkedNode());
            }
            return;
        }
        // resolve children
        for (XsdNode xsdChildNode : xsdNode.getChildren()) {
            create(xsdChildNode, elementNode, forceRepeatable);
        }
    }

    private void createElement(XsdNode xsdNode, Node elementNode, boolean forceRepeatable) {
        if (xsdNode.getLinkedNode() != null) {
            switch (xsdNode.getLinkedNode().getNodeType()) {
            case ELEMENT -> {
                Node globalElementNode = this.root.getElementNode(xsdNode.getLinkedNode().getName());
                boolean hasSameNodeAlready = elementNode.has(globalElementNode.name);
                elementNode.put(globalElementNode.name,
                    new RepeatableInfo(globalElementNode, hasSameNodeAlready || forceRepeatable));
            }
            case COMPLEXTYPE -> {
                Node globalComplexTypeNode = this.root.getComplexTypeNode(xsdNode.getLinkedNode().getName());
                Node childElementNode = new Node(xsdNode.getName(), XsdNodeType.ELEMENT, false);
                elementNode.put(childElementNode.name, new RepeatableInfo(childElementNode, forceRepeatable));
                childElementNode.put(globalComplexTypeNode.name, new RepeatableInfo(globalComplexTypeNode, false));
            }
            }
        } else {
            Node childElementNode = new Node(xsdNode.getName(), XsdNodeType.ELEMENT, false);
            elementNode.put(xsdNode.getName(), new RepeatableInfo(childElementNode, forceRepeatable));
            for (XsdNode xsdChildNode : xsdNode.getChildren()) {
                create(xsdChildNode, childElementNode, false);
            }
        }
    }

    public Integer getMaxOccurs(XsdNode xsdNode) {
        // maxOccurs is set
        String maxOccurs = xsdNode.getAttribute("maxOccurs");
        if (maxOccurs != null) {
            return maxOccurs.equals("unbounded") ? Integer.MAX_VALUE : Integer.parseInt(maxOccurs);
        }
        // maxOccurs is not set -> return default values depending on type
        switch (xsdNode.getNodeType()) {
        case COMPLEXTYPE, COMPLEXCONTENT, RESTRICTION, EXTENSION -> {
            return null;
        }
        case ELEMENT, GROUP -> {
            return xsdNode.getParent() == null ? null : 1;
        }
        case CHOICE, ALL, SEQUENCE -> {
            return xsdNode.getParent().getNodeType().equals(XsdNodeType.GROUP) ? null : 1;
        }
        case ANY -> {
            // relevant case?
            return 1;
        }
        }
        throw new SerializerException("Unexpected Type " + xsdNode.getNodeType());
    }

    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        this.root.nodes().forEach(e -> sb.append(e.toTreeString()).append("\n"));
        return sb.toString();
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
            switch (node.nodeType) {
            case ELEMENT -> this.elementMap.put(node.name, node);
            case COMPLEXTYPE -> this.complexTypeMap.put(node.name, node);
            case GROUP -> this.groupMap.put(node.name, node);
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

        private final XmlExpandedName name;

        private final XsdNodeType nodeType;

        private final boolean root;

        private final Map<XmlExpandedName, RepeatableInfo> children;

        public Node(XmlExpandedName name, XsdNodeType nodeType, boolean root) {
            this.name = name;
            this.nodeType = nodeType;
            this.root = root;
            this.children = new LinkedHashMap<>();
        }

        public Map<XmlExpandedName, RepeatableInfo> getChildren() {
            return children;
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
            return this.name.toString() + " (" + nodeType + ")";
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
                boolean isRoot = node.root;
                sb.append(indent);
                if (isRoot) {
                    sb.append("-> ");
                }
                sb.append(name).append(" (").append(repeatableInfo.repeatable()).append(")").append('\n');
                if (!isRoot) {
                    node.toTreeString(sb, indent + "|  ");
                }
            });
        }

    }

}
