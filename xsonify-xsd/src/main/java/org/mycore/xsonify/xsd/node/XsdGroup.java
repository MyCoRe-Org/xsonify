package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;

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
    public XsdGroup getReferenceOrSelf() {
        XsdGroup reference = getReference();
        return reference != null ? reference : this;
    }

    @Override
    public XsdNode getLinkedNode() {
        return getReference();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public XsdGroup clone() {
        XsdGroup group = new XsdGroup(getXsd(), getUri(), getElement(), getParent());
        group.setReferenceName(getReferenceName());
        group.setLink(getLink());
        cloneChildren(group);
        return group;
    }

}
