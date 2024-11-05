package org.mycore.xsonify.xsd.node;

import java.util.List;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;

/**
 * Represents an XSD attribute node within the XML schema document hierarchy.
 * This class supports reference resolution, datatype handling, and allows
 * for attributes with fixed values.
 *
 * <p>An instance of {@code XsdAttribute} may reference another attribute node
 * or contain a datatype reference within the same XSD document.</p>
 */
public class XsdAttribute extends XsdNode implements XsdReferenceable<XsdAttribute> {

    public static final String TYPE = "attribute";

    private XmlExpandedName referenceName;

    private XmlExpandedName datatypeName;

    private String fixedValue;

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

    /**
     * Sets the reference name for this attribute, pointing to another attribute in the XSD.
     *
     * @param referenceName The {@link XmlExpandedName} representing the reference.
     */
    public void setReferenceName(XmlExpandedName referenceName) {
        this.referenceName = referenceName;
    }

    /**
     * Sets the datatype name for this attribute.
     *
     * @param typeName The {@link XmlExpandedName} of the datatype.
     */
    public void setDatatypeName(XmlExpandedName typeName) {
        this.datatypeName = typeName;
    }

    /**
     * Sets a fixed value for this attribute.
     *
     * @param fixedValue The fixed value as a {@link String}.
     */
    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    /**
     * Retrieves the referenced {@code XsdAttribute} if a reference is set.
     *
     * @return The referenced {@code XsdAttribute}, or {@code null} if no reference exists.
     */
    @Override
    public XsdAttribute getReference() {
        if (this.referenceName == null) {
            return null;
        }
        return this.getXsd().getNamedNode(XsdAttribute.class, this.referenceName);
    }

    /**
     * Gets the datatype name for this attribute.
     *
     * @return The datatype name as an {@link XmlExpandedName}.
     */
    public XmlExpandedName getDatatypeName() {
        return datatypeName;
    }

    /**
     * Retrieves the datatype node associated with this attribute, if defined.
     *
     * @return The {@code XsdSimpleType} representing the datatype, or {@code null} if none is set.
     */
    public XsdSimpleType getDatatype() {
        if (datatypeName == null) {
            return null;
        }
        if (XsdBuiltInDatatypes.is(this.datatypeName)) {
            return null;
        }
        return this.getXsd().getNamedNode(XsdSimpleType.class, datatypeName);
    }

    /**
     * Checks if this attribute has a fixed value.
     *
     * @return {@code true} if a fixed value is set, {@code false} otherwise.
     */
    public boolean hasFixedValue() {
        return fixedValue != null;
    }

    /**
     * Gets the fixed value of this attribute, if set.
     *
     * @return The fixed value as a {@link String}, or {@code null} if not set.
     */
    public String getFixedValue() {
        return fixedValue;
    }

    /**
     * Gets the type of this node, represented as {@code "attribute"}.
     *
     * @return A string identifier of the node type.
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Collects nodes of a specified type from the schema hierarchy, visiting
     * referenced or datatype nodes as needed.
     *
     * @param type         The class type of nodes to collect.
     * @param searchNodes  A list of node classes to search.
     * @param found        A list to store found nodes.
     * @param visited      A list to track visited nodes.
     * @param <T>          The node type.
     * @return {@code true} if the collection process is completed, {@code false} otherwise.
     */
    @Override
    protected <T extends XsdNode> boolean collect(Class<T> type, List<Class<? extends XsdNode>> searchNodes,
        List<T> found,
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
        attribute.setFixedValue(this.fixedValue);
        cloneChildren(attribute);
        return attribute;
    }

    /**
     * Resolves attribute references within a list of {@code XsdAttribute} nodes,
     * returning each attribute's resolved reference or itself if no reference exists.
     *
     * @param nodes A list of {@code XsdAttribute} nodes to resolve.
     * @return A list of resolved {@code XsdAttribute} nodes.
     */
    public static List<XsdAttribute> resolveReferences(List<? extends XsdAttribute> nodes) {
        return nodes.stream()
            .map(XsdAttribute::getReferenceOrSelf)
            .toList();
    }

}
