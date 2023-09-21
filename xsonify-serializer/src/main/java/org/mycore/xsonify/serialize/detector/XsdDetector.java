package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlAttribute;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;

import java.util.Map;

public interface XsdDetector<R> {

    default R detect(String path, Map<String, XmlNamespace> namespaceMap) {
        return detect(XmlPath.of(path, namespaceMap));
    }

    default R detect(XmlElement element) {
        return detect(XmlPath.of(element));
    }

    default R detect(XmlAttribute attribute) {
        return detect(XmlPath.of(attribute));
    }

    R detect(XmlPath path);

}
