package org.mycore.xsonify.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mycore.xsonify.serialize.model.JsonElementProperty;
import org.mycore.xsonify.serialize.model.JsonModel;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;

public class Xml2JsonSerializer2 {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonModel model;

    public Xml2JsonSerializer2(JsonModel model) throws SerializationException {
        this.model = model;
    }

    public ObjectNode serialize(XmlDocument document) throws SerializationException {
        XmlElement root = document.getRoot();
        ObjectNode json = MAPPER.createObjectNode();

        JsonElementProperty jsonElementProperty = model.get(root);
        String name = jsonElementProperty.getName();
        if (jsonElementProperty.isArray()) {
            ArrayNode arrayNode = MAPPER.createArrayNode();
            json.set(name, arrayNode);
        } else {
            ObjectNode objectNode = MAPPER.createObjectNode();
            json.set(name, objectNode);
        }
        return json;
    }

}
