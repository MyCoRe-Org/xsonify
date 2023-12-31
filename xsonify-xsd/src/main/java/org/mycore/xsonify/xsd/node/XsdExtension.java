package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xsd.Xsd;

public class XsdExtension extends XsdTypeDerivation {

    public static final String TYPE = "extension";

    private boolean resolved;

    /**
     * Constructs a new XsdNode.
     *
     * @param xsd     The XSD object to which this node belongs.
     * @param uri     The URI that identifies the XML namespace of this node.
     * @param element The XmlElement representing this node in the XML document.
     * @param parent  The parent node of this node in the XSD hierarchy.
     */
    public XsdExtension(Xsd xsd, String uri, XmlElement element, XsdNode parent) {
        super(xsd, uri, element, parent);
        this.resolved = false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public boolean isResolved() {
        return resolved;
    }

    @Override
    public XsdExtension clone() {
        XsdExtension extension = new XsdExtension(getXsd(), getUri(), getElement(), getParent());
        extension.setBaseName(getBaseName());
        cloneChildren(extension);
        return extension;
    }

}
