package org.mycore.xsonify.serialize.model;

import org.mycore.xsonify.xsd.node.XsdElement;

public class JsonInterface extends JsonNode<XsdElement> {

    public JsonInterface(XsdElement xsdElement) {
        super(xsdElement);
    }

}
