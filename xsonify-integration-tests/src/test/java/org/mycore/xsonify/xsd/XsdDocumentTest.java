package org.mycore.xsonify.xsd;

import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlException;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsdDocumentTest extends XsdBaseTest {

    @Test
    public void expandAttributes()
        throws IOException, ParserConfigurationException, SAXException, XsdParseException, XmlException {
        XmlDocument redefineADocument = getXml("/xsd/test/redefineA.xsd");

        XsdDocument xsdDocument = new XsdDocument("redefineA.xsd", "https://test.com/redefineA");
        xsdDocument.setRoot(redefineADocument.getRoot().copy(xsdDocument));
        xsdDocument.expandAttributes();
        Map<String, XmlNamespace> namespaces = xsdDocument.collectNamespacesSqueezed();

        // xs:extension/@base
        List<String> extensionBase = xsdDocument.queryAttributes(
            XmlPath.of("/xs:schema/xs:redefine/xs:complexType/xs:complexContent/xs:extension/@base", namespaces));
        assertEquals(2, extensionBase.size());
        assertTrue(extensionBase.contains("{https://test.com/redefineA}contentC"));
        assertTrue(extensionBase.contains("{https://test.com/redefineA}contentB"));

        // xs:element/@type
        List<String> elementType = xsdDocument.queryAttributes(
            XmlPath.of(
                "/xs:schema/xs:redefine/xs:complexType/xs:complexContent/xs:extension/xs:choice/xs:element/@type",
                namespaces));
        assertEquals(2, elementType.size());
        assertTrue(elementType.contains("{http://www.w3.org/2001/XMLSchema}string"));

        // xs:element/@ref
        List<String> elementRef = xsdDocument.queryAttributes(
            XmlPath.of("/xs:schema/xs:element/xs:complexType/xs:choice/xs:element/@ref", namespaces));
        assertEquals(2, elementRef.size());
        assertTrue(elementRef.contains("{https://test.com/redefineA}elementB"));
        assertTrue(elementRef.contains("{https://test.com/redefineA}elementC"));
    }

}
