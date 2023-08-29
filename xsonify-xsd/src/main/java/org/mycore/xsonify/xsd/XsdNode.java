package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a node in the XSD (XML Schema Definition) hierarchy.
 * This class captures various properties and behaviors of an XSD node,
 * like its type, attributes, children, and links.
 */
public class XsdNode {

    private final Xsd xsd;

    private final String uri;

    private final XsdNodeType nodeType;

    private final XmlElement element;

    private final XsdNode parent;

    private final List<XsdNode> children;

    private XsdLink link;

    private List<XsdNode> elementCache;

    private List<XsdNode> attributeCache;

    /**
     * Indicates that this node has a xs:any element. It's not necessary a child, but somewhere down the hierarchy
     * in a xs:sequence or xs:choice.
     */
    private Boolean hasAny;

    /**
     * Indicates that this node contains a xs:anyAttribute. It's not necessary a child, but somewhere down its
     * hierarchy.
     */
    private Boolean hasAnyAttribute;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd      The XSD object to which this node belongs.
     * @param uri      The URI that identifies the XML namespace of this node.
     * @param nodeType The type of this node.
     * @param element  The XmlElement representing this node in the XML document.
     * @param parent   The parent node of this node in the XSD hierarchy.
     */
    public XsdNode(Xsd xsd, String uri, XsdNodeType nodeType, XmlElement element, XsdNode parent) {
        this.xsd = xsd;
        this.uri = uri;
        this.nodeType = nodeType;
        this.element = element;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public Xsd getXsd() {
        return xsd;
    }

    public String getLocalName() {
        return element.getAttribute("name");
    }

    public XmlExpandedName getName() {
        return new XmlExpandedName(getLocalName(), uri);
    }

    public XsdNodeType getNodeType() {
        return nodeType;
    }

    public XmlElement getElement() {
        return element;
    }

    public String getAttribute(String qualifiedName) {
        return element.getAttribute(qualifiedName);
    }

    public XsdLink getLink() {
        return link;
    }

    public XsdNode getLinkedNode() {
        if (this.link == null) {
            return null;
        }
        return this.xsd.getNamedNode(this.link.type(), this.link.name());
    }

    public XsdNode getParent() {
        return parent;
    }

    public List<XsdNode> getChildren() {
        return children;
    }

    public void setLink(XsdLink link) {
        this.link = link;
    }

    public String getUri() {
        return uri;
    }

    public XsdDocument getDocument() {
        return ((XsdDocument) this.getElement().getDocument());
    }

    /**
     * Clones this node and all of its children.
     * <p>Be aware that this method does not add the cloned node to the newParent.</p>
     *
     * @param newParent the newParent node
     * @return the cloned node
     */
    public XsdNode cloneTo(XsdNode newParent) {
        XsdNode clone = new XsdNode(this.getXsd(), this.getUri(), this.getNodeType(), this.getElement(), newParent);
        clone.setLink(this.link);
        getChildren().stream()
            .map(thisChild -> thisChild.cloneTo(clone))
            .forEach(clonedChild -> clone.getChildren().add(clonedChild));
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getLocalName() != null) {
            sb.append(getName()).append(" ");
        }
        sb.append("(").append(nodeType).append(")");
        if (link != null) {
            sb.append(" -> ").append(link);
        }
        return sb.toString();
    }

    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        this.toTreeString(sb, "");
        return sb.toString();
    }

    void toTreeString(StringBuilder sb, String indent) {
        sb.append(indent).append(this).append('\n');
        this.children.forEach(child -> {
            child.toTreeString(sb, indent + "|  ");
        });
    }

    /**
     * Builds various caches for optimizing node operations.
     * <p>This is usually called at the end of the {@link XsdParser} through
     * {@link Xsd#buildCache()}.</p>
     */
    public void buildCache() {
        this.collectElements();
        this.collectAttributes();
        this.hasAny();
        this.hasAnyAttribute();
    }

    /**
     * Clears the cache of this node. Should be called if the node structure has changed.
     */
    public void clearCache() {
        this.elementCache = null;
        this.attributeCache = null;
        this.hasAny = null;
        this.hasAnyAttribute = null;
    }

    /**
     * <p>Returns a list of all element nodes which can appear under this node.</p>
     * <p>Be aware that the result of this method is cached after it's first call. If the xsd structure has
     * changed, you have to call {@link Xsd#clearCache()} manually to avoid receiving incorrect data.</p>
     *
     * @return list of elements
     */
    public List<XsdNode> collectElements() {
        if (this.elementCache == null) {
            this.elementCache = new ArrayList<>();
            collect(XsdNodeType.ELEMENT, XsdNodeType.ELEMENT_CONTAINER_NODES, this, this.elementCache,
                new ArrayList<>());
        }
        return Collections.unmodifiableList(this.elementCache);
    }

    /**
     * <p>Returns a list of all attribute nodes which can appear under this node.</p>
     * <p>Be aware that the result of this method is cached after it's first call. If the xsd structure has
     * changed, you have to call {@link Xsd#clearCache()} manually to avoid receiving incorrect data.</p>
     *
     * @return list of attributes
     */
    public List<XsdNode> collectAttributes() {
        if (this.attributeCache == null) {
            this.attributeCache = new ArrayList<>();
            List<XsdNodeType> searchNodes = new ArrayList<>(XsdNodeType.ATTRIBUTE_CONTAINER_NODES);
            searchNodes.remove(XsdNodeType.ELEMENT);
            collect(XsdNodeType.ATTRIBUTE, searchNodes, this, this.attributeCache, new ArrayList<>());
        }
        return Collections.unmodifiableList(this.attributeCache);
    }

    /**
     * Same as {@link #collectAttributes()}, but additionally filters for the attribute local name.
     *
     * @param attributeLocalName name of the attribute
     * @return list of nodes which match the given local attribute name
     */
    public List<XsdNode> collectAttributes(String attributeLocalName) {
        return collectAttributes().stream()
            .filter(attributeNode -> attributeNode.getReferenceOrSelf().getLocalName().equals(attributeLocalName))
            .toList();
    }

    /**
     * Checks if this {@link XsdNode} contains any xs:any nodes down the hierarchy. Be aware that this does not search
     * though sub xs:element's.
     *
     * @return true if the node contains a xs:any node.
     */
    public boolean hasAny() {
        if (this.hasAny == null) {
            this.hasAny = this.hasAnyCheck(this, new ArrayList<>(), XsdNodeType.ANY);
        }
        return this.hasAny;
    }

    /**
     * Checks if this {@link XsdNode} contains any xs:anyAttribute nodes down the hierarchy.
     * Be aware that this does not search though sub xs:element's.
     *
     * @return true if the node contains a xs:anyAttribute node.
     */
    public boolean hasAnyAttribute() {
        if (this.hasAnyAttribute == null) {
            this.hasAnyAttribute = this.hasAnyCheck(this, new ArrayList<>(), XsdNodeType.ANYATTRIBUTE);
        }
        return this.hasAnyAttribute;
    }

    private void collect(XsdNodeType type, List<XsdNodeType> searchNodes, XsdNode node, List<XsdNode> found,
        List<XsdNode> visited) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        if (node.getLinkedNode() != null) {
            collect(type, searchNodes, node.getLinkedNode(), found, visited);
            return;
        }
        for (XsdNode childNode : node.getChildren()) {
            if (childNode.getNodeType().equals(type)) {
                found.add(childNode);
                continue;
            }
            if (searchNodes.contains(childNode.getNodeType())) {
                collect(type, searchNodes, childNode, found, visited);
            }
        }
    }

    private boolean hasAnyCheck(XsdNode node, List<XsdNode> visited, XsdNodeType nodeTypeToCheck) {
        if (visited.contains(node)) {
            return false;
        }
        visited.add(node);
        if (node.getLinkedNode() != null) {
            return hasAnyCheck(node.getLinkedNode(), visited, nodeTypeToCheck);
        }
        for (XsdNode childNode : node.getChildren()) {
            if (nodeTypeToCheck.equals(childNode.getNodeType())) {
                return true;
            }
            if (XsdNodeType.ELEMENT.equals(childNode.getNodeType())) {
                continue;
            }
            boolean hasChildAny = hasAnyCheck(childNode, visited, nodeTypeToCheck);
            if (hasChildAny) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the referenced node of this node. If there is no reference node then this node is returned. A reference
     * in xsd is defined by the @ref attribute.
     *
     * @return the reference or this node
     */
    public XsdNode getReferenceOrSelf() {
        if (this.getLinkedNode() != null && this.getLinkedNode().getNodeType().is(this.nodeType)) {
            return this.getLinkedNode();
        }
        return this;
    }

    public static List<XsdNode> resolveReferences(List<XsdNode> nodes) {
        return nodes.stream().map(XsdNode::getReferenceOrSelf).toList();
    }

}
