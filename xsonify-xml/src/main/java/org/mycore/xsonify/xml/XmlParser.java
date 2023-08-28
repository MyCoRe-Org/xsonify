package org.mycore.xsonify.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * <p>Interface for XML parsing functionality.</p>
 *
 * <p>The interface provides a unified way to parse XML data from different types of sources,
 * such as an {@link InputStream} or a {@link URL}. Parsing exceptions are propagated
 * to the caller in form of {@link XmlParseException} or {@link IOException}.</p>
 */
public interface XmlParser {

    /**
     * Parse XML content from the given InputStream into an {@link XmlDocument}.
     *
     * @param inputStream the InputStream from which XML data is read
     * @return the parsed XmlDocument
     * @throws XmlParseException if an error occurs during parsing of the XML data
     * @throws IOException if an I/O error occurs when reading from the input stream
     */
    XmlDocument parse(InputStream inputStream) throws XmlParseException, IOException;

    /**
     * Parse XML content from the given URL into an {@link XmlDocument}.
     *
     * @param url the URL from which XML data is read
     * @return the parsed XmlDocument
     * @throws XmlParseException if an error occurs during parsing of the XML data
     * @throws IOException if an I/O error occurs when reading from the URL or opening the InputStream
     */
    default XmlDocument parse(URL url) throws XmlParseException, IOException {
        try (InputStream stream = url.openStream()) {
            return parse(stream);
        } catch (IOException ioException) {
            throw new IOException("Unable to parse " + url, ioException);
        } catch (XmlParseException xmlParseException) {
            throw new XmlParseException("Unable to parse " + url, xmlParseException);
        }
    }

}
