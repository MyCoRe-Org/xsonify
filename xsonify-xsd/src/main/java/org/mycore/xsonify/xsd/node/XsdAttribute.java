package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;

import java.util.List;

public class XsdAttribute extends XsdNode implements XsdReferenceable<XsdAttribute> {

    public static final String TYPE = "attribute";

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
    protected <T extends XsdNode> boolean collect(Class<T> type, List<Class<? extends XsdNode>> searchNodes, List<T> found,
        List<XsdNode> visited) {
        if (super.collect(type, searchNodes, found, visited)) {
            return true;
        }
        XsdAttribute reference = getReference();
        XsdDatatype datatype = getDatatype();
        if (reference != null) {
            reference.collect(type, searchNodes, found, visited);
        } else if (datatype != null) {
            datatype.collect(type, searchNodes, found, visited);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        XsdAttribute reference = getReference();
        XsdDatatype datatype = getDatatype();
        if (reference != null) {
            sb.append(" -> ").append(reference);
        } else if (datatype != null) {
            sb.append(" -> ").append(datatype);
        }
        return sb.toString();
    }

    @Override
    public XsdAttribute clone() {
        XsdAttribute attribute = new XsdAttribute(getXsd(), getUri(), getElement(), getParent());
        attribute.setReferenceName(this.referenceName);
        attribute.setDatatypeName(this.datatypeName);
        cloneChildren(attribute);
        return attribute;
    }

    public static List<XsdAttribute> resolveReferences(List<? extends XsdAttribute> nodes) {
        return nodes.stream().map(XsdAttribute::getReferenceOrSelf).toList();
    }

}
