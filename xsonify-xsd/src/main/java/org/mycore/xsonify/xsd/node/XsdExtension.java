package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;
import org.mycore.xsonify.xsd.XsdNode;

public class XsdExtension extends XsdNode {

    public static final String TYPE = "extension";

    private XmlExpandedName baseName;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdExtension(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
        this.baseName = null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void setBaseName(XmlExpandedName baseName) {
        this.baseName = baseName;
    }

    public XmlExpandedName getBaseName() {
        return baseName;
    }

    public XsdDatatype getBase() {
        if (this.baseName == null) {
            return null;
        }
        if (XsdBuiltInDatatypes.is(this.baseName)) {
            return null;
        }
        XsdComplexType complexType = this.getXsd().getNamedNode(XsdComplexType.class, this.baseName);
        if (complexType != null) {
            return complexType;
        }
        return this.getXsd().getNamedNode(XsdSimpleType.class, this.baseName);
    }

    @Override
    public XsdExtension clone() {
        XsdExtension extension = new XsdExtension(getXsd(), getUri(), getElement(), getParent());
        extension.setLink(getLink());
        cloneChildren(extension);
        return extension;
    }

    /*
    TODO use base instead of "link"
    public void setBase(XsdLink base) {
        this.base = base;
    }

    public XsdLink getBase() {
        return base;
    }

    public XsdNode getBaseNode() {
        if (this.base == null) {
            return null;
        }
        return this.getXsd().getNamedNode(this.base.type(), this.base.name());
    }
    */

}
