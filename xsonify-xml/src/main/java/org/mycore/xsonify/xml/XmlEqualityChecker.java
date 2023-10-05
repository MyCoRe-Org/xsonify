package org.mycore.xsonify.xml;

import java.util.List;

/**
 * <p>Equality checker for XML elements.</p>
 */
public class XmlEqualityChecker {

    private boolean ignoreOrder;

    private boolean normalizeText;

    private boolean ignoreAdditionalNamespaces;

    public XmlEqualityChecker() {
        this.ignoreOrder = true;
        this.normalizeText = true;
        this.ignoreAdditionalNamespaces = true;
    }

    /**
     * Compares two XML elements for equality.
     *
     * @param e1 the first XML element
     * @param e2 the second XML element
     * @return true if the XML elements are considered equal, otherwise false
     */
    private boolean equals(XmlElement e1, XmlElement e2, boolean debug) {
        boolean name = equalName(e1, e2, debug);
        boolean attributes = equalAttributes(e1, e2, debug);
        boolean additionalNamespaces = this.ignoreAdditionalNamespaces || this.equalAdditionalNamespaces(e1, e2, debug);
        boolean content = equalContent(e1, e2, debug);
        return name && attributes && additionalNamespaces && content;
    }

    public boolean equals(XmlContent c1, XmlContent c2, boolean debug) {
        if (c1 instanceof XmlElement && c2 instanceof XmlElement) {
            return equals((XmlElement) c1, (XmlElement) c2, debug);
        }
        if (c1 instanceof XmlText && c2 instanceof XmlText) {
            return equals((XmlText) c1, (XmlText) c2, debug);
        }
        return false;
    }

    public boolean equals(XmlText t1, XmlText t2, boolean debug) {
        boolean equals = normalizeText ? t1.normalize().equals(t2.normalize()) :
                         t1.get().equals(t2.get());
        if (equals) {
            return true;
        }
        return fail(t1.getParent(), t2.getParent(), debug, false);
    }

    private boolean equalName(XmlElement e1, XmlElement e2, boolean debug) {
        boolean equal = e1.getName().equals(e2.getName());
        if (equal) {
            return true;
        }
        return fail(e1, e2, debug, false);
    }

    private boolean equalAttributes(XmlElement e1, XmlElement e2, boolean debug) {
        List<XmlAttribute> attributes1 = e1.getAttributes();
        List<XmlAttribute> attributes2 = e2.getAttributes();
        if (attributes1.size() != attributes2.size()) {
            return fail(e1, e2, debug, false);
        }
        for (XmlAttribute attribute : attributes1) {
            if (!attributes2.contains(attribute)) {
                return fail(e1, e2, debug, false);
            }
        }
        return true;
    }

    private boolean equalAdditionalNamespaces(XmlElement e1, XmlElement e2, boolean debug) {
        List<XmlNamespace> namespaces1 = e1.getAdditionalNamespaces();
        List<XmlNamespace> namespaces2 = e2.getAdditionalNamespaces();
        if (namespaces1.size() != namespaces2.size()) {
            return fail(e1, e2, debug, false);
        }
        for (XmlNamespace namespace : namespaces1) {
            if (!namespaces2.contains(namespace)) {
                return fail(e1, e2, debug, false);
            }
        }
        return true;
    }

    private boolean equalContent(XmlElement e1, XmlElement e2, boolean debug) {
        List<XmlContent> content1 = e1.getContent();
        List<XmlContent> content2 = e2.getContent();
        if (content1.size() != content2.size()) {
            return fail(e1, e2, debug, true);
        }
        return ignoreOrder ? equalIgnoreOrder(e1, e2, debug)
                           : equalConsiderOrder(e1, e2, debug);
    }

    private boolean equalConsiderOrder(XmlElement e1, XmlElement e2, boolean debug) {
        List<XmlContent> content1 = e1.getContent();
        List<XmlContent> content2 = e2.getContent();
        for (int i = 0; i < content1.size(); i++) {
            if (!equals(content1.get(i), content2.get(i), debug)) {
                return fail(e1, e2, debug, true);
            }
        }
        return true;
    }

    private boolean equalIgnoreOrder(XmlElement e1, XmlElement e2, boolean debug) {
        List<XmlContent> content1 = e1.getContent();
        List<XmlContent> content2 = e2.getContent();
        for (XmlContent content : content1) {
            if (!contains(content, content2)) {
                return fail(e1, e2, debug, true);
            }
        }
        return true;
    }

    private boolean contains(XmlContent content, List<XmlContent> contents) {
        for (XmlContent other : contents) {
            if (equals(content, other, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean fail(XmlElement e1, XmlElement e2, boolean debug, boolean debugRenderContent) {
        if (!debug) {
            return false;
        }
        String path = e1.getParent() != null ? (XmlPath.of(e1.getParent()) + ":\n") : "";
        if (debugRenderContent) {
            throw new RuntimeException("\n" + path + e1 + "\ndiffers from\n" + e2);
        } else {
            throw new RuntimeException("\n" + path + e1.toXmlNoContent() + "\ndiffers from\n" + e2.toXmlNoContent());
        }
    }

}
