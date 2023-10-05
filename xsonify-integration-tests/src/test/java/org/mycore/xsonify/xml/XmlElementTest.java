package org.mycore.xsonify.xml;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlElementTest extends XmlBaseTest {

    private final String ACCESS_STRING = "<access type=\"public\">Open <b>S</b>ource</access>";

    @Test
    public void encodeContent() throws ParserConfigurationException, SAXException, IOException, XmlException {
        XmlDocument modsXml = getXml("/xml/mods-simple.xml");

        Map<String, XmlNamespace> namespaceMap = modsXml.collectNamespacesSqueezed();
        XmlPath path = XmlPath.of("/mods:mods/mods:accessCondition", namespaceMap);
        XmlElement accessCondition = modsXml.queryFirstElement(path);
        assertNotNull(accessCondition);
        assertEquals(ACCESS_STRING, accessCondition.encodeContent(Charset.defaultCharset()));
    }

    @Test
    public void decodeContent() throws ParserConfigurationException, SAXException, IOException, XmlParseException {
        List<XmlContent> contentList = XmlElement.decodeContent(ACCESS_STRING, Charset.defaultCharset());
        XmlElement tempElement = new XmlElement("temp");
        tempElement.addAll(contentList);
        assertEquals(ACCESS_STRING, tempElement.encodeContent(Charset.defaultCharset()));
    }

    @Test
    public void getNamespacesLocal() throws ParserConfigurationException, IOException, SAXException, XmlException {
        // mods simple
        XmlDocument modsSimple = getXml("/xml/mods-simple.xml");
        XmlElement titleInfo = modsSimple.queryFirstElement(
            XmlPath.of("/mods:mods/mods:titleInfo", modsSimple.collectNamespacesSqueezed()));
        Map<String, XmlNamespace> modsNamespacesLocal = titleInfo.getNamespacesLocal();
        assertEquals(3, modsNamespacesLocal.size());
        assertTrue(modsNamespacesLocal.containsValue(XmlNamespace.XML));
        assertTrue(modsNamespacesLocal.containsValue(XLINK_NS));
        assertTrue(modsNamespacesLocal.containsValue(MODS_NS));

        // journal
        XmlDocument journal = getXml("/xml/jportal_jpjournal_00000109.xml");
        Map<String, XmlNamespace> journalNamespacesLocal = journal.getRoot().getNamespacesLocal();
        assertEquals(2, journalNamespacesLocal.size());
        assertTrue(journalNamespacesLocal.containsValue(XSI_NS));
        assertTrue(journalNamespacesLocal.containsValue(XLINK_NS));

        // openagrar
        XmlDocument agrarMods = getXml("/xml/openagrar_mods_00084602.xml");
        XmlElement copyright = agrarMods.queryFirstElement(XmlPath.of(
            "/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:accessCondition/cmd:copyright",
            agrarMods.collectNamespacesSqueezed()));
        Map<String, XmlNamespace> copyrightNamespacesLocal = copyright.getNamespacesLocal();
        assertEquals(2, copyrightNamespacesLocal.size());
        assertTrue(copyrightNamespacesLocal.containsValue(XSI_NS));
        assertTrue(copyrightNamespacesLocal.containsValue(CMD_NS));
    }

    @Test
    public void getNamespacesInScope() throws ParserConfigurationException, IOException, SAXException, XmlException {
        // mods simple
        XmlDocument modsXml = getXml("/xml/mods-simple.xml");
        XmlElement title = modsXml.queryFirstElement(XmlPath.of("/mods:mods/mods:titleInfo/mods:title",
            modsXml.collectNamespacesSqueezed()));
        Map<String, XmlNamespace> modsNamespacesInScope = title.getNamespacesInScope();
        assertEquals(4, modsNamespacesInScope.size());
        assertTrue(modsNamespacesInScope.containsValue(XmlNamespace.EMPTY));
        assertTrue(modsNamespacesInScope.containsValue(XmlNamespace.XML));
        assertTrue(modsNamespacesInScope.containsValue(XLINK_NS));
        assertTrue(modsNamespacesInScope.containsValue(MODS_NS));

        // journal
        XmlDocument journalXml = getXml("/xml/jportal_jpjournal_00000109.xml");
        Map<String, XmlNamespace> journalNamespacesInScope = journalXml.getRoot().getNamespacesInScope();
        assertEquals(4, journalNamespacesInScope.size());
        assertTrue(journalNamespacesInScope.containsValue(XmlNamespace.EMPTY));
        assertTrue(modsNamespacesInScope.containsValue(XmlNamespace.XML));
        assertTrue(journalNamespacesInScope.containsValue(XSI_NS));
        assertTrue(journalNamespacesInScope.containsValue(XLINK_NS));

        // openagrar
        XmlDocument agrarMods = getXml("/xml/openagrar_mods_00084602.xml");
        XmlElement copyright = agrarMods.queryFirstElement(XmlPath.of(
            "/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:accessCondition/cmd:copyright",
            agrarMods.collectNamespacesSqueezed()));
        Map<String, XmlNamespace> copyrightNamespacesInScope = copyright.getNamespacesInScope();
        assertEquals(6, copyrightNamespacesInScope.size());
        assertTrue(copyrightNamespacesInScope.containsValue(XmlNamespace.EMPTY));
        assertTrue(copyrightNamespacesInScope.containsValue(XmlNamespace.XML));
        assertTrue(copyrightNamespacesInScope.containsValue(XSI_NS));
        assertTrue(copyrightNamespacesInScope.containsValue(XLINK_NS));
        assertTrue(copyrightNamespacesInScope.containsValue(MODS_NS));
        assertTrue(copyrightNamespacesInScope.containsValue(CMD_NS));
    }

    @Test
    public void getNamespacesIntroduced() throws ParserConfigurationException, IOException, SAXException, XmlException {
        // mods simple
        XmlDocument modsXml = getXml("/xml/mods-simple.xml");
        XmlElement titleInfo = modsXml.queryFirstElement(
            XmlPath.of("/mods:mods/mods:titleInfo", modsXml.collectNamespacesSqueezed()));
        Map<String, XmlNamespace> modsNamespacesIntroduced = titleInfo.getNamespacesIntroduced();
        assertEquals(0, modsNamespacesIntroduced.size());

        // journal
        XmlDocument journalXml = getXml("/xml/jportal_jpjournal_00000109.xml");
        Map<String, XmlNamespace> journalNamespacesIntroduced = journalXml.getRoot().getNamespacesIntroduced();
        assertEquals(2, journalNamespacesIntroduced.size());
        assertTrue(journalNamespacesIntroduced.containsValue(XSI_NS));
        assertTrue(journalNamespacesIntroduced.containsValue(XLINK_NS));

        // openagrar
        XmlDocument agrarMods = getXml("/xml/openagrar_mods_00084602.xml");
        XmlElement copyright = agrarMods.queryFirstElement(XmlPath.of(
            "/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:accessCondition/cmd:copyright",
            agrarMods.collectNamespacesSqueezed()));
        Map<String, XmlNamespace> copyrightNamespacesIntroduced = copyright.getNamespacesIntroduced();
        assertEquals(1, copyrightNamespacesIntroduced.size());
        assertTrue(copyrightNamespacesIntroduced.containsValue(CMD_NS));
    }

    @Test
    public void getTextNormalized() {
        XmlElement testElement = new XmlElement("test");
        XmlText c1 = new XmlText("""
            a
            b""");
        XmlText c2 = new XmlText("""
            c
            d""");
        testElement.addText(c1);
        testElement.addText(c2);
        assertEquals("a bc d", testElement.getTextNormalized());
    }

}
