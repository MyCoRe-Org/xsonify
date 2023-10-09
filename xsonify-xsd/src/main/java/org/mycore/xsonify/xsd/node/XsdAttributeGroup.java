package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;

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
    public XsdAttributeGroup getReferenceOrSelf() {
        XsdAttributeGroup reference = getReference();
        return reference != null ? reference : this;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public XsdAttributeGroup clone() {
        XsdAttributeGroup attributeGroup = new XsdAttributeGroup(getXsd(), getUri(), getElement(), getParent());
        attributeGroup.setReferenceName(this.referenceName);
        cloneChildren(attributeGroup);
        return attributeGroup;
    }

}
