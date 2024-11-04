package org.mycore.xsonify.serialize.model;

import org.mycore.xsonify.xsd.node.XsdNode;

import java.util.ArrayList;
import java.util.List;

public abstract class JsonNode<T extends XsdNode> {

    private String name;

    private final List<JsonNamespaceProperty> namespaceProperties;

    private final List<JsonElementProperty> elementProperties;

    private final List<JsonAttributeProperty> attributeProperties;

    private JsonTextProperty textProperty;

    // index, mixed content

    private final T xsdNode;

    public JsonNode(T xsdNode) {
        this.xsdNode = xsdNode;
        this.namespaceProperties = new ArrayList<>();
        this.elementProperties = new ArrayList<>();
        this.attributeProperties = new ArrayList<>();
        this.name = null;
    }

    public String getName() {
        return name;
    }

    public T getXsdNode() {
        return xsdNode;
    }

    public List<JsonNamespaceProperty> getNamespaceProperties() {
        return namespaceProperties;
    }

    public List<JsonElementProperty> getElementProperties() {
        return elementProperties;
    }

    public List<JsonAttributeProperty> getAttributeProperties() {
        return attributeProperties;
    }

    public void setName(String name) {
        this.name = name;
    }

}
