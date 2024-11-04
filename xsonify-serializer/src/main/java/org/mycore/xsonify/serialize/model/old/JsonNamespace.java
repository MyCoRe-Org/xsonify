package org.mycore.xsonify.serialize.model.old;

import org.mycore.xsonify.serialize.SerializerStyle;

import java.util.ArrayList;
import java.util.List;

public class JsonNamespace {

    private final String name;

    private final List<JsonInterface> interfaces;

    public JsonNamespace(String name) {
        this.name = name;
        this.interfaces = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<JsonInterface> getInterfaces() {
        return interfaces;
    }

    public void add(JsonInterface jsonInterface) {
        this.interfaces.add(jsonInterface);
    }

    @Override
    public String toString() {
        return this.toString(new SerializerStyle());
    }

    public String toString(SerializerStyle style) {
        boolean defaultNamespace = this.name.isEmpty();
        StringBuilder sb = new StringBuilder();
        String indent = defaultNamespace ? "" : "  ";
        if (!defaultNamespace) {
            sb.append("namespace ").append(this.name).append(" {").append(System.lineSeparator());
        }
        for (JsonInterface jsonInterface : interfaces) {
            sb.append(jsonInterface.toString(style, indent)).append(System.lineSeparator());
        }
        if (!defaultNamespace) {
            sb.append("}");
        }
        return sb.toString();
    }

}
