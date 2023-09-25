package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

import java.util.List;

public class XsdAttribute extends XsdNode implements XsdReferencable<XsdAttribute> {

    public static final String TYPE = "attribute";

    /**
     * List of nodes which can contain attributes. Either as a child or somewhere down their hierarchy.
     */
    public static final List<Class<? extends XsdNode>> CONTAINER_NODES = List.of(
        XsdInclude.class, XsdRedefine.class,
        XsdElement.class, XsdGroup.class,
        XsdComplexType.class, XsdSimpleType.class,
        XsdChoice.class, XsdAll.class, XsdSequence.class,
        XsdSimpleContent.class, XsdComplexContent.class,
        XsdAttributeGroup.class,
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
    public XsdAttribute(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, XsdNodeType.ATTRIBUTE, element, parent);
    }

    @Override
    public XsdAttribute getReference() {
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
