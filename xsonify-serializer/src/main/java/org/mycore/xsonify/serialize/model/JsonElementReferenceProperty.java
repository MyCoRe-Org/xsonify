package org.mycore.xsonify.serialize.model;

import org.mycore.xsonify.xsd.node.XsdNode;

import java.util.List;

public class JsonElementReferenceProperty extends JsonElementProperty {

    private final JsonInterface reference;

    public JsonElementReferenceProperty(List<XsdNode> path, JsonInterface reference) {
        super(path);
        this.reference = reference;
    }

}
