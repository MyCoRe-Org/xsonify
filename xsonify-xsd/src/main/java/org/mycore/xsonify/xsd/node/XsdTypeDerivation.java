package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;

public abstract class XsdTypeDerivation extends XsdNode {

    private XmlExpandedName baseName;

    /**
     * Constructs a new XsdTypeDerivation.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdTypeDerivation(Xsd xsd, String uri, XmlElement element,
        XsdNode parent) {
        super(xsd, uri, element, parent);
        this.baseName = null;
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
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        XsdDatatype base = getBase();
        if (base != null) {
            sb.append(" -> ").append(base);
        }
        return sb.toString();
    }

}
