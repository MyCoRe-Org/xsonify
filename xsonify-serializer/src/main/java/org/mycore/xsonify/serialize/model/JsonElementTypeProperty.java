package org.mycore.xsonify.serialize.model;

import org.mycore.xsonify.xsd.node.XsdNode;

import java.util.List;

public class JsonElementTypeProperty extends JsonElementProperty {

    private final JsonType type;

    public JsonElementTypeProperty(List<XsdNode> path, JsonType type) {
        super(path);
        this.type = type;
    }

}
