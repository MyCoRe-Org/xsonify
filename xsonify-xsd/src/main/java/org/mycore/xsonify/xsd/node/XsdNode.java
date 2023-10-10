package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdDocument;

import java.util.ArrayList;
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

    private final List<XsdNode> children;

    private XsdNode parent;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
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

    public XsdNode getParent() {
        return parent;
    }

    public List<XsdNode> getChildren() {
        return children;
    }

    public <T extends XsdNode> List<T> getChildren(Class<T> nodeClass) {
        return getChildren().stream()
            .filter(nodeClass::isInstance)
            .map(nodeClass::cast)
            .toList();
    }

    public <T extends XsdNode> T getFirstChild(Class<T> nodeClass) {
        return getChildren().stream()
            .filter(nodeClass::isInstance)
            .map(nodeClass::cast)
            .findFirst()
            .orElse(null);
    }

    public void setParent(XsdNode parent) {
        this.parent = parent;
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
        // local name
        if (getLocalName() != null) {
            sb.append(getName()).append(" ");
        }
        // type - make first character upper case
        char[] typeCharArray = getType().toCharArray();
        typeCharArray[0] = Character.toUpperCase(typeCharArray[0]);
        sb.append("(").append(new String(typeCharArray)).append(")");
        return sb.toString();
    }

    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        this.toTreeString(sb, "");
        return sb.toString();
    }

    public void toTreeString(StringBuilder sb, String indent) {
        sb.append(indent).append(this).append('\n');
        this.children.forEach(child -> {
            child.toTreeString(sb, indent + "|  ");
        });
    }

    @SuppressWarnings("unchecked")
    protected <T> boolean collect(Class<T> type, List<Class<? extends XsdNode>> searchNodes, List<T> found,
        List<XsdNode> visited) {
        if (visited.contains(this)) {
            return true;
        }
        visited.add(this);
        for (XsdNode childNode : getChildren()) {
            if (childNode.getClass().isAssignableFrom(type)) {
                found.add((T) childNode);
                continue;
            }
            if (searchNodes.contains(childNode.getClass())) {
                childNode.collect(type, searchNodes, found, visited);
            }
        }
        return false;
    }

    public abstract XsdNode clone();

    protected void cloneChildren(XsdNode newParent) {
        this.getChildren().stream()
            .map(XsdNode::clone)
            .forEach(clonedChild -> {
                clonedChild.setParent(newParent);
                newParent.getChildren().add(clonedChild);
            });
    }

}
