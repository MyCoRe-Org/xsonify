package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;

import java.util.List;

public class XsdAttributeGroup extends XsdNode implements XsdReferenceable<XsdAttributeGroup> {

    public static final String TYPE = "attributeGroup";

    private XmlExpandedName referenceName;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdAttributeGroup(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
    }

    public void setReferenceName(XmlExpandedName referenceName) {
        this.referenceName = referenceName;
    }

    @Override
    public XsdAttributeGroup getReference() {
        if (this.referenceName == null) {
            return null;
        }
        return this.getXsd().getNamedNode(XsdAttributeGroup.class, this.referenceName);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        XsdAttributeGroup reference = getReference();
        if (reference != null) {
            sb.append(" -> ").append(reference);
        }
        return sb.toString();
    }

    @Override
    protected <T> boolean collect(Class<T> type, List<Class<? extends XsdNode>> searchNodes, List<T> found,
        List<XsdNode> visited) {
        if (super.collect(type, searchNodes, found, visited)) {
            return true;
        }
        XsdAttributeGroup reference = getReference();
        if (reference != null) {
            reference.collect(type, searchNodes, found, visited);
        }
        return false;
    }

    @Override
    public XsdAttributeGroup clone() {
        XsdAttributeGroup attributeGroup = new XsdAttributeGroup(getXsd(), getUri(), getElement(), getParent());
        attributeGroup.setReferenceName(this.referenceName);
        cloneChildren(attributeGroup);
        return attributeGroup;
    }

}
