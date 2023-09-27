package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;

import java.util.ArrayList;
import java.util.List;

public class XsdUnion extends XsdNode {

    public static final String TYPE = "union";

    private final List<XmlExpandedName> memberTypes;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdUnion(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
        this.memberTypes = new ArrayList<>();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<XmlExpandedName> getMemberTypes() {
        return memberTypes;
    }

    public void addMemberType(XmlExpandedName memberType) {
        this.memberTypes.add(memberType);
    }

}
