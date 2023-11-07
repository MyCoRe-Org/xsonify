package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdBuiltInDatatypes;
import org.mycore.xsonify.xsd.XsdParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XsdElement extends XsdNode implements XsdReferenceable<XsdElement> {

    public static final String TYPE = "element";

    /**
     * List of container nodes.
     */
    public static final List<Class<? extends XsdNode>> CONTAINER_NODES = List.of(
        XsdChoice.class, XsdSequence.class, XsdAll.class,
        XsdGroup.class,
        XsdComplexType.class,
        XsdExtension.class, XsdRestriction.class,
        XsdComplexContent.class
    );

    /**
     * List of nodes which can contain xs:elements Either as a child or somewhere down their hierarchy.
     */
    public static final List<Class<? extends XsdNode>> ELEMENT_NODES = List.of(
        XsdElement.class, XsdGroup.class,
        XsdComplexType.class,
        XsdChoice.class, XsdAll.class, XsdSequence.class,
        XsdComplexContent.class,
        XsdRestriction.class, XsdExtension.class
    );

    /**
     * List of nodes which can contain attributes. Either as a child or somewhere down their hierarchy.
     */
    public static final List<Class<? extends XsdNode>> ATTRIBUTE_NODES = List.of(
        XsdGroup.class,
        XsdComplexType.class, XsdSimpleType.class,
        XsdChoice.class, XsdAll.class, XsdSequence.class,
        XsdSimpleContent.class, XsdComplexContent.class,
        XsdAttributeGroup.class,
        XsdRestriction.class, XsdExtension.class
    );

    /**
     * List of nodes which can contain xs:anyAttribute. Either as a child or somewhere down their hierarchy.
     */
    public static final List<Class<? extends XsdNode>> ANY_ATTRIBUTE_NODES = List.of(
        XsdAttributeGroup.class,
        XsdComplexContent.class,
        XsdExtension.class, XsdRestriction.class,
        XsdComplexType.class
    );

    private XmlExpandedName referenceName;

    private XmlExpandedName datatypeName;

    private List<XsdElement> elementCache;

    private List<XsdAttribute> attributeCache;

    /**
     * Indicates that this node has a xs:any element. It's not necessary a child, but somewhere down the hierarchy
     * in a xs:sequence or xs:choice.
     */
    private Boolean hasAny;

    /**
     * Indicates that this node contains a xs:anyAttribute. It's not necessary a child, but somewhere down its
     * hierarchy.
     */
    private Boolean hasAnyAttribute;

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

    /**
     * Builds various caches for optimizing node operations.
     * <p>This is usually called at the end of the {@link XsdParser} through
     * {@link Xsd#buildCache()}.</p>
     */
    public void buildCache() {
        this.collectElements();
        this.collectAttributes();
        this.hasAny();
        this.hasAnyAttribute();
    }

    /**
     * Clears the cache of this node. Should be called if the node structure has changed.
     */
    public void clearCache() {
        this.elementCache = null;
        this.attributeCache = null;
        this.hasAny = null;
        this.hasAnyAttribute = null;
    }

    /**
     * <p>Returns a list of all element nodes which can appear under this node.</p>
     * <p>Be aware that the result of this method is cached after it's first call. If the xsd structure has
     * changed, you have to call {@link Xsd#clearCache()} manually to avoid receiving incorrect data.</p>
     *
     * @return list of elements
     */
    public List<XsdElement> collectElements() {
        if (this.elementCache == null) {
            this.elementCache = new ArrayList<>();
            this.collect(XsdElement.class, ELEMENT_NODES, this.elementCache, new ArrayList<>());
        }
        return Collections.unmodifiableList(this.elementCache);
    }

    /**
     * <p>Returns a list of all attribute nodes which can appear under this node.</p>
     * <p>Be aware that the result of this method is cached after it's first call. If the xsd structure has
     * changed, you have to call {@link Xsd#clearCache()} manually to avoid receiving incorrect data.</p>
     *
     * @return list of attributes
     */
    public List<XsdAttribute> collectAttributes() {
        if (this.attributeCache == null) {
            this.attributeCache = new ArrayList<>();
            this.collect(XsdAttribute.class, ATTRIBUTE_NODES, this.attributeCache, new ArrayList<>());
        }
        return Collections.unmodifiableList(this.attributeCache);
    }

    /**
     * Same as {@link #collectAttributes()}, but additionally filters for the attribute local name.
     *
     * @param attributeLocalName name of the attribute
     * @return list of nodes which match the given local attribute name
     */
    public List<XsdAttribute> collectAttributes(String attributeLocalName) {
        return collectAttributes().stream()
            .filter(attributeNode -> attributeNode.getReferenceOrSelf().getLocalName().equals(attributeLocalName))
            .toList();
    }

    public <T extends XsdNode> boolean has(Class<T> nodeClass, List<Class<? extends XsdNode>> searchNodes) {
        List<T> nodeList = new ArrayList<>();
        this.collect(nodeClass, searchNodes, nodeList, new ArrayList<>());
        return !nodeList.isEmpty();
    }

    /**
     * Checks if this {@link XsdElement} contains a xs:any.
     *
     * @return true if the node contains a xs:any node.
     */
    public boolean hasAny() {
        if (this.hasAny == null) {
            List<XsdAny> anyNodes = new ArrayList<>();
            this.collect(XsdAny.class, CONTAINER_NODES, anyNodes, new ArrayList<>());
            this.hasAny = !anyNodes.isEmpty();
        }
        return this.hasAny;
    }

    /**
     * Checks if this {@link XsdElement} contains a xs:anyAttribute.
     *
     * @return true if the node contains a xs:anyAttribute node.
     */
    public boolean hasAnyAttribute() {
        if (this.hasAnyAttribute == null) {
            List<XsdAnyAttribute> anyAttributeNodes = new ArrayList<>();
            this.collect(XsdAnyAttribute.class, ANY_ATTRIBUTE_NODES, anyAttributeNodes, new ArrayList<>());
            this.hasAnyAttribute = !anyAttributeNodes.isEmpty();
        }
        return this.hasAnyAttribute;
    }

    @Override
    protected <T extends XsdNode> boolean collect(Class<T> type, List<Class<? extends XsdNode>> searchNodes,
        List<T> found,
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
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        XsdElement reference = getReference();
        XsdDatatype datatype = getDatatype();
        if (reference != null) {
            sb.append(" -> ").append(reference);
        } else if (datatype != null) {
            sb.append(" -> ").append(datatype);
        }
        return sb.toString();
    }

    @Override
    public XsdElement clone() {
        XsdElement element = new XsdElement(getXsd(), getUri(), getElement(), getParent());
        element.setReferenceName(this.referenceName);
        element.setDatatypeName(this.datatypeName);
        cloneChildren(element);
        return element;
    }

    public static List<XsdElement> resolveReferences(List<? extends XsdElement> nodes) {
        return nodes.stream().map(XsdElement::getReferenceOrSelf).toList();
    }

}
