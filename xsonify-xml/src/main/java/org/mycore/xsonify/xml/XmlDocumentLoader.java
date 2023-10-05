package org.mycore.xsonify.xml;

import org.xml.sax.SAXException;

import java.io.IOException;

public interface XmlDocumentLoader {

    XmlDocument load(String systemId) throws IOException, SAXException, XmlParseException;

}
