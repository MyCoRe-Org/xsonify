package org.mycore.xsonify.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.net.URI;

/**
 * <p>Responsible for loading XML documents from specified system identifiers.</p>
 * <p>It uses an {@link XmlParser} to parse the content of the XML document and an {@link EntityResolver} to resolve any
 * external entities referenced by the document.</p>
 *
 * <p>This class is typically used for loading XML documents that might reference external resources, such as DTDs,
 * or XML Schemas.</p>
 */
public class XmlDocumentLoader {

    private final EntityResolver entityResolver;

    private final XmlParser xmlParser;

    /**
     * Constructs a new XmlDocumentLoader with the provided EntityResolver and XmlParser.
     *
     * @param entityResolver The EntityResolver used to resolve any external entities referenced by XML documents loaded by this loader.
     * @param parser The XmlParser used to parse the content of the XML documents loaded by this loader.
     */
    public XmlDocumentLoader(EntityResolver entityResolver, XmlParser parser) {
        this.entityResolver = entityResolver;
        this.xmlParser = parser;
    }

    /**
     * <p>Loads an XML document from a specified system identifier.</p>
     * The system identifier is resolved using the EntityResolver provided in the constructor,
     * and the content of the document is parsed using the XmlParser provided in the constructor.
     *
     * @param systemId The system identifier of the XML document to load.
     * @return The parsed XML document.
     * @throws IOException If an I/O error occurs while reading the document.
     * @throws SAXException If a parsing error occurs while reading the document.
     */
    public XmlDocument load(String systemId) throws IOException, SAXException {
        InputSource source = loadSource(systemId);
        URI uri = URI.create(source.getSystemId());
        return this.xmlParser.parse(uri.toURL());
    }

    private InputSource loadSource(String systemId) throws IOException, SAXException {
        if (entityResolver instanceof EntityResolver2) {
            return ((EntityResolver2) entityResolver).resolveEntity(null, null, null, systemId);
        } else {
            //  if (includeURI is not absolute) {
            //    includeURI = resolve(baseURI, includeURI)
            //  }
            return entityResolver.resolveEntity(null, systemId);
        }
    }

}
