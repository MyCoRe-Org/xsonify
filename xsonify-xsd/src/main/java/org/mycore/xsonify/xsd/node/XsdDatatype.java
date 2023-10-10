package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xsd.Xsd;

public abstract class XsdDatatype extends XsdNode {

    /**
     * Constructs a new XsdDatatype.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdDatatype(Xsd xsd, String uri, XmlElement element,
        XsdNode parent) {
        super(xsd, uri, element, parent);
    }

}
