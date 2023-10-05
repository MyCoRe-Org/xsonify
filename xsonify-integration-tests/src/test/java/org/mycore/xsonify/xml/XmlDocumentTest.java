package org.mycore.xsonify.xml;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlDocumentTest extends XmlBaseTest {

    @Test
    public void collectNamespaces() throws ParserConfigurationException, SAXException, IOException, XmlException {
        XmlDocument xmlDocument = getXml("/xml/mods-simple.xml");
        Map<String, XmlNamespace> namespaces = xmlDocument.collectNamespacesSqueezed();
        assertEquals(2, namespaces.size());
        assertTrue(namespaces.containsValue(MODS_NS));
        assertTrue(namespaces.containsValue(XLINK_NS));
    }

}
