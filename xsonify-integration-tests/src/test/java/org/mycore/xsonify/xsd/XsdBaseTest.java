package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlParser;
import org.mycore.xsonify.xml.XmlDocumentLoader;
import org.mycore.xsonify.xml.XmlException;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlSaxParser;
import org.xml.sax.SAXException;

import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public abstract class XsdBaseTest {

    public static final XmlNamespace XSI_NS = new XmlNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final XmlNamespace XLINK_NS = new XmlNamespace("xlink", "http://www.w3.org/1999/xlink");
    public static final XmlNamespace MODS_NS = new XmlNamespace("mods", "http://www.loc.gov/mods/v3");
    public static final XmlNamespace CMD_NS = new XmlNamespace("cmd", "http://www.cdlib.org/inside/diglib/copyrightMD");
    public static final XmlNamespace TEST_NS = new XmlNamespace("", "https://test.com/v1");

    public XmlDocument getXml(String resourceName) throws ParserConfigurationException, SAXException, IOException {
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
        XmlDocumentLoader loader = new XmlDocumentLoader(catalogResolver, new XmlSaxParser());
        XsdParser parser = new XsdParser(loader);
        return parser.parse(schemaLocation);
    }

}
