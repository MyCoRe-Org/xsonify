package org.mycore.xsonify.serialize.model.old;

import org.mycore.xsonify.serialize.SerializationException;
import org.mycore.xsonify.serialize.SerializerStyle;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonModel2 {

    // key: xml namespace uri
    // value: json namespace
    private Map<String, JsonNamespace> namespaces;

    public JsonModel2(Map<String, JsonNamespace> namespaces) {
        this.namespaces = namespaces;
    }

    public Collection<JsonNamespace> getNamespaces() {
        return namespaces.values();
    }

    public void add(JsonInterface jsonInterface) throws SerializationException {
        String xmlNamespaceUri = jsonInterface.node().getUri();
        JsonNamespace jsonNamespace = namespaces.get(xmlNamespaceUri);
        if (jsonNamespace == null) {
            throw new SerializationException("Unable to find json namespace with the uri " + xmlNamespaceUri);
        }
        jsonNamespace.add(jsonInterface);
    }

    public void cleanupEmptyNamespaces() {
        this.namespaces = this.namespaces.entrySet().stream()
            .filter(entry -> !entry.getValue().getInterfaces().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public String toString() {
        return this.toString(new SerializerStyle());
    }

    public String toString(SerializerStyle style) {
        StringBuilder sb = new StringBuilder();
        for (JsonNamespace jsonNamespace : namespaces.values()) {
            sb.append(jsonNamespace.toString(style)).append(System.lineSeparator());
        }
        return sb.toString();
    }

}
