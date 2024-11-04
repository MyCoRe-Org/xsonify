package org.mycore.xsonify.serialize.model.old;

import org.mycore.xsonify.serialize.SerializerStyle;

public record JsonPropertyKey(String name, JsonPropertyType type) {
    public String toString(SerializerStyle style) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        return sb.toString();
    }

}
