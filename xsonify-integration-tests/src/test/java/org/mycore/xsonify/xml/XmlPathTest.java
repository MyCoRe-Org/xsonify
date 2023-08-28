package org.mycore.xsonify.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlPathTest extends XmlBaseTest {

    @Test
    public void ofPath() {
        Map<String, XmlNamespace> namespaceMap = new HashMap<>();
        namespaceMap.put("", MODS_NS);
        namespaceMap.put(XLINK_NS.prefix(), XLINK_NS);
        Assertions.assertEquals(
            "/mods",
            XmlPath.of("/mods", namespaceMap).toString());
        assertEquals(
            "/mods/@name",
            XmlPath.of("/mods/@name", namespaceMap).toString());
        assertEquals(
            "/mods/titleInfo",
            XmlPath.of("/mods/titleInfo", namespaceMap).toString());
        assertEquals(
            "/mods/titleInfo/@xlink:href",
            XmlPath.of("/mods/titleInfo/@xlink:href", namespaceMap).toString());
    }

    @Test
    public void ofElement() throws ParserConfigurationException, IOException, SAXException {
        XmlDocument xml = getXml("/xml/mods-simple.xml");
        assertEquals(
            "/mods:mods",
            XmlPath.of(xml.getRoot()).toString());
        assertEquals(
            "/mods:mods/mods:titleInfo",
            XmlPath.of(xml.getRoot().getElement("mods:titleInfo")).toString());
        assertEquals(
            "/mods:mods/mods:titleInfo/@xlink:type",
            XmlPath.of(xml.getRoot().getElement("mods:titleInfo").getXmlAttribute("xlink:type")).toString());
    }

}
