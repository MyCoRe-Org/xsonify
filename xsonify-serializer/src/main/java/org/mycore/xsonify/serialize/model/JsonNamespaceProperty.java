package org.mycore.xsonify.serialize.model;

import org.mycore.xsonify.xml.XmlNamespace;

public class JsonNamespaceProperty {

    private XmlNamespace namespace;

    public JsonNamespaceProperty(XmlNamespace namespace) {
        this.namespace = namespace;
    }

    public XmlNamespace getNamespace() {
        return namespace;
    }

}
