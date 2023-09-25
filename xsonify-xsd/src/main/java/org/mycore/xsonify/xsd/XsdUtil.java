package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlDocumentLoader;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlEntityResolverDocumentLoader;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlResourceDocumentLoader;
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

import static org.mycore.xsonify.xml.XmlNamespace.XML_SCHEMA_INSTANCE_URI;

/**
 * Utility class for XSD-related operations.
 * Includes methods for determining the schema location of an XML document,
 * creating a catalog resolver, and parsing an XSD.
 */
public abstract class XsdUtil {

    /**
     * Determines the location of the associated XSD schema for a given XML document.
     *
     * <p>This method retrieves the XSD schema location from the XML document's root element.
     * It looks for the location through the following prioritized methods:</p>
     * <ol>
     *   <li>Checks for the {@code xsi:schemaLocation} attribute, which typically contains a namespace URI and the schema location separated by a space.</li>
     *   <li>If not found, checks for the {@code xsi:noNamespaceSchemaLocation} attribute, which directly specifies the schema location.</li>
     *   <li>If neither are found, it consults the namespace URI of the root element.</li>
     * </ol>
     *
     * <h4>Example:</h4>
     * <p>For the following XML document:</p>
     * <pre>
     * {@code
     * <root xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     *       xsi:schemaLocation="http://www.example.com example.xsd">
     * </root>
     * }
     * </pre>
     * <p>The method will return {@code "example.xsd"}.</p>
     * @param document The XML document whose associated XSD schema location needs to be determined.
     * @return The XSD schema location as a URI string if found; otherwise returns {@code null}.
     */
    public static String getXsdSchemaLocation(XmlDocument document) {
        XmlElement root = document.getRoot();

        // schemaLocation
        String schemaLocation = root.getAttribute("schemaLocation", XML_SCHEMA_INSTANCE_URI);
        if (schemaLocation != null) {
            String[] split = schemaLocation.split(" ");
            return split.length == 2 ? split[1] : split[0];
        }

        // noNamespaceSchemaLocation
        String noNamespaceSchemaLocation = root.getAttribute("noNamespaceSchemaLocation", XML_SCHEMA_INSTANCE_URI);
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

    /**
     * Creates a catalog resolver using the given resource name.
     *
     * @param resourceName The name of the resource to use for the catalog. Usually this is 'catalog.xml'.
     * @return A CatalogResolver instance.
     * @throws IOException If an I/O error occurs.
     */
    public static CatalogResolver createCatalogResolver(String resourceName) throws IOException {
        Enumeration<URL> systemResources = ClassLoader.getSystemResources(resourceName);
        URI[] catalogURIs = Collections.list(systemResources).stream()
            .map(URL::toString)
            .distinct()
            .map(URI::create)
            .toArray(URI[]::new);
        return CatalogManager.catalogResolver(CatalogFeatures.defaults(), catalogURIs);
    }

    /**
     * Parses an XSD from a given schema location.
     *
     * @param schemaLocation The location of the XSD schema.
     * @return A Xsd object representing the parsed XSD.
     * @throws IOException                  If an I/O error occurs while reading the 'catalog.xml'.
     * @throws XsdParseException            If an xsd parse error occur.
     * @throws ParserConfigurationException If the xml sax parser couldn't be created.
     * @throws SAXException                 for SAX errors.
     */
    public static Xsd getXsdFromCatalog(String schemaLocation)
        throws IOException, XsdParseException, ParserConfigurationException, SAXException {
        CatalogResolver catalogResolver = XsdUtil.createCatalogResolver("catalog.xml");
        return getXsdFromCatalog(schemaLocation, catalogResolver);
    }

    /**
     * Parses an XSD from a given schema location using a specified catalog resolver.
     *
     * @param schemaLocation  The location of the XSD schema.
     * @param catalogResolver The CatalogResolver to use for resolving catalogs.
     * @return A Xsd object representing the parsed XSD.
     * @throws XsdParseException            If an xsd parse error occur.
     * @throws ParserConfigurationException If the xml sax parser couldn't be created.
     * @throws SAXException                 for SAX errors.
     */
    public static Xsd getXsdFromCatalog(String schemaLocation, CatalogResolver catalogResolver)
        throws ParserConfigurationException, SAXException {
        XmlDocumentLoader loader = new XmlEntityResolverDocumentLoader(catalogResolver, new XmlSaxParser());
        XsdParser parser = new XsdParser(loader);
        return parser.parse(schemaLocation);
    }

    public static Xsd getXsdFromResource(String systemResource) throws ParserConfigurationException, SAXException {
        XmlDocumentLoader loader = new XmlResourceDocumentLoader(new XmlSaxParser());
        XsdParser parser = new XsdParser(loader);
        return parser.parse(systemResource);
    }

}
