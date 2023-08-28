package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlExpandedName;

public record XsdLink(XsdNodeType type, XmlExpandedName name) {

    @Override
    public String toString() {
        return name.toString() + "(" + type + ")";
    }

}
