package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.node.XsdNode;

/**
 * Represents a link within the XSD, consisting of a type and a name.
 */
public record XsdLink(Class<? extends XsdNode> nodeClass, XmlExpandedName name) {

    @Override
    public String toString() {
        return name.toString() + " (" + nodeClass.getSimpleName().substring(3) + ")";
    }

}
