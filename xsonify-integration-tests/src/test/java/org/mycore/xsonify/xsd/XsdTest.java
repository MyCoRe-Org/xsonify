package org.mycore.xsonify.xsd;

import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xml.XmlNamespace;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mycore.xsonify.xml.XmlBaseTest.MODS_NS;

public class XsdTest extends XsdBaseTest {

    @Test
    public void getTargetNamespace() throws IOException, ParserConfigurationException, SAXException {
        Xsd modsXsd = getXsd("mods-3-8.xsd");
        assertEquals(MODS_NS.uri(), modsXsd.getTargetNamespace());

        Xsd testXsd = getXsd("test.xsd");
        assertEquals("https://test.com/v1", testXsd.getTargetNamespace());

        Xsd journalXsd = getXsd("datamodel-jpjournal.xsd");
        assertEquals(XmlNamespace.EMPTY.uri(), journalXsd.getTargetNamespace());
    }

}
