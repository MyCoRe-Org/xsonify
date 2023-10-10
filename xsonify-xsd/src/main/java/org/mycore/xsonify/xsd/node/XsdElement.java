package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;

import java.util.List;

public class XsdElement extends XsdNode implements XsdReferenceable<XsdElement> {

    public static final String TYPE = "element";

    /**
     * List of nodes which can contain elements. Either as a child or somewhere down their hierarchy.
     */
    public static final List<Class<? extends XsdNode>> CONTAINER_NODES = List.of(
        XsdElement.class, XsdGroup.class,
        XsdComplexType.class,
        XsdChoice.class, XsdAll.class, XsdSequence.class,
        XsdComplexContent.class,
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
    public XsdElement(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
    }

    public void setReferenceName(XmlExpandedName referenceName) {
        this.referenceName = referenceName;
    }

    public void setDatatypeName(XmlExpandedName datatypeName) {
        this.datatypeName = datatypeName;
    }

    @Override
    public XsdElement getReference() {
        if (this.referenceName == null) {
            return null;
        }
        return this.getXsd().getNamedNode(XsdElement.class, this.referenceName);
    }

    public XmlExpandedName getDatatypeName() {
        return datatypeName;
    }

    public XsdDatatype getDatatype() {
        if (this.datatypeName == null) {
            return null;
        }
        if (XsdBuiltInDatatypes.is(this.datatypeName)) {
            return null;
        }
        XsdComplexType complexType = this.getXsd().getNamedNode(XsdComplexType.class, this.datatypeName);
        if (complexType != null) {
            return complexType;
        }
        return this.getXsd().getNamedNode(XsdSimpleType.class, this.datatypeName);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected <T> boolean collect(Class<T> type, List<Class<? extends XsdNode>> searchNodes, List<T> found,
        List<XsdNode> visited) {
        if (super.collect(type, searchNodes, found, visited)) {
            return true;
        }
        XsdElement reference = getReference();
        XsdDatatype datatype = getDatatype();
        if (reference != null) {
            reference.collect(type, searchNodes, found, visited);
        } else if (datatype != null) {
            datatype.collect(type, searchNodes, found, visited);
        }
        return false;
    }

    @Override
    public XsdElement clone() {
        XsdElement element = new XsdElement(getXsd(), getUri(), getElement(), getParent());
        element.setReferenceName(this.referenceName);
        element.setDatatypeName(this.datatypeName);
        element.setLink(this.getLink()); // TODO remove
        cloneChildren(element);
        return element;
    }

    public static List<XsdElement> resolveReferences(List<? extends XsdElement> nodes) {
        return nodes.stream().map(XsdElement::getReferenceOrSelf).toList();
    }

}
