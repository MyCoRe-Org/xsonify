package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

public class XsdAttribute extends XsdNode implements XsdReferencable<XsdAttribute> {

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdAttribute(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, XsdNodeType.ATTRIBUTE, element, parent);
    }

    @Override
    public XsdAttribute getReference() {
        return null;
    }

}
