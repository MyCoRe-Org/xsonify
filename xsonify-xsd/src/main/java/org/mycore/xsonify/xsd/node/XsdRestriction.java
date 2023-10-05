package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;

public class XsdRestriction extends XsdNode {

    public static final String TYPE = "restriction";

    private XmlExpandedName baseName;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdRestriction(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
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

    @Override
    public XsdRestriction clone() {
        XsdRestriction restriction = new XsdRestriction(getXsd(), getUri(), getElement(), getParent());
        restriction.setLink(getLink());
        restriction.setBaseName(getBaseName());
        cloneChildren(restriction);
        return restriction;
    }

}
