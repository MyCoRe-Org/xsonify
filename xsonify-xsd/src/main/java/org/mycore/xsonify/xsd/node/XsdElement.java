package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

import java.util.List;

public class XsdElement extends XsdNode implements XsdReferencable<XsdElement> {

    public static final String TYPE = "element";

    /**
     * List of nodes which can contain elements. Either as a child or somewhere down their hierarchy.
     */
    public static final List<Class<? extends XsdNode>> CONTAINER_NODES = List.of(
        XsdInclude.class, XsdRedefine.class,
        XsdElement.class, XsdGroup.class,
        XsdComplexType.class,
        XsdChoice.class, XsdAll.class, XsdSequence.class,
        XsdComplexContent.class,
        XsdRestriction.class, XsdExtension.class
    );

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdElement(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, XsdNodeType.ELEMENT, element, parent);
    }

    @Override
    public XsdElement getReference() {
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
