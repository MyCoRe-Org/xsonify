package org.mycore.xsonify.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class XmlParserIntegrationTest extends XmlBaseTest {

    @Test
    public void modsSimple() throws Exception {
        XmlDocument xml = getXml("/xml/mods-simple.xml");

        // check namespaces
        assertEquals(2, xml.collectNamespaces().size());

        // root
        XmlElement root = xml.getRoot();
        assertEquals(4, root.getContent().size());
        assertEquals(2, root.getNamespacesIntroduced().size());
        assertEquals(1, root.getAdditionalNamespaces().size());
        assertEquals(MODS_NS, root.getNamespace());
        assertEquals(XLINK_NS, root.getAdditionalNamespaces().get(0));

        // titleInfo
        assertEquals(2, root.getElements("mods:titleInfo").size());

        XmlElement firstTitleInfo = root.getElements().get(0);
        assertNotNull(firstTitleInfo);
        assertEquals("mods:titleInfo", firstTitleInfo.getQualifiedName().toString());
        assertEquals(MODS_NS, firstTitleInfo.getNamespace());
        assertEquals(1, firstTitleInfo.getContent().size());
        assertEquals(4, firstTitleInfo.getAttributes().size());
        assertEquals("yes", firstTitleInfo.getAttribute("supplied"));
        assertEquals("primary", firstTitleInfo.getAttribute("usage"));
        assertEquals("de", firstTitleInfo.getAttribute("xml:lang"));
        assertEquals("simple", firstTitleInfo.getAttribute("xlink:type"));
        assertNull(firstTitleInfo.getAttribute("notDefinedAttribute"));
        assertEquals("", firstTitleInfo.getText());

        XmlElement title = firstTitleInfo.getElement("mods:title");
        assertNotNull(title);
        assertEquals(1, title.getContent().size());
        assertEquals(0, title.getAttributes().size());
        assertEquals("DINI-Zertifikat für Open-Access-Repositorien und -Publikationsdienste 2013",
            title.getText());

        // genre
        XmlElement genre = root.getElements().get(2);
        assertNotNull(genre);
        assertEquals("mods:genre", genre.getQualifiedName().toString());
        assertEquals(MODS_NS, genre.getNamespace());
        assertEquals(0, genre.getContent().size());
        assertEquals(4, genre.getAttributes().size());
        assertEquals("intern", genre.getAttribute("type"));
        assertEquals("http://www.mycore.org/classifications/mir_genres", genre.getAttribute("authorityURI"));
        assertEquals("http://www.mycore.org/classifications/mir_genres#article", genre.getAttribute("valueURI"));
        assertEquals("primary", genre.getAttribute("usage"));
        assertEquals("", genre.getText());

        // accessCondition
        XmlElement accessCondition = root.getElements().get(3);
        assertNotNull(accessCondition);
        assertEquals("mods:accessCondition", accessCondition.getQualifiedName().toString());
        assertEquals(MODS_NS, accessCondition.getNamespace());
        assertEquals(1, accessCondition.getContent().size());
        assertEquals(0, accessCondition.getAttributes().size());

        XmlElement access = accessCondition.getElements().get(0);
        assertNotNull(access);
        assertEquals(3, access.getContent().size());
        assertEquals(1, access.getAttributes().size());
        Assertions.assertEquals(XmlNamespace.EMPTY, access.getNamespace());
        assertEquals("<access type=\"public\">Open <b>S</b>ource</access>", access.toXml());
    }

}
