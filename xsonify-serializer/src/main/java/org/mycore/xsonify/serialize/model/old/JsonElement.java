package org.mycore.xsonify.serialize.model.old;

import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector;
import org.mycore.xsonify.serialize.model.JsonElementProperty;
import org.mycore.xsonify.xsd.node.XsdElement;

import java.util.ArrayList;
import java.util.List;

public class JsonElement {

    private final XsdElement xsdElement;

    private final List<JsonElementProperty> properties;

    private String name;

    private XsdJsonPrimitiveDetector.JsonPrimitive primitive;

    public JsonElement(XsdElement xsdElement) {
        this.xsdElement = xsdElement;
        this.properties = new ArrayList<>();
        this.name = null;
        this.primitive = null;
    }

    public XsdElement getXsdElement() {
        return xsdElement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JsonElementProperty> getProperties() {
        return properties;
    }

    public void setPrimitive(XsdJsonPrimitiveDetector.JsonPrimitive primitive) {
        this.primitive = primitive;
    }

    public XsdJsonPrimitiveDetector.JsonPrimitive getPrimitive() {
        return primitive;
    }

}
