package org.mycore.xsonify.serialize.model.old;

import org.mycore.xsonify.serialize.SerializerStyle;
import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector.JsonPrimitive;
import org.mycore.xsonify.xsd.node.XsdNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonInterface implements JsonValue {

    private final XsdNode node;

    private final Map<JsonPropertyKey, JsonProperty2> propertyMap;

    private String name;

    private boolean hasDuplicateName;

    private JsonPrimitive primitive;

    private boolean useIndex;

    private boolean hasAny;

    private boolean hasAnyAttribute;

    public JsonInterface(XsdNode node) {
        this.node = node;
        this.propertyMap = new LinkedHashMap<>();
        this.name = null;
        this.hasDuplicateName = false;
        this.primitive = null;
        this.useIndex = false;
        this.hasAny = false;
        this.hasAnyAttribute = false;
    }

    public XsdNode node() {
        return node;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public boolean hasDuplicateName() {
        return hasDuplicateName;
    }

    public void hasDuplicateName(boolean hasDuplicateName) {
        this.hasDuplicateName = hasDuplicateName;
    }

    private JsonPrimitive getPrimitive() {
        return primitive;
    }

    public void setPrimitive(JsonPrimitive primitive) {
        this.primitive = primitive;
    }

    public Map<JsonPropertyKey, JsonProperty2> propertyMap() {
        return propertyMap;
    }

    public boolean useIndex() {
        return useIndex;
    }

    public void useIndex(boolean useIndex) {
        this.useIndex = useIndex;
    }

    public boolean hasAny() {
        return hasAny;
    }

    public void hasAny(boolean hasAny) {
        this.hasAny = hasAny;
    }

    public boolean hasAnyAttribute() {
        return hasAnyAttribute;
    }

    public void hasAnyAttribute(boolean hasAnyAttribute) {
        this.hasAnyAttribute = hasAnyAttribute;
    }

    public String toString(SerializerStyle style, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("export interface ").append(this.name).append(" {").append(System.lineSeparator());
        for (Map.Entry<JsonPropertyKey, JsonProperty2> entry : this.propertyMap.entrySet()) {
            String key = entry.getKey().toString(style);
            String value = entry.getValue().toString(style);
            sb.append(indent).append(key).append(": ").append(value).append(";").append(System.lineSeparator());
        }
        sb.append(indent).append("}");
        return sb.toString();
    }

}
