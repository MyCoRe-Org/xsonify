package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlAttribute;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;

import java.util.Map;

public interface XsdDetector {

    default boolean is(String path, Map<String, XmlNamespace> namespaceMap) {
        return is(XmlPath.of(path, namespaceMap));
    }

    default boolean is(XmlElement element) {
        return is(XmlPath.of(element));
    }

    default boolean is(XmlAttribute attribute) {
        return is(XmlPath.of(attribute));
    }

    boolean is(XmlPath path);

}
