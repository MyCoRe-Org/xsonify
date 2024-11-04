package org.mycore.xsonify.serialize.model;

import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdNode;

import java.util.List;

public abstract class JsonElementProperty {

    private String name;

    private final List<XsdNode> path;

    private boolean array;

    private boolean optional;

    public JsonElementProperty(List<XsdNode> path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public List<XsdNode> getPath() {
        return path;
    }

    public boolean isArray() {
        return array;
    }

    public boolean isOptional() {
        return optional;
    }

    public XsdElement xsdElement() {
        if (path.isEmpty()) {
            throw new RuntimeException("invalid path");
        }
        XsdNode lastNode = path.get(path.size() - 1);
        if (!(lastNode instanceof XsdElement)) {
            throw new RuntimeException("invalid path");
        }
        return (XsdElement) lastNode;
    }

}
