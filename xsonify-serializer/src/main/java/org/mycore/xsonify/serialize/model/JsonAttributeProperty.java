package org.mycore.xsonify.serialize.model;

import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdNode;

import java.util.List;

public class JsonAttributeProperty {

    private String name;

    private final List<XsdNode> path;

    private XsdJsonPrimitiveDetector.JsonPrimitive primitive;

    public JsonAttributeProperty(List<XsdNode> path) {
        this.path = path;
    }

    public XsdAttribute xsdAttribute() {
        if (path.isEmpty()) {
            throw new RuntimeException("invalid path");
        }
        XsdNode lastNode = path.get(path.size() - 1);
        if (!(lastNode instanceof XsdAttribute)) {
            throw new RuntimeException("invalid path");
        }
        return (XsdAttribute) lastNode;
    }

}
