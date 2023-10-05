package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;

public class XsdSimpleType extends XsdDatatype {

    public static final String TYPE = "simpleType";

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdSimpleType(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public XsdSimpleType clone() {
        XsdSimpleType simpleType = new XsdSimpleType(getXsd(), getUri(), getElement(), getParent());
        cloneChildren(simpleType);
        return simpleType;
    }

}
