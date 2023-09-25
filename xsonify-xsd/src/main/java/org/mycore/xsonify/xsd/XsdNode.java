package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.node.XsdAny;
import org.mycore.xsonify.xsd.node.XsdAnyAttribute;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a node in the XSD (XML Schema Definition) hierarchy.
 * This class captures various properties and behaviors of an XSD node,
 * like its type, attributes, children, and links.
 */
public abstract class XsdNode {

    private final Xsd xsd;

    private final String uri;

    private final XmlElement element;

    private final XsdNode parent;

    private final List<XsdNode> children;

    private XsdLink link;

    private List<XsdElement> elementCache;

    private List<XsdAttribute> attributeCache;

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
     * @param element  The XmlElement representing this node in the XML document.
     * @param parent   The parent node of this node in the XSD hierarchy.
     */
    public XsdNode(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        this.xsd = xsd;
        this.uri = uri;
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

    public abstract String getType();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getLocalName() != null) {
            sb.append(getName()).append(" ");
        }
        sb.append("(").append(getType()).append(")");
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
    public List<XsdElement> collectElements() {
        if (this.elementCache == null) {
            this.elementCache = new ArrayList<>();
            collect(XsdElement.class, XsdElement.CONTAINER_NODES, this, this.elementCache,
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
            List<Class<? extends XsdNode>> searchNodes = new ArrayList<>(XsdAttribute.CONTAINER_NODES);
            searchNodes.remove(XsdElement.class);
            collect(XsdAttribute.class, searchNodes, this, this.attributeCache, new ArrayList<>());
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
            this.hasAny = this.hasAnyCheck(this, new ArrayList<>(), XsdAny.class);
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
            this.hasAnyAttribute = this.hasAnyCheck(this, new ArrayList<>(), XsdAnyAttribute.class);
        }
        return this.hasAnyAttribute;
    }

    @SuppressWarnings("unchecked")
    private <T> void collect(Class<T> type, List<Class<? extends XsdNode>> searchNodes, XsdNode node,
        List<T> found, List<XsdNode> visited) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        if (node.getLinkedNode() != null) {
            collect(type, searchNodes, node.getLinkedNode(), found, visited);
            return;
        }
        for (XsdNode childNode : node.getChildren()) {
            if (childNode.getClass().equals(type)) {
                found.add((T) childNode);
                continue;
            }
            if (searchNodes.contains(childNode.getClass())) {
                collect(type, searchNodes, childNode, found, visited);
            }
        }
    }

    private boolean hasAnyCheck(XsdNode node, List<XsdNode> visited, Class<? extends XsdNode> nodeTypeToCheck) {
        if (visited.contains(node)) {
            return false;
        }
        visited.add(node);
        if (node.getLinkedNode() != null) {
            return hasAnyCheck(node.getLinkedNode(), visited, nodeTypeToCheck);
        }
        for (XsdNode childNode : node.getChildren()) {
            if (nodeTypeToCheck.equals(childNode.getClass())) {
                return true;
            }
            if (XsdElement.class.equals(childNode.getClass())) {
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
        if (this.getLinkedNode() != null && this.getLinkedNode().getType().equals(this.getType())) {
            return this.getLinkedNode();
        }
        return this;
    }

    public static List<XsdNode> resolveReferences(List<? extends XsdNode> nodes) {
        return nodes.stream().map(XsdNode::getReferenceOrSelf).toList();
    }

}
