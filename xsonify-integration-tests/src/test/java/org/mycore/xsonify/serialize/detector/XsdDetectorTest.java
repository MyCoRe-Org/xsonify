package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlException;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlParser;
import org.mycore.xsonify.xml.XmlSaxParser;
import org.mycore.xsonify.xsd.Xsd;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public abstract class XsdDetectorTest {

    protected XmlDocument getXmlDocument(String resource) throws IOException, ParserConfigurationException,
        SAXException {
        URL xmlResource = XsdDetectorTest.class.getResource(resource);
        return getXmlParser().parse(xmlResource);
    }

    protected XmlParser getXmlParser() throws ParserConfigurationException, SAXException {
        return new XmlSaxParser();
    }

    public Map<String, XmlNamespace> getNamespaces(Xsd xsd) {
        LinkedHashMap<String, LinkedHashSet<XmlNamespace>> collectedNamespaces = xsd.collectNamespaces();
        LinkedHashMap<String, XmlNamespace> namespaces = new LinkedHashMap<>();
        collectedNamespaces.forEach((prefix, namespaceSet) -> {
            if (namespaceSet.isEmpty()) {
                throw new XmlException("Empty namespace set for prefix '" + prefix + "'.");
            }
            namespaces.put(prefix, namespaceSet.stream().findFirst().get());
        });
        return namespaces;
    }

}
