package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlDocumentLoader;
import org.mycore.xsonify.xml.XmlElement;
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

public abstract class XsdUtil {

    /**
     * Try's to determine the related xsd schemaLocation of a xml document.
     *
     * @param document xml document
     * @return schemaLocation of the related xsd
     */
    public static String getXsdSchemaLocation(XmlDocument document) {
        XmlElement root = document.getRoot();

        String xsiNsUri = "http://www.w3.org/2001/XMLSchema-instance";
        // schemaLocation
        String schemaLocation = root.getAttribute("schemaLocation", xsiNsUri);
        if (schemaLocation != null) {
            String[] split = schemaLocation.split(" ");
            return split.length == 2 ? split[1] : split[0];
        }

        // noNamespaceSchemaLocation
        String noNamespaceSchemaLocation = root.getAttribute("noNamespaceSchemaLocation", xsiNsUri);
        if (noNamespaceSchemaLocation != null) {
            return noNamespaceSchemaLocation;
        }

        // namespace based on root
        XmlNamespace namespace = root.getNamespace();
        if (!XmlNamespace.EMPTY.equals(namespace)) {
            return namespace.uri();
        }

        return null;
    }

    public static CatalogResolver createCatalogResolver(String resourceName) throws IOException {
        Enumeration<URL> systemResources = ClassLoader.getSystemResources(resourceName);
        URI[] catalogURIs = Collections.list(systemResources).stream()
            .map(URL::toString)
            .distinct()
            .map(URI::create)
            .toArray(URI[]::new);
        return CatalogManager.catalogResolver(CatalogFeatures.defaults(), catalogURIs);
    }

    public static Xsd getXsd(String schemaLocation)
        throws IOException, XsdParseException, ParserConfigurationException, SAXException {
        CatalogResolver catalogResolver = XsdUtil.createCatalogResolver("catalog.xml");
        return getXsd(schemaLocation, catalogResolver);
    }

    public static Xsd getXsd(String schemaLocation, CatalogResolver catalogResolver)
        throws ParserConfigurationException, SAXException {
        XmlDocumentLoader loader = new XmlDocumentLoader(catalogResolver, new XmlSaxParser());
        XsdParser parser = new XsdParser(loader);
        return parser.parse(schemaLocation);
    }

}
