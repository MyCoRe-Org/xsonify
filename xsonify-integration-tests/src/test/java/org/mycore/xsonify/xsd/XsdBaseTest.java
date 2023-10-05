package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlEntityResolverDocumentLoader;
import org.mycore.xsonify.xml.XmlParseException;
import org.mycore.xsonify.xml.XmlParser;
import org.mycore.xsonify.xml.XmlSaxParser;
import org.xml.sax.SAXException;

import javax.xml.catalog.CatalogResolver;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

public abstract class XsdBaseTest {

    public static Collection<String> NODE_TYPES = XsdParser.NODE_TYPE_CLASS_MAP.keySet();

    public XmlDocument getXml(String resourceName)
        throws ParserConfigurationException, SAXException, IOException, XmlParseException {
        URL resource = XsdBaseTest.class.getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Unable to locate resource '" + resourceName + "'.");
        }
        XmlParser parser = new XmlSaxParser();
        return parser.parse(resource);
    }

    public Xsd getXsd(String schemaLocation)
        throws IOException, XsdParseException, ParserConfigurationException, SAXException {
        CatalogResolver catalogResolver = XsdUtil.createCatalogResolver("catalog.xml");
        XmlEntityResolverDocumentLoader loader = new XmlEntityResolverDocumentLoader(catalogResolver,
            new XmlSaxParser());
        XsdParser parser = new XsdParser(loader);
        return parser.parse(schemaLocation);
    }

}
