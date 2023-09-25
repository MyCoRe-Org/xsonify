package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

public class XsdAnyAttribute extends XsdNode {

    public static final String TYPE = "anyAttribute";

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdAnyAttribute(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, XsdNodeType.ANYATTRIBUTE, element, parent);
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
