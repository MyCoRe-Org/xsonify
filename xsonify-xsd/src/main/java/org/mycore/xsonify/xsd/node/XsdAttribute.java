package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;
import org.mycore.xsonify.xsd.XsdNode;

import java.util.List;

public class XsdAttribute extends XsdNode implements XsdReferenceable<XsdAttribute> {

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

    private XmlExpandedName referenceName;

    private XmlExpandedName datatypeName;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdAttribute(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
    }

    public void setReferenceName(XmlExpandedName referenceName) {
        this.referenceName = referenceName;
    }

    public void setDatatypeName(XmlExpandedName typeName) {
        this.datatypeName = typeName;
    }

    @Override
    public XsdAttribute getReference() {
        if (this.referenceName == null) {
            return null;
        }
        return this.getXsd().getNamedNode(XsdAttribute.class, this.referenceName);
    }

    @Override
    public XsdAttribute getReferenceOrSelf() {
        XsdAttribute reference = getReference();
        return reference != null ? reference : this;
    }

    public XmlExpandedName getDatatypeName() {
        return datatypeName;
    }

    public XsdSimpleType getDatatype() {
        if (datatypeName == null) {
            return null;
        }
        if (XsdBuiltInDatatypes.is(this.datatypeName)) {
            return null;
        }
        return this.getXsd().getNamedNode(XsdSimpleType.class, datatypeName);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public XsdAttribute clone() {
        XsdAttribute attribute = new XsdAttribute(getXsd(), getUri(), getElement(), getParent());
        attribute.setReferenceName(this.referenceName);
        attribute.setDatatypeName(this.datatypeName);
        attribute.setLink(this.getLink()); // TODO remove
        cloneChildren(attribute);
        return attribute;
    }

}
