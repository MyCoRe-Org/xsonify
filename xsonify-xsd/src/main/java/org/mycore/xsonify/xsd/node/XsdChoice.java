package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xsd.Xsd;

public class XsdChoice extends XsdNode {

    public static final String TYPE = "choice";

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd      The XSD object to which this node belongs.
     * @param uri      The URI that identifies the XML namespace of this node.
     * @param element  The XmlElement representing this node in the XML document.
     * @param parent   The parent node of this node in the XSD hierarchy.
     */
    public XsdChoice(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public XsdChoice clone() {
        XsdChoice choice = new XsdChoice(getXsd(), getUri(), getElement(), getParent());
        cloneChildren(choice);
        return choice;
    }

}
