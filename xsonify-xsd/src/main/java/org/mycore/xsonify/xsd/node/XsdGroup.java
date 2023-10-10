package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;

import java.util.List;

public class XsdGroup extends XsdNode implements XsdReferenceable<XsdGroup> {

    public static final String TYPE = "group";

    private XmlExpandedName referenceName;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdGroup(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
    }

    public void setReferenceName(XmlExpandedName referenceName) {
        this.referenceName = referenceName;
    }

    public XmlExpandedName getReferenceName() {
        return referenceName;
    }

    @Override
    public XsdGroup getReference() {
        if (this.referenceName == null) {
            return null;
        }
        return this.getXsd().getNamedNode(XsdGroup.class, this.referenceName);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected <T> boolean collect(Class<T> type, List<Class<? extends XsdNode>> searchNodes, List<T> found,
        List<XsdNode> visited) {
        if (super.collect(type, searchNodes, found, visited)) {
            return true;
        }
        XsdGroup reference = getReference();
        if (reference != null) {
            reference.collect(type, searchNodes, found, visited);
        }
        return false;
    }

    @Override
    public XsdGroup clone() {
        XsdGroup group = new XsdGroup(getXsd(), getUri(), getElement(), getParent());
        group.setReferenceName(getReferenceName());
        cloneChildren(group);
        return group;
    }

}
