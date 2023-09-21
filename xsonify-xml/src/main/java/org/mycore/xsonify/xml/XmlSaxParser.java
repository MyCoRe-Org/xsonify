package org.mycore.xsonify.xml;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * XmlSaxParser is an implementation of the XmlParser interface that uses a SAX parser and a custom SAX handler
 * {@link XmlSaxBuilder} to parse an XML document into an {@link XmlDocument}.
 *
 * <p>XmlSaxParser leverages the SAX parser provided by the JDK and sets it to be namespace aware.</p>
 *
 * <p>In case of any parsing errors, an XmlParseException is thrown, wrapping the original exception.</p>
 */
public class XmlSaxParser implements XmlParser {

    private final SAXParser saxParser;

    public XmlSaxParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        this.saxParser = factory.newSAXParser();
    }

    /**
     * Parses an XML document from an InputStream into an XmlDocument.
     *
     * @param inputStream The InputStream containing the XML document to parse.
     * @return The parsed XmlDocument.
     * @throws XmlParseException If an error occurs during the parsing of the XML document.
     * @throws IOException       If an error occurs when reading the InputStream.
     */
    @Override
    public XmlDocument parse(InputStream inputStream) throws XmlParseException, IOException {
        Objects.requireNonNull(inputStream);
        XmlSaxBuilder saxBuilder = new XmlSaxBuilder();
        try {
            saxParser.parse(inputStream, saxBuilder);
        } catch (SAXException e) {
            throw new XmlParseException("Error parsing XML", e);
        }
        return saxBuilder.getDocument();
    }

}
