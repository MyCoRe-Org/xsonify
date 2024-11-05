package org.mycore.xsonify.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class XmlResourceDocumentLoader implements XmlDocumentLoader {

    private final XmlParser xmlParser;

    public XmlResourceDocumentLoader(XmlParser xmlParser) {
        this.xmlParser = xmlParser;
    }

    @Override
    public XmlDocument load(String systemResource) throws IOException, XmlParseException {
        URL url = ClassLoader.getSystemResource(systemResource);
        if (url == null) {
            throw new NullPointerException("Unable to locate system resource " + systemResource);
        }
        try (InputStream is = url.openStream()) {
            return xmlParser.parse(is);
        }
    }

}
