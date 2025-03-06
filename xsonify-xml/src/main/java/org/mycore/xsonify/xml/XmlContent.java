package org.mycore.xsonify.xml;

/**
 * XmlContent is an abstract class that represents content within an XML document.
 * The content could be either an element or a text node.
 *
 * <p>This class provides methods for setting and getting parent elements, as well as
 * methods for generating both pretty-formatted and non-formatted XML strings.</p>
 */
public abstract class XmlContent {

    private XmlDocument document;

    private XmlElement parent;

    /**
     * Creates a new XmlContent with a null XmlDocument.
     */
    public XmlContent() {
        this(null);
    }

    /**
     * Creates a new XmlContent with the specified XmlDocument.
     *
     * @param document The XmlDocument to be associated with this XmlContent.
     */
    public XmlContent(XmlDocument document) {
        this.document = document;
    }

    /**
     * Sets the parent XmlElement for this XmlContent, and updates the document
     * for this XmlContent and all its descendants.
     *
     * @param parent The parent XmlElement.
     */
    public void setParent(XmlElement parent) {
        this.parent = parent;
        this.setDocumentAndPropagate(parent != null ? parent.getDocument() : null);
    }

    /**
     * Returns the parent element of this content.
     *
     * @return the parent element or null
     */
    public XmlElement getParent() {
        return parent;
    }

    /**
     * Sets the document of this XML element.
     *
     * @param document the new document
     */
    public void setDocument(XmlDocument document) {
        this.document = document;
    }

    /**
     * Sets the XmlDocument for this XmlContent and propagates the same document to all child content of this XmlContent.
     *
     * @param document the XmlDocument to be set
     */
    public void setDocumentAndPropagate(XmlDocument document) {
        this.setDocument(document);
    }

    /**
     * Returns the document of this XML element.
     *
     * @return the document
     */
    public XmlDocument getDocument() {
        return document;
    }

    /**
     * Detaches this XmlContent from its parent element. If this XmlContent does not have a parent,
     * this method has no effect.
     * <p>If the content is detached, its document will be also null.</p>
     */
    public void detach() {
        if (this.parent == null) {
            return;
        }
        parent.remove(this);
    }

    /**
     * Creates a copy of this XmlContent object, linking it to the provided document.
     *
     * @param document The document to which the copied XmlContent will be linked.
     * @return A copy of this XmlContent.
     */
    protected abstract XmlContent copy(XmlDocument document);

    /**
     * Formats the xml as a pretty string and appends it to the given string builder.
     *
     * @param sb the string builder to append this xml content
     * @param indent the indent to make the xml pretty
     */
    abstract void toPrettyXml(StringBuilder sb, String indent);

    /**
     * Formats the xml as string and appends it to the given string builder.
     *
     * @param sb the string builder to append this xml content
     */
    abstract void toXml(StringBuilder sb);

    /**
     * Generates a pretty-formatted XML string from this XmlContent.
     *
     * @return A pretty-formatted XML string.
     */
    public abstract String toPrettyXml();

    /**
     * Generates a non-formatted XML string from this XmlContent.
     *
     * @return A non-formatted XML string.
     */
    public abstract String toXml();

}
