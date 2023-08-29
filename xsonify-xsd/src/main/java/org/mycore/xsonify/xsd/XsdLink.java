package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlExpandedName;

/**
 * Represents a link within the XSD, consisting of a type and a name.
 */
public record XsdLink(XsdNodeType type, XmlExpandedName name) {

    @Override
    public String toString() {
        return name.toString() + "(" + type + ")";
    }

}
