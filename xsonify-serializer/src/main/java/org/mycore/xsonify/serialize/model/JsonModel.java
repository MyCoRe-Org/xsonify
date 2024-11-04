package org.mycore.xsonify.serialize.model;

import org.mycore.xsonify.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;

public class JsonModel {

    private final List<JsonInterface> interfaces;

    private final List<JsonType> types;

    // TODO groups here too?

    public JsonModel() {
        this.interfaces = new ArrayList<>();
        this.types = new ArrayList<>();
    }

    public List<JsonInterface> getInterfaces() {
        return interfaces;
    }

    public List<JsonType> getTypes() {
        return types;
    }

    public JsonElementProperty get(XmlElement root) {
        return null;
    }

}
